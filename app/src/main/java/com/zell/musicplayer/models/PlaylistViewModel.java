package com.zell.musicplayer.models;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class PlaylistViewModel extends ViewModel {

    private MutableLiveData<List<Song>> playlist = new MutableLiveData<>();

    public MutableLiveData<List<Song>> getPlaylist() {
        return playlist;
    }

    public void setPlaylist(List<Song> playlist) {
        this.playlist.setValue(playlist);
    }

;


}
