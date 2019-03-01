package com.callee.calleeclient.thread;

import com.callee.calleeclient.Global;
import com.callee.calleeclient.client.Message;
import com.callee.calleeclient.client.ToM;
import com.callee.calleeclient.database.Contact;

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

import static java.nio.charset.StandardCharsets.UTF_8;

public class addContactThread extends Thread {

    private String email;
    private Message m;
    private Contact c;

    public addContactThread(String email) {
        this.email = email;
        m = new Message(-1L, Global.username, "SERVER", Global.email,
                Global.SERVERMAIL, System.currentTimeMillis(), ToM.CONFIRMCONTACT);
    }

    @Override
    public void run() {
        m.putText(email);

        try {
            InetAddress addr = InetAddress.getByName(Global.SERVERHOST);
            Socket socket = new Socket(addr.getHostAddress(), Global.PORT);
            Writer outW = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), UTF_8));
            outW.append(this.m.toJSON()).append("\n").flush();

            InputStream fromClient = socket.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fromClient, UTF_8));
            String content = bufferedReader.readLine();

            Message response = new Message(new JSONObject(content));
            if (!response.getText().equals("")) {
                c = new Contact(response.getText(), email, null);
            }

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    public Contact _join() {
        try {
            this.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return c;
    }
}
