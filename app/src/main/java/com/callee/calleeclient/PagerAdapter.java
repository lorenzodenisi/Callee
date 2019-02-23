package com.callee.calleeclient;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.callee.calleeclient.client.SingleChat;
import com.callee.calleeclient.fragments.ChatListFragment;
import com.callee.calleeclient.fragments.UserInfoActivityFragment;

import java.util.ArrayList;

public class PagerAdapter extends FragmentPagerAdapter {

    private ArrayList<SingleChat> chats;
    private ChatListFragment cF;

    private ArrayList<SingleChat> newChats;

    PagerAdapter(FragmentManager fm, ArrayList<SingleChat> chats){
        super(fm);
        this.chats=chats;
    }

    @Override
    public Fragment getItem(int i) {

        switch (i){
            case 2:     //TODO list of users
            case 0: return new UserInfoActivityFragment();
            case 1: {
                Bundle b=new Bundle();
                cF= new ChatListFragment();
                b.putParcelableArrayList("chats", chats);
                cF.setArguments(b);
                return cF;
            }
            default:return null;
        }
    }

    @Override
    public int getCount() {
        return Global.tabNumber;
    }

    @Override
    public CharSequence getPageTitle(int position){
        switch (position){
            case 0: return"INFO";
            case 1: return"CHAT";
            case 2: return"CONTACTS";
            default:return "";
        }
    }

   public void updateData(ArrayList<SingleChat> newChats){
        this.newChats=newChats;
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemPosition(Object object){
        if(object instanceof ChatListFragment){
            ((ChatListFragment) object).updateData(newChats);
        }
        return super.getItemPosition(object);
    }
}
