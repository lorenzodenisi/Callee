package com.callee.calleeclient.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

import com.callee.calleeclient.R;
import com.callee.calleeclient.activities.ChatActivity;
import com.callee.calleeclient.client.SingleChat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class ChatListFragment extends ListFragment {

    private SimpleAdapter adapter;
    private ArrayList<HashMap<String, String>> data;
    private ArrayList<SingleChat> chats;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            chats = bundle.getParcelableArrayList("chats");       //getting data from Bundle
        }

        data = new ArrayList<>();

        for (SingleChat sc : chats) {
            HashMap<String, String> map = new HashMap<>();
            map.put("user", sc.getUser());
            map.put("newMessages", String.valueOf(sc.getNewMessages()));
            map.put("lastMessagePreview", sc.getLastMessagePreview());
            map.put("lastMessageHour", sc.getFormattedLastMessageTime());

            data.add(map);
        }


        String[] from = {"user", "newMessages", "lastMessagePreview", "lastMessageHour"};
        int[] to = {R.id.userNameTextBoxPreview, R.id.messageCounter, R.id.lastMessagePreview, R.id.lastMessageTime};
        adapter = new SimpleAdapter(getActivity(), data, R.layout.chat_preview, from, to);
        setListAdapter(adapter);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        getListView().setOnItemClickListener((av, v, pos, index) -> {

            chats.get((int)index).setNewMessages(0);
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            Bundle b = new Bundle();
            b.putParcelable("chat", chats.get((int) index));
            intent.putExtra("data", b);

            startActivity(intent);
        });
    }

    public void updateData(ArrayList<SingleChat> newChats) {

        chats=new ArrayList<>(newChats);

        Collections.sort(chats);

        data.clear();
        for (SingleChat sc : chats) {
            HashMap<String, String> map = new HashMap<>();
            map.put("user", sc.getUser());
            map.put("newMessages", String.valueOf(sc.getNewMessages()));
            map.put("lastMessagePreview", sc.getLastMessagePreview());
            map.put("lastMessageHour", sc.getFormattedLastMessageTime());
            data.add(map);
        }

        Collections.sort(data, (o1, o2) -> o2.get("lastMessageHour").compareTo(o1.get("lastMessageHour")));

        adapter.notifyDataSetChanged();
    }
}
