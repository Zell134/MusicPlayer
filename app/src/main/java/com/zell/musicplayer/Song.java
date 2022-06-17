package com.zell.musicplayer;

import android.net.Uri;

public class Song {
    private String path;
    private String name;
    private String album;
    private String artist;

    public Song() {

    }

    public Song(String path, String name, String album, String artist) {
        this.path = path;
        this.name = name;
        this.album = album;
        this.artist = artist;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
}
