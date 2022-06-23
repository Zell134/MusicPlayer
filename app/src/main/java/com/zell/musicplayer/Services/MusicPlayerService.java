package com.zell.musicplayer.Services;

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

import com.zell.musicplayer.models.Song;

import java.util.List;

public class MusicPlayerService extends Service implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener
{

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
        player.setWakeMode(getApplicationContext(),
        PowerManager.PARTIAL_WAKE_LOCK);
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
