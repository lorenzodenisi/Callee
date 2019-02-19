package com.callee.calleeclient.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class dbHelper extends SQLiteOpenHelper {

    static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "Callee.db";

    dbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Query.SQL_CREATE_CREDENTIALS);
        db.execSQL(Query.SQL_CREATE_CONTACTS);
        db.execSQL(Query.SQL_CREATE_MESSAGES);
        db.execSQL(Query.SQL_CREATE_CHATS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(Query.SQL_DELETE_CHATS);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    void restoreDB(SQLiteDatabase db) {
        try {
            db.execSQL(Query.SQL_DELETE_CHATS);
            db.execSQL(Query.SQL_DELETE_MESSAGES);
            db.execSQL(Query.SQL_DELETE_CONTACTS);
            db.execSQL(Query.SQL_DELETE_CREDENTIALS);
        } catch (SQLiteException e) {
            e.printStackTrace();
            System.err.println("Error resoring database");
        }
        this.onCreate(db);
    }

}