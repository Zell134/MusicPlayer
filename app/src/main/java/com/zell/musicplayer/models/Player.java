package com.zell.musicplayer.models;

import android.media.MediaPlayer;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.NoiseSuppressor;

public class Player {

    private static Player INSTANCE;
    private MediaPlayer player;
    private Equalizer equalizer;
    private short currentPreset = 0;
    private BassBoost bassBoost;
    private short currentBassBoostStrength = 0;
    private AcousticEchoCanceler echoCanceler;
    private NoiseSuppressor noiseSuppressor;

    private Player(){
        player = new MediaPlayer();
        int playerSession = player.getAudioSessionId();
        equalizer = new Equalizer(0,playerSession);
        bassBoost = new BassBoost(0, playerSession);
        equalizer.setEnabled(true);
        bassBoost.setEnabled(true);
        echoCanceler = AcousticEchoCanceler.create(playerSession);
        noiseSuppressor = NoiseSuppressor.create(playerSession);
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
            INSTANCE = new Player();
        }
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

    public short getCurrentPreset(){
        return currentPreset;
    }

    public void setCurrentPreset(short preset){
        currentPreset = preset;
    }

    public short getCurrentBassBoostStrength() {
        return currentBassBoostStrength;
    }

    public void setCurrentBassBoostStrength(short currentBassBoostStrength) {
        this.currentBassBoostStrength = currentBassBoostStrength;
    }

    public AcousticEchoCanceler getEchoCanceler() {
        return echoCanceler;
    }

    public NoiseSuppressor getNoiseSuppressor() {
        return noiseSuppressor;
    }
}
