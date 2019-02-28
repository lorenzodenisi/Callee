package com.callee.calleeclient.thread;

import com.callee.calleeclient.Global;
import com.callee.calleeclient.client.Message;
import com.callee.calleeclient.client.ToM;
import com.callee.calleeclient.database.dbDriver;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SendMessageThread extends Thread {

    private dbDriver localDB;
    private Message sendMessage;
    private final ArrayList<Message> messages;

    public SendMessageThread(dbDriver localDB, Message m, ArrayList<Message> messages){
        this.localDB=localDB;
        this.sendMessage=m;
        this.messages=messages;
    }

    @Override
    public void run(){

        try {
            InetAddress addr = InetAddress.getByName(Global.SERVERHOST);
            Socket socket = new Socket(addr.getHostAddress(), Global.PORT);

            Writer out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), UTF_8));
            out.append(this.sendMessage.toJSON()).append("\n").flush();

            InputStream fromClient = socket.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fromClient, UTF_8));
            String content = bufferedReader.readLine();

            if (content != null) {
                JSONObject obj = new JSONObject(content);
                Message response = new Message(obj);

                if (response.getType() == ToM.MESSAGERESPONSE) {
                    sendMessage.setId(response.getId());

                    dbDriver.putMessageThread t = localDB.putMessage(sendMessage);
                    if (!t._join()) {
                        System.out.println("Error saving message to local database");
                        return;
                    }

                    synchronized (messages) {
                        messages.add(sendMessage);
                    }

                }
            }
        } catch (IOException | JSONException e){
            e.printStackTrace();
        }
    }
}
