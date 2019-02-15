package com.callee.calleeclient;


import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.callee.calleeclient.client.Message;
import com.callee.calleeclient.client.SingleChat;
import com.callee.calleeclient.client.ToM;
import com.callee.calleeclient.fragments.MessageListFragment;
import com.callee.calleeclient.fragments.UserInfoActivityFragment;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    SingleChat chatData;
    ArrayList<Message> messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        Bundle b = this.getIntent().getBundleExtra("data");
        chatData=b.getParcelable("chat");

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

        messages=new ArrayList<>();
        fetchMessages(chatData);

        FragmentManager fm = getSupportFragmentManager();

        b = new Bundle();
        b.putParcelableArrayList("messages", messages);
        b.putString("user_email", chatData.getEmail());     //send also email of other user
        MessageListFragment msgListFragment = new MessageListFragment();
        msgListFragment.setArguments(b);
        fm.beginTransaction().add(R.id.messagelist_container, msgListFragment).commit();
    }

    private void fetchMessages(SingleChat sc){

        switch (sc.getUser()){
            case "Mario Rossi":
            {
                Message m1 = new Message(1L, "Mario Rossi", "Lorenzo De Nisi",
                        "mariorossi@gmail.com", "lorenzodenisi@gmail.com", 1550246135870L, ToM.MESSAGE);
                Message m2 = new Message(2L,  "Lorenzo De Nisi", "Mario Rossi",
                        "lorenzodenisi@gmail.com","mariorossi@gmail.com", 1550246504878L, ToM.MESSAGE);

                m1.putText("Ciao come va?");
                m2.putText("Tutto bene tu???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????");

                this.messages.add(m1);
                this.messages.add(m2);
            }
        }

    }
}
