package com.callee.calleeclient.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.callee.calleeclient.Global;
import com.callee.calleeclient.PagerAdapter;
import com.callee.calleeclient.R;
import com.callee.calleeclient.client.Message;
import com.callee.calleeclient.client.SingleChat;
import com.callee.calleeclient.database.Contact;
import com.callee.calleeclient.database.dbDriver;
import com.callee.calleeclient.thread.UpdateThread;

import java.util.ArrayList;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private PagerAdapter mAdapter;
    private UpdateThread updateService;
    private HomeBReceiver broadcastReceiver;

    private ArrayList<SingleChat> chats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if(Global.db==null) {
            Global.db = new dbDriver();
        }
        Global.db.openConnection(this);

        Global.db.joinDbThread();

        fetchCredentials();
        fetchInformations();

        updateService=new UpdateThread(this, Global.db, 5000, Global.db.getLastUpdate());
        updateService.start();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mViewPager = (ViewPager) findViewById(R.id.mainPager);
        mAdapter = new PagerAdapter(this.getSupportFragmentManager(), this.chats);
        mViewPager.setAdapter(mAdapter);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(mViewPager);
        mViewPager.setCurrentItem(1);     //default tab

        //setting bradcast receiver
        broadcastReceiver=new HomeBReceiver();
        this.registerReceiver(broadcastReceiver, new IntentFilter("com.callee.calleeclient.Broadcast"));

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
    public void onDestroy() {
        this.unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    private void fetchInformations() {
        this.chats = new ArrayList<>();
        Global.db.getChats(chats);
        Global.db.joinDbThread();
    }

    private void fetchCredentials() {

        Contact c = new Contact(null, null, null);
        Global.db.getCredentials(c);

        if (! Global.db.joinDbThread()) {
            return;
        }

        Global.username = c.getName();
        Global.email = c.getEmail();
    }

    public class HomeBReceiver extends BroadcastReceiver{

        public HomeBReceiver(){
            super();
        }

        @Override
        public void onReceive(Context context, Intent intent){
            Toast.makeText(context,"New messages!", Toast.LENGTH_LONG).show();

            ArrayList<Message> ms=intent.getParcelableArrayListExtra("messages");
            ArrayList<SingleChat> newChats=intent.getParcelableArrayListExtra("chats");
            if(! newChats.isEmpty())
            mAdapter.updateData(newChats);
        }
    }
}
