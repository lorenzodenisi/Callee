package com.callee.calleeclient.thread;

import com.callee.calleeclient.Global;
import com.callee.calleeclient.client.Message;

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

public class ConfirmReadThread extends Thread {

    Message message;
    boolean res=true;

    public ConfirmReadThread(Message m) {
        this.message=m;
    }

    @Override
    public void run() {
        try {
            InetAddress addr = InetAddress.getByName(Global.SERVERHOST);
            Socket socket = new Socket(addr.getHostAddress(), Global.PORT);

            Writer out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), UTF_8));
            out.append(message.toJSON()).append("\n").flush();

            InputStream fromClient = socket.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fromClient, UTF_8));
            String content = bufferedReader.readLine();
            if (content != null) {
                Message response = new Message(new JSONObject(content));
                if(response.getText().equals("OK")){
                    res=true;
                }else res=false;
            }

            Global.db.confirmRead(message.getText(), Long.parseLong(message.getTimestamp()));
            if(Global.db.joinDbThread()){
                res=true;
            }else res=false;

        }catch (IOException | JSONException e){
            e.printStackTrace();
            res=false;
        }
    }

    public boolean _join(){
        try {
            this.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return res;
    }
}
