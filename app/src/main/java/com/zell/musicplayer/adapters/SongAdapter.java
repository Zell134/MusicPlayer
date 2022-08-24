package com.zell.musicplayer.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zell.musicplayer.R;
import com.zell.musicplayer.Services.MediaLibraryService;
import com.zell.musicplayer.db.LibraryType;
import com.zell.musicplayer.models.Item;
import com.zell.musicplayer.models.Playlist;
import com.zell.musicplayer.models.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.PlaylistViewHolder> {

    private SongAdapter.Listener listener;
    private List<Item> playlist;

    public interface Listener {
        void itemSelected(int position);
        int getCurrentSongPosition();
    }

    @SuppressLint("NotifyDataSetChanged")
    public SongAdapter(Listener listener, List<Item> playlist) {
        setPlaylist(playlist);
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setPlaylist(List<Item> playlist) {
        this.playlist = playlist;
        notifyDataSetChanged();
    }

    public void setSelectedPosition(int oldPosition, int newPosition) {
        notifyItemChanged(oldPosition);
        notifyItemChanged(newPosition);
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
        holder.itemView.setBackgroundResource(listener.getCurrentSongPosition() == position ? R.color.selected_item : R.color.default_list_color);
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

        public void bind(Item song, int position) {
            ImageView icon = itemView.findViewById(R.id.content_icon);
            TextView songView = itemView.findViewById(R.id.song_title);
            TextView artistView = itemView.findViewById(R.id.song_artist);

            if(position == 0){
                songView.setText(song.getTitle());
            }else {
                songView.setText(String.valueOf(position) + ". " + song.getTitle());
            }
            if (song.isAudioFile()) {
                artistView.setText(((Song) song).getArtist());
                icon.setImageResource(R.drawable.music_icon);
            } else {
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
