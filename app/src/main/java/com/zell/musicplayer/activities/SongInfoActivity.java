package com.zell.musicplayer.activities;

import android.content.ComponentName;
import android.graphics.drawable.BitmapDrawable;
import android.media.session.MediaSession;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationManagerCompat;

import com.zell.musicplayer.R;
import com.zell.musicplayer.Services.MusicPlayerService;
import com.zell.musicplayer.Services.NotificationService;
import com.zell.musicplayer.Services.PlaylistService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class SongInfoActivity extends AppCompatActivity {

    public static String CURRENT_STATE = "currentState";

    private MediaBrowserCompat mediaBrowser;
    private int currentState;
    private MediaControllerCompat mediaController;
    private Handler handler = new Handler();
    private SeekBar seekbar;
    private TextView timer;
    private TextView songInfo;
    private ImageView albumArt;
    private DateFormat formatter = new SimpleDateFormat("mm:ss");


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_info);
        mediaBrowser = new MediaBrowserCompat(getApplicationContext(),
                new ComponentName(this, MusicPlayerService.class),
                connectionCalback,
                null);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        currentState = getIntent().getIntExtra(CURRENT_STATE, -1);
        buildTransportControls();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!mediaBrowser.isConnected()) {
            mediaBrowser.connect();
        }
        if(mediaBrowser.isConnected()) {
            mediaBrowser.disconnect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaController != null) {
            mediaController.unregisterCallback(controllerCallback);
        }
    }

    private final MediaBrowserCompat.ConnectionCallback connectionCalback = new MediaBrowserCompat.ConnectionCallback() {
        @Override
        public void onConnected() {
            try {
                mediaController = new MediaControllerCompat(SongInfoActivity.this, mediaBrowser.getSessionToken());
                MediaControllerCompat.setMediaController(SongInfoActivity.this, mediaController);
                mediaController.registerCallback(controllerCallback);
                setCurrentState();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    private void setCurrentState(){
        albumArt = findViewById(R.id.album_art);
        songInfo = findViewById(R.id.playing_song_info);

        setSongInfo();

        handler.post(new Runnable() {
            @Override
            public void run() {
                updateSeekBar();
                handler.postDelayed(this, 1000);
            }
        });
    }

    private void updateSeekBar(){
        long duration = mediaController.getMetadata().getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        long position = mediaController.getPlaybackState().getPosition();
        if(position > 0) {
            timer.setText(formatter.format(position) + " / " + formatter.format(duration));
        }else{
            timer.setText("0/0");
        }
        seekbar.setProgress((int) position);
    }

    private void setSongInfo(){
        albumArt.setImageDrawable(new BitmapDrawable(mediaController.getMetadata().getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART)));
        long duration = mediaController.getMetadata().getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        seekbar.setMax((int) duration);

        StringBuilder str = new StringBuilder();
        str.append(mediaController.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_ARTIST) + " - ");
        str.append(mediaController.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_TITLE) + " (");
        str.append(formatter.format(duration) + ")");
        songInfo.setText(str);
    }

    private void buildTransportControls(){
        ImageButton playButton = findViewById(R.id.play);
        ImageButton stopButton = findViewById(R.id.stop);
        ImageButton previousButton = findViewById(R.id.previous);
        ImageButton nextButton = findViewById(R.id.next);
        seekbar = findViewById(R.id.seekbar);
        timer = findViewById(R.id.timer);
        seekbar.setOnSeekBarChangeListener(onSeekBarChangeListener);

        if(currentState == PlaybackStateCompat.STATE_PLAYING){
            playButton.setImageResource(R.drawable.pause_icon_black);
        }else{
            playButton.setImageResource(R.drawable.play_icon_black);
        }

        playButton.setOnClickListener(view -> {
            switch (currentState) {

                case PlaybackStateCompat.STATE_PLAYING:
                    mediaController.getTransportControls().pause();
                    break;

                case PlaybackStateCompat.STATE_PAUSED:
                    mediaController.getTransportControls().play();
            }
        });

        stopButton.setOnClickListener(view -> {
            if (currentState == PlaybackStateCompat.STATE_PLAYING || currentState == PlaybackStateCompat.STATE_PAUSED) {
                mediaController.getTransportControls().stop();
            }
        });

        previousButton.setOnClickListener(view -> {
            mediaController.getTransportControls().skipToPrevious();
        });

        nextButton.setOnClickListener(view -> {
            mediaController.getTransportControls().skipToNext();
        });
    }

    private MediaControllerCompat.Callback controllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            currentState = state.getState();
            ImageButton playPauseButton = findViewById(R.id.play);

            switch (currentState) {
                case PlaybackStateCompat.STATE_NONE:
                case PlaybackStateCompat.STATE_STOPPED:
                    playPauseButton.setImageResource(R.drawable.play_icon_black);
                    handler.removeCallbacks(null);
                    break;

                case PlaybackStateCompat.STATE_PLAYING:
                    playPauseButton.setImageResource(R.drawable.pause_icon_black);
                    handler.removeCallbacks(null);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            updateSeekBar();
                            handler.postDelayed(this, 1000);
                        }
                    });
                    break;

                case PlaybackStateCompat.STATE_PAUSED:
                    playPauseButton.setImageResource(R.drawable.play_icon_black);
                    handler.removeCallbacks(null);
                    break;
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            seekbar.setProgress(0);
            timer.setText("0/0");
            setSongInfo();
        }

        @Override
        public void onSessionDestroyed() {

        }
    };

    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener(){
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            long songDuration = mediaController.getMetadata().getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
            DateFormat formatter = new SimpleDateFormat("mm:ss");
            timer.setText(formatter.format(i) + " / " + formatter.format(songDuration));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if(currentState == PlaybackStateCompat.STATE_PLAYING || currentState == PlaybackStateCompat.STATE_PAUSED ) {
                mediaController.getTransportControls().seekTo(seekbar.getProgress());
            }
        }
    };
}