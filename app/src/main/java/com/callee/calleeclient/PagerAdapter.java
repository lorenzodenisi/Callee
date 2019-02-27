package com.callee.calleeclient;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.callee.calleeclient.client.SingleChat;
import com.callee.calleeclient.database.Contact;
import com.callee.calleeclient.fragments.ChatListFragment;
import com.callee.calleeclient.fragments.ContactFragment;
import com.callee.calleeclient.fragments.UserInfoFragment;

import java.util.ArrayList;

public class PagerAdapter extends FragmentPagerAdapter {

    private ArrayList<SingleChat> chats;
    private ArrayList<Contact> contacts;
    private ChatListFragment cF;
    private ContactFragment contactF;
    private UserInfoFragment uF;

    private ArrayList<SingleChat> newChats;
    private Contact newContact;

    public PagerAdapter(FragmentManager fm, ArrayList<SingleChat> chats, ArrayList<Contact> contacts){
        super(fm);
        this.chats=chats;
        this.contacts=contacts;
    }

    @Override
    public Fragment getItem(int i) {

        switch (i){
            case 0: {
                uF = new UserInfoFragment();
                Bundle b = new Bundle();
                b.putParcelable("contact", new Contact(Global.username, Global.email, null));
                uF.setArguments(b);
                return uF;
            }
            case 1: {
                Bundle b=new Bundle();
                cF= new ChatListFragment();
                b.putParcelableArrayList("chats", chats);
                cF.setArguments(b);
                return cF;
            }
            case 2:{
                Bundle b = new Bundle();
                contactF=new ContactFragment();
                b.putParcelableArrayList("contacts", contacts);
                contactF.setArguments(b);
                return contactF;
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

   public void updateChats(ArrayList<SingleChat> newChats){
        this.newChats=newChats;
        this.notifyDataSetChanged();
    }


   public void updateContacts(Contact newContact){
        this.newContact=newContact;
        this.notifyDataSetChanged();
   }

    @Override
    public int getItemPosition(Object object){
        if(object instanceof ChatListFragment && newChats!=null){
            ((ChatListFragment) object).updateData(newChats);
            newChats=null;
        }

        if(object instanceof ContactFragment && newContact!=null){
            ((ContactFragment)object).addContact(newContact);
            newContact=null;

        }
        return super.getItemPosition(object);
    }

    public void removeContactInfo(){
        if(contactF!=null)
            this.contactF.removeUserInfo();
    }
}
