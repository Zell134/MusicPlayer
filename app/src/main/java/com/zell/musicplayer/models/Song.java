package com.zell.musicplayer.models;

import androidx.annotation.NonNull;

public class Song {
    private String path;
    private String title;
    private String album;
    private String artist;
    private boolean isAudioFile;

    public Song() {

    }

    /**
     * @param path
     * @param title
     * @param album
     * @param artist
     */
    public Song(String path, String title, String album, String artist, boolean isAudioFile) {
        this.path = path;
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.isAudioFile = isAudioFile;
    }

    public String getPath() {
        return path;
    }

    public String getTitle() {
        return title;
    }
    public String getAlbum() {
        return album;
    }

    public String getArtist() {
        return artist;
    }

    public boolean isAudioFile() {
        return isAudioFile;
    }

    @NonNull
    @Override
    public String toString() {
        return artist + " - " + title + " (" + album + ")";
    }
}
