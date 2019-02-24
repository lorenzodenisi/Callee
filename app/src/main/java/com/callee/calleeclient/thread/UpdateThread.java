package com.callee.calleeclient.thread;

import android.content.Context;
import android.content.Intent;

import com.callee.calleeclient.Global;
import com.callee.calleeclient.client.Message;
import com.callee.calleeclient.client.SingleChat;
import com.callee.calleeclient.client.ToM;
import com.callee.calleeclient.client.Update;
import com.callee.calleeclient.database.Contact;
import com.callee.calleeclient.database.dbDriver;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import static java.nio.charset.StandardCharsets.UTF_8;

public class UpdateThread extends Thread {

    private dbDriver localDB;
    private int updateRate;

    private Context context;
    private ArrayList<Message> messages;
    private Message updateMessage;
    private Long lastUpdatedTime;
    private ArrayList<String> chatEmails;


    public UpdateThread(Context context, dbDriver localDB, int updateRate, Long lastUpdate) {
        super();

        this.context = context;
        this.localDB = localDB;
        this.updateRate = updateRate;
        this.lastUpdatedTime = lastUpdate;

        this.messages = new ArrayList<>();

        ArrayList<SingleChat> chats = new ArrayList<>();   //get current chats
        chatEmails = new ArrayList<>();
        this.localDB.getChats(chats);
        this.localDB.joinDbThread();
        for (SingleChat sc : chats) {
            this.chatEmails.add(sc.getEmail());
        }

        this.updateMessage = new Message(-1L, Global.username, "SERVER",
                Global.email, "server@server.server", System.currentTimeMillis(), ToM.UPDATEREQUEST);
        this.updateMessage.addLastUpdated(this.lastUpdatedTime);

    }

    @Override
    public void run() {

        try {
            InetAddress addr = InetAddress.getByName(Global.SERVERHOST);

            while (true) {

                try {

                    Socket socket = new Socket(addr.getHostAddress(), Global.PORT);

                    this.updateMessage.addLastUpdated(this.lastUpdatedTime);
                    Writer out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), UTF_8));
                    out.append(this.updateMessage.toJSON()).append("\n").flush();

                    InputStream fromClient = socket.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fromClient, UTF_8));
                    String content = bufferedReader.readLine();

                    if (content != null) {
                        try {
                            JSONArray a = new JSONArray(content);
                            Update update = new Update(a);
                            messages.clear();
                            messages.addAll(update.getMessages());
                            if (!messages.isEmpty())
                                this.lastUpdatedTime = Long.parseLong(messages.get(messages.size() - 1).getTimestamp());

                        } catch (JSONException e) {
                            e.printStackTrace();    //ignore message
                        }
                    }

                    //assuming that messages are sorted


                    out.close();
                    socket.close();


                    ArrayList<SingleChat> newChats = new ArrayList<>();
                    for (Message m : messages) {

                        Contact user;
                        if (m.getFromEmail().equals(Global.email)) {
                            user = new Contact(m.getToName(), m.getToEmail(), null);
                        } else {
                            user = new Contact(m.getFromName(), m.getFromEmail(), null);
                        }

                        //check if relative chat is present
                        if (!this.chatEmails.contains(user.getEmail())) {
                            chatEmails.add(user.getEmail());
                            SingleChat newSC = new SingleChat(user.getName(), user.getEmail(),
                                    "", 0, 0L);
                            this.localDB.putChat(newSC);
                            newChats.add(newSC);
                            this.localDB.joinDbThread();
                        }

                        this.localDB.putMessage(m);
                        this.localDB.joinDbThread();
                    }

                    if (!messages.isEmpty()) {
                        Intent broadcastIntent = new Intent();
                        broadcastIntent.setAction("com.callee.calleeclient.Broadcast");
                        broadcastIntent.putExtra("messages", messages);
                        broadcastIntent.putExtra("chats", newChats);
                        context.sendBroadcast(broadcastIntent);
                    }

                } catch (IOException e) {
                    //ignored
                    e.printStackTrace();
                }

                Thread.sleep(updateRate);

            }
        } catch (InterruptedException | UnknownHostException e) {
            //jump out of loop
        }
    }
}
