package com.callee.calleeclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.callee.calleeclient.client.Message;
import com.callee.calleeclient.client.SingleChat;
import com.callee.calleeclient.client.ToM;
import com.callee.calleeclient.database.Contact;
import com.callee.calleeclient.database.dbDriver;
import com.callee.calleeclient.fragments.MessageListFragment;

import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Pattern;

import static java.lang.System.currentTimeMillis;

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
        Global.db = new dbDriver();
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

        //fill user and email on appbar
        TextView user = findViewById(R.id.chat_username);
        user.setText(chatData.getUser());
        TextView email = findViewById(R.id.chat_email);
        email.setText(chatData.getEmail());

        FragmentManager fm = getSupportFragmentManager();

        //wait retrieving thread for messages
        if (!Global.db.joinDbThread()) {
            System.out.println("Error retrieving messages");
        }

        b = new Bundle();
        b.putParcelableArrayList("messages", messages);
        b.putString("user_email", chatData.getEmail());     //send also email of other user
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
    public void onDestroy() {
        this.unregisterReceiver(broadcastReceiver);
        Global.db.closeConnection();
        super.onDestroy();
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
