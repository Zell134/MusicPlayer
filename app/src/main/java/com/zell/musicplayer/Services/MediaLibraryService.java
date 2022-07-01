package com.zell.musicplayer.Services;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.zell.musicplayer.R;
import com.zell.musicplayer.models.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;


public class MediaLibraryService {

    private static Cursor cursor;
    private static String[] projection = new String[] {
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST
    };

    public static List<Song> getAllMediaFromLibrary(Context context){
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        List<Song> playlist = new ArrayList<>();
        playlist.add(new Song(uri.toString(),"/","","",false));
        getMediaFiles(playlist, "",context);
        return playlist;
    }

    public static List<Song> getFilesList(Context context, File file) {
        List<Song> playlist = new ArrayList<>();
        File previousFile = new File(file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf("/")));
        if(previousFile.exists() && previousFile.canRead()) {
            playlist.add(new Song(file.getAbsolutePath(), context.getResources().getString(R.string.previous_directory), "", "", false));
        }
        Stream.of(file.listFiles(f -> f.isDirectory() && f.canRead()))
                .forEach(f->playlist.add(new Song(f.getAbsolutePath(), f.getName(),"","",false)));
        getMediaFiles(playlist, file.getPath(),context);
        return playlist;
    }

    private static void getMediaFiles(List<Song> playlist, String filePath, Context context){
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.DATA + " like " + "'%" + filePath + "/%[^'/']'";
        try{
            if(filePath.equals("")){
                cursor = contentResolver.query(uri, projection, null, null, null);

            }else {
                cursor = contentResolver.query(uri, projection, selection, null, null);
            }
            if (cursor!=null && cursor.moveToFirst()) {
                playlist.add(getSongFromCursorRecord());
                while (cursor.moveToNext()) {
                    playlist.add(getSongFromCursorRecord());
                }
                cursor.close();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    private static Song getSongFromCursorRecord() {
        String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
        String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
        String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
        return new Song(data, title, album, artist, true);
    }
}
