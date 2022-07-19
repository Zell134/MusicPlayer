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

    private List<Item> playlist;
    private int currentSongPosition = 0;
    private int previousSongPosition = 0;

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

    public List<Item> getPlaylist() {
        return playlist;
    }

    public void setPlaylist(List<Item> playlist) {
        this.playlist = playlist;
    }

    public int getCurrentSongPosition() {
        return currentSongPosition;
    }

    public void setCurrentSongPosition(int currentSongPosition) {
        previousSongPosition = this.currentSongPosition;
        this.currentSongPosition = currentSongPosition;
    }

    public Song getCurrentSong(){
        if(playlist!=null) {
            return (Song) playlist.get(currentSongPosition);
        }
        return null;
    }

    public Song getPreviousSong(){
        if(playlist != null) {
            previousSongPosition = currentSongPosition;
            while (true) {
                int i = 0;
                currentSongPosition--;
                if (currentSongPosition < 0) {
                    currentSongPosition = playlist.size() - 1;
                }
                Item item = playlist.get(currentSongPosition);
                if (item.isAudioFile()) {
                    return (Song) item;
                }
                i++;
                if (i >= playlist.size()) {
                    break;
                }
            }
            currentSongPosition = previousSongPosition;
        }
        return null;
    }

    public Song getNextSong(){
        if(playlist != null) {
            previousSongPosition = currentSongPosition;
            while (true) {
                int i = 0;
                currentSongPosition++;
                if (currentSongPosition > playlist.size() - 1) {
                    currentSongPosition = 0;
                }
                Item item = playlist.get(currentSongPosition);
                if (item.isAudioFile()) {
                    return (Song) item;
                }
                i++;
                if (i >= playlist.size()) {
                    break;
                }
            }
            currentSongPosition = previousSongPosition;
        }
        return null;
    }

    public int getPreviousSongPosition() {
        return previousSongPosition;
    }
}
