package com.callee.calleeclient.activities;

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
import com.callee.calleeclient.NotifyManager;
import com.callee.calleeclient.PagerAdapter;
import com.callee.calleeclient.R;
import com.callee.calleeclient.client.SingleChat;
import com.callee.calleeclient.database.Contact;
import com.callee.calleeclient.database.dbDriver;
import com.callee.calleeclient.services.UpdateService;
import com.callee.calleeclient.threads.addContactThread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.regex.Pattern;

import static com.callee.calleeclient.Global.db;

public class HomeActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private PagerAdapter mAdapter;
    private HomeBReceiver broadcastReceiver;
    private Intent serviceIntent;
    private boolean isRegistered = false;

    private LinkedHashMap<String, SingleChat> chats;
    private ArrayList<Contact> contacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (db == null) {
            db = new dbDriver();
        }
        db.openConnection(this);

        long dbLastUpdate = db.getLastUpdate();
        if (dbLastUpdate >= Global.lastUpdate)
            Global.lastUpdate = dbLastUpdate;

        fetchCredentials();

        //start update service if not running
        serviceIntent = new Intent(getApplicationContext(), UpdateService.class);
        if (!Global.isUpdateServiceRunning) {
            startService(serviceIntent);
        }
    }

    @Override
    public void onStart() {

        //init notifyManager
        if (Global.notifyManager == null) {
            Global.notifyManager = new NotifyManager(getApplicationContext());
        }
        Global.notifyManager.setCurrentChat(null);

        //getting chats and messages from db
        fetchInformations();

        //setting up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //setting pager with tabs
        mViewPager = findViewById(R.id.mainPager);
        mAdapter = new PagerAdapter(this.getSupportFragmentManager(), new ArrayList<>(this.chats.values()), this.contacts, this);
        mViewPager.addOnPageChangeListener(new PagerListener(this, mAdapter));
        mViewPager.setAdapter(mAdapter);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(mViewPager);
        mViewPager.setCurrentItem(1);     //default tab

        //setting bradcast receiver
        broadcastReceiver = new HomeBReceiver(chats, mAdapter);
        this.registerReceiver(broadcastReceiver, new IntentFilter("com.callee.calleeclient.Broadcast"));
        isRegistered = true;

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
        if (id == R.id.action_settings) {
            return true;          //TODO settings
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        if (isRegistered) {
            this.unregisterReceiver(broadcastReceiver);
            isRegistered = false;
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        stopService(serviceIntent);
        super.onDestroy();
    }

    private void fetchInformations() {

        this.chats = new LinkedHashMap<>();
        Thread t1 = db.getChats(chats);
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.contacts = new ArrayList<>();
        Thread t2 = db.getContacts(contacts);
        try {
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void fetchCredentials() {

        Contact c = new Contact(null, null, null);
        Thread t = db.getCredentials(c);
        db.join(t);

        //remember credentials
        if (c.getName() != null && c.getEmail() != null) {
            Global.username = c.getName();
            Global.email = c.getEmail();
        }
    }

    public static class HomeBReceiver extends BroadcastReceiver {
        LinkedHashMap<String, SingleChat> chats;
        PagerAdapter mAdapter;

        public HomeBReceiver() {
            super();
        }

        public HomeBReceiver(LinkedHashMap<String, SingleChat> chats, PagerAdapter mAdapter) {
            super();
            this.mAdapter = mAdapter;
            this.chats = chats;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "New messages!", Toast.LENGTH_LONG).show();

            //ArrayList<Message> ms = intent.getParcelableArrayListExtra("messages");
            ArrayList<SingleChat> newChats = intent.getParcelableArrayListExtra("chats");

            chats.clear();
            for (SingleChat c : newChats) {
                chats.put(c.getEmail(), c);
            }

            mAdapter.updateChats(newChats);
        }
    }


    private class PagerListener implements ViewPager.OnPageChangeListener {

        FloatingActionButton button;
        Activity activity;
        PagerAdapter adapter;

        PagerListener(Activity a, PagerAdapter p) {
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
                    button.setOnClickListener(new addContactListener());
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

        addContactListener() {
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

            ConfirmListener(View view, PopupWindow popup) {
                this.emailField = view.findViewById(R.id.new_contact_field);
                pattern = Pattern.compile(getString(R.string.email_regex));       //check email
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
                    emailField.setText("");
                    if (c == null) {
                        Toast.makeText(v.getContext(), email + " is not associated with an account", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(v.getContext(), email + " added with username " + c.getName(), Toast.LENGTH_LONG).show();
                        dbDriver.putContactThread t = db.putContact(c);
                        t._join();
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

        newMessageListener(Activity c) {
            super();

            this.context = c;
            contacts = new ArrayList<>();
            Thread t = db.getContacts(contacts);
            db.join(t);

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
                tip.setText(getString(R.string.new_message_no_contact_tip));
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

        private void goToChat(Contact c, Activity a) {

            SingleChat chat = null;
            for (SingleChat ch : chats.values()) {
                if (ch.getEmail().equals(c.getEmail())) {
                    chat = ch;
                    break;
                }
            }
            dbDriver.putChatThread t = null;
            if (chat == null) {     //new chat
                chat = new SingleChat(c.getName(), c.getEmail(), "", 0, 0L);
                chats.put(chat.getEmail(), chat);
                t = db.putChat(chat);
            }

            Intent intent = new Intent(a, ChatActivity.class);
            Bundle b = new Bundle();
            b.putParcelable("chat", chat);
            intent.putExtra("data", b);

            if (t == null || t._join()) {
                startActivity(intent);
            }
        }
    }
}
