package com.zell.musicplayer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.ListFragment;

import com.zell.musicplayer.adapters.SongAdapter;
import com.zell.musicplayer.models.Item;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseFragment  extends ListFragment {

    protected abstract void updatePlaylist();

    protected ExternalStorageFragment.Listener listener;
    public interface Listener{
        void setPlaylist(List<Item> playlist);
        void setCurrentSongPosition(int currentSong);
        void playSong();
    }

    protected Context context;
    protected List<Item> playlist = new ArrayList<>();
    protected SongAdapter adapter;
    protected View currentSelectedView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = inflater.getContext();
        updatePlaylist();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (ExternalStorageFragment.Listener) context;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

}
