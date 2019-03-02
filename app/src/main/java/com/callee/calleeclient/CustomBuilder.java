package com.callee.calleeclient;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

public class CustomBuilder extends NotificationCompat.Builder {

    private String text;

    public CustomBuilder(@NonNull Context context, @NonNull String channelId) {
        super(context, channelId);
    }

    @Override
    public NotificationCompat.Builder setContentText(CharSequence c) {
        this.text = c.toString();
        return super.setContentText(c);
    }

    public void appendContentText(String s) {
        this.setContentText(text + s);
        this.setStyle(new NotificationCompat.BigTextStyle().bigText(text + s));
    }

    public String getText() {
        return this.text;
    }
}
