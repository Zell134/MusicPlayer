package com.zell.musicplayer.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.Properties;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "database";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        updateDB(sqLiteDatabase, 0, DB_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        updateDB(sqLiteDatabase, i, i1);
    }

    public void updateDB(SQLiteDatabase db, int oldVersion, int newVersion){
        if (oldVersion < 1) {
            db.execSQL("CREATE TABLE PROPERTIES (_id INTEGER PRIMARY KEY AUTOINCREMENT, NAME TEXT,VALUE TEXT);");
            insertProperty(db, PropertiesList.LIBRARY_TYPE, LibraryType.LIBRARY_TYPE_EXTERNAL_STORAGE.getValue());
        }
    }

    public static void insertProperty(SQLiteDatabase db, String name,
                                    String value) {
        ContentValues property = new ContentValues();
        property.put("NAME", name);
        property.put("VALUE", value);
        long i = db.insert("PROPERTIES", null, property);
    }

    public static void updateProperty(SQLiteDatabase db, String name,
                                      String value) {
        ContentValues property = new ContentValues();
        property.put("NAME", name);
        property.put("VALUE", value);
        String selection = "NAME = ? ";
        String[] selectionArgs = new String[]{name};
        if(db.update("PROPERTIES", property, selection, selectionArgs)==0){
            insertProperty(db, name, value);
        }
    }

    public static String getProperty(SQLiteDatabase db, String property){
        String[] projection = new String[]{"_id", "NAME", "VALUE"};
        String selection = "NAME LIKE ? ";
        String[] selectionArgs = new String[]{property};
        Cursor cursor = db.query("PROPERTIES",
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null);
        if (cursor!=null && cursor.moveToFirst()) {
            String result = cursor.getString(cursor.getColumnIndexOrThrow("VALUE"));
            cursor.close();
            return result;
        }
        return null;
    }

    public static Properties getAllProperties(SQLiteDatabase db){
        String[] projection = new String[]{"_id", "NAME", "VALUE"};
        Cursor cursor = db.query("PROPERTIES", projection, null, null, null, null, null);
        Properties result = new Properties();
        if (cursor!=null && cursor.moveToFirst()) {
            result.put(cursor.getString(1), cursor.getString(2));
            while (cursor.moveToNext()) {
                result.put(cursor.getString(1), cursor.getString(2));
            }
            cursor.close();
        }
        return result;
    }
}
