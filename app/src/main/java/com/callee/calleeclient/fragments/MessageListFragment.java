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
    ChatSimpleAdapter sa;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle bundle = getArguments();

        if (bundle != null) {
            messages = bundle.getParcelableArrayList("messages");
        }

        data = new ArrayList<>();
        for (Message m : messages) {
            HashMap<String, String> map = new HashMap<>();
            map.put("text", m.getText());
            map.put("date", m.getDate());
            map.put("to", m.getToEmail());      //just for positioning

            data.add(map);
        }

        String[] from = {"text", "date"};
        int[] to = {R.id.message_text, R.id.message_date};
        sa = new ChatSimpleAdapter(this.getContext(), data, R.layout.message, from, to, messages);
        setListAdapter(sa);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        this.getListView().setDivider(null);        //remove grey lines that divide messages
    }

    public void addMessage(Message m) {

        HashMap<String, String> map = new HashMap<>();
        map.put("text", m.getText());
        map.put("date", m.getDate());
        map.put("to", m.getToEmail());      //just for positioning

        this.data.add(map);
        sa.notifyDataSetChanged();
    }

    public void scrollDown() {
        this.getListView().smoothScrollToPosition(messages.size() - 1);
    }

    public void goDown(){
        this.getListView().setSelection(messages.size()-1);
    }
}
