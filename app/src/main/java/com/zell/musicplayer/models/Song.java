package com.zell.musicplayer.models;

import androidx.annotation.NonNull;

public class Song extends Folder{
    private String album;
    private String artist;

    public Song(String path, String title, String album, String artist, boolean isAudioFile) {
        super(path, title,isAudioFile);
        this.album = album;
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }
    public String getArtist() {
        return artist;
    }

    @NonNull
    @Override
    public String toString() {
        return artist + " - " + title + " (" + album + ")";
    }
}
