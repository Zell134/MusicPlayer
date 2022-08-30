package com.zell.musicplayer.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Song extends Folder{
    private String album;
    private String artist;
    private long duration;

    public Song(String path, String title, String album, String artist, long duration, boolean isAudioFile) {
        super(path, title);
        this.album = album;
        this.duration = duration;
        this.artist = artist;
        this.isAudioFile = isAudioFile;
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

    @Override
    public boolean equals(@Nullable Object obj) {
        Song song;
        try{
            song = (Song) obj;
        }catch (Exception e){
            return false;
        }
        return super.equals(obj) && album.equals(song.getAlbum()) && artist.equals(song.getArtist()) && duration == song.getDuration();
    }
}
