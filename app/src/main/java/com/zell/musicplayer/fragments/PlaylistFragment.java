package com.zell.musicplayer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.ListFragment;
import androidx.lifecycle.ViewModelProvider;

import com.zell.musicplayer.Services.MediaLibraryService;
import com.zell.musicplayer.adapters.SongAdapter;
import com.zell.musicplayer.models.PlaylistViewModel;
import com.zell.musicplayer.models.Song;

import java.util.ArrayList;
import java.util.List;

public class PlaylistFragment extends ListFragment {

    private Listener listener;

    public interface Listener{
        void playSong();
    }

    private PlaylistViewModel playlistViewModel;
    private MediaLibraryService mediaLibraryService;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = inflater.getContext();
        playlistViewModel = new ViewModelProvider(getActivity()).get(PlaylistViewModel.class);
        List<Song> playlist = new ArrayList<>();
        playlist = getSongsList();
        playlistViewModel.setPlaylist(playlist);
        playlistViewModel.setCurrentSongPosition(0);
        SongAdapter adapter = new SongAdapter(context, playlist);
        setListAdapter(adapter);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (Listener) context;
    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        playlistViewModel.setCurrentSongPosition(position);
        listener.playSong();
    }

    public List<Song> getSongsList(){
        mediaLibraryService = new MediaLibraryService();
        return mediaLibraryService.getAllMediaFromLibrary(context);
    }
}