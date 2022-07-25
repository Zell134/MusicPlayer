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
        return DatabaseHelper.getAllProperties(db);
    }

    public static LibraryType getLibraryType(Context context) {
        SQLiteDatabase db = new DatabaseHelper(context).getReadableDatabase();
        switch (DatabaseHelper.getProperty(db, LIBRARY_TYPE)) {
            case "MediaLibrary": {
                return LibraryType.LIBRARY_TYPE_MEDIA_LIBRARY;
            }
            case "ExternalStorage": {
                return LibraryType.LIBRARY_TYPE_EXTERNAL_STORAGE;
            }
            case "Artists": {
                return LibraryType.LIBRARY_TYPE_ARTISTS;
            }
        }
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
    }

    public static String getCurrentSong(Context context) {
        SQLiteDatabase db = new DatabaseHelper(context).getReadableDatabase();
        return DatabaseHelper.getProperty(db, CURRENT_SONG);
    }

    public static void setCurrentSong(Context context, String songPath) {
        SQLiteDatabase db = new DatabaseHelper(context).getWritableDatabase();
        DatabaseHelper.updateProperty(db, CURRENT_SONG, songPath);
    }
}
