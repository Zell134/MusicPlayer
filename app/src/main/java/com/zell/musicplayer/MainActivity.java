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
import android.view.MenuItem;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.navigation.NavigationView;
import com.zell.musicplayer.Services.MusicPlayerService;
import com.zell.musicplayer.Services.PermissionsService;
import com.zell.musicplayer.fragments.PlaylistFragment;
import com.zell.musicplayer.models.Song;
import com.zell.musicplayer.models.StateViewModel;

import java.util.List;


public class MainActivity extends AppCompatActivity implements PlaylistFragment.Listener, NavigationView.OnNavigationItemSelectedListener{

    public static final String LIBRARY_TYPE_MEDIA_LIBRARY = "MediaLibrary";
    public static final String LIBRARY_TYPE_EXTERNAL_STORAGE = "ExternalStorage";

    private MediaBrowserCompat mediaBrowser;
    private List<Song> playlist;
    private int currentSongPosition;
    private int currentState;
    private StateViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewModel = new ViewModelProvider(this).get(StateViewModel.class);
        viewModel.setLibraryType(LIBRARY_TYPE_EXTERNAL_STORAGE);

        if (checkPermissions(this)) {
            setup();
        }

        mediaBrowser = new MediaBrowserCompat(this,
                new ComponentName(this, MusicPlayerService.class),
                connectionCalback,
                null);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.nav_open_drawer, R.string.nav_close_drawer);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!mediaBrowser.isConnected()) {
            mediaBrowser.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (MediaControllerCompat.getMediaController(MainActivity.this) != null) {
            MediaControllerCompat.getMediaController(MainActivity.this).unregisterCallback(controllerCallback);
        }
    }

    @Override
    protected void onDestroy() {
        MediaControllerCompat
                .getMediaController(MainActivity.this)
                .getTransportControls()
                .stop();
        mediaBrowser.disconnect();
        super.onDestroy();
    }

    private final MediaBrowserCompat.ConnectionCallback connectionCalback = new MediaBrowserCompat.ConnectionCallback() {
        @Override
        public void onConnected() {
            try {
                MediaControllerCompat mediaController = new MediaControllerCompat(MainActivity.this, mediaBrowser.getSessionToken());
                MediaControllerCompat.setMediaController(MainActivity.this, mediaController);
                buildTransportControls();
//                playSong();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

    };

    private void buildTransportControls() {
        ImageButton playButton = findViewById(R.id.play);
        ImageButton stopButton = findViewById(R.id.stop);
        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(MainActivity.this);

        playButton.setOnClickListener(view -> {
            switch (currentState) {

                case PlaybackStateCompat.STATE_NONE:
                case PlaybackStateCompat.STATE_STOPPED: {
                    playSong();
                    break;
                }
                case PlaybackStateCompat.STATE_PLAYING: {
                    mediaController.getTransportControls().pause();
                    break;
                } case PlaybackStateCompat.STATE_PAUSED: {
                    mediaController.getTransportControls().play();
                }
            }
        });


        stopButton.setOnClickListener(view -> {
            if (currentState == PlaybackStateCompat.STATE_PLAYING || currentState == PlaybackStateCompat.STATE_PAUSED) {
                mediaController.getTransportControls().stop();
            }
        });

        mediaController.registerCallback(controllerCallback);
    }

    MediaControllerCompat.Callback controllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            currentState = state.getState();
            ImageButton playPauseButton = findViewById(R.id.play);

            switch (currentState) {
                case PlaybackStateCompat.STATE_NONE:
                case PlaybackStateCompat.STATE_STOPPED: {
                    playPauseButton.setImageResource(R.drawable.play_icon);
                    break;
                }
                case PlaybackStateCompat.STATE_PLAYING: {
                    playPauseButton.setImageResource(R.drawable.pause_icon);
                    break;
                }
                case PlaybackStateCompat.STATE_PAUSED:{
                    playPauseButton.setImageResource(R.drawable.play_icon);
                    break;
                }
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
        }

        @Override
        public void onSessionDestroyed() {

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionsService.onRequestPermissionsResult(requestCode, permissions, grantResults, this)) {
            setup();
        }else{
            checkPermissions(this);
        }
    }

    private void setup() {
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.playlist_container, PlaylistFragment.class, null)
                .commit();

    }

    @Override
    public void playSong() {
        Uri uri = Uri.parse(playlist.get(currentSongPosition).getPath());
        MediaControllerCompat.getMediaController(MainActivity.this)
                    .getTransportControls()
                    .playFromUri(uri, getBundle());
    }

    @Override
    public void setPlaylist(List<Song> playlist){
        this.playlist = playlist;
    }
    @Override
    public void setCurrentSongPosition(int currentSongPosition){
        this.currentSongPosition = currentSongPosition;
    }

    private Bundle getBundle(){
        Bundle bundle = new Bundle();
        bundle.putString(MediaStore.Audio.Media.DATA, playlist.get(currentSongPosition).getPath());
        bundle.putString(MediaStore.Audio.Media.TITLE, playlist.get(currentSongPosition).getTitle());
        bundle.putString(MediaStore.Audio.Media.ALBUM, playlist.get(currentSongPosition).getAlbum());
        bundle.putString(MediaStore.Audio.Media.ARTIST, playlist.get(currentSongPosition).getArtist());
        return bundle;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case(R.id.all_media):
                viewModel.setLibraryType(LIBRARY_TYPE_MEDIA_LIBRARY);
                break;
            case(R.id.external_storage):
                viewModel.setLibraryType(LIBRARY_TYPE_EXTERNAL_STORAGE);
                break;
            case(R.id.exit):
                onDestroy();
                System.exit(0);
                break;
        }
        item.setChecked(true);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}