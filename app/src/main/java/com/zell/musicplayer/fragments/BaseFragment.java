package com.zell.musicplayer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.ListFragment;

import com.zell.musicplayer.R;
import com.zell.musicplayer.adapters.SongAdapter;
import com.zell.musicplayer.models.Item;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseFragment  extends ListFragment {

    protected abstract void updatePlaylist();
    public abstract void onBackPressed();

    protected Listener listener;

    public interface Listener{
        void setPlaylist(List<Item> playlist);
        void setCurrentSongPosition(int currentSong);
        void playSong();
    }

    protected Context context;
    protected List<Item> playlist = new ArrayList<>();
    protected SongAdapter adapter;
    private LinearLayout currentLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = inflater.getContext();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (Listener) context;
    }

    @Override
    public void onStart() {
        super.onStart();
        updatePlaylist();
    }

    protected void updateAdapter() {
        adapter = new SongAdapter(context, playlist);
        adapter.notifyDataSetChanged();
        setListAdapter(adapter);
    }

    public ListView getPlaylist(){
        return getActivity().findViewById(android.R.id.list);
    }

    public void currentSongHighlight(int positiion){
        ListView listView = getActivity().findViewById(android.R.id.list);
        LinearLayout layout = (LinearLayout) getViewByPosition(positiion, listView);

        if(currentLayout!=null){
            currentLayout.setBackgroundResource(R.color.default_list_color);
        }
        layout.setBackgroundResource(R.color.selected_item);
        currentLayout = layout;
    }

    private View getViewByPosition(int position, ListView list) {
        int firstListItemPosition = list.getFirstVisiblePosition();
        int lastListItemPosition = firstListItemPosition + list.getChildCount() - 1;
        int childIndex = position - firstListItemPosition;
        if (position < firstListItemPosition || position > lastListItemPosition ) {
            return list.getAdapter().getView(position, null, list);
        } else {
            return list.getChildAt(childIndex);
        }
    }

    public void setPlaylist(List<Item> playlist) {
        this.playlist = playlist;
        updateAdapter();
    }
}
