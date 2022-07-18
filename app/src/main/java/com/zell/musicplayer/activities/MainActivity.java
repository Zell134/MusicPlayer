package com.zell.musicplayer.activities;


import static com.zell.musicplayer.Services.PermissionsService.checkPermissions;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.navigation.NavigationView;
import com.zell.musicplayer.R;
import com.zell.musicplayer.Services.MusicPlayerService;
import com.zell.musicplayer.Services.PermissionsService;
import com.zell.musicplayer.Services.PlaylistService;
import com.zell.musicplayer.fragments.AllLibraryFragment;
import com.zell.musicplayer.fragments.ArtistsFragment;
import com.zell.musicplayer.fragments.BaseFragment;
import com.zell.musicplayer.fragments.ExternalStorageFragment;
import com.zell.musicplayer.fragments.PermissionFragment;
import com.zell.musicplayer.models.Item;
import com.zell.musicplayer.models.StateViewModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;


public class MainActivity extends AppCompatActivity implements AllLibraryFragment.Listener, NavigationView.OnNavigationItemSelectedListener{

    public static final String LIBRARY_TYPE_MEDIA_LIBRARY = "MediaLibrary";
    public static final String LIBRARY_TYPE_EXTERNAL_STORAGE = "ExternalStorage";
    public static final String LIBRARY_TYPE_ARTISTS = "Artists";

    private PlaylistService playlistService;
    private boolean bound;
    private MediaBrowserCompat mediaBrowser;
    private int currentState;
    private StateViewModel viewModel;
    private String LibraryType;
    private Handler handler = new Handler();
    private SeekBar seekbar;
    private TextView timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewModel = new ViewModelProvider(this).get(StateViewModel.class);
        viewModel.setLibraryType(LIBRARY_TYPE_EXTERNAL_STORAGE);
        LibraryType = LIBRARY_TYPE_EXTERNAL_STORAGE;
        seekbar = findViewById(R.id.seekbar);
        timer = findViewById(R.id.timer);
        connectToMusicRepositoryService();

