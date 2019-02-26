package com.callee.calleeclient.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

import com.callee.calleeclient.R;
import com.callee.calleeclient.database.Contact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class ContactFragment extends ListFragment {

    private ArrayList<Contact> contacts;
    private ArrayList<HashMap<String, String>> data;
    private SimpleAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        Bundle bundle= getArguments();
        if (bundle != null) {
            contacts = bundle.getParcelableArrayList("contacts");       //getting data from Bundle
        }
        data=new ArrayList<>();

        for(Contact c : contacts){
            HashMap<String, String> map=new HashMap<>();
            map.put("user", c.getName());
            map.put("email", c.getEmail());
            map.put("number", c.getNumber());

            data.add(map);
        }

        String[] from = {"user", "email"};
        int[] to = {R.id.contactName, R.id.contactEmail};
        adapter=new SimpleAdapter(getActivity(), data, R.layout.contact, from, to);
        setListAdapter(adapter);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStart(){
        super.onStart();
        getListView().setOnItemClickListener((av, v, pos, index) -> {

        });
    }

    public void addContact(Contact c){
        if(!contacts.contains(c)){
            contacts.add(c);
        }

        Collections.sort(contacts);


        HashMap<String, String> map=new HashMap<>();
        map.put("user", c.getName());
        map.put("email", c.getEmail());
        map.put("number", c.getNumber());

        this.data.add(map);
        Collections.sort(data, (o1, o2) -> o1.get("user").compareTo(o2.get("user")));

        adapter.notifyDataSetChanged();
    }
}
