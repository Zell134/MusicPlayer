package com.zell.musicplayer.models;

import androidx.annotation.NonNull;

public class Song extends Folder{
    private String album;
    private String artist;
    private long duration;

    public Song(String path, String title, String album, String artist, long duration, boolean isAudioFile) {
        super(path, title,isAudioFile);
        this.album = album;
        this.duration = duration;
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getArtist() {
        return artist;
    }

    public long getDuration() {
        return duration;
    }

    @NonNull
    @Override
    public String toString() {
        return artist + " - " + title + " (" + album + ")";
    }
}
