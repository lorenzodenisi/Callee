package com.callee.calleeclient.activities;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.callee.calleeclient.Global;
import com.callee.calleeclient.PagerAdapter;
import com.callee.calleeclient.R;
import com.callee.calleeclient.client.Message;
import com.callee.calleeclient.client.SingleChat;
import com.callee.calleeclient.database.Contact;
import com.callee.calleeclient.database.dbDriver;
import com.callee.calleeclient.thread.UpdateThread;
import com.callee.calleeclient.thread.addContactThread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.regex.Pattern;

public class HomeActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private PagerAdapter mAdapter;
    private UpdateThread updateService;
    private HomeBReceiver broadcastReceiver;

    private LinkedHashMap<String, SingleChat> chats;
    private ArrayList<Contact> contacts;
    private static boolean isThreadRunning=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (Global.db == null) {
            Global.db = new dbDriver();
        }
        Global.db.openConnection(this);
        Global.db.joinDbThread();

        fetchCredentials();
        if(!isThreadRunning && (updateService==null || !updateService.isAlive())) {
            updateService = new UpdateThread(this, Global.db, 5000, Global.db.getLastUpdate());
            updateService.start();
            isThreadRunning=true;
        }
    }

    @Override
    public void onStart(){

        //setting bradcast receiver
        broadcastReceiver = new HomeBReceiver();
        this.registerReceiver(broadcastReceiver, new IntentFilter("com.callee.calleeclient.Broadcast"));

        fetchInformations();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mViewPager = (ViewPager) findViewById(R.id.mainPager);

        mAdapter = new PagerAdapter(this.getSupportFragmentManager(), new ArrayList<>(this.chats.values()), this.contacts, this);
        mViewPager.addOnPageChangeListener(new PagerListener(this, mAdapter));
        mViewPager.setAdapter(mAdapter);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(mViewPager);
        mViewPager.setCurrentItem(1);     //default tab

        super.onStart();
        //TODO add custom width to tabs (maybe icons) extension needed
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause(){
        this.unregisterReceiver(broadcastReceiver);
        super.onPause();
    }


    private void fetchInformations() {
        this.chats = new LinkedHashMap<>();
        Global.db.getChats(chats);
        Global.db.joinDbThread();

        this.contacts = new ArrayList<>();
        Global.db.getContacts(contacts);
        Global.db.joinDbThread();
    }

    private void fetchCredentials() {

        Contact c = new Contact(null, null, null);
        Global.db.getCredentials(c);

        if (!Global.db.joinDbThread()) {
            return;
        }

        Global.username = c.getName();
        Global.email = c.getEmail();
    }

    public class HomeBReceiver extends BroadcastReceiver {

        public HomeBReceiver() {
            super();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "New messages!", Toast.LENGTH_LONG).show();

            ArrayList<Message> ms = intent.getParcelableArrayListExtra("messages");
            ArrayList<SingleChat> newChats = intent.getParcelableArrayListExtra("chats");

            chats.clear();
            for (SingleChat c: newChats){
                chats.put(c.getEmail(), c);
            }

            mAdapter.updateChats(newChats);
        }
    }


    public class PagerListener implements ViewPager.OnPageChangeListener {

        FloatingActionButton button;
        Activity activity;
        PagerAdapter adapter;

        public PagerListener(Activity a, PagerAdapter p) {
            button = findViewById(R.id.new_button);
            this.activity = a;
            this.adapter = p;
        }

        @Override
        public void onPageScrolled(int i, float v, int i1) {

        }

        @Override
        public void onPageSelected(int i) {
            switch (i) {
                case 0:
                    button.hide();
                    adapter.removeContactInfo();
                    break;

                case 1:
                    button.show();
                    button.setOnClickListener(new newMessageListener(activity));
                    adapter.removeContactInfo();
                    break;

                case 2:
                    button.show();
                    button.setOnClickListener(new addContactListener(getApplicationContext()));
                    break;

                default:
                    break;
            }
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    }

    public class addContactListener implements View.OnClickListener {

        PopupWindow popup;
        View popupLayout;
        Button confirmButton;

        public addContactListener(Context context) {
            popupLayout = getLayoutInflater().inflate(R.layout.add_contact_popup, null);
            popup = new PopupWindow(popupLayout, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT, true);

        }

        @Override
        public void onClick(View v) {
            popup.setAnimationStyle(R.style.PopupAnimation);
            popup.showAtLocation(popupLayout, Gravity.CENTER, 0, 0);
            confirmButton = popupLayout.findViewById(R.id.new_contact_popup_button);
            confirmButton.setOnClickListener(new ConfirmListener(popupLayout, popup));
        }


        public class ConfirmListener implements View.OnClickListener {

            Pattern pattern;
            EditText emailField;
            String email;
            PopupWindow popup;

            public ConfirmListener(View view, PopupWindow popup) {
                this.emailField = view.findViewById(R.id.new_contact_field);
                pattern = Pattern.compile("^([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,5})$");
                this.popup = popup;
            }

            @Override
            public void onClick(View v) {
                email = emailField.getText().toString();
                if (!pattern.matcher(email).matches() || email.equals("")) {
                    emailField.setHint("Type an email!");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        emailField.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_red_dark));
                    }
                    emailField.setText("");
                } else {
                    addContactThread acT = new addContactThread(email);
                    acT.start();
                    Contact c = acT._join();
                    if (c == null) {
                        emailField.setText("");
                        Toast.makeText(v.getContext(), email + " is not associated with an account", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(v.getContext(), email + " added with username " + c.getName(), Toast.LENGTH_LONG).show();
                        Global.db.putContact(c);
                        Global.db.joinDbThread();
                        popup.dismiss();
                        mAdapter.updateContacts(c);
                    }
                }
            }
        }

    }

    private class newMessageListener implements View.OnClickListener {

        private PopupWindow popup;
        private View popupLayout;
        private ArrayList<Contact> contacts;
        private ListView list;
        private Activity context;

        public newMessageListener(Activity c) {
            super();

            this.context = c;
            contacts = new ArrayList<>();
            Global.db.getContacts(contacts);
            Global.db.joinDbThread();

            popupLayout = getLayoutInflater().inflate(R.layout.new_message_popup, null);
            popup = new PopupWindow(popupLayout, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT, true);
        }

        @Override
        public void onClick(View v) {
            list = popupLayout.findViewById(R.id.new_message_contact_list);

            ArrayList<HashMap<String, String>> data = new ArrayList<>();

            for (Contact c : contacts) {
                HashMap<String, String> map = new HashMap<>();
                map.put("user", c.getName());
                map.put("email", c.getEmail());
                map.put("number", c.getNumber());

                data.add(map);
            }

            if (contacts.isEmpty()) {
                FrameLayout container = popupLayout.findViewById(R.id.new_message_contact_list_container);
                TextView tip = new TextView(context);
                tip.setText("Before messaging you need to add your friends to Contacts!");
                tip.setGravity(Gravity.CENTER);
                container.addView(tip);
                list.setVisibility(View.INVISIBLE);
            } else {
                String[] from = {"user", "email"};
                int[] to = {R.id.contactName, R.id.contactEmail};
                SimpleAdapter adapter = new SimpleAdapter(context, data, R.layout.contact, from, to);

                list.setAdapter(adapter);

            }

            popup.setAnimationStyle(R.style.PopupAnimation);
            popup.showAtLocation(popupLayout, Gravity.CENTER, 0, 0);


            list.setOnItemClickListener((parent, view, position, id) -> {

                goToChat(contacts.get(position), context);
                popup.dismiss();
            });
        }
    }

    public void goToChat(Contact c, Activity a) {

        SingleChat chat = null;

        for (SingleChat ch : chats.values()) {
            if (ch.getEmail().equals(c.getEmail()))
                chat = ch;
        }
        if (chat == null) {     //new chat
            chat = new SingleChat(c.getName(), c.getEmail(), "", 0, 0L);
            chats.put(chat.getEmail(),chat);
            Global.db.putChat(chat);
        }

        Intent intent = new Intent(a, ChatActivity.class);
        Bundle b = new Bundle();
        b.putParcelable("chat", chat);
        intent.putExtra("data", b);

        if (Global.db.joinDbThread()) {
            startActivity(intent);
        }
    }
}
