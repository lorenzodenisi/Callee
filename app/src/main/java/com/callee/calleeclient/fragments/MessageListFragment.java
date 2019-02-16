package com.callee.calleeclient.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Layout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.callee.calleeclient.ChatSimpleAdapter;
import com.callee.calleeclient.R;
import com.callee.calleeclient.client.Message;

import java.util.ArrayList;
import java.util.HashMap;

public class MessageListFragment extends ListFragment {

    private ArrayList<HashMap<String, String>> data;
    private ArrayList<Message> messages;
    private String userEmail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Bundle bundle= getArguments();

        messages=bundle.getParcelableArrayList("messages");
        userEmail=bundle.getString("user_email");

        data=new ArrayList<>();
        for(Message m: messages){
            HashMap<String, String> map=new HashMap<>();
            map.put("text", m.getText());
            map.put("date", m.getDate());

            data.add(map);
        }

        String[] from = {"text", "date"};
        int[] to = {R.id.message_text, R.id.message_date};
        ChatSimpleAdapter sa = new ChatSimpleAdapter(this.getContext(), data,R.layout.message, from, to, messages, userEmail);      //TODO reduce arguments
        setListAdapter(sa);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStart(){
        super.onStart();
        this.getListView().setDivider(null);
    }
}
