package com.callee.calleeclient.client;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class SingleChat implements Parcelable, Comparable {
    private String user;
    private int newMessages = 0;
    private String lastMessagePreview;
    private Long lastMessageTime;
    private String email;

    public SingleChat(String name, String email, String lastMessagePreview, int newMessages, Long lastMessageTime) {
        this.user = name;
        this.email = email;
        this.lastMessagePreview = lastMessagePreview;
        this.newMessages = newMessages;
        this.lastMessageTime = lastMessageTime;
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

    public Long getLastMessageTime() {
        return lastMessageTime;
    }

    public String getFormattedLastMessageTime() {
        return Message.getFormattedChatTime(lastMessageTime);
    }

    public void setLastMessageTime(Long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @NonNull
    @Override
    public String toString() {
        return this.user;
    }

    //code for make SingleChat parselable
    public SingleChat(Parcel p) {
        String[] data = new String[5];

        p.readStringArray(data);
        this.user = data[0];
        this.email = data[1];
        newMessages = Integer.parseInt(data[2]);
        this.lastMessagePreview = data[3];
        this.lastMessageTime = Long.parseLong(data[4]);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{this.user,
                this.email,
                String.valueOf(this.newMessages),
                this.lastMessagePreview,
                this.lastMessageTime.toString()});
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public SingleChat createFromParcel(Parcel in) {
            return new SingleChat(in);
        }

        public SingleChat[] newArray(int size) {
            return new SingleChat[size];
        }
    };

    @Override
    public int compareTo(Object o) {
        SingleChat s = (SingleChat) o;

        Long t1, t2;
        t1 = this.lastMessageTime;
        t2 = s.getLastMessageTime();

        if(t2-t1 > 0) return 1;
        if(t2.equals(t1)) return 0;
        if(t2-t1 < 0) return -1;

        return 0;
        //return (int) (s.getLastMessageTime() - this.lastMessageTime);
    }
}
