package com.zell.musicplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

public class MediaLibraryAccess {

    Cursor cursor;

    public MediaLibraryAccess(){

    }

    public List<Song> getAllMediaFromLibrary(Context context){
        Song song = new Song();
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        List<Song> list = new ArrayList<>();
        String[] projection = new String[] {
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST
        };
        try{
            cursor = contentResolver.query(uri, projection, null, null, null);

            if (cursor.moveToNext()) {
                list.add(getSongFromCursorRecord());
                while (cursor.moveToNext()) {
                    list.add(getSongFromCursorRecord());
                }
            } else {
                return null;
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Song getSongFromCursorRecord(){
        return new Song(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)),
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)),
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)),
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
        );
    }

    public void checkPermissions(){

    }
}
