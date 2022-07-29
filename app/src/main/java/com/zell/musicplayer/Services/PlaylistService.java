package com.zell.musicplayer.Services;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zell.musicplayer.R;
import com.zell.musicplayer.adapters.SongAdapter;
import com.zell.musicplayer.db.LibraryType;
import com.zell.musicplayer.models.Item;
import com.zell.musicplayer.models.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlaylistService {

    private final RecyclerView listView;
    private final Context context;
    private SongAdapter adapter;
    private LibraryType libraryType;


    public PlaylistService(Context context, LibraryType libraryType, String songPath) {
        this.listView = ((AppCompatActivity)context).findViewById(R.id.playlist);
        this.context = context;
        this.libraryType = libraryType;
        setup(songPath);
    }

    private void setup( String songPath){
        List<Item> playlist = getPlaylistBySongPathAndLibraryType(songPath);
        adapter = new SongAdapter(context, playlist, libraryType);
        listView.setLayoutManager(new LinearLayoutManager(context));
        listView.setAdapter(adapter);
        int position = getItemIndexFromPlaylist(playlist, songPath);
        if(position >= 0) {
            adapter.serCurrentSongPosition(position);
            adapter.scrollToPosition(position - 2);
            adapter.play();
        }else{
            adapter.serCurrentSongPosition(0);
        }
    }

    public void setLibraryType(LibraryType libraryType) {
        this.libraryType = libraryType;
       adapter.setPlaylist(getPlaylistBySongPathAndLibraryType(null));
    }

    @SuppressLint("NewApi")
    public List<Item> getPlaylistBySongPathAndLibraryType(String songPath){

        List<Item> playlist = new ArrayList<>();

        switch (libraryType) {
            case LIBRARY_TYPE_EXTERNAL_STORAGE:
                if(songPath!=null) {
                    playlist = MediaLibraryService.getFilesList(context, new File(songPath.substring(0, songPath.lastIndexOf("/"))));
                }else{
                    playlist = MediaLibraryService.getFilesList(context, new File(Environment.getStorageDirectory().getAbsolutePath()));
                }
                break;
            case LIBRARY_TYPE_MEDIA_LIBRARY:
                playlist = MediaLibraryService.getAllMedia(context);
                break;
            case LIBRARY_TYPE_ARTISTS:
                if(songPath!=null) {
                    Song song = MediaLibraryService.getSongByPath(context, songPath);
                    playlist = MediaLibraryService.getSongsOfAlbum(context, song.getAlbum(), song.getArtist());
                }else{
                    playlist = MediaLibraryService.getArtistList(context);
                }
                break;
        }
        return playlist;
    }

    private int getItemIndexFromPlaylist(List <Item> playlist, String songPath){
        for (int i = 0; i <playlist.size(); i++){
            if(playlist.get(i).getPath().equals(songPath)){
                return i;
            }
        }
        return -1;
    }

    public void onBackPressed() {
        adapter.onBackPressed();
    }


    public void playNextSong() {
        adapter.playNextSong();
    }

    public void playPreviousSong() {
        adapter.playPreviousSong();
    }

    public void play() {
        adapter.play();
    }

}
