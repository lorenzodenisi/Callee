package com.callee.calleeclient.libs;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.callee.calleeclient.Client.SingleChat;
import com.callee.calleeclient.R;

import java.util.ArrayList;


public class ChatFragment extends ListFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Bundle bundle= getArguments();
        ArrayList<SingleChat> chats = bundle.getParcelableArrayList("chats");
        ArrayAdapter<SingleChat> adapter = new ArrayAdapter<SingleChat>
                (inflater.getContext(), R.layout.single_chat_layout, chats);        //TODO add custom layout instead of TextLayout
        setListAdapter(adapter);

        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
