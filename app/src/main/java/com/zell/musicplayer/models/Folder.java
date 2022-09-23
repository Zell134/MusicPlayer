package com.zell.musicplayer.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Folder implements Item{

    protected String path;
    protected String title;
    protected boolean isAudioFile;

    public Folder(String path, String title) {
        this.path = path;
        this.title = title;
        this.isAudioFile = false;
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

    @Override
    public boolean equals(@Nullable Object obj) {
        Folder folder;
        try {
            folder = (Folder) obj;
        }catch (Exception e){
            return false;
        }
        return title.equals(folder.getTitle()) && path.equals(folder.getPath());
    }
}
