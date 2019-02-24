package com.callee.calleeclient;

import com.callee.calleeclient.database.dbDriver;

public class Global {
    public static final int MAXAUTHATTEMPT = 3;
    static int tabNumber=3;
    static public String username;
    static public String email;
    static public dbDriver db;

    static public String SERVERHOST="www.lorenzodenisi.com";
    static public int PORT = 7777;
}