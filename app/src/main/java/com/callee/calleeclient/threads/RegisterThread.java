package com.callee.calleeclient.threads;

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
            //resolving server address
            InetAddress addr = InetAddress.getByName(Global.SERVERHOST);
            //creating socket
            Socket socket = new Socket(addr.getHostAddress(), Global.PORT);
            Writer outW = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), UTF_8));
            //sending message
            outW.append(this.message.toJSON()).append("\n").flush();

            //listening for response
            InputStream fromClient = socket.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fromClient, UTF_8));
            String content = bufferedReader.readLine();

            if (content == null) {
                out1.write(0);  //ERROR
                return;
            }
            JSONObject obj = new JSONObject(content);
            Message response = new Message(obj);

            if (response.getType() != ToM.REGISTERUSERRESPONSE || (!response.getText().equals("OK"))) {
                out1.write(0);  //ERROR
                return;
            }

            //if I am here, the combination of username and email is right
            out1.write(1);       //OK

            //variable accessible from outside, used to hint thread to finish before blocking functions
            if (stopped) return;

            int i;
            for (i = 0; i < Global.MAXAUTHATTEMPT; i++) {

                byte[] codeByte = new byte[6];
                in2.read(codeByte);

                //variable accessible from outside, used to hint thread to finish before blocking functions
                if (stopped) return;

                int code;
                try {
                    code = Integer.parseInt(new String(codeByte));
                } catch (NumberFormatException e) {
                    continue;
                }

                //creating message to send typed 6-digit code
                message.setType(ToM.REGISTERCONFIRM);
                message.putText(Integer.toString(code));

                outW.append(this.message.toJSON()).append("\n").flush();        //send message
                content = bufferedReader.readLine();                            //receive message

                if (content == null) {
                    out1.write(0);  //ERROR
                    return;
                }

                obj = new JSONObject(content);
                response = new Message(obj);

                if (response.getType() != ToM.REGISTERCONFIRMRESPONSE || (!response.getText().equals("OK"))) {
                    if (i == Global.MAXAUTHATTEMPT - 1) {
                        out1.write(2);     //LAST ERROR
                        return;
                    }
                    out1.write(0);  //ERROR
                } else break;
            }

            //finally set credentials inside db
            dbDriver.setCredentialsThread t = Global.db.setCredentials(response.getToName(), response.getToEmail(), null);
            if (t._join()) {
                out1.write(1);
            }
            outW.close();
            socket.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public void _join() {
        this.stopped = true;    //setting flag to stop
        try {
            out2.write('x');    //write something on pipe if it's stuck on it
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