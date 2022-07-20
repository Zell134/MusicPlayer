package com.zell.musicplayer.fragments;

import android.view.View;
import android.widget.ListView;

import androidx.annotation.NonNull;

import com.zell.musicplayer.Services.MediaLibraryService;
import com.zell.musicplayer.models.Item;

import java.util.List;

public class AllLibraryFragment extends BaseFragment {

    @Override
    public void setSelection(int position) {
        super.setSelection(position);
    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        listener.setPlaylist(playlist);
        listener.setCurrentSongPosition(position);
        listener.playSong();
        currentSongHighlight(position);
    }

    public List<Item> getSongsList() {
        return MediaLibraryService.getAllMedia(context);
    }

    protected void updatePlaylist(){
        playlist = getSongsList();
        updateAdapter();
    }

    public void onBackPressed(){

    }
}