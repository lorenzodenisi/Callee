package com.callee.calleeclient.services;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.callee.calleeclient.Global;
import com.callee.calleeclient.activities.HomeActivity;
import com.callee.calleeclient.database.dbDriver;
import com.callee.calleeclient.threads.UpdateThread;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static com.callee.calleeclient.Global.lastUpdate;

public class UpdateService extends Service {

    private Context context;
    private int updateRate;
    private dbDriver db;

    private UpdateThread update;

    public UpdateService() {
        context = this;
        db = Global.db;
        updateRate = Global.UPDATERATE;
    }

    @Override
    public void onCreate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(1, new Notification());
        }

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        update = new UpdateThread(context, updateRate, lastUpdate);
        update.setPriority(THREAD_PRIORITY_BACKGROUND);
        update.start();
        Global.isUpdateServiceRunning = true;

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        update.running = false;
        Global.isUpdateServiceRunning = false;
        super.onDestroy();
        Intent broadcastIntent = new Intent(this, RestarterBroadcastReceiver.class);
        sendBroadcast(broadcastIntent);
    }

    public boolean isAlive() {
        return this.update.isAlive();
    }


    public static class RestarterBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(RestarterBroadcastReceiver.class.getSimpleName(), "Service Stops!");
            Intent i = new Intent(context, UpdateService.class);
            i.putExtra("lastUpdate", lastUpdate);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(i);
            } else context.startService(i);
        }
    }


    public static class StartOnBootReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())
                    || "android.intent.action.REBOOT".equals(intent.getAction())) {

                if (Global.db == null) {
                    Global.db = new dbDriver();
                }
                Global.db.openConnection(context);

                HomeActivity.fetchCredentials();

                Log.i(RestarterBroadcastReceiver.class.getSimpleName(), "Service Stops!");
                //System.out.println("LOLLO");    //debug
                Intent i = new Intent(context, UpdateService.class);
                i.putExtra("lastUpdate", lastUpdate);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(i);
                } else context.startService(i);
            }
        }
    }
}