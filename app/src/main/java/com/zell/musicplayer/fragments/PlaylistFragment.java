package com.zell.musicplayer.fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.ListFragment;
import androidx.lifecycle.ViewModelProvider;

import com.zell.musicplayer.R;
import com.zell.musicplayer.Services.MediaLibraryService;
import com.zell.musicplayer.adapters.SongAdapter;
import com.zell.musicplayer.models.PlaylistViewModel;
import com.zell.musicplayer.models.Song;

import java.io.File;
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
    private boolean isFileList = true;
    private List<Song> playlist = new ArrayList<>();
    private SongAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = inflater.getContext();
        playlistViewModel = new ViewModelProvider(getActivity()).get(PlaylistViewModel.class);
        if(isFileList) {
            playlist = getFilelist(String.valueOf(Environment.getStorageDirectory()));
            Log.d("", String.valueOf(Environment.getStorageDirectory()));
        }else {
            playlist = getSongsList();
        }
        playlistViewModel.setPlaylist(playlist);
        playlistViewModel.setCurrentSongPosition(0);
        adapter = new SongAdapter(context, playlist);
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
        Song song = playlist.get(position);
        if(song.isAudioFile()){
            listener.playSong();
        }else {
            if(song.getTitle().equals(getResources().getString(R.string.previous_directory))){
                String filePath = song.getPath();
                playlist = getFilelist(filePath.substring(0,filePath.lastIndexOf("/")));
            }else {
                    playlist = getFilelist(song.getPath());
            }
            adapter = new SongAdapter(context, playlist);
            adapter.notifyDataSetChanged();
            setListAdapter(adapter);
        }
    }

    public List<Song> getSongsList(){
        return MediaLibraryService.getAllMediaFromLibrary(context);
    }

    public List<Song> getFilelist(String fileName){
        return MediaLibraryService.getFilesList(context, new File(fileName));
    }
}