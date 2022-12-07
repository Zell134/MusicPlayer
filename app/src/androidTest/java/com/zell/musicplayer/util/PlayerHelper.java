package com.zell.musicplayer.util;

import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;

import com.zell.musicplayer.models.Player;

public class PlayerHelper {

    private static MediaPlayer player = Player.getInstance().getPlayer();
    private static Equalizer equalizer = Player.getInstance().getEqualizer();

    public static boolean isPaused() {
        return Player.getInstance().isPaused();
    }

    public static boolean isPlaying(){
        return !isStopped() && !isPaused();
    }

    public static boolean isStopped() {
        return player.isPlaying();
    }

    public static int getCurrentPosition() {
        return player.getCurrentPosition();
    }

    public static short getEqualizerPresetsCount(){
        return equalizer.getNumberOfPresets();
    }

    public static String getEqualizerPresetNameAtPosition(short position){
        return equalizer.getPresetName(position);
    }

    public static short getNumberOfEqualizerBands(){
        return equalizer.getNumberOfBands();
    }
}
