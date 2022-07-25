package com.zell.musicplayer.db;

public enum LibraryType {
    LIBRARY_TYPE_MEDIA_LIBRARY("MediaLibrary"),
    LIBRARY_TYPE_EXTERNAL_STORAGE("ExternalStorage"),
     LIBRARY_TYPE_ARTISTS("Artists");

    private String value;

    LibraryType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