        if (checkPermissions(this)) {
            setMainFragment();
        }else{
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.main_fragment, new PermissionFragment(),null)
                    .commit();
        }

        mediaBrowser = new MediaBrowserCompat(this,
                new ComponentName(this, MusicPlayerService.class),
                connectionCalback,
                null);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.nav_open_drawer, R.string.nav_close_drawer);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
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
        if(bound){
            unbindService(serviceConnection);
            bound=false;
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

    private void connectToMusicRepositoryService(){
        Intent intent = new Intent(this, PlaylistService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
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

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            PlaylistService.PlaylistServiceBinder musicRepositoryBinder = (PlaylistService.PlaylistServiceBinder) iBinder;
            playlistService = musicRepositoryBinder.getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bound= false;
        }
    };

    private void buildTransportControls() {
        ImageButton playButton = findViewById(R.id.play);
        ImageButton stopButton = findViewById(R.id.stop);
        ImageButton previousButton = findViewById(R.id.previous);
        ImageButton nextButton = findViewById(R.id.next);
        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(MainActivity.this);
        seekbar.setOnSeekBarChangeListener(onSeekBarChangeListener);


        playButton.setOnClickListener(view -> {
            switch (currentState) {
                case PlaybackStateCompat.STATE_NONE:
                case PlaybackStateCompat.STATE_STOPPED:
                    playSong();
                    break;

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
                case PlaybackStateCompat.STATE_STOPPED:
                    playPauseButton.setImageResource(R.drawable.play_icon);
                    handler.removeCallbacks(null);
                    break;

                case PlaybackStateCompat.STATE_PLAYING:
                    playPauseButton.setImageResource(R.drawable.pause_icon);
                    handler.post(new Runnable() {
                        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(MainActivity.this);

                        @Override
                        public void run() {
                            long playerPosition = (int) mediaController.getPlaybackState().getPosition();
                            long songDuration = playlistService.getCurrentSong().getDuration();
                            seekbar.setProgress((int) playerPosition);
                            DateFormat formatter = new SimpleDateFormat("mm:ss");
                            timer.setText(formatter.format(playerPosition) + " / " + formatter.format(songDuration));
                            handler.postDelayed(this, 1000);
                        }
                    });
                    break;

                case PlaybackStateCompat.STATE_PAUSED:
                    playPauseButton.setImageResource(R.drawable.play_icon);
                    handler.removeCallbacks(null);
                    break;

                case PlaybackStateCompat.STATE_SKIPPING_TO_NEXT:
                case PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS:
                    BaseFragment playlistFragment = (BaseFragment) getSupportFragmentManager().findFragmentByTag("main");

                    ListView list = playlistFragment.getplaylist();

                    int position = playlistService.getCurrentSongPosition();
                    int previousPosition = playlistService.getPreviousSongPosition();
                    int listSize = list.getCount();
                    int centerOfList = (list.getLastVisiblePosition() - list.getFirstVisiblePosition()) / 2;

                    list.requestFocus();
                    if (previousPosition != listSize - 1) {
                        list.setSelection(position - centerOfList);
                    } else {
                        list.setSelection(position);
                    }
                    playlistFragment.currentSongHighlight(playlistService.getCurrentSongPosition());
                    break;

            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            seekbar.setMin(0);
            seekbar.setMax((int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
            seekbar.setProgress(0);
            timer.setText("0/0");
        }

        @Override
        public void onSessionDestroyed() {

        }
    };

    SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener(){
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            long songDuration = playlistService.getCurrentSong().getDuration();
            DateFormat formatter = new SimpleDateFormat("mm:ss");
            timer.setText(formatter.format(i) + " / " + formatter.format(songDuration));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(MainActivity.this);
            mediaController.getTransportControls().seekTo(seekbar.getProgress());
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionsService.onRequestPermissionsResult(requestCode, permissions, grantResults, this)) {
            PermissionsService.closeNotification(this);
            setMainFragment();
        }
    }

    @Override
    public void playSong() {
        Uri uri = Uri.parse(playlistService.getCurrentSong().getPath());
        MediaControllerCompat.getMediaController(MainActivity.this)
                    .getTransportControls()
                    .playFromUri(uri, new Bundle());
    }

    @Override
    public void setPlaylist(List<Item> playlist){
        playlistService.setPlyalist(playlist);
        playlistService.setCurrentSongPosition(0);
    }

    @Override
    public void setCurrentSongPosition(int currentSongPosition){
        playlistService.setCurrentSongPosition(currentSongPosition);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case(R.id.external_storage):
                viewModel.setLibraryType(LIBRARY_TYPE_EXTERNAL_STORAGE);
                LibraryType = LIBRARY_TYPE_EXTERNAL_STORAGE;
                break;
            case(R.id.all_media):
                viewModel.setLibraryType(LIBRARY_TYPE_MEDIA_LIBRARY);
                LibraryType = LIBRARY_TYPE_MEDIA_LIBRARY;
                break;
            case(R.id.artists):
                viewModel.setLibraryType(LIBRARY_TYPE_ARTISTS);
                LibraryType = LIBRARY_TYPE_ARTISTS;
                break;
            case(R.id.exit):
                onDestroy();
                System.exit(0);
                break;
        }
        item.setChecked(true);
        setMainFragment();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void setMainFragment(){
        switch (LibraryType) {
            case LIBRARY_TYPE_EXTERNAL_STORAGE:
                setFragment(new ExternalStorageFragment());
                break;
            case LIBRARY_TYPE_MEDIA_LIBRARY:
                setFragment(new AllLibraryFragment());
                break;
            case LIBRARY_TYPE_ARTISTS:
                setFragment(new ArtistsFragment());
                break;
        }
    }

    private void setFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        if(fragmentManager.getFragments().size() == 0){
            fragmentManager.beginTransaction()
                    .add(R.id.main_fragment, fragment,"main")
                    .commit();
        }else {
            fragmentManager.beginTransaction()
                    .detach(fragmentManager.getFragments().get(0))
                    .replace(R.id.main_fragment, fragment,"main")
                    .commit();
        }
    }

}