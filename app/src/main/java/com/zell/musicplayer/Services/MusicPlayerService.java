package com.zell.musicplayer.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.zell.musicplayer.MainActivity;
import com.zell.musicplayer.R;
import com.zell.musicplayer.models.Song;

import java.util.List;

public class MusicPlayerService extends Service implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener
{
    private static final String NOTIFY_ID="com.zell.musicplayer.Services";

    private MediaPlayer player;
    private List<Song> playlist;
    private int songPosition;

    private final IBinder musicBind = new MusicBinder();

    public void setSongPosition(int songPosition){
        this.songPosition = songPosition;
    }

    public void playSong() {
        player.reset();
        try{
            player.setDataSource(getApplicationContext(), Uri.parse(playlist.get(songPosition).getPath()));
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();
    }

    public class MusicBinder extends Binder {
        public MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        songPosition=0;
        player = new MediaPlayer();
        initMusicPlayer();
    }

    public void initMusicPlayer(){
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setPlaylist(List<Song> playlist){
        this.playlist = playlist;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();

        NotificationChannel notificationChannel;

        PendingIntent pendInt = PendingIntent.getActivity(this,
                0,
                new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationChannel = new NotificationChannel(String.valueOf(NOTIFY_ID), "permission_denied", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), String.valueOf(NOTIFY_ID));
        Song song = playlist.get(songPosition);
         builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.play)
                .setTicker(song.toString())
                .setOngoing(true)
                .setContentTitle(getResources().getString(R.string.playing))
                .setContentText(song.toString());
        Notification notification = builder.build();

        startForeground(1, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    public int getPosition(){
        return player.getCurrentPosition();
    }

    public int getDuration(){
        return player.getDuration();
    }

    public boolean isPlaying(){
        return player.isPlaying();
    }

    public void pausePlayer(){
        player.pause();
    }

    public void seek(int position){
        player.seekTo(position);
    }

    public void start(){
        player.start();
    }

    public void playPrev(){
        songPosition--;
        if(songPosition < 0)
            songPosition=playlist.size()-1;
        playSong();
    }

    public void playNext(){
        songPosition++;
        if(songPosition>=playlist.size()) songPosition=0;
        playSong();
    }

}
