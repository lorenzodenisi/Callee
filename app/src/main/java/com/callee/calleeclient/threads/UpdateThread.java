package com.callee.calleeclient.threads;

import android.content.Context;
import android.content.Intent;

import com.callee.calleeclient.Global;
import com.callee.calleeclient.NotifyManager;
import com.callee.calleeclient.activities.HomeActivity;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

import static com.callee.calleeclient.Global.db;
import static java.nio.charset.StandardCharsets.UTF_8;

public class UpdateThread extends Thread {

    private int updateRate;

    private Context context;
    private ArrayList<Message> messages;
    private Message updateMessage;
    private HashMap<String, SingleChat> chats;
    public boolean running;

    public UpdateThread(Context context, int updateRate, Long lastUpdate) {
        super();

        this.running=true;
        this.context = context;
        this.updateRate = updateRate;
        this.messages = new ArrayList<>();
        chats = new HashMap<>();   //get current chats


        this.updateMessage = new Message(-1L, Global.username, "SERVER",
                Global.email, Global.SERVERMAIL, System.currentTimeMillis(), ToM.UPDATEREQUEST);
        this.updateMessage.addLastUpdated(Global.lastUpdate);

        if(Global.notifyManager==null)
            Global.notifyManager=new NotifyManager(context);
    }

    @Override
    public void run() {

        try {
            InetAddress addr = InetAddress.getByName(Global.SERVERHOST);

            while (running) {

                if(updateMessage.getFromEmail()==null)
                    continue;

                System.out.println("CALLEE THREAD" + Thread.currentThread().getId());    //debug
                try {

                    if(Global.db!=null) {
                        Thread t = Global.db.getChats(chats);
                        t.join();
                    }

                    Socket socket = new Socket(addr.getHostAddress(), Global.PORT);

                    long dbLastUpdate=Global.db.getLastUpdate();        //TODO use function
                    if(dbLastUpdate>=Global.lastUpdate)
                        Global.lastUpdate=dbLastUpdate;

                    this.updateMessage.addLastUpdated(Global.lastUpdate);

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


                        } catch (JSONException e) {
                            e.printStackTrace();    //ignore message
                        }
                    }

                    //assuming that messages are sorted

                    out.close();
                    socket.close();

                    for (Message m : messages) {

                        if(Long.parseLong(m.getTimestamp())>Global.lastUpdate){
                            Global.lastUpdate=Long.parseLong(m.getTimestamp());
                        }

                        //get the other user of message
                        Contact user;
                        if (m.getFromEmail().equals(Global.email)) {
                            user = new Contact(m.getToName(), m.getToEmail(), null);

                        } else {
                            user = new Contact(m.getFromName(), m.getFromEmail(), null);
                        }

                        //check if relative chat is present (only if app is running)
                        if(Global.db!=null && chats!=null) {
                            if (!this.chats.containsKey(user.getEmail())) {
                                SingleChat newSC = new SingleChat(user.getName(), user.getEmail(),
                                        m.getText(), 1, Long.parseLong(m.getTimestamp()));
                                chats.put(newSC.getEmail(), newSC);
                                Global.db.putChat(newSC)._join();

                            } else {                                            //new chats could be modified here if there are more than one message
                                SingleChat SC = chats.get(user.getEmail());
                                if (m.getFromEmail().equals(user.getEmail())) {   //if is received
                                    SC.setNewMessages(Objects.requireNonNull(SC).getNewMessages() + 1);
                                }
                                Objects.requireNonNull(SC).setLastMessagePreview(m.getText());
                                SC.setLastMessageTime(Long.parseLong(m.getTimestamp()));
                            }

                            dbDriver.putMessageThread t2 = Global.db.putMessage(m);
                            t2._join();
                        }
                    }

                    if (!messages.isEmpty()) {

                        Collections.sort(messages, (a,b)-> a.getTimestamp().compareTo(b.getTimestamp()));
                        Global.notifyManager.notifyMessages(context, messages);

                        if(Global.db!=null) {
                            Global.db.updateChats(new ArrayList<>(chats.values()))._join();      //update all chats (only if app is running)
                            Intent broadcastIntent = new Intent();
                            broadcastIntent.setAction("com.callee.calleeclient.Broadcast");
                            broadcastIntent.putExtra("messages", messages);
                            broadcastIntent.putExtra("chats", new ArrayList<>(chats.values()));
                            context.sendBroadcast(broadcastIntent);
                        }
                    }

                } catch (IOException e) {
                    //ignored
                    e.printStackTrace();
                    System.out.println("CALLEE: Error on update thread");
                }

                Thread.sleep(updateRate);
            }
        } catch (InterruptedException | UnknownHostException e) {
            //jump out of loop
            e.printStackTrace();
            System.out.println("CALLEE: Error on update thread");    //debug
        }
    }
}
