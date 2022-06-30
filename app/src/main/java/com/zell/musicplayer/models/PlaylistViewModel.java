package com.zell.musicplayer.models;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class PlaylistViewModel extends ViewModel {

    private MutableLiveData<List<Song>> playlist = new MutableLiveData<>();
    private MutableLiveData<Integer> currentSongPosition = new MutableLiveData<>();


    public MutableLiveData<List<Song>> getPlaylist() {
        return playlist;
    }
    public void setPlaylist(List<Song> playlist) {
        this.playlist.setValue(playlist);
    }
    public void setCurrentSongPosition(Integer position){
        this.currentSongPosition.setValue(position);
    }

    public MutableLiveData<Integer> getCurrentSongPosition(){return currentSongPosition;}

;


}
