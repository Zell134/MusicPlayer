package com.zell.musicplayer.activities;


import static com.zell.musicplayer.Services.PermissionsService.checkPermissions;
import static com.zell.musicplayer.db.LibraryType.LIBRARY_TYPE_ARTISTS;
import static com.zell.musicplayer.db.LibraryType.LIBRARY_TYPE_EXTERNAL_STORAGE;
import static com.zell.musicplayer.db.LibraryType.LIBRARY_TYPE_MEDIA_LIBRARY;
import static com.zell.musicplayer.db.PropertiesList.CURRENT_SONG;
import static com.zell.musicplayer.db.PropertiesList.LIBRARY_TYPE;

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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;
import com.zell.musicplayer.R;
import com.zell.musicplayer.Services.MusicPlayerService;
import com.zell.musicplayer.Services.NotificationService;
import com.zell.musicplayer.Services.PermissionsService;
import com.zell.musicplayer.Services.PlaylistService;
import com.zell.musicplayer.Services.PropertiesService;
import com.zell.musicplayer.adapters.SongAdapter;
import com.zell.musicplayer.db.LibraryType;
import com.zell.musicplayer.fragments.PermissionFragment;
import com.zell.musicplayer.fragments.PlaylistFragment;
import com.zell.musicplayer.models.Song;

import java.text.SimpleDateFormat;
import java.util.Properties;


public class MainActivity extends AppCompatActivity implements SongAdapter.Listener, NavigationView.OnNavigationItemSelectedListener{

    public static String TITLE = "Title";
    public static String ALBUM = "Album";
    public static String ARTIST = "Artist";
    public static String DURATION = "Duration";

