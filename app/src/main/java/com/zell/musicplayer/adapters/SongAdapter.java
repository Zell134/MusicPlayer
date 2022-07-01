package com.zell.musicplayer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zell.musicplayer.R;
import com.zell.musicplayer.models.Song;

import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends BaseAdapter {
    private List<Song> songs = new ArrayList<>();
    private LayoutInflater songInf;

    public SongAdapter(Context context, List<Song> songs) {
        this.songs = songs;
        songInf=LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LinearLayout songLayout = (LinearLayout) songInf.inflate(R.layout.song, parent, false);
        ImageView icon = songLayout.findViewById(R.id.content_icon);
        TextView songView = songLayout.findViewById(R.id.song_title);
        TextView artistView = songLayout.findViewById(R.id.song_artist);
        Song currSong = songs.get(position);
        songView.setText(currSong.getTitle());
        artistView.setText(currSong.getArtist());
        if(currSong.isAudioFile()){
            icon.setImageResource(R.drawable.music_icon);
        }else{
            icon.setImageResource(R.drawable.folder_icon);
        }
        return songLayout;
    }
}
