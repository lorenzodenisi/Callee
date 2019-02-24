package com.callee.calleeclient.thread;

import com.callee.calleeclient.Global;
import com.callee.calleeclient.client.Message;
import com.callee.calleeclient.client.ToM;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;

import static java.nio.charset.StandardCharsets.UTF_8;

public class RegisterThread extends Thread {

    private Message message;
    private boolean stopped = false;
    public PipedOutputStream out1 = new PipedOutputStream(), out2 = new PipedOutputStream();
    public PipedInputStream in1, in2;

    public RegisterThread(Message m) {
        this.message = m;

        try {
            in1 = new PipedInputStream(out1);
            in2 = new PipedInputStream(out2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            InetAddress addr = InetAddress.getByName(Global.SERVERHOST);
            Socket socket = new Socket(addr.getHostAddress(), Global.PORT);
            Writer outW = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), UTF_8));
            outW.append(this.message.toJSON()).append("\n").flush();

            InputStream fromClient = socket.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fromClient, UTF_8));
            String content = bufferedReader.readLine();

            if (content == null) {
                out1.write(-1);  //ERROR
                return;
            }
            JSONObject obj = new JSONObject(content);
            Message response = new Message(obj);

            if (response.getType() != ToM.REGISTERUSERRESPONSE || (!response.getText().equals("OK"))) {
                out1.write(-1);  //ERROR
                return;
            }

            /*while(in1.available()>0)
                in2.read();*/

            out1.write(1);       //OK

            if (stopped) return;

            byte[] codeByte = new byte[6];
            in2.read(codeByte);
            if (stopped) return;
            int code = Integer.parseInt(new String(codeByte));

            message.setType(ToM.REGISTERCONFIRM);
            message.putText(Integer.toString(code));

            outW.append(this.message.toJSON()).append("\n").flush();     //send message
            content = bufferedReader.readLine();                        //receive message

            if (content == null) {
                out1.write(-1);  //ERROR
                return;
            }

            obj = new JSONObject(content);
            response = new Message(obj);

            if (response.getType() != ToM.REGISTERCONFIRMRESPONSE | (!response.getText().equals("OK"))) {
                out1.write(-1);  //ERROR
                return;
            }

            Global.db.setCredentials(response.getToName(), response.getToEmail(), null);
            out1.write(1);

            outW.close();
            socket.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public void customJoin() {
        this.stopped = true;
        try {
            out2.write('x');
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            this.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}