package com.callee.calleeclient;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

public class CustomBuilder extends NotificationCompat.Builder {

    private String text;
    private int nMessages=0;

    public CustomBuilder(@NonNull Context context, @NonNull String channelId) {
        super(context, channelId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this.setPriority(NotificationManager.IMPORTANCE_MAX);
        }else{
            this.setPriority(NotificationCompat.PRIORITY_MAX);
        }
    }

    @Override
    public NotificationCompat.Builder setContentText(CharSequence c) {
        this.text = c.toString();
        return super.setContentText(c);
    }


    public void appendContentText(String s) {
        text=text+s;
        nMessages++;
        super.setContentText(nMessages+" new messages");
        this.setStyle(new NotificationCompat.BigTextStyle().bigText(text.trim()));
    }

    public String getText() {
        return this.text;
    }
}
