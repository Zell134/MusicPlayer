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
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.zell.musicplayer.R;
import com.zell.musicplayer.Services.MusicPlayerService;
import com.zell.musicplayer.Services.PermissionsService;
import com.zell.musicplayer.Services.PlaylistService;
import com.zell.musicplayer.adapters.PlaylistPagerAdapter;
import com.zell.musicplayer.fragments.AllLibraryFragment;
import com.zell.musicplayer.fragments.BaseFragment;
import com.zell.musicplayer.fragments.ExternalStorageFragment;
import com.zell.musicplayer.fragments.MainFragment;
import com.zell.musicplayer.fragments.PermissionFragment;
import com.zell.musicplayer.models.Item;
import com.zell.musicplayer.models.StateViewModel;

import java.util.List;


public class MainActivity extends AppCompatActivity implements AllLibraryFragment.Listener, NavigationView.OnNavigationItemSelectedListener{

    public static final String LIBRARY_TYPE_MEDIA_LIBRARY = "MediaLibrary";
    public static final String LIBRARY_TYPE_EXTERNAL_STORAGE = "ExternalStorage";

    private PlaylistService playlistService;
    private boolean bound;
    private MediaBrowserCompat mediaBrowser;
    private int currentState;
    private StateViewModel viewModel;
    private String LibraryType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewModel = new ViewModelProvider(this).get(StateViewModel.class);
        viewModel.setLibraryType(LIBRARY_TYPE_MEDIA_LIBRARY);
        LibraryType = LIBRARY_TYPE_MEDIA_LIBRARY;

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
                case PlaybackStateCompat.STATE_SKIPPING_TO_NEXT:
                case PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS:{
                    ListView list = findViewById(R.id.main_fragment).findViewById(android.R.id.list);
                    int position =  playlistService.getCurrentSongPosition();
                    int previousPosition = playlistService.getPreviousSongPosition();
                    int listSize = list.getCount();
                    int centerOfList = (list.getLastVisiblePosition() - list.getFirstVisiblePosition()) /2;

                    getViewByPosition(previousPosition, list).setBackgroundResource(R.color.default_list_color);
                    getViewByPosition(position, list).setBackgroundResource(R.color.selected_item);

                    if(previousPosition  != listSize - 1) {
                        list.setSelection(position - centerOfList);
                    }else{
                        list.setSelection(position);
                    }
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
        ListView list = findViewById(R.id.main_fragment).findViewById(android.R.id.list);
        getViewByPosition(playlistService.getCurrentSongPosition(), list).setBackgroundResource(R.color.default_list_color);
        playlistService.setCurrentSongPosition(currentSongPosition);
        getViewByPosition(playlistService.getCurrentSongPosition(), list).setBackgroundResource(R.color.selected_item);
    }

    private View getViewByPosition(int position, ListView list) {
        int firstListItemPosition = list.getFirstVisiblePosition();
        int lastListItemPosition = firstListItemPosition + list.getChildCount() - 1;
        int childIndex = position - firstListItemPosition;
        if (position < firstListItemPosition || position > lastListItemPosition ) {
            return list.getAdapter().getView(position, null, list);
        } else {
            return list.getChildAt(childIndex);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case(R.id.all_media):
                viewModel.setLibraryType(LIBRARY_TYPE_MEDIA_LIBRARY);
                LibraryType = LIBRARY_TYPE_MEDIA_LIBRARY;
                break;
            case(R.id.external_storage):
                viewModel.setLibraryType(LIBRARY_TYPE_EXTERNAL_STORAGE);
                LibraryType = LIBRARY_TYPE_EXTERNAL_STORAGE;
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
        TabLayout tabs = findViewById(R.id.tabs);
        switch (LibraryType) {
            case LIBRARY_TYPE_EXTERNAL_STORAGE:
                setFragment(new ExternalStorageFragment());
                int count = tabs.getTabCount();
                if(count>0) {
                    for (int i = 1; i < count; i++) {
                        tabs.removeTabAt(i);
                    }
                    tabs.getTabAt(0).setText(getResources().getString(R.string.external_storage));
                }else {
                    tabs.addTab(tabs.newTab().setText(getResources().getString(R.string.external_storage)));
                }
                break;
            case LIBRARY_TYPE_MEDIA_LIBRARY:
                setFragment(new MainFragment());
                break;
        }
    }

    private void setFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        if(fragmentManager.getFragments().size() == 0){
            fragmentManager.beginTransaction()
                    .add(R.id.main_fragment, fragment,null)
                    .commit();
        }else {
            fragmentManager.beginTransaction()
                    .detach(fragmentManager.getFragments().get(0))
                    .replace(R.id.main_fragment, fragment,null)
                    .commit();
        }
    }

}