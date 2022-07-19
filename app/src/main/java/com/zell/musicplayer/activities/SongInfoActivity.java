package com.zell.musicplayer.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.media.MediaBrowserCompat;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.zell.musicplayer.R;
import com.zell.musicplayer.Services.PlaylistService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class SongInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_info);

    }


}