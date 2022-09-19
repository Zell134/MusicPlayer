package com.zell.musicplayer.helpers;

import android.content.Context;
import android.os.Environment;

import com.zell.musicplayer.R;
import com.zell.musicplayer.db.LibraryType;
import com.zell.musicplayer.models.Folder;
import com.zell.musicplayer.models.Item;
import com.zell.musicplayer.models.Song;
import com.zell.musicplayer.services.MediaLibraryServiceInterface;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FakeMediaLibraryService implements MediaLibraryServiceInterface {

    private int index;
    private List<Item> songs = new ArrayList<>() ;
    private List<Item> folders = new ArrayList<>() ;
    private List<Item> all = new ArrayList<>() ;


    public FakeMediaLibraryService(Context context) {
        do {
            index = new Random().nextInt(10);
        } while (index <= 2);

        songs.addAll(getSongsList());
        addRootElement(context, folders);
        folders.addAll(getFoldersList());
        all.addAll(folders);
        all.addAll(songs);
    }

    void addRootElement(Context context, List<Item> list) {
        list.add(new Folder("root/", context.getResources().getString(R.string.previous_directory)));
    }

    public Song getRandomSong() {
        int position;
        do {
            position = new Random().nextInt(songs.size());
        } while (position <= 0);
        return (Song) songs.get(position);
    }

    public int findSongPosition(Context context, String path, LibraryType lType) {
        int position = -1;
        List<Item> list = getListByLibraryType(context, path, lType);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getPath().equals(path)) {
                position = i;
                break;
            }
        }
        return position;
    }

    @Override
    public List<Item> getAllMedia(Context context) {
        return songs;
    }

    @Override
    public List<Item> getArtistList(Context context) {
        return folders;
    }

    @Override
    public List<Item> getAlbumsOfArtist(Context context, String artist) {
        return folders;
    }

    @Override
    public List<Item> getSongsOfAlbum(Context context, String album, String artist) {
        return songs;
    }

    @Override
    public Song getSongByPath(Context context, String path) {
        Song song = null;
        for(int i = 0; i < songs.size(); i++) {
            if(path.equals(songs.get(i).getPath())){
                song = (Song) songs.get(i);
            }
        }
        return song;
    }

    @Override
    public List<Item> getFilesList(Context context, String folderName) {
        return all;
    }

    private List getSongsList(){
        List <Item> list = new ArrayList<>();
        for (int i = 0; i < index; i++){
            list.add(new Song("root/" + index + i,
                    "title_" + index + i,
                    "album" + index + i,
                    "artist" + index + i,
                    index)
            );
        };
        return list;
    }

    private List getFoldersList(){
        List <Item> list = new ArrayList<>();
        for (int i = 0; i < index; i++){
            list.add(new Folder("artist" + (index + i), "artist" + (index + i)));
        }
        return list;
    }

    private List<Item> getListByLibraryType(Context context, String path, LibraryType lType){
        List<Item> list = new ArrayList<>();
        switch (lType) {
            case LIBRARY_TYPE_EXTERNAL_STORAGE:
                if(path!=null) {
                    list = getFilesList(context, path);
                }
                break;
            case LIBRARY_TYPE_MEDIA_LIBRARY:
                list = getAllMedia(context);
                break;
            case LIBRARY_TYPE_ARTISTS:
                if(path!=null) {
                    list = getSongsOfAlbum(context, path, path);
                }
                break;
        }
        return list;
    }
}
