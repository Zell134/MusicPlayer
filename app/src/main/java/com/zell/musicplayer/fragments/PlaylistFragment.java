package com.zell.musicplayer.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zell.musicplayer.R;
import com.zell.musicplayer.activities.MainActivity;
import com.zell.musicplayer.adapters.SongAdapter;
import com.zell.musicplayer.models.Item;
import com.zell.musicplayer.services.PlaylistService;
import com.zell.musicplayer.viewModels.PlaylistViewModel;

import java.util.List;

public class PlaylistFragment extends Fragment implements SongAdapter.Listener, MainActivity.PlayListViewListener {


    private PlaylistService playlistService;
    private RecyclerView playlistView;
    private SongAdapter adapter;

    @Override
    public void setSelectedPosition(int oldPosition, int newPosition) {
        adapter.notifyItemChanged(oldPosition);
        adapter.notifyItemChanged(newPosition);
        scrollToPosition(newPosition);
    }

    @Override
    public void updateAdapter(List<Item> playlist) {
        new Handler().post(() -> {
            if (playlistService == null) {
                PlaylistViewModel playlistViewModel = new ViewModelProvider(getActivity()).get(PlaylistViewModel.class);
                playlistService = playlistViewModel.getPlaylistService();
            }
            adapter = new SongAdapter(this, playlistService.getPlaylist());
            adapter.setPlaylist(playlist);
            playlistView.setAdapter(adapter);
            getView().findViewById(R.id.progressBar).setVisibility(View.GONE);
            playlistView.setAdapter(adapter);
            scrollToPosition(playlistService.getCurrentSongPosition());
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playlist, container, false);
    }

    @Override
    public void onResume() {

        playlistView = getView().findViewById(R.id.playlist);
        playlistView.setLayoutManager(new LinearLayoutManager(getContext()));

        super.onResume();
    }

    @Override
    public int getCurrentSongPosition() {
        return playlistService.getCurrentSongPosition();
    }

    @Override
    public void onDestroy() {
        if (adapter != null) {
            adapter.clearListener();
        }
        super.onDestroy();
    }

    @Override
    public void itemSelected(int position) {
        playlistService.itemSelected(position);
    }

    public void scrollToPosition(int position) {
        List<Item> playlist = playlistService.getPlaylist();
        LinearLayoutManager layoutManager = (LinearLayoutManager) playlistView.getLayoutManager();
        int firstVisibleView = layoutManager.findFirstVisibleItemPosition();
        int lastVisibleView = layoutManager.findLastVisibleItemPosition();
        int centerVisibleList = (lastVisibleView - firstVisibleView) / 2;
        int playlistSize = playlist.size();
        if (position >= playlistSize - centerVisibleList) {
            layoutManager.scrollToPosition(playlistSize - 1);
            return;
        }
        if (position <= centerVisibleList) {
            layoutManager.scrollToPosition(0);
            return;
        }
        if (position < firstVisibleView + 1) {
            layoutManager.scrollToPosition(position - centerVisibleList);
        } else {
            layoutManager.scrollToPosition(position + centerVisibleList);
        }
    }
}