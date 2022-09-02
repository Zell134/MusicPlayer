package com.zell.musicplayer.Services;

import static com.zell.musicplayer.db.PropertiesList.BASS_BOOST;
import static com.zell.musicplayer.db.PropertiesList.CURRENT_PRESET;
import static com.zell.musicplayer.db.PropertiesList.CURRENT_SONG;
import static com.zell.musicplayer.db.PropertiesList.DELIMITER;
import static com.zell.musicplayer.db.PropertiesList.EQUALIZER;
import static com.zell.musicplayer.db.PropertiesList.LIBRARY_TYPE;
import static com.zell.musicplayer.db.PropertiesList.VOLUME_LEVEL;

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

    public static String getBassBoostValue(Context context) {
        return getValue(context, BASS_BOOST);
    }

    public static void setBassBoostValue(Context context, String value) {
        setValue(context, BASS_BOOST, value);
    }

    public static String getVolume(Context context) {
        return getValue(context, VOLUME_LEVEL);
    }

    public static void setVolume(Context context, String value) {
        setValue(context, VOLUME_LEVEL, value);
    }

    public static String getCurrentSong(Context context) {
        return getValue(context, CURRENT_SONG);
    }

    public static void setCurrentSong(Context context, String songPath) {
        setValue(context, CURRENT_SONG, songPath);
    }

    public static String getCurrentPreset(Context context) {
        return getValue(context, CURRENT_PRESET);
    }

    public static void setCurrentPreset(Context context, String preset) {
        setValue(context, CURRENT_PRESET, preset);
    }

    public static String getEqualizerBand(Context context, short band) {
        return getValue(context, EQUALIZER + DELIMITER + String.valueOf(band));
    }

    public static void setEqualizerBand(Context context, short band, String value) {
        setValue(context, EQUALIZER + DELIMITER + String.valueOf(band), value);
    }

    private static String getValue(Context context, String key){
        SQLiteDatabase db = new DatabaseHelper(context).getReadableDatabase();
        String result = DatabaseHelper.getProperty(db, key);
        db.close();
        return result;
    }

    public static void setValue(Context context, String key, String value) {
        SQLiteDatabase db = new DatabaseHelper(context).getWritableDatabase();
        DatabaseHelper.updateProperty(db, key, value);
        db.close();
    }
}
