package com.callee.calleeclient;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.callee.calleeclient.activities.HomeActivity;
import com.callee.calleeclient.client.Message;

import java.util.ArrayList;
import java.util.HashMap;

public class NotifyManager {

    private final String CHANNEL_ID = "Callee";
    private final String GROUPKEY = "chats";
    private NotificationManager notificationManager;
    private int lastId = 0;
    private HashMap<String, CustomBuilder> builders;
    private HashMap<CustomBuilder, Integer> ids;
    private int SUMMARYID = 0;
    private String currentChat = null;


    public NotifyManager(Context context) {
        createNotificationChannel(context);
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Callee";
            String description = "CalleeNotificationManager";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLightColor(Color.RED);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);


        }
        builders = new HashMap<>();
        ids = new HashMap<>();
    }

    public void notifyMessages(Context context, ArrayList<Message> upt) {

        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();

        Intent tapIntent=new Intent(context, HomeActivity.class);

        for (Message m : upt) {
            if (m.getFromEmail().equals(Global.email) || m.getFromEmail().equals(this.currentChat)) {
                continue;
            }

            CustomBuilder builder = builders.get(m.getFromEmail());
            if (builder == null) {
                builder = new CustomBuilder(context, this.CHANNEL_ID);
                builder.setContentText("");
                builder.setNumber(upt.size());
                builder.setContentTitle(m.getFromName());
                builder.setGroup(this.GROUPKEY);
                builder.setColor(context.getResources().getColor(R.color.colorAccent));
                builder.setSmallIcon(R.drawable.ic_notificationicon);
                builders.put(m.getFromEmail(), builder);
                ids.put(builder, ++lastId);
            }

            builder.appendContentText(m.getText() + "\n");

        }


        NotificationCompat.Builder summary = new NotificationCompat.Builder(context, CHANNEL_ID);
        summary.setContentTitle("Callee")
                .setContentText("New Messages")
                .setSmallIcon(R.drawable.ic_notificationicon)
                .setGroupSummary(true)
                .setGroup(GROUPKEY)
                .setStyle(style)
                .setContentIntent(PendingIntent.getActivity(context, 0, tapIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setDefaults(~Notification.DEFAULT_ALL)
                .setColor(context.getResources().getColor(R.color.colorAccent))
                .setVibrate(new long[]{1000,1000,1000,1000})
                .setAutoCancel(true);

        int i=0;
        for (String email : builders.keySet()) {
            CustomBuilder builder = builders.get(email);
            if (builder.getText().equals(""))
                continue;
            if (email.equals(this.currentChat))
                continue;

            style.addLine(email);
            style.setSummaryText(upt.size()+" new messages");

            notificationManager.notify(ids.get(builder), builder.build());
            i++;
        }

        if (i > 1) {
            notificationManager.notify(SUMMARYID, summary.build());
        }else if(i==1){
            String email=new ArrayList<>(builders.keySet()).get(0);
            CustomBuilder cb = builders.get(email);
            cb.setContentIntent(PendingIntent.getActivity(context, 0, tapIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT));
            cb.setAutoCancel(true);
            cb.setColor(context.getResources().getColor(R.color.colorAccent));
            notificationManager.notify(ids.get(cb), cb.build());
            cb.setAutoCancel(false);
            cb.setContentIntent(null);
        }
    }

    public void resetNotifications(String email) {

        CustomBuilder builder = builders.get(email);
        if (builder == null)
            return;
        Integer id = ids.get(builder);

        if (id == null) {
            return;
        }

        notificationManager.cancel(id);
        ids.remove(builder);
        builders.remove(email);

        if (builders.values().isEmpty() && ids.values().isEmpty()) {
            notificationManager.cancelAll();
        }
    }

    public void setCurrentChat(String currentChat) {
        this.currentChat = currentChat;
    }
}