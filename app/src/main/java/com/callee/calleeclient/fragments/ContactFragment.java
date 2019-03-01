package com.callee.calleeclient.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

import com.callee.calleeclient.R;
import com.callee.calleeclient.database.Contact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import static java.util.Collections.sort;

public class ContactFragment extends Fragment {

    ContactListFragment list;
    UserInfoFragment uF;
    Boolean isUserShowing = false;
    FloatingActionButton newButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.contact_container, container, false);
    }

    public void addContact(Contact c) {
        list.addContact(c);
    }

    public void setNewButton(FloatingActionButton button) {
        this.newButton = button;
    }

    public void showUserInfo(Contact c) {
        Bundle b = new Bundle();
        b.putParcelable("contact", c);
        b.putBoolean("isUser", false);
        uF = new UserInfoFragment();
        uF.setArguments(b);
        getChildFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fadein, R.anim.fadeout)
                .add(R.id.contacts_list_container, uF).commit();
        isUserShowing = true;

        if (newButton != null) {
            newButton.hide();
        }
    }

    public void removeUserInfo() {
        System.out.println();
        if (uF != null && isUserShowing) {
            getChildFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fadein, R.anim.fadeout)
                    .remove(uF).commit();
            isUserShowing = false;
            if (newButton != null)
                newButton.show();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        list = new ContactListFragment();
        list.setArguments(getArguments());
        getChildFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fadein, R.anim.fadeout)
                .replace(R.id.contacts_list_container, list).commit();
    }

    public static class ContactListFragment extends ListFragment {

        private ArrayList<Contact> contacts;
        private ArrayList<HashMap<String, String>> data;
        private SimpleAdapter adapter;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            Bundle bundle = getArguments();
            if (bundle != null) {
                contacts = bundle.getParcelableArrayList("contacts");       //getting data from Bundle
            }

            data = new ArrayList<>();
            for (Contact c : contacts) {
                HashMap<String, String> map = new HashMap<>();
                map.put("user", c.getName());
                map.put("email", c.getEmail());
                map.put("number", c.getNumber());
                data.add(map);
            }

            String[] from = {"user", "email"};
            int[] to = {R.id.contactName, R.id.contactEmail};
            adapter = new SimpleAdapter(getActivity(), data, R.layout.contact, from, to);
            setListAdapter(adapter);

            return super.onCreateView(inflater, container, savedInstanceState);
        }

        @Override
        public void onStart() {
            super.onStart();
            getListView().setOnItemClickListener((av, v, pos, index) -> {
                Contact c = this.contacts.get(pos);
                if (getParentFragment() != null) {
                    ((ContactFragment) getParentFragment()).showUserInfo(c);
                }
            });
        }

        public void addContact(Contact c) {
            if (!contacts.contains(c)) {
                contacts.add(c);
            }

            sort(contacts);


            HashMap<String, String> map = new HashMap<>();
            map.put("user", c.getName());
            map.put("email", c.getEmail());
            map.put("number", c.getNumber());

            this.data.add(map);
            sort(data, (o1, o2) -> Objects.requireNonNull(o1.get("user"))
                    .compareTo(Objects.requireNonNull(o2.get("user"))));

            adapter.notifyDataSetChanged();
        }
    }
}
