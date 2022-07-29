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
       Item item = playlist.get(currentSongPosition);
        if(item.isAudioFile()){
            return (Song) item;
        }
        return null;
    }

    public Item getCurrentItem() {
            return playlist.get(currentSongPosition);
    }

    public void setCurrentSongPosition(int currentSong) {
        this.currentSongPosition = currentSong;
    }

    public int getCurrentSongPosition() {
        return currentSongPosition;
    }

    public Item getItemAtPosition(int position){
        return playlist.get(position);
    }

    public void setPreviousSongPosition(int previousSong) {
        this.previousSongPosition = previousSong;
    }

    public int getPreviousSongPosition(){
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
                    return current;
                }
                i++;
                if (i >= playlist.size()) {
                    break;
                }
            }
        }
        return -1;
    }

    public int getNextSongPosition(){
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
                    return current;
                }
                i++;
                if (i >= playlist.size()) {
                    break;
                }
            }
        }
        return -1;
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
