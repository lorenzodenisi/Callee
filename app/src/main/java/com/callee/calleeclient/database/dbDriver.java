package com.callee.calleeclient.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.callee.calleeclient.client.Message;
import com.callee.calleeclient.client.SingleChat;
import com.callee.calleeclient.client.ToM;

import java.util.ArrayList;
import java.util.List;

public class dbDriver {

    private dbHelper dbHelper = null;
    private SQLiteDatabase dbReadable = null, dbWritable = null;
    private Thread dbThread;
    private boolean[] res = {true};
    private ExceptionHandler handler = new ExceptionHandler(res);

    public dbDriver() {
    }

    public boolean openConnection(Context context) {
        this.dbHelper = new dbHelper(context);

        this.dbReadable = dbHelper.getReadableDatabase();
        this.dbWritable = dbHelper.getWritableDatabase();
        return dbWritable != null && dbReadable != null;
    }

    public void putMessage(Message m) {
        dbThread = new Thread(new putMessageRunnable(m));
        res[0] = true;
        dbThread.setUncaughtExceptionHandler(handler);
        dbThread.start();
    }

    private class putMessageRunnable implements Runnable {
        Message m;

        putMessageRunnable(Message m) {
            this.m = m;
        }

        @Override
        public void run() {
            ContentValues cv = new ContentValues();

            cv.put("ID", m.getId());
            cv.put("fromName", m.getFromName());
            cv.put("toName", m.getToName());
            cv.put("fromEmail", m.getFromEmail());
            cv.put("toEmail", m.getToEmail());
            cv.put("timestamp", m.getTimestamp());
            cv.put("text", m.getText());
            if (dbWritable.insert("MESSAGES", null, cv) == -1) {
                System.err.println("Error inserting message to database");
                throw new SQLiteException();
            }
        }
    }

    public void getMessages(List<Message> messages, Contact c) {

        res[0] = true;
        dbThread = new Thread(new getMessagesRunnable(messages, c));
        dbThread.setUncaughtExceptionHandler(handler);
        dbThread.start();
    }

    private class getMessagesRunnable implements Runnable {

        List[] messages = new ArrayList[1];
        Contact c;

        getMessagesRunnable(List<Message> messages, Contact c) {
            this.messages[0] = messages;
            this.c = c;
        }

