package com.callee.calleeclient;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;

import com.callee.calleeclient.client.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatSimpleAdapter extends SimpleAdapter {

    private List<Message> messages;
    private List<Map<String, String>> data;

    private int messageBackMargin, messageFrontMargin, messageVerticalMargin;

    public ChatSimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to, ArrayList<Message> messages) {
        super(context, data, resource, from, to);
        this.messages = messages;

        this.data = (List<Map<String, String>>)data;

        this.messageBackMargin = (int) context.getResources().getDimension(R.dimen.message_back_margin);
        this.messageFrontMargin = (int) context.getResources().getDimension(R.dimen.message_front_margin);
        this.messageVerticalMargin = (int) context.getResources().getDimension(R.dimen.message_vertical_margin);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = super.getView(position, convertView, parent);

        try {
            LinearLayout ll = v.findViewById(R.id.message_container);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams
                    (FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            lp.width = FrameLayout.LayoutParams.WRAP_CONTENT;
            lp.height = FrameLayout.LayoutParams.WRAP_CONTENT;
            lp.topMargin = this.messageVerticalMargin;
            lp.bottomMargin = this.messageVerticalMargin;

            if (isReceived(position)) {          //message received
                lp.gravity = Gravity.START;
                lp.leftMargin = this.messageBackMargin;
                lp.rightMargin = this.messageFrontMargin;
            } else {
                lp.gravity = Gravity.END;              //message sent
                lp.leftMargin = this.messageFrontMargin;
                lp.rightMargin = this.messageBackMargin;
            }

            ll.setLayoutParams(lp);
        } catch (ClassCastException e) {
            e.printStackTrace();
        }

        return v;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    private boolean isReceived(int i) {
        //Message m = messages.get(i);
        //return (Global.email.equals(m.getToEmail()));
        return (data.get(i).get("to").compareTo(Global.email)==0);
    }
}