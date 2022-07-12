package com.zell.musicplayer.Services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.zell.musicplayer.models.Item;
import com.zell.musicplayer.models.Song;

import java.util.List;

public class PlaylistService extends Service {

    private List<Item> plyalist;
    private int currentSongPosition;

    private final IBinder binder = new PlaylistServiceBinder();

    public class PlaylistServiceBinder extends Binder {
        public PlaylistService getService() {
            return PlaylistService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public List<Item> getPlyalist() {
        return plyalist;
    }

    public void setPlyalist(List<Item> plyalist) {
        this.plyalist = plyalist;
    }

    public int getCurrentSongPosition() {
        return currentSongPosition;
    }

    public void setCurrentSongPosition(int currentSongPosition) {
        this.currentSongPosition = currentSongPosition;
    }

    public Song getCurrentSong(){
        return (Song) plyalist.get(currentSongPosition);
    }
}
