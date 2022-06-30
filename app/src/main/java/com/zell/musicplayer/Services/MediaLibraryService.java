package com.zell.musicplayer.Services;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.zell.musicplayer.models.Song;

import java.util.ArrayList;
import java.util.List;

public class MediaLibraryService {

    Cursor cursor;

    public MediaLibraryService(){

    }

    public List<Song> getAllMediaFromLibrary(Context context){
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

            if (cursor!=null && cursor.moveToFirst()) {
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

    private Song getSongFromCursorRecord() {
        String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
        String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
        String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
        return new Song(data, title, album, artist);
    }
}
