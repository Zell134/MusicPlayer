package com.zell.musicplayer.activities;


import static com.zell.musicplayer.Services.PermissionsService.checkPermissions;
import static com.zell.musicplayer.db.LibraryType.LIBRARY_TYPE_ARTISTS;
import static com.zell.musicplayer.db.LibraryType.LIBRARY_TYPE_EXTERNAL_STORAGE;
import static com.zell.musicplayer.db.LibraryType.LIBRARY_TYPE_MEDIA_LIBRARY;
import static com.zell.musicplayer.db.PropertiesList.CURRENT_SONG;
import static com.zell.musicplayer.db.PropertiesList.LIBRARY_TYPE;

import android.content.ComponentName;
import android.content.Intent;
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
import android.widget.ListView;
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
import com.zell.musicplayer.Services.MediaLibraryService;
import com.zell.musicplayer.Services.MusicPlayerService;
import com.zell.musicplayer.Services.NotificationService;
import com.zell.musicplayer.Services.PermissionsService;
import com.zell.musicplayer.db.LibraryType;
import com.zell.musicplayer.models.PlaylistViewModel;
import com.zell.musicplayer.Services.PropertiesService;
import com.zell.musicplayer.fragments.AllLibraryFragment;
import com.zell.musicplayer.fragments.ArtistsFragment;
import com.zell.musicplayer.fragments.BaseFragment;
import com.zell.musicplayer.fragments.ExternalStorageFragment;
import com.zell.musicplayer.fragments.PermissionFragment;
import com.zell.musicplayer.models.Item;
import com.zell.musicplayer.models.Song;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class MainActivity extends AppCompatActivity implements BaseFragment.Listener, NavigationView.OnNavigationItemSelectedListener{

    public static String TITLE = "Title";
    public static String ALBUM = "Album";
    public static String ARTIST = "Artist";
    public static String DURATION = "Duration";

    private LibraryType libraryType;
    private MediaControllerCompat mediaController;
    private Handler handler = new Handler();
    private SeekBar seekbar;
    private TextView timer;
    private int currentState;
    private MediaBrowserCompat mediaBrowser;
    private DateFormat formatter = new SimpleDateFormat("mm:ss");
    private Properties properties;
    private List<Item> playlist;
    private PlaylistViewModel playlistViewModel;
    private int currentSong;
    private int previousSong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        properties = PropertiesService.getAllProperties(this);
        libraryType = getLibraryTypeFromProps();

        buildTransportControls();

        if (checkPermissions(this)) {
            setMainFragment();
        }else{
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.main_fragment, new PermissionFragment(),null)
                    .commit();
        }

            mediaBrowser = new MediaBrowserCompat(getApplicationContext(),
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

    private void buildTransportControls() {
        seekbar = findViewById(R.id.seekbar);
        timer = findViewById(R.id.timer);

        ImageButton playButton = findViewById(R.id.play);
        ImageButton stopButton = findViewById(R.id.stop);
        ImageButton previousButton = findViewById(R.id.previous);
        ImageButton nextButton = findViewById(R.id.next);
        ImageView exit = findViewById(R.id.exit);
        ImageView expandInfoButton = findViewById(R.id.expand_info);
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
                    break;
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

        exit.setOnClickListener(view -> {
            onDestroy();
            System.exit(0);
        });


        expandInfoButton.setOnClickListener(view -> {
            if(playlist!=null) {
                Intent intent = new Intent(this, SongInfoActivity.class);
                intent.putExtra(SongInfoActivity.CURRENT_STATE, currentState);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        playlistViewModel = new ViewModelProvider(this).get(PlaylistViewModel.class);
        playlistViewModel.getPlaylist().observe(this, items -> {playlist = items;});
        playlistViewModel.getCurrentSong().observe(this, items -> {currentSong = items;});
        playlistViewModel.getPreviousSong().observe(this, items -> {previousSong = items;});
        if(!mediaBrowser.isConnected()) {
            mediaBrowser.connect();
        }else{
            mediaController.registerCallback(controllerCallback);
            currentState = mediaController.getPlaybackState().getState();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    setCurrentState();
                    handler.postDelayed(this, 1000);
                }
            });
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
        mediaController
                .getTransportControls()
                .stop();
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
                String songPath = properties.getProperty(CURRENT_SONG);
                if(songPath!=null) {
                    fillPlaylistOnStart(songPath);
                    playSong();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };


    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener(){
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            long songDuration = getCurrentSong().getDuration();
            timer.setText(formatter.format(i) + " / " + formatter.format(songDuration));
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

    @Override
    public void setPlaylist(List<Item> playlist){
        playlistViewModel.setPlaylist(playlist);
        playlistViewModel.setPreviousSong(0);
        playlistViewModel.setCurrentSong(0);
    }

    @Override
    public void setCurrentSongPosition(int currentSongPosition){
        playlistViewModel.setPreviousSong(this.currentSong);
        playlistViewModel.setCurrentSong(currentSongPosition);
    }

    @Override
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
                libraryType = LIBRARY_TYPE_EXTERNAL_STORAGE;
                break;
            case(R.id.all_media):
                PropertiesService.setLibraryType(this,LIBRARY_TYPE_MEDIA_LIBRARY);
                libraryType = LIBRARY_TYPE_MEDIA_LIBRARY;
                break;
            case(R.id.artists):
                PropertiesService.setLibraryType(this,LIBRARY_TYPE_ARTISTS);
                libraryType = LIBRARY_TYPE_ARTISTS;
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
            BaseFragment playlistFragment = (BaseFragment) getSupportFragmentManager().findFragmentByTag("main");
            playlistFragment.onBackPressed();
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

    private void setMainFragment(){
        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        switch (libraryType) {
            case LIBRARY_TYPE_EXTERNAL_STORAGE:
                setFragment(new ExternalStorageFragment());
                menu.findItem(R.id.external_storage).setChecked(true);
                break;
            case LIBRARY_TYPE_MEDIA_LIBRARY:
                setFragment(new AllLibraryFragment());
                menu.findItem(R.id.all_media).setChecked(true);
                break;
            case LIBRARY_TYPE_ARTISTS:
                setFragment(new ArtistsFragment());
                menu.findItem(R.id.artists).setChecked(true);
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

    private MediaControllerCompat.Callback controllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            currentState = state.getState();
            ImageButton playPauseButton = findViewById(R.id.play);
            Song song;

            switch (currentState) {
                case PlaybackStateCompat.STATE_NONE:
                case PlaybackStateCompat.STATE_STOPPED:
                    playPauseButton.setImageResource(R.drawable.play_icon_black);
                    handler.removeCallbacks(null);
                    break;

                case PlaybackStateCompat.STATE_PLAYING:
                    PropertiesService.setCurrentSong(MainActivity.this, getCurrentSong().getPath());
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
                    song = getNextSong();
                    if(song != null){
                        playSong();
                        hightlightSong();
                    }
                    break;
                case PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS:
                    song = getPreviousSong();
                    if(song != null){
                        playSong();
                        hightlightSong();
                    }
                    break;
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            seekbar.setMax((int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
            seekbar.setProgress(0);
            timer.setText("0/0");
        }

        @Override
        public void onSessionDestroyed() {

        }
    };

    private void hightlightSong() {
        BaseFragment playlistFragment = (BaseFragment) getSupportFragmentManager().findFragmentByTag("main");

        ListView list = playlistFragment.getPlaylist();
        if (list.getCount() < currentSong) {
            playlistFragment.setPlaylist(playlist);
            playlistFragment.getPlaylist();
            list = playlistFragment.getPlaylist();
        }

        TextView songTitleView = list.getAdapter().getView(currentSong, null, list).findViewById(R.id.song_title);
        if (!songTitleView.getText().equals(playlist.get(currentSong).getTitle())) {
            playlistFragment.setPlaylist(playlist);
            playlistFragment.getPlaylist();
            list = playlistFragment.getPlaylist();
        }

        list.requestFocus();
        int listSize = list.getCount();

        if (previousSong != listSize - 1) {
            list.setSelection(currentSong - 3);
        }else {
            list.setSelection(currentSong);
        }
        playlistFragment.currentSongHighlight(currentSong);
    }

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

    public void fillPlaylistOnStart(String songPath){
        List<Item> playlist = new ArrayList<>();
        BaseFragment playlistFragment = (BaseFragment) getSupportFragmentManager().findFragmentByTag("main");
        switch (libraryType) {
            case LIBRARY_TYPE_EXTERNAL_STORAGE:
                playlist = ((ExternalStorageFragment)playlistFragment).getFilelist(songPath.substring(0, songPath.lastIndexOf("/")));
                break;
            case LIBRARY_TYPE_ARTISTS:
                Song song = MediaLibraryService.getSongByPath(this, songPath);
                playlist = MediaLibraryService.getSongsOfAlbum(this,song.getAlbum(),song.getArtist());
                break;
            case LIBRARY_TYPE_MEDIA_LIBRARY:
                playlist = MediaLibraryService.getAllMedia(this);
                break;
        }
        playlistFragment.setPlaylist(playlist);
        setPlaylist(playlist);
        int index = findSongIndexByPath(songPath);
        if(index>=0) {
            playlistViewModel.setCurrentSong(index);
        }
        hightlightSong();
    }

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

    private int findSongIndexByPath(String songPath){
     for(int i = 0; i<playlist.size(); i++){
         if(playlist.get(i).getPath().equals(songPath)){
             return i;
         }
     }
     return -1;
    }
}