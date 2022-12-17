package com.example.notificationuploader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NotificationDbHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "NotificationDb.db";
    public static final String TABLE_NAME = "NotificationTbl";
    public static final int DATABASE_VERSION = 1;
    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + TABLE_NAME + " (packageName TEXT PRIMARY KEY, applicationName TEXT)";
    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;

    NotificationDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL( SQL_CREATE_ENTRIES );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL( SQL_DELETE_ENTRIES );
        onCreate(db);
    }

    public void deletePackageName(SQLiteDatabase db, String packageName){
        String name = hasPackageName(db, packageName);
        if( name == null )
            return;

        ContentValues values = new ContentValues();
        values.put("packageName", packageName);

        db.delete(TABLE_NAME, "packageName = ?", new String[]{ packageName });
    }

    public void insertPackageName(SQLiteDatabase db, String packageName, String applicationName){
        String name = hasPackageName(db, packageName);
        if( name != null )
            return;

        ContentValues values = new ContentValues();
        values.put("packageName", packageName);
        values.put("applicationName", applicationName);

        db.insert(TABLE_NAME, null, values);
    }

    public String hasPackageName(SQLiteDatabase db, String packageName){
        Cursor cursor = db.query(TABLE_NAME, new String[] { "packageName", "applicationName" },
                "packageName = ?",
                new String[]{ packageName },
                null,
                null,
                null );

        String applicationName = null;
        if( cursor.getCount() > 0 ) {
            cursor.moveToNext();
            applicationName = cursor.getString(1);
        }

        cursor.close();

        return applicationName;
    }
}