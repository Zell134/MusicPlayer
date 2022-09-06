package com.zell.musicplayer.viewModels;

import androidx.lifecycle.ViewModel;
import com.zell.musicplayer.Services.PlaylistService;

public class PlaylistViewModel extends ViewModel {

    private PlaylistService playlistService;

    public PlaylistService getPlaylistService() {
        return playlistService;
    }

    public void setPlaylistService(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }
}
