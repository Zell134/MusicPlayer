package com.zell.musicplayer.Services;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.zell.musicplayer.R;
import com.zell.musicplayer.models.Folder;
import com.zell.musicplayer.models.Item;
import com.zell.musicplayer.models.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;


public class MediaLibraryService {

    private static Cursor cursor;
    private static ContentResolver contentResolver;
    private static String[] projection = new String[] {
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ARTIST
    };

    public static List<Item> getAllMedia(Context context){
        List<Item> playlist = new ArrayList<>();
        getMediaFiles(playlist, "",context);
        return playlist;
    }

    public static List<Item> getArtistList(Context context){
        List<Item> playlist = new ArrayList<>();
        sortByArtists(playlist, context);
        return playlist;
    }

    public static List<Item> getFilesList(Context context, File file) {
        List<Item> playlist = new ArrayList<>();
        File previousFile = new File(file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf("/")));
        if(previousFile.exists() && previousFile.canRead()) {
            playlist.add(new Folder(file.getAbsolutePath(), context.getResources().getString(R.string.previous_directory),  false));
        }
        Stream.of(file.listFiles(f -> f.isDirectory() && f.canRead()))
                .forEach(f->playlist.add(new Folder(f.getAbsolutePath(), f.getName(),false)));
        getMediaFiles(playlist, file.getPath(),context);
        return playlist;
    }

    public static List<Item> getAlbumsOfArtist(Context context, String artist){
        List<Item> playlist = new ArrayList<>();
        getAlbumList(playlist, context, artist);
        return playlist;
    }
    public static List<Item> getSongsOfAlbum(Context context, String album, String artist){
        List<Item> playlist = new ArrayList<>();
        getSongsList(playlist, context, album, artist);
        return playlist;
    }

    private static void getSongsList(List<Item> playlist, Context context, String album, String artist) {
        contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        playlist.add(new Folder(artist, context.getResources().getString(R.string.previous_directory),  false));
        String selection = MediaStore.Audio.Media.ALBUM + " LIKE ? AND " + MediaStore.Audio.Media.ARTIST + " LIKE ? ";
        String[] selectionArgs = new String[]{
                album,
                artist
        };
        cursor = contentResolver.query(uri, projection, selection, selectionArgs, null);
        if (cursor!=null && cursor.moveToFirst()) {
            playlist.add(getSongFromCursorRecord());
            while (cursor.moveToNext()) {
                playlist.add(getSongFromCursorRecord());
            }
            cursor.close();
        }
    }
    private static void getAlbumList(List<Item> playlist, Context context, String artist) {
        contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        playlist.add(new Folder("root", context.getResources().getString(R.string.previous_directory),  false));
        String selection = MediaStore.Audio.Media.ARTIST + " LIKE ? ";
        String[] selectionArgs = new String[]{artist};
        cursor = contentResolver.query(uri, new String[] {MediaStore.Audio.Media.ALBUM}, selection, selectionArgs, null);
        Set<String> set = new HashSet<>();
        if (cursor!=null && cursor.moveToFirst()) {
            set.add(cursor.getString(0));
            while (cursor.moveToNext()) {
                set.add(cursor.getString(0));
            }
            cursor.close();
        }
        set.forEach(e -> playlist.add(new Folder(artist, e, false)));
    }

    private static void getMediaFiles(List<Item> playlist, String filePath, Context context){
        contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.DATA + " LIKE ? AND " + MediaStore.Audio.Media.DATA + " NOT LIKE ? ";
        String[] selectionArgs = new String[]{
                "%" + filePath + "%",
                "%" + filePath + "/%/%"
        };
        try{
            if(filePath.equals("")){
                cursor = contentResolver.query(uri, projection, null, null, null);

            }else {
                cursor = contentResolver.query(uri, projection, selection, selectionArgs, null);
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

    private static void sortByArtists(List<Item> playlist, Context context){
        contentResolver = context.getContentResolver();
        cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] {MediaStore.Audio.Media.ARTIST},
                null,
                null,
                null
        );
        Set<String> set = new HashSet<>();
        if (cursor!=null && cursor.moveToFirst()) {
            set.add(cursor.getString(0));
            while (cursor.moveToNext()) {
                set.add(cursor.getString(0));
            }
            cursor.close();
        }
        set.forEach(e -> playlist.add(new Folder("root", e, false)));
    }



    private static Item getSongFromCursorRecord() {
        String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
        String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
        String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
        long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
        return new Song(data, title, album, artist, duration, true);
    }
}
