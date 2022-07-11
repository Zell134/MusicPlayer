package com.zell.musicplayer.fragments;

import android.view.View;
import android.widget.ListView;

import androidx.annotation.NonNull;

import com.zell.musicplayer.R;
import com.zell.musicplayer.Services.MediaLibraryService;
import com.zell.musicplayer.adapters.SongAdapter;
import com.zell.musicplayer.models.Item;

import java.util.List;

public class AllLibraryFragment extends BaseFragment {


    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        listener.setPlaylist(playlist);
        listener.setCurrentSongPosition(position);
        listener.playSong();
        if (currentSelectedView != null) {
            currentSelectedView.setBackgroundResource(R.color.white);
        }
        currentSelectedView = v;
        v.setBackgroundResource(R.color.selected_item);
        l.setItemChecked(position, true);

    }

    public List<Item> getSongsList(){
        return MediaLibraryService.getAllMedia(context);
    }

    protected void updatePlaylist(){
        playlist = getSongsList();
        adapter = new SongAdapter(context, playlist);
        adapter.notifyDataSetChanged();
        setListAdapter(adapter);
    }
}