package com.callee.calleeclient.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

import com.callee.calleeclient.Client.SingleChat;
import com.callee.calleeclient.R;

import java.util.ArrayList;
import java.util.HashMap;


public class ChatFragment extends ListFragment {

    SimpleAdapter adapter;
    ArrayList<HashMap<String, String>> data;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Bundle bundle= getArguments();
        ArrayList<SingleChat> chats = bundle.getParcelableArrayList("chats");       //getting data from Bundle

        data=new ArrayList<>();
        for(SingleChat sc : chats){

            HashMap<String, String> map=new HashMap<>();
            map.put("user", sc.getUser());
            map.put("newMessages", String.valueOf(sc.getNewMessages()));
            map.put("lastMessagePreview", sc.getLastMessagePreview());
            map.put("lastMessageHour", sc.getLastMessageHour());

            data.add(map);
        }

        String[] from={"user", "newMessages", "lastMessagePreview", "lastMessageHour"};
        int[] to={R.id.userNameTextBoxPreview, R.id.messageCounter, R.id.lastMessagePreview, R.id.lastMessageTime};
        adapter=new SimpleAdapter(getActivity(), data, R.layout.single_chat_layout, from, to);
        setListAdapter(adapter);

        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