    private MediaControllerCompat mediaController;
    private final Handler handler = new Handler();
    private SeekBar seekbar;
    private TextView timer;
    private TextView songInfo;
    private ImageView albumArt;
    private int currentState;
    private MediaBrowserCompat mediaBrowser;
    private final SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
    private Properties properties;
    private PlaylistService playlistService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkPermissions(this)) {
            properties = PropertiesService.getAllProperties(this);
            setMainFragment();
        } else {
            setFragment(new PermissionFragment());
        }

        mediaBrowser = new MediaBrowserCompat(getApplicationContext(),
                new ComponentName(this, MusicPlayerService.class),
                connectionCalback,
                null);

        setToolbar();
    }

    private void setToolbar(){
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

    private void buildControls() {
        ImageButton playButton = findViewById(R.id.play);
        ImageButton stopButton = findViewById(R.id.stop);
        ImageButton previousButton = findViewById(R.id.previous);
        ImageButton nextButton = findViewById(R.id.next);
        songInfo = findViewById(R.id.playing_song_info);
        albumArt = findViewById(R.id.album_art);
        seekbar = findViewById(R.id.seekbar);
        timer = findViewById(R.id.timer);
        ImageView exit = findViewById(R.id.exit);
        playlistService = new PlaylistService(this, getLibraryTypeFromProps(), properties.getProperty(CURRENT_SONG));

        seekbar.setOnSeekBarChangeListener(onSeekBarChangeListener);

        playButton.setOnClickListener(view -> {
            switch (currentState) {
                case PlaybackStateCompat.STATE_NONE:
                case PlaybackStateCompat.STATE_STOPPED:
                    playlistService.play();
                    break;

                case PlaybackStateCompat.STATE_PLAYING:
                    mediaController.getTransportControls().pause();
                    break;
                case PlaybackStateCompat.STATE_PAUSED:
                    mediaController.getTransportControls().play();
                    break;
            }
        });

        stopButton.setOnClickListener(view -> {
            if (currentState == PlaybackStateCompat.STATE_PLAYING || currentState == PlaybackStateCompat.STATE_PAUSED) {
                mediaController.getTransportControls().stop();
            }
        });

        previousButton.setOnClickListener(view -> mediaController.getTransportControls().skipToPrevious());

        nextButton.setOnClickListener(view -> mediaController.getTransportControls().skipToNext());

        exit.setOnClickListener(view -> {
            onDestroy();
            System.exit(0);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (checkPermissions(this)) {
            if (!mediaBrowser.isConnected()) {
                mediaBrowser.connect();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaController != null) {
            mediaController.unregisterCallback(controllerCallback);
        }
    }

    @Override
    protected void onDestroy() {
        if(mediaController != null) {
            mediaController
                    .getTransportControls()
                    .stop();
        }
        if(mediaBrowser.isConnected()) {
            mediaBrowser.disconnect();
        }
        NotificationManagerCompat.from(this).cancel(NotificationService.ID);
        super.onDestroy();
    }

    private final MediaBrowserCompat.ConnectionCallback connectionCalback = new MediaBrowserCompat.ConnectionCallback() {
        @Override
        public void onConnected() {
            try {
                mediaController = new MediaControllerCompat(MainActivity.this, mediaBrowser.getSessionToken());
                MediaControllerCompat.setMediaController(MainActivity.this, mediaController);
                mediaController.registerCallback(controllerCallback);
                buildControls();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };


    private final SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener(){
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            long duration = mediaController.getMetadata().getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
            timer.setText(formatter.format(i) + " / " + formatter.format(duration));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if(currentState == PlaybackStateCompat.STATE_PLAYING || currentState == PlaybackStateCompat.STATE_PAUSED) {
                mediaController.getTransportControls().seekTo(seekbar.getProgress());
            }
        }
    };
    @Override
    public void playSong(Song song) {
        if(song!=null) {

            Bundle bundle = new Bundle();
            bundle.putString(TITLE, song.getTitle());
            bundle.putString(ALBUM, song.getAlbum());
            bundle.putString(ARTIST, song.getArtist());
            bundle.putLong(DURATION, song.getDuration());
            mediaController
                    .getTransportControls()
                    .playFromUri(Uri.parse(song.getPath()), bundle);
            PropertiesService.setCurrentSong(MainActivity.this, song.getPath());
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionsService.onRequestPermissionsResult(requestCode, permissions, grantResults, this)) {
            PermissionsService.closeNotification(this);
            setMainFragment();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case(R.id.external_storage):
                PropertiesService.setLibraryType(this, LIBRARY_TYPE_EXTERNAL_STORAGE);
                playlistService.setLibraryType(LIBRARY_TYPE_EXTERNAL_STORAGE);
                break;
            case(R.id.all_media):
                PropertiesService.setLibraryType(this,LIBRARY_TYPE_MEDIA_LIBRARY);
                playlistService.setLibraryType(LIBRARY_TYPE_MEDIA_LIBRARY);
                break;
            case(R.id.artists):
                PropertiesService.setLibraryType(this,LIBRARY_TYPE_ARTISTS);
                playlistService.setLibraryType(LIBRARY_TYPE_ARTISTS);
                break;
            case(R.id.exit):
                onDestroy();
                System.exit(0);
                break;
        }
        item.setChecked(true);

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
            playlistService.onBackPressed();
        }
    }

    private void setCurrentState(){
        MediaMetadataCompat metadata = mediaController.getMetadata();
        long duration;
        long position = mediaController.getPlaybackState().getPosition();
        if(metadata != null) {
             duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        }else{
            duration = 0;
        }
        seekbar.setProgress((int) position);
        timer.setText(formatter.format(position) + " / " + formatter.format(duration));
    }

    private void fillSongInfo() {
        MediaMetadataCompat metadata = mediaController.getMetadata();
        long duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        albumArt.setImageDrawable(new BitmapDrawable(mediaController.getMetadata().getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART)));
        StringBuilder str = new StringBuilder();
        str.append(mediaController.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_ARTIST))
                .append(" - ").append(mediaController.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_TITLE))
                .append(" (").append(formatter.format(duration))
                .append(")");
        songInfo.setText(str);
    }

    private void setMainFragment(){
        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        switch (getLibraryTypeFromProps()) {
            case LIBRARY_TYPE_EXTERNAL_STORAGE:
                menu.findItem(R.id.external_storage).setChecked(true);
                break;
            case LIBRARY_TYPE_MEDIA_LIBRARY:
                menu.findItem(R.id.all_media).setChecked(true);
                break;
            case LIBRARY_TYPE_ARTISTS:
                menu.findItem(R.id.artists).setChecked(true);
                break;
        }
        setFragment(new PlaylistFragment());
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

    private final MediaControllerCompat.Callback controllerCallback = new MediaControllerCompat.Callback() {
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
                            setCurrentState();
                            handler.postDelayed(this, 1000);
                        }
                    });
                    break;

                case PlaybackStateCompat.STATE_PAUSED:
                    playPauseButton.setImageResource(R.drawable.play_icon_black);
                    handler.removeCallbacks(null);
                    break;

                case PlaybackStateCompat.STATE_SKIPPING_TO_NEXT:
                    playlistService.playNextSong();
                    break;
                case PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS:
                    playlistService.playPreviousSong();
                    break;
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            seekbar.setMax((int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
            seekbar.setProgress(0);
            timer.setText("0/0");
            fillSongInfo();
        }

        @Override
        public void onSessionDestroyed() {}
    };

    private LibraryType getLibraryTypeFromProps(){
        switch ((String)properties.get(LIBRARY_TYPE)) {
            case "MediaLibrary": {
                return LibraryType.LIBRARY_TYPE_MEDIA_LIBRARY;
            }
            case "ExternalStorage": {
                return LibraryType.LIBRARY_TYPE_EXTERNAL_STORAGE;
            }
            case "Artists": {
                return LibraryType.LIBRARY_TYPE_ARTISTS;
            }
        }
        return null;
    }
}