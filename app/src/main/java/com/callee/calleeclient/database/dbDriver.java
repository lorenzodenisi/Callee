package com.callee.calleeclient.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.callee.calleeclient.client.Message;
import com.callee.calleeclient.client.SingleChat;
import com.callee.calleeclient.client.ToM;

import java.util.ArrayList;


public class dbDriver {

    private Context context;
    private dbHelper dbHelper;
    private SQLiteDatabase dbReadable, dbWritable;


    public dbDriver(Context context) {
        this.context = context;
    }

    private boolean createDB() {
        return true;
    }

    public boolean openConnection() {
        this.dbHelper = new dbHelper(context);

        this.dbReadable = dbHelper.getReadableDatabase();
        this.dbWritable = dbHelper.getWritableDatabase();

        return dbWritable != null && dbReadable != null;
    }

    public boolean putMessage(Message m) {

        ContentValues cv = new ContentValues();

        cv.put("ID", m.getId());
        cv.put("fromName", m.getFromName());
        cv.put("toName", m.getToName());
        cv.put("fromEmail", m.getFromEmail());
        cv.put("toEmail", m.getToEmail());
        cv.put("timestamp", m.getTimestamp());
        cv.put("text", m.getText());

        return dbWritable.insert("MESSAGES", null, cv) != -1;
    }

    public ArrayList<Message> getMessages(Contact c) {

        String selection = "MESSAGES.toEmail =? OR MESSAGES.fromEmail =?";
        String[] selectionArgs = {c.getEmail(), c.getEmail()};
        String sortOrder = "MESSAGES.timestamp ASC";

        Cursor cursor = dbReadable.query("MESSAGES", null, selection, selectionArgs, null, null, sortOrder);

        long id, timestamp;
        String toEmail, fromEmail, toName, fromName, text;
        ArrayList<Message> messages = new ArrayList<>();

        while (cursor.moveToNext()) {
            id = cursor.getLong(cursor.getColumnIndexOrThrow("ID"));
            toEmail = cursor.getString(cursor.getColumnIndexOrThrow("toEmail"));
            toName = cursor.getString(cursor.getColumnIndexOrThrow("toName"));
            fromEmail = cursor.getString(cursor.getColumnIndexOrThrow("fromEmail"));
            fromName = cursor.getString(cursor.getColumnIndexOrThrow("fromName"));
            timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"));
            text = cursor.getString(cursor.getColumnIndexOrThrow("text"));

            Message m = new Message(id, fromName, toName, fromEmail, toEmail, timestamp, ToM.MESSAGE);
            m.putText(text);

            messages.add(m);
        }

        cursor.close();
        return messages;
    }

    public boolean setCredentials(String user, String email, String number) {
        ContentValues cv = new ContentValues();

        cv.put("username", user);
        cv.put("email", email);
        cv.put("number", number);

        return dbWritable.insert("CREDENTIALS", null, cv) != -1;
    }

    public Contact getCredentials() {
        Cursor c = dbReadable.query("CREDENTIALS", null, null, null, null, null, null);
        Contact credentials = null;
        String user, email, number;

        if (c.moveToNext()) {
            user = c.getString(c.getColumnIndexOrThrow("username"));
            email = c.getString(c.getColumnIndexOrThrow("email"));
            number = c.getString(c.getColumnIndexOrThrow("number"));
            credentials = new Contact(user, email, number);
        }
        c.close();
        return credentials;
    }

    public boolean putContact(Contact c) {

        ContentValues cv = new ContentValues();

        cv.put("username", c.getName());
        cv.put("email", c.getEmail());
        cv.put("number", c.getNumber());

        return dbWritable.insert("CONTACTS", null, cv) != -1;
    }

    public ArrayList<Contact> getContacts() {

        String order = "CONTACTS.username DESC";
        Cursor c = dbReadable.query("CONTACTS", null, null, null, null, null, order);

        String user, email, number;
        ArrayList<Contact> contacts = new ArrayList<>();

        while (c.moveToNext()) {
            user = c.getString(c.getColumnIndexOrThrow("username"));
            email = c.getString(c.getColumnIndexOrThrow("email"));
            number = c.getString(c.getColumnIndexOrThrow("number"));

            contacts.add(new Contact(user, email, number));
        }

        c.close();
        return contacts;
    }

    public ArrayList<SingleChat> getChats() {
        String order = "CHATS.lastMessageTime";
        Cursor c = dbReadable.query("CHATS", null, null, null, null, null, order);

        String user, email, lastMessagePreview;
        long lastMessageTime;
        int newMessages;

        ArrayList<SingleChat> chats = new ArrayList<>();

        while (c.moveToNext()) {
            user = c.getString(c.getColumnIndexOrThrow("user"));
            email = c.getString(c.getColumnIndexOrThrow("email"));
            lastMessagePreview = c.getString(c.getColumnIndexOrThrow("lastMessagePreview"));
            newMessages = c.getInt(c.getColumnIndexOrThrow("newMessages"));
            lastMessageTime = c.getLong(c.getColumnIndexOrThrow("lastMessageTime"));

            chats.add(new SingleChat(user, email, lastMessagePreview, newMessages, lastMessageTime));
        }

        c.close();
        return chats;
    }

    public boolean putChat(SingleChat sc) {
        ContentValues cv = new ContentValues();

        cv.put("user", sc.getUser());
        cv.put("email", sc.getEmail());
        cv.put("lastMessagePreview", sc.getLastMessagePreview());
        cv.put("newMessages", sc.getNewMessages());
        cv.put("lastMessageTime", sc.getLastMessageTime());

        return dbWritable.insert("CHATS", null, cv) != -1;
    }


    public void restoreDB(){
        dbHelper.restoreDB(dbWritable);
    }

    public void closeConnection() {
        this.dbHelper.close();
    }
}
