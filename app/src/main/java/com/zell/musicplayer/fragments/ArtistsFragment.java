package com.zell.musicplayer.fragments;

import android.view.View;
import android.widget.ListView;

import androidx.annotation.NonNull;

import com.zell.musicplayer.R;
import com.zell.musicplayer.Services.MediaLibraryService;
import com.zell.musicplayer.models.Item;

import java.util.List;

public class ArtistsFragment extends BaseFragment {


    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {

        Item item = playlist.get(position);
        if(item.isAudioFile()){
            listener.setPlaylist(playlist);
            listener.setCurrentSongPosition(position);
            listener.playSong();
            currentSongHighlight(position);
        }else {
            if(item.getTitle().equals(getResources().getString(R.string.previous_directory))){
                if(item.getPath().equals("root")) {
                    playlist = getArtistList();
                }else{
                    playlist = MediaLibraryService.getAlbumsOfArtist(context, item.getPath());
                }
            }else {
                if(!item.getPath().equals("root")) {
                    playlist = MediaLibraryService.getSongsOfAlbum(context,item.getTitle(),item.getPath());
                }else{
                    playlist = MediaLibraryService.getAlbumsOfArtist(context, item.getTitle());
                }
            }
            updateAdapter();
        }
    }

    public List<Item> getArtistList(){
        return MediaLibraryService.getArtistList(context);
    }

    protected void updatePlaylist(){
        playlist = getArtistList();
        updateAdapter();
    }

    public void onBackPressed(){
        Item item = playlist.get(0);
        if(item.getTitle().equals(getResources().getString(R.string.previous_directory))) {
            onListItemClick(null, null, 0, 0);
        }
    }
}