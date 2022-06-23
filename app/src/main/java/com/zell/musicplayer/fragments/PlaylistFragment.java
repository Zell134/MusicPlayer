package com.zell.musicplayer.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;

import com.zell.musicplayer.Services.MediaLibraryService;
import com.zell.musicplayer.Services.MusicPlayerService;
import com.zell.musicplayer.adapters.SongAdapter;
import com.zell.musicplayer.models.Song;

import java.util.List;

public class PlaylistFragment extends ListFragment {

    private Listener listener;

    public interface Listener{
        void playSong(Song song);
    }
    private List<Song> playlist;
    private MediaLibraryService mediaLibraryService;
    private Context context;
    private long songIndex;

    private MusicPlayerService musicService;
    private Intent playIntent;
    private boolean musicBound=false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = inflater.getContext();
        getSongsList();
        SongAdapter adapter = new SongAdapter(context, playlist);
        setListAdapter(adapter);
        if(playIntent==null){
            playIntent = new Intent(getActivity(), MusicPlayerService.class);
            getActivity().bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(playIntent);
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
    }

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicPlayerService.MusicBinder binder = (MusicPlayerService.MusicBinder) iBinder;
            musicService = binder.getService();
            musicService.setPlaylist(playlist);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            musicBound = false;
        }
    };


    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        songIndex = position;
        musicService.playSong(playlist.get(position));
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    public void getSongsList(){
        mediaLibraryService = new MediaLibraryService();
        playlist = mediaLibraryService.getAllMediaFromLibrary(context);
    }


    @Override
    public void onDestroy() {
        getActivity().stopService(playIntent);
        musicService=null;
        super.onDestroy();
    }
}