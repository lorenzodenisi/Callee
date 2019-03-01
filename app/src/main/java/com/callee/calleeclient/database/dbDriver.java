package com.callee.calleeclient.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.callee.calleeclient.Global;
import com.callee.calleeclient.client.Message;
import com.callee.calleeclient.client.SingleChat;
import com.callee.calleeclient.client.ToM;

import java.util.ArrayList;
import java.util.HashMap;

public class dbDriver {

    private dbHelper dbHelper = null;
    private SQLiteDatabase dbReadable = null, dbWritable = null;
    private boolean[] res = {true};
    private ExceptionHandler handler = new ExceptionHandler(res);

    public boolean openConnection(Context context) {
        this.dbHelper = new dbHelper(context);

        this.dbReadable = dbHelper.getReadableDatabase();
        this.dbWritable = dbHelper.getWritableDatabase();
        return dbWritable != null && dbReadable != null;
    }

    public putMessageThread putMessage(Message m) {
        putMessageThread dbThread = new putMessageThread(m);
        dbThread.setUncaughtExceptionHandler(handler);
        dbThread.start();
        return dbThread;
    }

    public class putMessageThread extends Thread {
        Message m;
        boolean res = true;

        putMessageThread(Message m) {
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
                res = false;
            }
        }

        public boolean _join() {
            try {
                this.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return res;
        }

    }

    public Thread getMessages(ArrayList<Message> messages, Contact c) {

        getMessagesThread dbThread = new getMessagesThread(messages, c);
        dbThread.setUncaughtExceptionHandler(handler);
        dbThread.start();
        return dbThread;
    }

    private class getMessagesThread extends Thread {

        ArrayList<Message> messages;
        Contact c;

        getMessagesThread(ArrayList<Message> messages, Contact c) {
            this.messages = messages;
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

                messages.add(m);
            }
            cursor.close();
        }

        public ArrayList<Message> _join() {
            try {
                this.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return messages;
        }

    }

    public confirmReadThread confirmRead(String email, Long timestamp) {
        confirmReadThread dbThread = new confirmReadThread(email, timestamp);
        dbThread.setUncaughtExceptionHandler(handler);
        dbThread.start();
        return dbThread;
    }

    public class confirmReadThread extends Thread {

        boolean res = true;
        String email;
        Long timestamp;

        confirmReadThread(String email, Long timestamp) {
            this.email = email;
            this.timestamp = timestamp;
        }

        @Override
        public void run() {
            ContentValues cv = new ContentValues();
            cv.put("read", "1");
            String condition = "fromEmail = ? AND toEmail= ? AND timestamp <= ?";

            if (dbWritable.update("MESSAGES", cv, condition, new String[]{email, Global.email, timestamp.toString()}) == -1) {
                System.err.println("Error updating message to database");
                res = false;
            }
        }

        public boolean _join() {
            try {
                this.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return res;
        }
    }

    public setCredentialsThread setCredentials(String user, String email, String number) {

        setCredentialsThread dbThread = new setCredentialsThread(user, email, number);
        dbThread.setUncaughtExceptionHandler(handler);
        dbThread.start();
        return dbThread;
    }

    public class setCredentialsThread extends Thread {

        boolean res = true;
        String user, email, number;


        setCredentialsThread(String user, String email, String number) {
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
                res = false;
            }
        }

        public boolean _join() {
            try {
                this.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return res;
        }
    }

    public Thread getCredentials(Contact credentials) {
        getCredentialsThread dbThread = new getCredentialsThread(credentials);
        dbThread.setUncaughtExceptionHandler(handler);
        dbThread.start();
        return dbThread;
    }

    private class getCredentialsThread extends Thread {

        Contact credentials;

        getCredentialsThread(Contact credentials) {
            this.credentials = credentials;
        }

        @Override
        public void run() {
            Cursor c = dbReadable.query("CREDENTIALS", null, null, null, null, null, null);
            String user, email, number;

            if (c.moveToNext()) {
                user = c.getString(c.getColumnIndexOrThrow("username"));
                email = c.getString(c.getColumnIndexOrThrow("email"));
                number = c.getString(c.getColumnIndexOrThrow("number"));
                credentials.setName(user);
                credentials.setEmail(email);
                credentials.setNumber(number);
            }
            c.close();
        }
    }

    public putContactThread putContact(Contact c) {

        putContactThread dbThread = new putContactThread(c);
        dbThread.setUncaughtExceptionHandler(handler);
        dbThread.start();
        return dbThread;
    }

    public class putContactThread extends Thread {
        boolean res = true;
        Contact c;

        putContactThread(Contact c) {
            this.c = c;
        }

