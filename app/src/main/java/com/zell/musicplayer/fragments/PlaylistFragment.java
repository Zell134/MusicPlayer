package com.zell.musicplayer.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.ListFragment;

import com.zell.musicplayer.Services.MediaLibraryService;
import com.zell.musicplayer.adapters.SongAdapter;
import com.zell.musicplayer.models.Song;

import java.util.List;
import java.util.stream.Collectors;

public class PlaylistFragment extends ListFragment {

    Listener listener;

    public static interface Listener{
        void playSong(Uri id);
    }
    private List<Song> playlist;
    private MediaLibraryService mediaLibraryService;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = inflater.getContext();
//        setListAdapter(getSongsList(inflater.getContext()));
        getSongsList();
        SongAdapter adapter = new SongAdapter(context, playlist);
        setListAdapter(adapter);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        ((TextView)v).setTextIsSelectable(true);
        listener.playSong(Uri.parse(playlist.get(position).getPath()));
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
//        listener = (Listener) context;
    }

    public void getSongsList(){
        mediaLibraryService = new MediaLibraryService();
        playlist = mediaLibraryService.getAllMediaFromLibrary(context);
//
//        List<String> listOfSongs = null;
//
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
//            listOfSongs = playlist
//                    .stream()
//                    .map(element->element.getArtist() + " - " + element.getName())
//                    .collect(Collectors.toList()
//                    );
//        }
//
//        return new ArrayAdapter<>(context,
//                android.R.layout.simple_list_item_1,
//                listOfSongs);
    }
}