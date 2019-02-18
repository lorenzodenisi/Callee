package com.callee.calleeclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.callee.calleeclient.client.SingleChat;
import com.callee.calleeclient.database.Contact;
import com.callee.calleeclient.database.dbDriver;

import java.util.ArrayList;

import static com.callee.calleeclient.Global.db;

public class HomeActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private PagerAdapter mAdapter;

    private ArrayList<SingleChat> chats;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = new dbDriver(this);
        db.openConnection();
        //db.restoreDB();
        //db.setCredentials("lorenzo", "lorenzodenisi@gmail.com", "+393338846260"); //TODO add welcome activity

        fetchCredentials();
        fetchInformations();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mViewPager = (ViewPager) findViewById(R.id.mainPager);
        mAdapter=new PagerAdapter(this.getSupportFragmentManager(), this.chats);
        mViewPager.setAdapter(mAdapter);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(mViewPager);
        mViewPager.setCurrentItem(1);     //default tab

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

    //TODO integrate with a sqlite db
    private void fetchInformations(){
       /* db.putChat(new SingleChat("Mario Rossi", "mariorossi@gmail.com", "ciao come va?", 99, (System.currentTimeMillis() - 1000000L)));
        db.putChat(new SingleChat("Luca Bianchi", "lucabianchi@gmail.com", "vuoi due noci?", 32, (System.currentTimeMillis() - 500000L)));
        db.putChat(new SingleChat("Alberto Neri", "albertoneri@gmail.com", "rispondi a Luca! le vuoi due noci?", 1, (System.currentTimeMillis() - 10000L)));
        db.putChat(new SingleChat("Maria Blu","mariablu@gmail.com" , "Buonanotte", 2, (System.currentTimeMillis() - 5000L)));
*/
        this.chats=db.getChats();
    }

    private void fetchCredentials(){

        Contact c = db.getCredentials();

        Global.username=c.getName();
        Global.email=c.getEmail();
    }

}
