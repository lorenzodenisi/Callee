package com.callee.calleeclient;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;

import com.callee.calleeclient.client.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatSimpleAdapter extends SimpleAdapter {

    ArrayList<Message> messages;
    String userEmail;

    int messageBackMargin, messageFrontMargin, messageVerticalMargin;

    /**
     * Constructor
     *
     * @param context  The context where the View associated with this SimpleAdapter is running
     * @param data     A List of Maps. Each entry in the List corresponds to one row in the list. The
     *                 Maps contain the data for each row, and should include all the entries specified in
     *                 "from"
     * @param resource Resource identifier of a view layout that defines the views for this list
     *                 item. The layout file should include at least those named views defined in "to"
     * @param from     A list of column names that will be added to the Map associated with each
     *                 item.
     * @param to       The views that should display column in the "from" parameter. These should all be
     *                 TextViews. The first N views in this list are given the values of the first N columns
     */
    public ChatSimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to, ArrayList<Message> messages, String userEmail) {
        super(context, data, resource, from, to);
        this.messages=messages;
        this.userEmail=userEmail;

        this.messageBackMargin=(int)context.getResources().getDimension(R.dimen.message_back_margin);
        this.messageFrontMargin=(int)context.getResources().getDimension(R.dimen.message_front_margin);
        this.messageVerticalMargin=(int)context.getResources().getDimension(R.dimen.message_vertical_margin);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v =  super.getView(position, convertView, parent);

        try {
            LinearLayout ll = v.findViewById(R.id.message_container);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams
                    (FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            lp.width= FrameLayout.LayoutParams.WRAP_CONTENT;
            lp.height= FrameLayout.LayoutParams.WRAP_CONTENT;
            lp.topMargin=this.messageVerticalMargin;
            lp.bottomMargin=this.messageVerticalMargin;


            if(isReceived(position)) {          //message received
                lp.gravity = Gravity.START;
                lp.leftMargin= this.messageBackMargin;
                lp.rightMargin=this.messageFrontMargin;
            }else {
                lp.gravity = Gravity.END;              //message sent
                lp.leftMargin=this.messageFrontMargin;
                lp.rightMargin= this.messageBackMargin;
            }

            ll.setLayoutParams(lp);
        }catch (ClassCastException e){
            e.printStackTrace();
        }

        return v;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    private boolean isReceived(int i){
        Message m = messages.get(i);
        return (this.userEmail.equals(m.getFromEmail()));
    }

    private int dpToPx(Context context,float dp){
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (int)((dp * displayMetrics.density) + 0.5);
    }
}