        @Override
        public void run() {
            String selection = "MESSAGES.toEmail =? OR MESSAGES.fromEmail =?";
            String[] selectionArgs = {c.getEmail(), c.getEmail()};
            String sortOrder = "MESSAGES.timestamp ASC";

            Cursor cursor = dbReadable.query("MESSAGES", null, selection, selectionArgs, null, null, sortOrder);

            long id, timestamp;
            String toEmail, fromEmail, toName, fromName, text;

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

                messages[0].add(m);
            }
            cursor.close();
        }
    }

    public void setCredentials(String user, String email, String number) {

        res[0] = true;
        dbThread = new Thread(new setCredentialsRunnable(user, email, number));
        dbThread.setUncaughtExceptionHandler(handler);
        dbThread.start();
    }

    private class setCredentialsRunnable implements Runnable {

        String user, email, number;


        setCredentialsRunnable(String user, String email, String number) {
            this.user = user;
            this.email = email;
            this.number = number;
        }

        @Override
        public void run() {
            ContentValues cv = new ContentValues();

            cv.put("username", user);
            cv.put("email", email);
            cv.put("number", number);

            dbWritable.delete("CREDENTIALS", null, null);

            if (dbWritable.insert("CREDENTIALS", null, cv) == -1) {
                System.out.println("Error setting credentials");
                throw new SQLiteException();
            }
        }
    }

    public void getCredentials(Contact credentials) {
        res[0] = true;
        dbThread = new Thread(new getCredentialsRunnable(credentials));
        dbThread.setUncaughtExceptionHandler(handler);
        dbThread.start();
    }

    private class getCredentialsRunnable implements Runnable {

        Contact[] credentials = new Contact[1];

        getCredentialsRunnable(Contact credentials) {
            this.credentials[0] = credentials;
        }

        @Override
        public void run() {
            Cursor c = dbReadable.query("CREDENTIALS", null, null, null, null, null, null);
            String user, email, number;

            if (c.moveToNext()) {
                user = c.getString(c.getColumnIndexOrThrow("username"));
                email = c.getString(c.getColumnIndexOrThrow("email"));
                number = c.getString(c.getColumnIndexOrThrow("number"));
                credentials[0].setName(user);
                credentials[0].setEmail(email);
                credentials[0].setNumber(number);
            }
            c.close();
        }
    }

    public void putContact(Contact c) {
        res[0] = true;
        dbThread.setUncaughtExceptionHandler(handler);
        dbThread = new Thread(new putContactRunnable(c));
        dbThread.start();
    }

    private class putContactRunnable implements Runnable {
        Contact c;

        putContactRunnable(Contact c) {
            this.c = c;
        }

        @Override
        public void run() {
            ContentValues cv = new ContentValues();

            cv.put("username", c.getName());
            cv.put("email", c.getEmail());
            cv.put("number", c.getNumber());

            if (dbWritable.insert("CONTACTS", null, cv) == -1)
                System.err.println("Error adding contact to database");
        }
    }

    public void getContacts(ArrayList<Contact> contacts) {
        res[0] = true;
        dbThread = new Thread(new getContactsRunnable(contacts));
        dbThread.setUncaughtExceptionHandler(handler);
        dbThread.start();
    }

    private class getContactsRunnable implements Runnable {
        ArrayList[] contacts = new ArrayList[1];

        getContactsRunnable(ArrayList<Contact> contacts) {
            this.contacts[0] = contacts;
        }

        @Override
        public void run() {
            String order = "CONTACTS.username DESC";
            Cursor c = dbReadable.query("CONTACTS", null, null, null, null, null, order);

            String user, email, number;

            while (c.moveToNext()) {
                user = c.getString(c.getColumnIndexOrThrow("username"));
                email = c.getString(c.getColumnIndexOrThrow("email"));
                number = c.getString(c.getColumnIndexOrThrow("number"));

                contacts[0].add(new Contact(user, email, number));
            }

            c.close();
        }
    }

    public void getChats(ArrayList<SingleChat> chats) {
        res[0] = true;
        dbThread = new Thread(new getChatsRunnable(chats));
        dbThread.setUncaughtExceptionHandler(handler);
        dbThread.start();
    }

    private class getChatsRunnable implements Runnable {

        ArrayList[] chats = new ArrayList[1];

        getChatsRunnable(ArrayList<SingleChat> chats) {
            this.chats[0] = chats;
        }

        @Override
        public void run() {
            String order = "CHATS.lastMessageTime";
            Cursor c = dbReadable.query("CHATS", null, null, null, null, null, order);

            String user, email, lastMessagePreview;
            long lastMessageTime;
            int newMessages;

            while (c.moveToNext()) {
                user = c.getString(c.getColumnIndexOrThrow("user"));
                email = c.getString(c.getColumnIndexOrThrow("email"));
                lastMessagePreview = c.getString(c.getColumnIndexOrThrow("lastMessagePreview"));
                newMessages = c.getInt(c.getColumnIndexOrThrow("newMessages"));
                lastMessageTime = c.getLong(c.getColumnIndexOrThrow("lastMessageTime"));

                chats[0].add(new SingleChat(user, email, lastMessagePreview, newMessages, lastMessageTime));
            }

            c.close();
        }
    }

    public void putChat(SingleChat sc) {
        res[0] = true;
        dbThread.setUncaughtExceptionHandler(handler);
        dbThread = new Thread(new putChatRunnable(sc));
        dbThread.start();
    }

    private class putChatRunnable implements Runnable {
        SingleChat sc;

        putChatRunnable(SingleChat sc) {
            this.sc = sc;
        }

        @Override
        public void run() {
            ContentValues cv = new ContentValues();

            cv.put("user", sc.getUser());
            cv.put("email", sc.getEmail());
            cv.put("lastMessagePreview", sc.getLastMessagePreview());
            cv.put("newMessages", sc.getNewMessages());
            cv.put("lastMessageTime", sc.getLastMessageTime());

            if (dbWritable.insert("CHATS", null, cv) == -1)
                System.err.println("Error adding chat to database");
        }
    }

    public long getLastUpdate() {
        Cursor c = dbReadable.rawQuery("SELECT MAX(timestamp) FROM MESSAGES", null);
        c.moveToNext();
        long l = c.getLong(0);
        c.close();

        return l;
    }

    public void restoreDB() {
        dbHelper.restoreDB(dbWritable);
    }

    public void closeConnection() {
        this.dbHelper.close();
    }

    public boolean joinDbThread() {
        if (dbThread != null) {
            try {
                dbThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return res[0];
        }
        return false;
    }

    private class ExceptionHandler implements Thread.UncaughtExceptionHandler {
        boolean[] res;

        ExceptionHandler(boolean[] res) {
            this.res = res;
        }

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            System.out.println("UncaughtEx:" + e);
            res[0] = false;
        }
    }
}