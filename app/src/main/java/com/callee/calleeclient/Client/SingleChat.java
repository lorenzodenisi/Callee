package com.callee.calleeclient.Client;

import android.os.Parcel;
import android.os.Parcelable;

public class SingleChat implements Parcelable {
    private String user;
    private int newMessages;
    private String lastMessagePreview;

    public SingleChat(String name){
        this.user=name;
        this.newMessages=0;
        this.lastMessagePreview="";
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getNewMessages() {
        return newMessages;
    }

    public void setNewMessages(int newMessages) {
        this.newMessages = newMessages;
    }

    public String getLastMessagePreview() {
        return lastMessagePreview;
    }

    public void setLastMessagePreview(String lastMessagePreview) {
        this.lastMessagePreview = lastMessagePreview;
    }

    @Override
    public String toString(){
        return this.user;
    }


    //code for make SingleChat parselable
    public SingleChat(Parcel p){
        String[] data = new String[3];

        p.readStringArray(data);
        this.user = data[0];
        newMessages = Integer.parseInt(data[1]);
        this.lastMessagePreview = data[2];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {this.user,
                String.valueOf(this.newMessages),
                this.lastMessagePreview});
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public SingleChat createFromParcel(Parcel in) {
            return new SingleChat(in);
        }

        public SingleChat[] newArray(int size) {
            return new SingleChat[size];
        }
    };
}
