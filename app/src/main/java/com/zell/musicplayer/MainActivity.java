package com.zell.musicplayer;


import static com.zell.musicplayer.Services.PermissionsService.checkPermissions;

import android.content.ComponentName;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.Menu;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.zell.musicplayer.Services.MusicPlayerService;
import com.zell.musicplayer.Services.PermissionsService;
import com.zell.musicplayer.fragments.PlaylistFragment;
import com.zell.musicplayer.models.PlaylistViewModel;
import com.zell.musicplayer.models.Song;

import java.util.List;


public class MainActivity extends AppCompatActivity implements PlaylistFragment.Listener {

    private boolean isPermissionsGranted = false;
    private MediaBrowserCompat mediaBrowser;
    private PlaylistViewModel playlistViewModel;
    private List<Song> playlist;
    private int currentSongPosition;
    private int currentState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playlistViewModel = new ViewModelProvider(this).get(PlaylistViewModel.class);

        playlistViewModel
                .getPlaylist()
                .observe(this, songs -> {
                    playlist = songs;
                });
        playlistViewModel
                .getCurrentSongPosition()
                .observe(MainActivity.this,
                        position -> currentSongPosition = position);
        if (checkPermissions(this)) {
            isPermissionsGranted = true;
            setup(savedInstanceState == null ? true : false);
        }

        mediaBrowser = new MediaBrowserCompat(this,
                new ComponentName(this, MusicPlayerService.class),
                connectionCalback,
                null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaBrowser.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (MediaControllerCompat.getMediaController(MainActivity.this) != null) {
            MediaControllerCompat.getMediaController(MainActivity.this).unregisterCallback(controllerCallback);
        }
        mediaBrowser.disconnect();
    }

    private final MediaBrowserCompat.ConnectionCallback connectionCalback = new MediaBrowserCompat.ConnectionCallback() {
        @Override
        public void onConnected() {
            try {
                MediaControllerCompat mediaController = new MediaControllerCompat(MainActivity.this, mediaBrowser.getSessionToken());
                MediaControllerCompat.setMediaController(MainActivity.this, mediaController);
                buildTransportControls();
                playSong();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    private void buildTransportControls() {
        ImageButton play = findViewById(R.id.play);

        play.setOnClickListener(view -> {
            switch (currentState) {
                case PlaybackStateCompat.STATE_NONE:
                case PlaybackStateCompat.STATE_STOPPED: {
                    playSong();
                    break;
                }
                case PlaybackStateCompat.STATE_PLAYING: {
                    MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().pause();
                    break;
                } case PlaybackStateCompat.STATE_PAUSED: {
                    MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().play();
                }
            }
        });

        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(MainActivity.this);
        mediaController.registerCallback(controllerCallback);
    }

    MediaControllerCompat.Callback controllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            currentState = state.getState();
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
        }

        @Override
        public void onSessionDestroyed() {
            mediaBrowser.disconnect();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionsService.onRequestPermissionsResult(requestCode, permissions, grantResults, this)) {
            setup(false);
            isPermissionsGranted = true;
        }
    }

    private void setup(boolean flag) {
        if (flag) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.playlist_container, PlaylistFragment.class, null)
                    .commit();
        }
    }

    @Override
    public void playSong() {
        MediaControllerCompat.getMediaController(MainActivity.this)
                    .getTransportControls()
                    .playFromUri(Uri.parse(playlist.get(currentSongPosition).getPath()), getBundle());
    }

    private Bundle getBundle(){
        Bundle bundle = new Bundle();
        bundle.putString(MediaStore.Audio.Media.DATA, playlist.get(currentSongPosition).getPath());
        bundle.putString(MediaStore.Audio.Media.TITLE, playlist.get(currentSongPosition).getTitle());
        bundle.putString(MediaStore.Audio.Media.ALBUM, playlist.get(currentSongPosition).getAlbum());
        bundle.putString(MediaStore.Audio.Media.ARTIST, playlist.get(currentSongPosition).getArtist());
        return bundle;
    }
}