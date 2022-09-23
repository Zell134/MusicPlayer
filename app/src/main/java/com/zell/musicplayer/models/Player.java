package com.zell.musicplayer.models;

import android.media.MediaPlayer;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;


public class Player {

    private static Player INSTANCE;
    private MediaPlayer player;
    private Equalizer equalizer;
    private BassBoost bassBoost;

    private Player(){
        player = new MediaPlayer();
        int playerSession = player.getAudioSessionId();
        equalizer = new Equalizer(0,playerSession);
        bassBoost = new BassBoost(0, playerSession);
        equalizer.setEnabled(true);
        bassBoost.setEnabled(true);
    }

    public void destroy(){
        if(player!=null){
            player.release();
        }
        if(equalizer != null){
            equalizer.release();
        }
    }

    public synchronized static Player getInstance(){
        if(INSTANCE == null){
            INSTANCE = new Player();        }
        return INSTANCE;
    }

    public MediaPlayer getPlayer() {
        return player;
    }

    public Equalizer getEqualizer() {
        return equalizer;
    }

    public BassBoost getBassBoost(){
        return bassBoost;
    }

}
