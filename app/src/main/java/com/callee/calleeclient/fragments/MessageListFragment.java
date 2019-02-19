package com.callee.calleeclient.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.callee.calleeclient.ChatSimpleAdapter;
import com.callee.calleeclient.R;
import com.callee.calleeclient.client.Message;

import java.util.ArrayList;
import java.util.HashMap;

public class MessageListFragment extends ListFragment {

    private ArrayList<HashMap<String, String>> data;
    private ArrayList<Message> messages;
    private String userEmail;
    ChatSimpleAdapter sa;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Bundle bundle= getArguments();

        if (bundle != null) {
            messages=bundle.getParcelableArrayList("messages");
            userEmail=bundle.getString("user_email");
        }

        data=new ArrayList<>();
        for(Message m: messages){
            HashMap<String, String> map=new HashMap<>();
            map.put("text", m.getText());
            map.put("date", m.getDate());

            data.add(map);
        }

        String[] from = {"text", "date"};
        int[] to = {R.id.message_text, R.id.message_date};
        sa = new ChatSimpleAdapter(this.getContext(), data, R.layout.message, from, to, messages, userEmail);
        setListAdapter(sa);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStart(){
        super.onStart();
        this.getListView().setDivider(null);        //remove grey lines that divide messages
    }

    public void addMessage(Message m){

        if( ! messages.contains(m)) messages.add(m);

        HashMap<String, String> map=new HashMap<>();
        map.put("text", m.getText());
        map.put("date", m.getDate());

        this.data.add(map);
        sa.notifyDataSetChanged();
    }

    public void scrollDown(){
        this.getListView().smoothScrollToPosition(messages.size()-1);
    }
}
