package com.zell.musicplayer.models;

import java.util.List;

public class Playlist {

    private List<Item> playlist;
    private int currentSongPosition;
    private int previousSongPosition;

    public List<Item> getPlaylist() {
        return playlist;
    }

    public void setPlaylist(List<Item> playlist) {
        this.playlist = playlist;
    }

    public Song getCurrentSong() {
        try {
            return (Song) playlist.get(currentSongPosition);
        } catch (Exception e) {
            return null;
        }
    }

    public void setCurrentSongPosition(int currentSong) {
        this.currentSongPosition = currentSong;
    }

    public int getCurrentSongPosition() {
        return currentSongPosition;
    }

    public int getPreviousSongPosition() {
        return previousSongPosition;
    }

    public void setPreviousSongPosition(int previousSong) {
        this.previousSongPosition = previousSong;
    }

    public Song getPreviousSong(){
        int current = currentSongPosition;
        if(playlist!= null) {
            int i = 0;
            while (true) {
                current--;
                if (current < 0) {
                    current = playlist.size() - 1;
                }
                Item item = playlist.get(current);
                if (item.isAudioFile()) {
                    previousSongPosition = currentSongPosition;
                    currentSongPosition = current;
                    return (Song) item;
                }
                i++;
                if (i >= playlist.size()) {
                    break;
                }
            }
        }
        return null;
    }

    public Song getNextSong(){
        int current = currentSongPosition;
        if(playlist != null) {
            int i = 0;
            while (true) {
                current++;
                if (current > playlist.size() - 1) {
                    current = 0;
                }
                Item item = playlist.get(current);
                if (item.isAudioFile()) {
                    previousSongPosition = currentSongPosition;
                    currentSongPosition = current;
                    return (Song) item;
                }
                i++;
                if (i >= playlist.size()) {
                    break;
                }
            }
        }
        return null;
    }

    public int findSongIndexByPath(String songPath){
        for(int i = 0; i<playlist.size(); i++){
            if(playlist.get(i).getPath().equals(songPath)){
                return i;
            }
        }
        return -1;
    }
}
