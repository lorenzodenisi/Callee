package com.callee.calleeclient.client;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Update {

    private JSONArray messages;

    public Update(String toName, String toEmail, Long timestamp) {
        Message header = new Message((long) -1, "SERVER", toName,
                "server@server.server", toEmail, timestamp, ToM.UPDATERESPONSE);

        messages = new JSONArray();
        this.addMessage(header);
    }

    public Update(JSONArray messages) {
        try {
            if (messages.getJSONObject(0).get("type").equals("UPDATERESPONSE"))
                this.messages = messages;
        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("Error creating Update message");
        }
    }


    public void addMessage(Message message) {
        messages.put(message.getContent());
    }

    public JSONObject getHeader() {
        try {
            return messages.getJSONObject(0);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<Message> getMessages() {
        ArrayList<Message> list = new ArrayList<>();
        for (int i = 1; i < messages.length(); i++) {
            try {
                list.add(new Message(messages.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public int getSize() {
        return this.messages.length();
    }

    public String toJSON() {
        return this.messages.toString();
    }
}
