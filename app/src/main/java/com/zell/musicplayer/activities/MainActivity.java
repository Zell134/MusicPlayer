package com.zell.musicplayer.activities;


import static com.zell.musicplayer.services.PermissionsService.checkPermissions;
import static com.zell.musicplayer.db.LibraryType.LIBRARY_TYPE_ARTISTS;
import static com.zell.musicplayer.db.LibraryType.LIBRARY_TYPE_EXTERNAL_STORAGE;
import static com.zell.musicplayer.db.LibraryType.LIBRARY_TYPE_MEDIA_LIBRARY;
import static com.zell.musicplayer.db.PropertiesList.BASS_BOOST;
import static com.zell.musicplayer.db.PropertiesList.CURRENT_SONG;
import static com.zell.musicplayer.db.PropertiesList.DELIMITER;
import static com.zell.musicplayer.db.PropertiesList.EQUALIZER;
import static com.zell.musicplayer.db.PropertiesList.LIBRARY_TYPE;
import static com.zell.musicplayer.db.PropertiesList.VOLUME_LEVEL;

import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.navigation.NavigationView;
import com.zell.musicplayer.R;
import com.zell.musicplayer.services.MediaLibraryService;
import com.zell.musicplayer.services.NotificationService;
import com.zell.musicplayer.services.PermissionsService;
import com.zell.musicplayer.services.PlaylistService;
import com.zell.musicplayer.services.PropertiesService;
import com.zell.musicplayer.db.LibraryType;
import com.zell.musicplayer.fragments.EqualizerFragment;
import com.zell.musicplayer.fragments.PermissionFragment;
import com.zell.musicplayer.fragments.PlaylistFragment;
import com.zell.musicplayer.models.Player;
import com.zell.musicplayer.models.Song;
import com.zell.musicplayer.viewModels.MediaBrowserViewModel;
import com.zell.musicplayer.viewModels.PlaylistViewModel;

