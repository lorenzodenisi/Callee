package com.callee.calleeclient.database;

class Query {

    static String SQL_CREATE_CREDENTIALS =
            "  CREATE TABLE IF NOT EXISTS CREDENTIALS(\n" +
                    "  username TEXT NOT NULL ,\n" +
                    "  email TEXT PRIMARY KEY NOT NULL ,\n" +
                    "  number TEXT\n" +
                    ")\n";

    static String SQL_CREATE_CONTACTS =
            "CREATE TABLE IF NOT EXISTS CONTACTS(\n" +
                    "  username TEXT NOT NULL ,\n" +
                    "  email TEXT PRIMARY KEY NOT NULL ,\n" +
                    "  number TEXT\n" +
                    "  )\n ";

    static String SQL_CREATE_MESSAGES =
            "CREATE TABLE IF NOT EXISTS MESSAGES(\n" +
                    "  ID BIGINT UNSIGNED PRIMARY KEY NOT NULL ,\n" +
                    "  fromName TEXT NOT NULL REFERENCES CONTACTS(username) ON UPDATE CASCADE ON DELETE NO ACTION ,\n" +
                    "  toName TEXT NOT NULL REFERENCES CONTACTS(username) ON UPDATE CASCADE ON DELETE NO ACTION ,\n" +
                    "  fromEmail TEXT NOT NULL REFERENCES CONTACTS(email) ON UPDATE NO ACTION ON DELETE NO ACTION ,\n" +
                    "  toEmail TEXT NOT NULL REFERENCES CONTACTS(email) ON UPDATE NO ACTION ON DELETE NO ACTION,\n" +
                    "  timestamp BIGINT UNSIGNED NOT NULL,\n" +
                    "  text TEXT,\n" +
                    "  read INTEGER DEFAULT 0\n" +
                    ");\n";

    static String SQL_CREATE_CHATS =
            "CREATE TABLE IF NOT EXISTS CHATS(\n" +
                    "  user TEXT NOT NULL REFERENCES CONTACTS(username) ON UPDATE CASCADE ON DELETE NO ACTION ,\n" +
                    "  email TEXT PRIMARY KEY NOT NULL REFERENCES CONTACTS(email) ON UPDATE CASCADE ON DELETE NO ACTION ,\n" +
                    "  newMessages INTEGER NOT NULL, \n" +
                    "  lastMessagePreview TEXT, \n" +
                    "  lastMessageTS BIGINT UNSIGNED NOT NULL\n" +
                    ")\n";


    static String SQL_DELETE_CHATS = "DROP TABLE CHATS";
    static String SQL_DELETE_MESSAGES = "DROP TABLE MESSAGES";
    static String SQL_DELETE_CONTACTS = "DROP TABLE CONTACTS";
    static String SQL_DELETE_CREDENTIALS = "DROP TABLE CREDENTIALS";

    //TODO add database tables info (names and column names)
}
