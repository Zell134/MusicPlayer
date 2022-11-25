package com.zell.musicplayer.util;

import android.media.MediaPlayer;

import com.zell.musicplayer.models.Player;

public class PlayerHelper {

    private static MediaPlayer player = Player.getInstance().getPlayer();


    public static boolean isPlaying() {
        return player.isPlaying();
    }
}
