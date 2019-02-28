package com.callee.calleeclient.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.callee.calleeclient.Global;
import com.callee.calleeclient.R;
import com.callee.calleeclient.client.Message;
import com.callee.calleeclient.client.SingleChat;
import com.callee.calleeclient.client.ToM;
import com.callee.calleeclient.database.Contact;
import com.callee.calleeclient.database.dbDriver;
import com.callee.calleeclient.fragments.MessageListFragment;
import com.callee.calleeclient.thread.ConfirmReadThread;
import com.callee.calleeclient.thread.SendMessageThread;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Pattern;

import static java.lang.System.currentTimeMillis;
import static java.nio.charset.StandardCharsets.UTF_8;

public class ChatActivity extends AppCompatActivity {

    private SingleChat chatData;
    private final ArrayList<Message> messages = new ArrayList<>();       //synchronized
    private MessageListFragment msgListFragment;
    private ChatBReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        //open database
        if(Global.db==null) {
            Global.db = new dbDriver();
        }
        Global.db.openConnection(this);

        Bundle b = this.getIntent().getBundleExtra("data");

        chatData = b.getParcelable("chat");

        //start retrieving messages
        if (chatData != null) {
            Global.db.getMessages(messages, new Contact(chatData.getUser(), chatData.getEmail(), null));
        }

        //toolbar used just for back button
        Toolbar toolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        //fill user and emailField on appbar
        TextView user = findViewById(R.id.chat_username);
        user.setText(chatData.getUser());
        TextView email = findViewById(R.id.chat_email);
        email.setText(chatData.getEmail());

        FragmentManager fm = getSupportFragmentManager();

        //wait retrieving thread for messages
        if (!Global.db.joinDbThread()) {
            System.out.println("Error retrieving messages");
        }

        if(chatData.getNewMessages()>0)
            updateRead();

        b = new Bundle();
        b.putParcelableArrayList("messages", messages);
        b.putString("user_email", chatData.getEmail());     //send also emailField of other user
        msgListFragment = new MessageListFragment();
        msgListFragment.setArguments(b);
        fm.beginTransaction().add(R.id.messagelist_container, msgListFragment, "messageList").commit();

        ImageView sendButton = findViewById(R.id.send_message_button);
        sendButton.setOnClickListener(new sendButtonOnClickListener());

        TextView tw = findViewById(R.id.message_box);
        tw.addTextChangedListener(new TextChecker(sendButton));

        //setting bradcast receiver
        broadcastReceiver = new ChatBReceiver();
        this.registerReceiver(broadcastReceiver, new IntentFilter("com.callee.calleeclient.Broadcast"));
    }

    @Override
    public void onStart() {
        super.onStart();
        msgListFragment.goDown();
    }

    @Override
    public void onPause(){
        ArrayList<SingleChat> updt = new ArrayList<>();
        updt.add(chatData);
        Global.db.updateChats(updt);
        if(! Global.db.joinDbThread()){
            System.err.println("Error updating chat");
        }
        this.unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed(){
        NavUtils.navigateUpFromSameTask(this);      //emulate actionbar back button
    }


    private boolean updateRead(){
        for(Message m: messages){
            if(!m.getRead()){
                m.setRead(true);
            }
        }
        Message confirmMessage = new Message(-1L, Global.username, "SERVER", Global.email,
                "server@server.server", System.currentTimeMillis(), ToM.CONFIRMREAD);
        confirmMessage.putText(chatData.getEmail());

        ConfirmReadThread confirm = new ConfirmReadThread(confirmMessage);
        confirm.start();
        chatData.setNewMessages(0);

        return confirm._join();
    }

    private class sendButtonOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            TextView tw = findViewById(R.id.message_box);
            String content = tw.getText().toString();
            Pattern p = Pattern.compile(".*\\S.*", Pattern.DOTALL);         //check illegal strings

            if (p.matcher(content).matches() && (!content.equals(""))) {

                Message toSend = new Message(-1L, Global.username, chatData.getUser(),
                        Global.email, chatData.getEmail(), currentTimeMillis(), ToM.MESSAGE);

                toSend.putText(content);

                messages.add(toSend);

                SendMessageThread sendThread = new SendMessageThread(Global.db, toSend, messages);
                sendThread.start();

                tw.setText("");

                msgListFragment.addMessage(toSend);
                msgListFragment.scrollDown();

                chatData.setLastMessagePreview(content);
                chatData.setLastMessageTime(currentTimeMillis());
            }
        }
    }

    private class TextChecker implements TextWatcher {

        boolean isValid = false;
        Pattern p = Pattern.compile(".*\\S.*", Pattern.DOTALL);
        ImageView button;

        TextChecker(ImageView button) {
            super();
            this.button = button;
            button.setImageResource(R.drawable.ic_callee_send_img_invalid);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            System.out.println();
            if (p.matcher(s).matches() && (!s.equals("")) && (!isValid)) {
                isValid = true;
                button.setImageResource(R.drawable.ic_send_icon);
            } else if (((!p.matcher(s).matches()) || (s.equals(""))) && (isValid)) {
                isValid = false;
                button.setImageResource(R.drawable.ic_callee_send_img_invalid);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

    public class ChatBReceiver extends BroadcastReceiver {

        public ChatBReceiver() {
            super();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            //Toast.makeText(context, "New messages!", Toast.LENGTH_LONG).show();
            long lastReceived = Long.parseLong(messages.get(messages.size() - 1).getTimestamp());

            ArrayList<Message> newMessages = intent.getParcelableArrayListExtra("messages");

            for (Message m : newMessages) {
                if (m.getToEmail().equals(chatData.getEmail())
                        || m.getFromEmail().equals(chatData.getEmail())) {

                    //adding message if is new
                    //I could get duplicate messages when message is sent and then received due to update

                    if (Long.valueOf(m.getTimestamp()) > lastReceived) {

                        synchronized (messages) {
                            messages.add(m);
                            msgListFragment.addMessage(m);
                            msgListFragment.scrollDown();
                        }
                    }
                }
            }
        }
    }
}
