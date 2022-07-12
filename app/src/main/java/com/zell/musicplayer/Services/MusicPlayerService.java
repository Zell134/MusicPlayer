package com.zell.musicplayer.Services;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

import com.zell.musicplayer.R;
import com.zell.musicplayer.activities.MainActivity;
import com.zell.musicplayer.models.Song;

import java.io.IOException;
import java.util.List;

public class MusicPlayerService extends MediaBrowserServiceCompat implements MediaPlayer.OnPreparedListener{

    private static final String MY_MEDIA_ROOT_ID = "com.zell.musicplayer";
    private static final int NOTIFY_ID = 1;


    private MediaSessionCompat mediaSession;
    private AudioManager audioManager;
    private MediaPlayer player;
    private AudioFocusRequest audioFocusRequest;
    private long playbackState;
    private PlaylistService playlistService;
    private boolean bound;

    @Override
    public void onCreate() {
        super.onCreate();

        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setOnAudioFocusChangeListener(audioFocusChangeListener)
                    .setAudioAttributes(attributes)
                    .build();
        }
        initMediaPlayer();
        initMediaSession();
        connectToMusicRepositoryService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mediaSession, intent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void connectToMusicRepositoryService(){
        Intent intent = new Intent(this, PlaylistService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    private void initMediaSession() {
        mediaSession = new MediaSessionCompat(this, getApplicationContext().getString(R.string.app_name));

        Intent mediaButtonIntent = new Intent(
                Intent.ACTION_MEDIA_BUTTON, null, getApplicationContext(), MediaButtonReceiver.class);
        mediaSession.setMediaButtonReceiver(
                PendingIntent.getBroadcast(getApplicationContext(), 0, mediaButtonIntent, 0));

        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setCallback(mediaSessionCallback);
        Intent activityIntent = new Intent(getApplicationContext(), MainActivity.class);
        mediaSession.setSessionActivity(PendingIntent.getActivity(getApplicationContext(), 0, activityIntent, 0));
        PlaybackStateCompat.Builder playbackStateBuilder = new PlaybackStateCompat.Builder();
        playbackStateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY |
                PlaybackStateCompat.ACTION_PAUSE |
                PlaybackStateCompat.ACTION_PLAY_PAUSE|
                PlaybackStateCompat.ACTION_STOP
        );
        mediaSession.setPlaybackState(playbackStateBuilder.build());
        setMediaPlaybackState(PlaybackStateCompat.STATE_NONE);
        setSessionToken(mediaSession.getSessionToken());
    }

    private void initMediaPlayer() {
        if(player == null) {
            player = new MediaPlayer();
            player.setOnPreparedListener(this);
            player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mediaSession!=null) {
            mediaSession.release();
        }
        if(player!=null){
            player.release();
        }
        NotificationManagerCompat.from(this).cancel(NOTIFY_ID);
        if(bound){
            unbindService(serviceConnection);
            bound=false;
        }
    }

    MediaSessionCompat.Callback mediaSessionCallback = new MediaSessionCompat.Callback() {
        @Override
        public void onPlay() {
            startService(new Intent(getApplicationContext(), MusicPlayerService.class));
            if (ifAudioFocusGranted()) {
                if(playbackState == PlaybackStateCompat.STATE_PAUSED) {
                    mediaSession.setActive(true);
                    setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
                    player.start();
                    showPlayingNotification();
                }
            }
        }

        @Override
        public void onPlayFromUri(Uri uri, Bundle bundle) {
            startService(new Intent(getApplicationContext(), MusicPlayerService.class));
            if (ifAudioFocusGranted()) {
                setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
                player.reset();
                setPlayerDataSource(playlistService.getCurrentSong());
                mediaSession.setActive(true);
                showPlayingNotification();
            }
        }

        @Override
        public void onPause() {
            if(playbackState == PlaybackStateCompat.STATE_PLAYING) {
                setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
                player.pause();
                showPausedNotification();
            }
        }
        @Override
        public void onStop() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioManager.abandonAudioFocusRequest(audioFocusRequest);
            }
            mediaSession.setActive(false);
            setMediaPlaybackState(PlaybackStateCompat.STATE_STOPPED);
            player.stop();
            NotificationManagerCompat.from(MusicPlayerService.this).cancel(NOTIFY_ID);
            stopSelf();
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

    AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusState) {
            switch(focusState){
                case AudioManager.AUDIOFOCUS_GAIN:
                    mediaSessionCallback.onPlay();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    mediaSessionCallback.onPause();
                    break;
                default:
                    mediaSessionCallback.onPause();
                    break;
            }
        }
    };

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        if (clientPackageName.contains(MY_MEDIA_ROOT_ID)) {
            return new BrowserRoot(getResources().getString(R.string.app_name), null);
        } else {
            return null;
        }
    }
    @Override
    public void onLoadChildren (@NonNull String parentId, @NonNull Result < List < MediaBrowserCompat.MediaItem >> result){
        result.sendResult(null);
    }

    private void setPlayerDataSource(Song song)  {
        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, song.getPath())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.getTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.getAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.getArtist())
                .build()
        );
        try {
            player.setDataSource(getApplicationContext(), Uri.parse(song.getPath()));
            player.prepareAsync();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    private boolean ifAudioFocusGranted() {
        int request = -1;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            request = audioManager.requestAudioFocus(audioFocusRequest);
        }
        return request == AudioManager.AUDIOFOCUS_GAIN;
    }

    private void setMediaPlaybackState(int state) {
        PlaybackStateCompat.Builder playbackstateBuilder = new PlaybackStateCompat.Builder();
        playbackState = state;
        switch(state) {
            case PlaybackStateCompat.STATE_STOPPED: {
                playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_STOP);
                break;
            }
            case PlaybackStateCompat.STATE_PLAYING: {
                playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY);
                break;
            }
            case PlaybackStateCompat.STATE_PAUSED:{
                playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PAUSE);
                break;
            }
        }
        playbackstateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0);
        mediaSession.setPlaybackState(playbackstateBuilder.build());
    }

    private void showPlayingNotification(){

        NotificationCompat.Builder builder = MediaStyleHelper.from(this, mediaSession);

        builder.addAction(
                new NotificationCompat.Action(android.R.drawable.ic_media_pause,
                        getResources().getString(R.string.pause),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(MusicPlayerService.this,  PlaybackStateCompat.ACTION_PAUSE))
                );
    NotificationManagerCompat.from(this).notify(NOTIFY_ID, builder.build());
    }

    private void showPausedNotification() {
        NotificationCompat.Builder builder = MediaStyleHelper.from(this, mediaSession);
        builder.addAction(
                new NotificationCompat.Action(
                        android.R.drawable.ic_media_play,
                        getResources().getString(R.string.play),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(MusicPlayerService.this,  PlaybackStateCompat.ACTION_PLAY))
                );
        NotificationManagerCompat.from(this).notify(NOTIFY_ID, builder.build());
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        player.start();
    }
}
