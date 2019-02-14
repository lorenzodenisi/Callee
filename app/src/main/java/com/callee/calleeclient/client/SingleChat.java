package com.callee.calleeclient.client;

import android.os.Parcel;
import android.os.Parcelable;

public class SingleChat implements Parcelable {
    private String user;
    private int newMessages;
    private String lastMessagePreview;
    private String lastMessageHour;
    private String email;

    public SingleChat(String name){
        this.user=name;
        this.newMessages=0;
        this.lastMessagePreview="";
    }

    public SingleChat(String name,String email, String lastMessagePreview, int newMessages, String lastMessageHour){
        this.user=name;
        this.email=email;
        this.lastMessagePreview=lastMessagePreview;
        this.newMessages=newMessages;
        this.lastMessageHour=lastMessageHour;
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

    public String getLastMessageHour() {
        return lastMessageHour;
    }

    public void setLastMessageHour(String lastMessageHour) {
        this.lastMessageHour = lastMessageHour;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString(){
        return this.user;
    }


    //code for make SingleChat parselable
    public SingleChat(Parcel p){
        String[] data = new String[5];

        p.readStringArray(data);
        this.user = data[0];
        this.email = data[1];
        newMessages = Integer.parseInt(data[2]);
        this.lastMessagePreview = data[3];
        this.lastMessageHour=data[4];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {this.user, this.email,
                String.valueOf(this.newMessages),
                this.lastMessagePreview, this.lastMessageHour});
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
