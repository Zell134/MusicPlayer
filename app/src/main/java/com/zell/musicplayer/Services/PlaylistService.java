package com.zell.musicplayer.Services;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zell.musicplayer.R;
import com.zell.musicplayer.activities.MainActivity;
import com.zell.musicplayer.adapters.SongAdapter;
import com.zell.musicplayer.db.LibraryType;
import com.zell.musicplayer.models.Item;
import com.zell.musicplayer.models.Playlist;
import com.zell.musicplayer.models.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlaylistService implements SongAdapter.Listener{

    private RecyclerView listView;
    private final Context context;
    private SongAdapter adapter;
    private LibraryType libraryType;
    private boolean isFirstStart = true;
    private Playlist playlist = new Playlist();

    public PlaylistService(Context context, LibraryType libraryType, String songPath) {
        this.context = context;
        this.libraryType = libraryType;
        setup(songPath);
    }

    private void setup(String songPath){
        setPlaylist(getPlaylistBySongPathAndLibraryType(songPath));
        adapter = new SongAdapter(this, playlist.getPlaylist());
        setAdapter();

        int position = getItemIndexFromPlaylist(playlist.getPlaylist(), songPath);

        if(position >= 0) {
            playlist.setCurrentSongPosition(position);
            scrollToPosition(position - 2);
            if(isFirstStart) {
                play();
                isFirstStart = false;
            }
        }else{
            playlist.setCurrentSongPosition(0);
        }
    }

    public void setAdapter(){
        listView = ((AppCompatActivity)context).findViewById(R.id.playlist);
        listView.setLayoutManager(new LinearLayoutManager(context));
        listView.setAdapter(adapter);
    }

    private void setPlaylist(List<Item> playlist){
        this.playlist.setPlaylist(playlist);
        this.playlist.setPreviousSongPosition(0);
        this.playlist.setCurrentSongPosition(0);
    }

    public void setLibraryType(LibraryType libraryType) {
        this.libraryType = libraryType;
        setPlaylist(getPlaylistBySongPathAndLibraryType(null));
        adapter.setPlaylist(playlist.getPlaylist());
    }

    @SuppressLint("NewApi")
    public List<Item> getPlaylistBySongPathAndLibraryType(String songPath){

        List<Item> playlist = new ArrayList<>();

        switch (libraryType) {
            case LIBRARY_TYPE_EXTERNAL_STORAGE:
                if(songPath!=null && new File(songPath).exists()) {
                    playlist = MediaLibraryService.getFilesList(context, new File(songPath.substring(0, songPath.lastIndexOf("/"))));
                }else{
                    playlist = MediaLibraryService.getFilesList(context, new File(Environment.getStorageDirectory().getAbsolutePath()));
                }
                break;
            case LIBRARY_TYPE_MEDIA_LIBRARY:
                playlist = MediaLibraryService.getAllMedia(context);
                break;
            case LIBRARY_TYPE_ARTISTS:
                if(songPath!=null && new File(songPath).exists()) {
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

    public void play() {
        Song song = playlist.getCurrentSong();
        if (song != null) {
            ((MainActivity)context).playSong(song);
        }
    }

    @Override
    public int getCurrentSongPosition(){
        return playlist.getCurrentSongPosition();
    }

    @Override
    public void itemSelected(int position) {
        Item item = playlist.getItemAtPosition(position);
        int oldPosition = playlist.getCurrentSongPosition();
        if (item.isAudioFile()) {
            playlist.setCurrentSongPosition(position);
            adapter.setSelectedPosition(oldPosition, position);
            play();
        } else {
            playlist.setCurrentSongPosition(position);
            onItemSelect();
        }
    }

    public void onItemSelect() {
        Item item = playlist.getCurrentItem();
        List<Item> playlist = new ArrayList<>();
        switch (libraryType) {
            case LIBRARY_TYPE_EXTERNAL_STORAGE: {
                if (item.getTitle().equals(context.getResources().getString(R.string.previous_directory))) {
                    String filePath = item.getPath();
                    playlist = MediaLibraryService.getFilesList(context, new File(filePath.substring(0, filePath.lastIndexOf("/"))));
                } else {
                    playlist = MediaLibraryService.getFilesList(context, new File(item.getPath()));
                }
                break;
            }
            case LIBRARY_TYPE_ARTISTS: {
                if (item.getTitle().equals(context.getResources().getString(R.string.previous_directory))) {
                    if (item.getPath().equals("root")) {
                        playlist = MediaLibraryService.getArtistList(context);
                    } else {
                        playlist = MediaLibraryService.getAlbumsOfArtist(context, item.getPath());
                    }
                } else {
                    if (!item.getPath().equals("root")) {
                        playlist = MediaLibraryService.getSongsOfAlbum(context, item.getTitle(), item.getPath());
                    } else {
                        playlist = MediaLibraryService.getAlbumsOfArtist(context, item.getTitle());
                    }
                }
                break;
            }
        }
        setPlaylist(playlist);
        adapter.setPlaylist(playlist);
    }

    public void onBackPressed() {
        if (libraryType != LibraryType.LIBRARY_TYPE_MEDIA_LIBRARY) {
            if (playlist.getItemAtPosition(0).getTitle().equals(context.getResources().getString(R.string.previous_directory))) {
                playlist.setCurrentSongPosition(0);
                onItemSelect();
            }
        }
    }

    public void playNextSong() {
        int oldPosition = playlist.getCurrentSongPosition();
        int newPosition = playlist.getNextSongPosition();
        setNewPositionAndPlay(oldPosition, newPosition);
    }

    public void playPreviousSong() {
        int oldPosition = playlist.getCurrentSongPosition();
        int newPosition = playlist.getPreviousSongPosition();
        setNewPositionAndPlay(oldPosition, newPosition);
    }

    private void setNewPositionAndPlay(int oldPosition, int newPosition){
        if (newPosition > 0) {
            playlist.setCurrentSongPosition(newPosition);
            adapter.setSelectedPosition(oldPosition, newPosition);
            scrollToPosition(newPosition);
        }
        play();
    }


    public void scrollToPosition(int position) {
        if(listView != null) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) listView.getLayoutManager();
            layoutManager.scrollToPosition(position);
        }
    }

    public void scrollToCurrentPosition(){
        scrollToPosition(playlist.getCurrentSongPosition());
    }
}
