package com.zell.musicplayer.fragments;

import android.os.Environment;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.NonNull;

import com.zell.musicplayer.R;
import com.zell.musicplayer.Services.MediaLibraryService;
import com.zell.musicplayer.adapters.SongAdapter;
import com.zell.musicplayer.models.Item;

import java.io.File;
import java.util.List;

public class ExternalStorageFragment extends BaseFragment {

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {

        Item item = playlist.get(position);
        if(item.isAudioFile()){
            listener.setPlaylist(playlist);
            listener.setCurrentSongPosition(position);
            listener.playSong();
        }else {
            if(item.getTitle().equals(getResources().getString(R.string.previous_directory))){
                String filePath = item.getPath();
                playlist = getFilelist(filePath.substring(0,filePath.lastIndexOf("/")));
            }else {
                playlist = getFilelist(item.getPath());
            }
            updateAdapter();
        }
    }

    public List<Item> getFilelist(String fileName){
        return MediaLibraryService.getFilesList(context, new File(fileName));
    }

    protected void updatePlaylist(){
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String path = Environment.getStorageDirectory().getAbsolutePath();
            playlist = getFilelist(path);
        }
        updateAdapter();
    }
}