import java.text.SimpleDateFormat;
import java.util.Properties;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, PlaylistService.Listener{

    public static String TITLE = "Title";
    public static String ALBUM = "Album";
    public static String ARTIST = "Artist";
    public static String DURATION = "Duration";

    private MediaControllerCompat mediaController;
    private final Handler handler = new Handler();
    private int currentState;
    private final SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
    private Properties properties;
    private PlaylistService playlistService;
    private boolean ifEqualizerOpened = false;
    private MediaBrowserViewModel mediaBrowserViewModel;
    private PlaylistViewModel playlistViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        properties = PropertiesService.getAllProperties(this);

        if (checkPermissions(this)) {
            setMainFragment();
        } else {
            setFragment(new PermissionFragment());
        }
        playlistViewModel = new ViewModelProvider(this).get(PlaylistViewModel.class);
        playlistService = playlistViewModel.getPlaylistService();
        if(playlistService != null){
            playlistService.setContext(this);
        }

        mediaBrowserViewModel = new ViewModelProvider(this).get(MediaBrowserViewModel.class);

        mediaBrowserViewModel
                .getMediaController()
                .observe(this,mediaControllerCompat -> {
                    this.mediaController = mediaControllerCompat;
                    MediaControllerCompat.setMediaController(MainActivity.this, mediaController);
                    mediaController.registerCallback(controllerCallback);
                    buildControls();
                    if(mediaController.getMetadata() != null) {
                        currentState = mediaController.getPlaybackState().getState();
                        controllerCallback.onMetadataChanged(mediaController.getMetadata());
                        controllerCallback.onPlaybackStateChanged(mediaController.getPlaybackState());

                    }else{
                        setEqualizer();
                    }
                });

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
        ImageButton equalizerButton = findViewById(R.id.equqlizer_button);
        ImageView exit = findViewById(R.id.exit);
        SeekBar seekbar = findViewById(R.id.seekbar);

        if(playlistService == null) {
            playlistService = new PlaylistService(this,
                    getLibraryTypeFromProps(),
                    properties.getProperty(CURRENT_SONG),
                    new MediaLibraryService()
            );
            playlistViewModel.setPlaylistService(playlistService);
        }
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

        stopButton.setOnClickListener(view -> stopPlaying());

        previousButton.setOnClickListener(view -> mediaController.getTransportControls().skipToPrevious());

        nextButton.setOnClickListener(view -> mediaController.getTransportControls().skipToNext());

        exit.setOnClickListener(view -> {
            NotificationManagerCompat.from(getApplication().getBaseContext()).cancel(NotificationService.ID);
            onDestroy();
            System.exit(0);
        });

        equalizerButton.setOnClickListener(view -> equalizerButtonOnClick());
    }

    private void setEqualizer(){
        Player player = Player.getInstance();
        Equalizer equalizer = player.getEqualizer();
        BassBoost bassBoost = player.getBassBoost();

        properties.forEach((k, v) -> {
            String key = k.toString();
            if(key.contains(EQUALIZER)){
                short band = Short.parseShort(key.split(DELIMITER)[1]);
                equalizer.setBandLevel(band,Short.parseShort(v.toString()));
            }
        } );

        if(properties.containsKey(BASS_BOOST)) {
            if (bassBoost.getStrengthSupported()) {
                bassBoost.setStrength(Short.parseShort(properties.getProperty(BASS_BOOST)));
            }
        }
        if(properties.containsKey(VOLUME_LEVEL)) {
            int volumeLevel = Integer.parseInt(properties.getProperty(VOLUME_LEVEL));
            AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
            am.setStreamVolume(AudioManager.STREAM_MUSIC,volumeLevel,0);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (checkPermissions(this)) {
            mediaBrowserViewModel.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if(mediaController != null) {
            mediaController.unregisterCallback(controllerCallback);
        }

        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        int volumeLevel= am.getStreamVolume(AudioManager.STREAM_MUSIC);
        PropertiesService.setVolume(this, String.valueOf(volumeLevel));
        super.onDestroy();
    }

    private final SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener(){
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            TextView timer = findViewById(R.id.timer);
            long duration = mediaController.getMetadata().getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
            timer.setText(formatter.format(i) + " / " + formatter.format(duration));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            SeekBar seekbar = findViewById(R.id.seekbar);
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
            if(mediaController != null) {
                mediaController
                        .getTransportControls()
                        .playFromUri(Uri.parse(song.getPath()), bundle);
                PropertiesService.setCurrentSong(MainActivity.this, song.getPath());
            }
        }
    }

    public void stopPlaying(){
        if (currentState == PlaybackStateCompat.STATE_PLAYING || currentState == PlaybackStateCompat.STATE_PAUSED) {
            mediaController.getTransportControls().stop();
        }
        resetStateOnStop();
    }

    private void resetStateOnStop(){
        TextView songName = findViewById(R.id.playing_song_name);
        TextView songInfo = findViewById(R.id.playing_song_info);
        ImageView albumArt = findViewById(R.id.album_art);
        TextView timer = findViewById(R.id.timer);

        songName.setText("");
        songInfo.setText("");
        albumArt.setImageResource(R.drawable.empty_album_art);
        timer.setText("0/0");
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionsService.onRequestPermissionsResult(requestCode, permissions, grantResults, this)) {
            mediaBrowserViewModel.connect();
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
            case(R.id.menu_equalizer):
                equalizerButtonOnClick();
                break;
            case(R.id.exit):
                NotificationManagerCompat.from(getApplication().getBaseContext()).cancel(NotificationService.ID);
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
        } else if (ifEqualizerOpened){
            setMainFragment();
            ifEqualizerOpened = false;
        }else
        {
            playlistService.onBackPressed();
        }
    }

    private void setCurrentState(){
        SeekBar seekbar = findViewById(R.id.seekbar);
        TextView timer = findViewById(R.id.timer);
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
        TextView songName = findViewById(R.id.playing_song_name);
        TextView songInfo = findViewById(R.id.playing_song_info);
        ImageView albumArt = findViewById(R.id.album_art);
        MediaMetadataCompat metadata = mediaController.getMetadata();
        long duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        albumArt.setImageDrawable(new BitmapDrawable(mediaController.getMetadata().getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART)));
        StringBuilder str = new StringBuilder();
        str.append(mediaController.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_TITLE))
                .append(" (")
                .append(formatter.format(duration))
                .append(")");
        songName.setText(str);
        str = new StringBuilder();
        str.append(mediaController.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_ARTIST))
                .append(" - ")
                .append(mediaController.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_ALBUM));
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
            SeekBar seekbar = findViewById(R.id.seekbar);
            TextView timer = findViewById(R.id.timer);
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

    public void setAdapterAndScroll(){
        if(playlistService != null) {
            playlistService.setAdapter();
            playlistService.scrollToCurrentPosition();
        }
    }

    private void equalizerButtonOnClick(){
        if(ifEqualizerOpened){
            setMainFragment();
            ifEqualizerOpened = false;
        }else {
            setFragment(new EqualizerFragment());
            ifEqualizerOpened = true;
        }
    }

}