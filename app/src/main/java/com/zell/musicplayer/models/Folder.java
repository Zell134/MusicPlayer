package com.zell.musicplayer.models;

import androidx.annotation.NonNull;

public class Folder implements Item{

    protected String path;
    protected String title;
    protected boolean isAudioFile;

    public Folder(String path, String title, boolean isAudioFile) {
        this.path = path;
        this.title = title;
        this.isAudioFile = isAudioFile;
    }

    public Folder() {
    }

    @NonNull
    @Override
    public String toString() {
        return title;
    }

    @Override
    public String getPath() {
        return path;
    }


    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public boolean isAudioFile() {
        return isAudioFile;
    }
}
