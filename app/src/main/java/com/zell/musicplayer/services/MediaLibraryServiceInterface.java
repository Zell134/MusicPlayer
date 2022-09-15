package com.zell.musicplayer.services;

import android.content.Context;

import com.zell.musicplayer.models.Item;
import com.zell.musicplayer.models.Song;

import java.io.File;
import java.util.List;

public interface MediaLibraryServiceInterface {

    List<Item> getAllMedia(Context context);
    List<Item> getArtistList(Context context);
    List<Item> getFilesList(Context context, File file);
    List<Item> getAlbumsOfArtist(Context context, String artist);
    List<Item> getSongsOfAlbum(Context context, String album, String artist);
    Song getSongByPath(Context context, String path);

}
