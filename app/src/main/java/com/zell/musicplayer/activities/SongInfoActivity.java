package com.zell.musicplayer.activities;

import static com.zell.musicplayer.activities.MainActivity.ALBUM;
import static com.zell.musicplayer.activities.MainActivity.ARTIST;
import static com.zell.musicplayer.activities.MainActivity.DURATION;
import static com.zell.musicplayer.activities.MainActivity.TITLE;

import android.content.ComponentName;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.zell.musicplayer.R;
import com.zell.musicplayer.Services.MusicPlayerService;
import com.zell.musicplayer.Services.PropertiesService;
import com.zell.musicplayer.models.Item;
import com.zell.musicplayer.models.PlaylistViewModel;
import com.zell.musicplayer.models.Song;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

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
    private SharedCallback sharedCallback;
    private PlaylistViewModel playlistViewModel;
    private List<Item> playlist;
    private int currentSong;
    private int previousSong;

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
        if (ContextCallback.getInstance().contextAssigned()) {
            if (ContextCallback.getInstance().getContext() instanceof SharedCallback)
                sharedCallback = (SharedCallback) ContextCallback.getInstance().getContext();
            playlistViewModel = new ViewModelProvider((ViewModelStoreOwner) sharedCallback).get(PlaylistViewModel.class);
            playlistViewModel.getPlaylist().observe(this, items -> {playlist = items;});
            playlistViewModel.getCurrentSong().observe(this, items -> {currentSong = items;});
            playlistViewModel.getPreviousSong().observe(this, items -> {previousSong = items;});
            ContextCallback.freeContext();
        }
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
        PropertiesService.setCurrentSong(SongInfoActivity.this, mediaController.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI));
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
                    setSongInfo();
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

                case PlaybackStateCompat.STATE_SKIPPING_TO_NEXT:
                    Song song = getNextSong();
                    if(song != null){
                        playSong();
                    }
                    break;
                case PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS:
                    song = getPreviousSong();
                    if(song != null){
                        playSong();
                    }
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

    public void playSong() {
        Song song = getCurrentSong();
        if(song!=null) {
            Bundle bundle = new Bundle();
            bundle.putString(TITLE, song.getTitle());
            bundle.putString(ALBUM, song.getAlbum());
            bundle.putString(ARTIST, song.getArtist());
            bundle.putLong(DURATION, song.getDuration());
            mediaController
                    .getTransportControls()
                    .playFromUri(Uri.parse(song.getPath()), bundle);
        }
    }

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

    public Song getCurrentSong() {
        return (Song)playlist.get(currentSong);
    }

    public Song getPreviousSong(){
        int current = currentSong;
        if(playlist!= null) {
            int i = 0;
            while (true) {
                current--;
                if (current < 0) {
                    current = playlist.size() - 1;
                }
                Item item = playlist.get(current);
                if (item.isAudioFile()) {
                    playlistViewModel.setPreviousSong(currentSong);
                    playlistViewModel.setCurrentSong(current);
                    return (Song) item;
                }
                i++;
                if (i >= playlist.size()) {
                    break;
                }
            }
        }
        return null;
    }

    public Song getNextSong(){
        int current = currentSong;
        if(playlist != null) {
            int i = 0;
            while (true) {
                current++;
                if (current > playlist.size() - 1) {
                    current = 0;
                }
                Item item = playlist.get(current);
                if (item.isAudioFile()) {
                    playlistViewModel.setPreviousSong(currentSong);
                    playlistViewModel.setCurrentSong(current);
                    return (Song) item;
                }
                i++;
                if (i >= playlist.size()) {
                    break;
                }
            }
        }
        return null;
    }
}