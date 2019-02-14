package com.callee.calleeclient;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.callee.calleeclient.client.SingleChat;

public class ChatActivity extends AppCompatActivity {

SingleChat chatData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        Bundle b = this.getIntent().getBundleExtra("data");
        chatData=b.getParcelable("chat");

        Toolbar toolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(chatData.getUser());
        getSupportActionBar().setSubtitle(chatData.getEmail());

    }
}
