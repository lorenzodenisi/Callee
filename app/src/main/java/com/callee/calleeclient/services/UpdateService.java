package com.callee.calleeclient.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.callee.calleeclient.Global;
import com.callee.calleeclient.R;
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
    private final String CHANNEL_ID = "com.callee.calleeclientupdate";
    private final int NOTIFICATION_ID = 999;


    public UpdateService() {
        context = this;
        db = Global.db;
        updateRate = Global.UPDATERATE;
    }

    @Override
    public void onCreate() {

        //Dummy notification to start foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = "Callee tray icon";
            String description = "Callee tray icon, it shows when the app is running update services";

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            channel.setDescription(description);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(channel);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
            notificationBuilder.setSmallIcon(R.drawable.ic_notificationicon);
            notificationBuilder.setContentTitle("Callee is running");
            notificationBuilder.setContentText("Tap here for disabling this tray icon");
            notificationBuilder.setContentIntent(getAppInfo());
            startForeground(NOTIFICATION_ID, notificationBuilder.build());
        } else
            startForeground(NOTIFICATION_ID, new Notification());

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


    private PendingIntent getAppInfo() {

        String SCHEME = "package";
        String APP_PKG_NAME_21 = "com.android.settings.ApplicationPkgName";
        String APP_PKG_NAME_22 = "pkg";
        String APP_DETAILS_PACKAGE_NAME = "com.android.settings";
        String APP_DETAILS_CLASS_NAME = "com.android.settings.InstalledAppDetails";
        String packageName = "com.callee.calleeclient";

        Intent intent = new Intent();
        final int apiLevel = Build.VERSION.SDK_INT;
        if (apiLevel >= 9) { // above 2.3
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts(SCHEME, packageName, null);
            intent.setData(uri);
        } else { // below 2.3
            final String appPkgName = (apiLevel == 8 ? APP_PKG_NAME_22
                    : APP_PKG_NAME_21);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName(APP_DETAILS_PACKAGE_NAME,
                    APP_DETAILS_CLASS_NAME);
            intent.putExtra(appPkgName, packageName);
        }
        return PendingIntent.getActivity(context, 1, intent, 0);
    }
}

