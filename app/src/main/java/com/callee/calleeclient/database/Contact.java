package com.callee.calleeclient.database;

import android.os.Parcel;
import android.os.Parcelable;

public class Contact implements Parcelable, Comparable {

    private String name;
    private String email;
    private String number;

    public Contact(String name, String email, String number) {
        this.name = name;
        this.email = email;
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    //code for make SingleChat parselable
    public Contact(Parcel p) {
        String[] data = new String[5];

        p.readStringArray(data);
        this.name = data[0];
        this.email = data[1];
        this.number = data[2];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{this.name, this.email,
                this.number});
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };

    @Override
    public int compareTo(Object o) {
        Contact c = (Contact)o;

        return this.name.compareTo(c.getName());
    }
}
