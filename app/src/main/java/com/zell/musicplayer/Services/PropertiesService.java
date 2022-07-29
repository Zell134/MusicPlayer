package com.zell.musicplayer.Services;

import static com.zell.musicplayer.db.PropertiesList.CURRENT_SONG;
import static com.zell.musicplayer.db.PropertiesList.LIBRARY_TYPE;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.zell.musicplayer.db.DatabaseHelper;
import com.zell.musicplayer.db.LibraryType;

import java.util.Properties;

public class PropertiesService {

    public static Properties getAllProperties(Context context){
        SQLiteDatabase db = new DatabaseHelper(context).getReadableDatabase();
        Properties properties = DatabaseHelper.getAllProperties(db);
        db.close();
        return properties;
    }

    public static LibraryType getLibraryType(Context context) {
        SQLiteDatabase db = new DatabaseHelper(context).getReadableDatabase();
        switch (DatabaseHelper.getProperty(db, LIBRARY_TYPE)) {
            case "MediaLibrary": {
                db.close();
                return LibraryType.LIBRARY_TYPE_MEDIA_LIBRARY;
            }
            case "ExternalStorage": {
                db.close();
                return LibraryType.LIBRARY_TYPE_EXTERNAL_STORAGE;
            }
            case "Artists": {
                db.close();
                return LibraryType.LIBRARY_TYPE_ARTISTS;
            }
        }
        db.close();
        return null;
    }

    public static void setLibraryType(Context context, LibraryType value) {
        SQLiteDatabase db = new DatabaseHelper(context).getWritableDatabase();
        switch (value) {
            case LIBRARY_TYPE_MEDIA_LIBRARY: {
                DatabaseHelper.updateProperty(db, LIBRARY_TYPE, "MediaLibrary");
                break;
            }
            case LIBRARY_TYPE_EXTERNAL_STORAGE: {
                DatabaseHelper.updateProperty(db, LIBRARY_TYPE, "ExternalStorage");
                break;
            }
            case LIBRARY_TYPE_ARTISTS: {
                DatabaseHelper.updateProperty(db, LIBRARY_TYPE, "Artists");
                break;
            }
        }
        db.close();
    }

    public static String getCurrentSong(Context context) {
        SQLiteDatabase db = new DatabaseHelper(context).getReadableDatabase();
        String result = DatabaseHelper.getProperty(db, CURRENT_SONG);
        db.close();
        return result;
    }

    public static void setCurrentSong(Context context, String songPath) {
        SQLiteDatabase db = new DatabaseHelper(context).getWritableDatabase();
        DatabaseHelper.updateProperty(db, CURRENT_SONG, songPath);
        db.close();
    }
}
