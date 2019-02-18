package com.callee.calleeclient.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class dbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Callee.db";


    public dbHelper(Context context){
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
        db.execSQL(Query.SQL_DELETE_DB);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void restoreDB(SQLiteDatabase db){
        db.execSQL(Query.SQL_DELETE_DB);
        this.onCreate(db);
    }

}
