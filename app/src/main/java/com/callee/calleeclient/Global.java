package com.callee.calleeclient;

import com.callee.calleeclient.database.dbDriver;

public class Global {
    static public String SERVERHOST = "www.lorenzodenisi.com";
    static public int PORT = 7777;
    static public String SERVERMAIL = "calleeproject@gmail.com";
    static public int UPDATERATE = 500;

    public static final int MAXAUTHATTEMPT = 3;
    static int tabNumber = 3;
    static public String username;
    static public String email;
    static public dbDriver db;
    public static NotifyManager notifyManager;
    public static Boolean isUpdateServiceRunning = false;
    public static Long lastUpdate = 0L;

}