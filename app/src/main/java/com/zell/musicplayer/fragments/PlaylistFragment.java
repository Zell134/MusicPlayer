package com.zell.musicplayer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.ListFragment;
import androidx.lifecycle.ViewModelProvider;

import com.zell.musicplayer.MainActivity;
import com.zell.musicplayer.R;
import com.zell.musicplayer.Services.MediaLibraryService;
import com.zell.musicplayer.adapters.SongAdapter;
import com.zell.musicplayer.models.Song;
import com.zell.musicplayer.models.StateViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlaylistFragment extends ListFragment {

    private Listener listener;
    public interface Listener{
        void setPlaylist(List<Song> playlist);
        void setCurrentSongPosition(int currentSong);
        void playSong();
    }

    private Context context;
    private boolean isFileList = true;
    private List<Song> playlist = new ArrayList<>();
    private SongAdapter adapter;
    private View currentSelectedView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = inflater.getContext();
        new ViewModelProvider(requireActivity())
                .get(StateViewModel.class)
                .getLibraryType()
                .observe(getViewLifecycleOwner(), s ->  {
                    switch(s) {
                        case MainActivity.LIBRARY_TYPE_MEDIA_LIBRARY:
                            isFileList = false;
                            break;
                        case MainActivity.LIBRARY_TYPE_EXTERNAL_STORAGE:
                            isFileList = true;
                            break;
                    }
                    updatePlaylist();
                });

        updatePlaylist();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (Listener) context;
    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {

        listener.setCurrentSongPosition(position);
        Song song = playlist.get(position);
        if(song.isAudioFile()){
            listener.playSong();
            if(currentSelectedView != null) {
                currentSelectedView.setBackgroundResource(R.color.white);
            }
            currentSelectedView = v;
            v.setBackgroundResource(R.color.selected_item);
            l.setItemChecked(position, true);
        }else {
            if(song.getTitle().equals(getResources().getString(R.string.previous_directory))){
                String filePath = song.getPath();
                playlist = getFilelist(filePath.substring(0,filePath.lastIndexOf("/")));
            }else {
                playlist = getFilelist(song.getPath());
            }
            listener.setPlaylist(playlist);
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

    private void updatePlaylist(){
        if(isFileList) {
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                String path = Environment.getStorageDirectory().getAbsolutePath();
                playlist = getFilelist(path);
            }
        }else {
            playlist = getSongsList();
        }
        listener.setPlaylist(playlist);
        listener.setCurrentSongPosition(0);
        adapter = new SongAdapter(context, playlist);
        adapter.notifyDataSetChanged();
        setListAdapter(adapter);
    }
}