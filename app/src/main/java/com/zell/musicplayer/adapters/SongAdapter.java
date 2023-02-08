package com.zell.musicplayer.adapters;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zell.musicplayer.R;
import com.zell.musicplayer.models.Item;
import com.zell.musicplayer.models.Song;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.PlaylistViewHolder> {

    private SongAdapter.Listener listener;
    private List<Item> playlist;
    private int foldersCount;

    public interface Listener {
        void itemSelected(int position);

        int getCurrentSongPosition();
    }

    @SuppressLint("NotifyDataSetChanged")
    public SongAdapter(Listener listener, List<Item> playlist) {
        setPlaylist(playlist);
        this.listener = listener;
    }

    public void clearListener() {
        if (listener != null) {
            listener = null;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setPlaylist(List<Item> playlist) {
        this.playlist = playlist;
        foldersCount = 0;
        playlist.forEach(item -> {
            if (!item.isAudioFile()) {
                foldersCount++;
            }
        });
        notifyDataSetChanged();
    }

    public List<Item> getPlaylist() {
        return playlist;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.song, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        holder.bind(playlist.get(position), position);
        if(listener.getCurrentSongPosition() == position){
            holder.itemView.setBackgroundResource(R.drawable.playlist_selected_item);
            ((TextView)holder.itemView.findViewById(R.id.song_title)).setTextColor(Color.BLACK);
            ((TextView)holder.itemView.findViewById(R.id.song_artist)).setTextColor(Color.BLACK);
        }else {
            holder.itemView.setBackgroundResource(R.drawable.playlist_item);
        }
    }

    @Override
    public int getItemCount() {
        return playlist.size();
    }

    public class PlaylistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        public void bind(Item item, int position) {
            ImageView icon = itemView.findViewById(R.id.content_icon);
            TextView songView = itemView.findViewById(R.id.song_title);
            TextView artistView = itemView.findViewById(R.id.song_artist);

            if (item.isAudioFile()) {
                songView.setText((position - foldersCount + 1) + ". " + item.getTitle());
                artistView.setText(((Song) item).getArtist());
                icon.setImageResource(R.drawable.music_icon);
            } else {
                songView.setText(item.getTitle());
                artistView.setText("");
                icon.setImageResource(R.drawable.folder_icon);
            }
        }

        @Override
        public void onClick(View view) {
            int position = getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return;
            listener.itemSelected(position);
        }
    }
}
