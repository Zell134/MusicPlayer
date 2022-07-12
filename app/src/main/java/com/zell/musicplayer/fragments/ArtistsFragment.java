package com.zell.musicplayer.fragments;

import android.view.View;
import android.widget.ListView;

import androidx.annotation.NonNull;

import com.zell.musicplayer.R;
import com.zell.musicplayer.Services.MediaLibraryService;
import com.zell.musicplayer.adapters.SongAdapter;
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
            if(currentSelectedView != null) {
                currentSelectedView.setBackgroundResource(R.color.white);
            }
            currentSelectedView = v;
            v.setBackgroundResource(R.color.selected_item);
            l.setItemChecked(position, true);
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
}