        @Override
        public void run() {
            ContentValues cv = new ContentValues();

            cv.put("username", c.getName());
            cv.put("email", c.getEmail());
            cv.put("number", c.getNumber());

            if (dbWritable.insert("CONTACTS", null, cv) == -1) {
                System.err.println("Error adding contact to database");
                res = false;
            }
        }

        public boolean _join() {
            try {
                this.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return res;
        }
    }

    public Thread getContacts(ArrayList<Contact> contacts) {
        getContactsThread dbThread = new getContactsThread(contacts);
        dbThread.setUncaughtExceptionHandler(handler);
        dbThread.start();
        return dbThread;
    }

    private class getContactsThread extends Thread {
        ArrayList<Contact> contacts;

        getContactsThread(ArrayList<Contact> contacts) {
            this.contacts = contacts;
        }

        @Override
        public void run() {
            String order = "CONTACTS.username ASC";
            Cursor c = dbReadable.query("CONTACTS", null, null, null, null, null, order);

            String user, email, number;

            while (c.moveToNext()) {
                user = c.getString(c.getColumnIndexOrThrow("username"));
                email = c.getString(c.getColumnIndexOrThrow("email"));
                number = c.getString(c.getColumnIndexOrThrow("number"));

                contacts.add(new Contact(user, email, number));
            }

            c.close();
        }
    }

    public Thread getChats(HashMap<String, SingleChat> chats) {

        getChatsThread dbThread = new getChatsThread(chats);
        dbThread.setUncaughtExceptionHandler(handler);
        dbThread.start();
        return dbThread;
    }

    private class getChatsThread extends Thread {

        HashMap<String, SingleChat> chats;

        getChatsThread(HashMap<String, SingleChat> chats) {
            this.chats = chats;
        }

        @Override
        public void run() {
            String order = "lastMessageTS DESC";
            Cursor c = dbReadable.query("CHATS", null, null, null, null, null, order);

            String user, email, lastMessagePreview;
            int newMessages;
            long lastMessageTS;

            while (c.moveToNext()) {
                user = c.getString(c.getColumnIndexOrThrow("user"));
                email = c.getString(c.getColumnIndexOrThrow("email"));
                newMessages = c.getInt(c.getColumnIndexOrThrow("newMessages"));
                lastMessagePreview = c.getString(c.getColumnIndexOrThrow("lastMessagePreview"));
                lastMessageTS = c.getLong(c.getColumnIndexOrThrow("lastMessageTS"));

                chats.put(email, new SingleChat(user, email, lastMessagePreview, newMessages, lastMessageTS));
            }

            c.close();
        }
    }

    public putChatThread putChat(SingleChat sc) {
        putChatThread dbThread = new putChatThread(sc);
        dbThread.setUncaughtExceptionHandler(handler);
        dbThread.start();
        return dbThread;
    }

    public class putChatThread extends Thread {
        SingleChat sc;
        boolean res = true;

        putChatThread(SingleChat sc) {
            this.sc = sc;
        }

        @Override
        public void run() {
            ContentValues cv = new ContentValues();

            cv.put("user", sc.getUser());
            cv.put("email", sc.getEmail());
            cv.put("newMessages", sc.getNewMessages());
            cv.put("lastMessagePreview", sc.getLastMessagePreview());
            cv.put("lastMessageTS", sc.getLastMessageTime());

            if (dbWritable.insert("CHATS", null, cv) == -1) {
                System.err.println("Error adding chat to database");
                res = false;
            }
        }

        public boolean _join() {
            try {
                this.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return res;
        }
    }

    public updateChatsThread updateChats(ArrayList<SingleChat> newChats) {
        updateChatsThread dbThread = new updateChatsThread(newChats);
        dbThread.setUncaughtExceptionHandler(handler);
        dbThread.start();
        return dbThread;
    }

    public class updateChatsThread extends Thread {

        ArrayList<SingleChat> newChats;
        boolean res = true;

        updateChatsThread(ArrayList<SingleChat> newChats) {
            this.newChats = newChats;
        }

        @Override
        public void run() {

            for (SingleChat chat : newChats) {
                ContentValues cv = new ContentValues();

                cv.put("newMessages", chat.getNewMessages());
                cv.put("lastMessagePreview", chat.getLastMessagePreview());
                cv.put("lastMessageTS", chat.getLastMessageTime());
                String condition = "email=?";
                String[] arg = new String[]{chat.getEmail()};

                if (dbWritable.update("CHATS", cv, condition, arg) == -1) {
                    System.err.println("Error updating chats");
                    this.res = false;
                }
            }
        }

        public boolean _join() {
            try {
                this.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return res;
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

    //join wrapper to reduce code on other classes
    public static void join(Thread t) {
        if (t == null) return;
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}