package com.zell.musicplayer.models;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class StateViewModel extends ViewModel {
    MutableLiveData<String> libraryType = new MutableLiveData<>();

    public MutableLiveData<String> getLibraryType() {
        return libraryType;
    }

    public void setLibraryType(String libraryType) {
        this.libraryType.setValue(libraryType);
    }
}
