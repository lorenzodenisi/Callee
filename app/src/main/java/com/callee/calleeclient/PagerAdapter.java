package com.callee.calleeclient;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.callee.calleeclient.Client.SingleChat;
import com.callee.calleeclient.fragments.ChatFragment;
import com.callee.calleeclient.fragments.UserInfoActivityFragment;

import java.util.ArrayList;

public class PagerAdapter extends FragmentPagerAdapter {

    private ArrayList<SingleChat> chats;

    public PagerAdapter(FragmentManager fm, ArrayList<SingleChat> chats){
        super(fm);
        this.chats=chats;
    }

    @Override
    public Fragment getItem(int i) {

        switch (i){
            case 0: return new UserInfoActivityFragment();
            case 2:     //TODO list of users
            case 1: {
                Bundle b=new Bundle();
                ChatFragment cF= new ChatFragment();
                b.putParcelableArrayList("chats", chats);
                cF.setArguments(b);
                return cF;
            }
            default:return null;
        }
    }

    @Override
    public int getCount() {
        return 3;       //TODO remove hardcode
    }

    @Override
    public CharSequence getPageTitle(int position){
        return String.valueOf(position+1);      //TODO to remove once tabs can be custom
    }
}
