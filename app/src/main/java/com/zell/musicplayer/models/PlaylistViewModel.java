package com.zell.musicplayer.models;

import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class PlaylistViewModel extends ViewModel {
    private MutableLiveData<List<Item>> playlist = new MediatorLiveData<>();
    private MutableLiveData<Integer> currentSong = new MediatorLiveData<>();
    private MutableLiveData<Integer> previousSong = new MediatorLiveData<>();

    public void setPlaylist(List<Item> playlist){
        this.playlist.setValue(playlist);
    };

    public MutableLiveData<List<Item>> getPlaylist(){
        return playlist;
    }

    public MutableLiveData<Integer> getCurrentSong() {
        return currentSong;
    }

    public void setCurrentSong(int currentSong) {
        this.currentSong.setValue(currentSong);
    }

    public MutableLiveData<Integer> getPreviousSong() {
        return previousSong;
    }

    public void setPreviousSong(int previousSong) {
        this.previousSong.setValue(previousSong);
    }
}
