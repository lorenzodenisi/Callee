package com.callee.calleeclient;


import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.callee.calleeclient.client.Message;
import com.callee.calleeclient.client.SingleChat;
import com.callee.calleeclient.client.ToM;
import com.callee.calleeclient.database.Contact;
import com.callee.calleeclient.fragments.MessageListFragment;

import java.util.ArrayList;
import java.util.regex.Pattern;

import static java.lang.System.currentTimeMillis;

public class ChatActivity extends AppCompatActivity {

    SingleChat chatData;
    ArrayList<Message> messages;
    MessageListFragment msgListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        Bundle b = this.getIntent().getBundleExtra("data");
        chatData = b.getParcelable("chat");

        //start retrieving messages
        messages = new ArrayList<>();
        Global.db.getMessages(messages, new Contact(chatData.getUser(), chatData.getEmail(), null));

        //toolbar used just for back button
        Toolbar toolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        //fill user and email on appbar
        TextView user = findViewById(R.id.chat_username);
        user.setText(chatData.getUser());
        TextView email = findViewById(R.id.chat_email);
        email.setText(chatData.getEmail());

        //putMessages(chatData);

        FragmentManager fm = getSupportFragmentManager();

        //wait retrieving thread for messages
        if(! Global.db.joinDbThread()){
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

    }

    private void putMessages(SingleChat sc) {

                /*Message m1 = new Message(1L, "Mario Rossi", "Lorenzo De Nisi",
                        "mariorossi@gmail.com", "lorenzodenisi@gmail.com", 1550246135870L, ToM.MESSAGE);
                Message m2 = new Message(2L,  "Lorenzo De Nisi", "Mario Rossi",
                        "lorenzodenisi@gmail.com","mariorossi@gmail.com", 1550246504878L, ToM.MESSAGE);
                Message m3 = new Message(3L,  "Lorenzo De Nisi", "Mario Rossi",
                        "lorenzodenisi@gmail.com","mariorossi@gmail.com", 1550246504878L, ToM.MESSAGE);

                m1.putText("Ciao come va?");
                m2.putText("Tutto bene tu??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????");
                m3.putText("Rispondi!");

                Global.db.putMessage(m1);
                Global.db.putMessage(m2);
                Global.db.putMessage(m3);*/
    }


    private class sendButtonOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            TextView tw = findViewById(R.id.message_box);
            String content = tw.getText().toString();
            Pattern p = Pattern.compile(".*\\S.*");         //check illegal strings

            if (p.matcher(content).matches() && (!content.equals(""))) {

                Message toSend = new Message(-1L, Global.username, chatData.getUser(),
                        Global.email, chatData.getEmail(), currentTimeMillis(), ToM.MESSAGE);

                toSend.putText(content);
                //update request
                //send to remote db
                //listen to reply from server
                //send to private db message from server

                Global.db.putMessage(toSend);

                if (!Global.db.joinDbThread()) {
                    System.out.println("Error saving message to local database");
                    return;
                }

                //if update and remote ok
                //set lastupdated at the last message(this one)

                messages.add(toSend);
                msgListFragment.addMessage(toSend);
                tw.setText("");
                msgListFragment.scrollDown();
            }
        }
    }

    private class TextChecker implements TextWatcher {

        boolean isValid = false;
        Pattern p = Pattern.compile(".*\\S.*");
        ImageView button;

        public TextChecker(ImageView button) {
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
}
