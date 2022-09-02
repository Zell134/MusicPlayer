package com.zell.musicplayer.Services;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
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
import com.zell.musicplayer.models.Player;
import com.zell.musicplayer.models.Song;

import java.io.IOException;
import java.util.List;

public class MusicPlayerService extends MediaBrowserServiceCompat implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener{

    private static final String MY_MEDIA_ROOT_ID = "com.zell.musicplayer";


    private MediaSessionCompat mediaSession;
    private NotificationCompat.Builder notificationBuilder;
    private AudioManager audioManager;
    private MediaPlayer player;
    private AudioFocusRequest audioFocusRequest;
    private PlaybackStateCompat.Builder playbackStateBuilder;
    private int playbackState;

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
        notificationBuilder = new NotificationService(this, mediaSession).getBuilder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mediaSession, intent);
        return super.onStartCommand(intent, flags, startId);
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
        mediaSession.setSessionActivity(PendingIntent.getActivity(this, 0, activityIntent, 0));

        playbackStateBuilder = new PlaybackStateCompat.Builder();
        playbackStateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY |
                PlaybackStateCompat.ACTION_PAUSE |
                PlaybackStateCompat.ACTION_PLAY_PAUSE|
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT|
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS|
                PlaybackStateCompat.ACTION_SEEK_TO|
                PlaybackStateCompat.ACTION_STOP
        );
        mediaSession.setPlaybackState(playbackStateBuilder.build());
        setMediaPlaybackState(PlaybackStateCompat.STATE_NONE);
        setSessionToken(mediaSession.getSessionToken());
    }

    private void initMediaPlayer() {
        if(player == null) {
            player = Player.getInstance().getPlayer();
            player.setOnPreparedListener(this);
            player.setOnCompletionListener(this);
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
        Player.getInstance().destroy();
        NotificationManagerCompat.from(this).cancel(NotificationService.ID);
        stopSelf();
    }

    MediaSessionCompat.Callback mediaSessionCallback = new MediaSessionCompat.Callback() {
        @Override
        public void onPlay() {
            play();
        }

        @Override
        public void onSkipToPrevious() {
            skipToPrevious();
        }

        @Override
        public void onSkipToNext() {
            skipToNext();
        }

        @Override
        public void onPlayFromUri(Uri uri, Bundle bundle) {
            playFromUri(uri, bundle);
        }

        @Override
        public void onPause() {
            pause();
        }

        @Override
        public void onStop() {
            stop();
        }

        @Override
        public void onSeekTo(long pos) {
            seekTo(pos);
        }
    };

    public void play(){
        if (ifAudioFocusGranted()) {
            if(playbackState == PlaybackStateCompat.STATE_PAUSED) {
                mediaSession.setActive(true);
                setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
                player.start();
                showPlayingNotification();
            }
        }
    }

    public void skipToPrevious() {
        stop();
        setMediaPlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS);
    }

    public void skipToNext() {
        stop();
        setMediaPlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT);
    }

    public void playFromUri(Uri uri, Bundle bundle) {
        if (ifAudioFocusGranted()) {
            player.reset();
            Song song = new Song(uri.getPath(),
                    bundle.getString(MainActivity.TITLE),
                    bundle.getString(MainActivity.ALBUM),
                    bundle.getString(MainActivity.ARTIST),
                    bundle.getLong(MainActivity.DURATION),
                    true 
            );
            setPlayerDataSource(song);
            mediaSession.setActive(true);
            setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
            showPlayingNotification();
        }
    }

    public void pause() {
        if(playbackState == PlaybackStateCompat.STATE_PLAYING) {
            setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
            player.pause();
            showPausedNotification();
        }
    }

    public void stop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(audioFocusRequest);
        }
        mediaSession.setActive(false);
        setMediaPlaybackState(PlaybackStateCompat.STATE_STOPPED);
        player.stop();
        NotificationManagerCompat.from(MusicPlayerService.this).cancel(NotificationService.ID);
    }

    public void seekTo(long pos){
        pause();
        player.seekTo((int) pos);
        play();
    }

    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
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
        if(song!=null) {
            MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
            metaRetriever.setDataSource(song.getPath());
            byte[] albumArt = metaRetriever.getEmbeddedPicture();
            MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();
            builder
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, song.getPath())
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.getTitle())
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.getAlbum())
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.getArtist())
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.getDuration());
            if (albumArt != null) {
                builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length));
            } else {
                builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, BitmapFactory.decodeResource(getResources(), R.drawable.empty_album_art));
            }


            mediaSession.setMetadata(builder.build());
            try {
                player.setDataSource(getApplicationContext(), Uri.parse(song.getPath()));
                player.prepareAsync();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
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
        playbackStateBuilder = new PlaybackStateCompat.Builder();
        playbackState = state;
        long actions = getAvailableActions(state);
        switch(state) {
            case PlaybackStateCompat.STATE_STOPPED:
                playbackStateBuilder.setActions(actions);
                playbackStateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1);
                mediaSession.setPlaybackState(playbackStateBuilder.build());
                break;
            case PlaybackStateCompat.STATE_PLAYING:
                playbackStateBuilder.setState(state, player.getCurrentPosition(), 1);
                playbackStateBuilder.setActions(actions);
                mediaSession.setPlaybackState(playbackStateBuilder.build());
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                playbackStateBuilder.setActions(actions);
                playbackStateBuilder.setState(state, player.getCurrentPosition(), 1);
                mediaSession.setPlaybackState(playbackStateBuilder.build());
                break;
            case PlaybackStateCompat.STATE_SKIPPING_TO_NEXT:
            case PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS:
                playbackStateBuilder.setState(state, player.getCurrentPosition(), 1);
                playbackStateBuilder.setActions(actions);
                mediaSession.setPlaybackState(playbackStateBuilder.build());
                break;
        }
    }

    private long getAvailableActions(int state){
        switch(state) {
            case PlaybackStateCompat.STATE_STOPPED:
                return PlaybackStateCompat.ACTION_PLAY_PAUSE |
                        PlaybackStateCompat.ACTION_PLAY |
                        PlaybackStateCompat.ACTION_PAUSE |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackStateCompat.ACTION_SEEK_TO |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
            case PlaybackStateCompat.STATE_PLAYING:
                return PlaybackStateCompat.ACTION_PLAY_PAUSE |
                        PlaybackStateCompat.ACTION_STOP |
                        PlaybackStateCompat.ACTION_PAUSE |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackStateCompat.ACTION_SEEK_TO |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
            case PlaybackStateCompat.STATE_PAUSED:
                return PlaybackStateCompat.ACTION_PLAY_PAUSE |
                        PlaybackStateCompat.ACTION_PLAY |
                        PlaybackStateCompat.ACTION_STOP |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackStateCompat.ACTION_SEEK_TO |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
        }
        return -1;
    }

    private void showPlayingNotification() {
        setMetadata();
        addNotificationAction();
        notificationBuilder
                .addAction(
                        new NotificationCompat.Action(android.R.drawable.ic_media_pause,
                                getResources().getString(R.string.pause),
                                MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PAUSE))
                )
                .setLargeIcon(
                        mediaSession
                                .getController()
                                .getMetadata()
                                .getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART)
                );
        NotificationManagerCompat.from(this).notify(NotificationService.ID, notificationBuilder.build());

    }

    private void showPausedNotification() {
        setMetadata();
        addNotificationAction();
        notificationBuilder.addAction(
                new NotificationCompat.Action(
                        android.R.drawable.ic_media_play,
                        getResources().getString(R.string.play),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY))
        );
        NotificationManagerCompat.from(this).notify(NotificationService.ID, notificationBuilder.build());
    }

    public void addNotificationAction(){
        notificationBuilder
                .clearActions()
                .addAction(
                        new NotificationCompat.Action(
                                android.R.drawable.ic_media_previous, getString(R.string.previous),
                                MediaButtonReceiver.buildMediaButtonPendingIntent(
                                        this,
                                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)))
                .addAction(
                        new NotificationCompat.Action(android.R.drawable.ic_media_next, getString(R.string.next),
                                MediaButtonReceiver.buildMediaButtonPendingIntent(
                                        this,
                                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT)));
    }

    public void setMetadata(){
        MediaControllerCompat controller = mediaSession.getController();
        MediaMetadataCompat mediaMetadata = controller.getMetadata();

        String title = mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
        String artist = mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
        String album = mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);

        notificationBuilder
                .setContentTitle(title)
                .setContentText(artist)
                .setSubText(artist + " - " + title + " (" + album + ")");
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        player.start();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        skipToNext();
    }
}
