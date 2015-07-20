package com.example.liuyg.myapplication;


import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class UriTable {

    // Database table
    public static final String TABLE_URI = "uri";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_URI_STRING = "uri_string";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    // Database creation SQL statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_URI
            + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_URI_STRING + " text not null, "
            + COLUMN_TIMESTAMP + " long"
            + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        Log.w(UriTable.class.getName(), "Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_URI);
        onCreate(database);
    }
}