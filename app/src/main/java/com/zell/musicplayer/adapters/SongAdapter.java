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

    private final Playlist playlist = new Playlist();
    private SongAdapter.Listener listener;
    private LibraryType libraryType;

    public interface Listener {
        void playSong(Song song);
    }

    @SuppressLint("NotifyDataSetChanged")
    public SongAdapter(Context context, List<Item> playlist, LibraryType libraryType) {
        setPlaylist(playlist);
        this.libraryType = libraryType;
        listener = (SongAdapter.Listener) context;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setPlaylist(List<Item> playlist) {
        this.playlist.setPlaylist(playlist);
        this.playlist.setPreviousSongPosition(0);
        this.playlist.setCurrentSongPosition(0);
        notifyDataSetChanged();
    }

    public void serCurrentSongPosition(int position) {
        playlist.setCurrentSongPosition(position);
    }

    private void setSelectedPosition(int position) {
        notifyItemChanged(playlist.getCurrentSongPosition());
        playlist.setCurrentSongPosition(position);
        notifyItemChanged(playlist.getCurrentSongPosition());
    }

    public void play() {
        Song song = playlist.getCurrentSong();
        if (song != null) {
            listener.playSong(song);
        }
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
        holder.bind(playlist.getItemAtPosition(position));
        holder.itemView.setBackgroundResource(playlist.getCurrentSongPosition() == position ? R.color.selected_item : R.color.default_list_color);
    }

    @Override
    public int getItemCount() {
        return playlist.getPlaylist().size();
    }

    public class PlaylistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        public void bind(Item song) {
            ImageView icon = itemView.findViewById(R.id.content_icon);
            TextView songView = itemView.findViewById(R.id.song_title);
            TextView artistView = itemView.findViewById(R.id.song_artist);

            songView.setText(song.getTitle());
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
            if (getBindingAdapterPosition() == RecyclerView.NO_POSITION) return;
            int position = getBindingAdapterPosition();
            Item item = playlist.getItemAtPosition(position);
            if (item.isAudioFile()) {
                setSelectedPosition(position);
                play();
            } else {
                playlist.setCurrentSongPosition(position);
                onItemSelect();
            }
        }
    }

    public void onBackPressed() {
        if (libraryType != LibraryType.LIBRARY_TYPE_MEDIA_LIBRARY) {
            if (playlist.getItemAtPosition(0).getTitle().equals(((Context) listener).getResources().getString(R.string.previous_directory))) {
                playlist.setCurrentSongPosition(0);
                onItemSelect();
            }
        }
    }

    public void onItemSelect() {
        Item item = playlist.getCurrentItem();
        Context context = (Context) listener;
        List<Item> playlist = new ArrayList<>();
        switch (libraryType) {
            case LIBRARY_TYPE_EXTERNAL_STORAGE: {
                if (item.getTitle().equals(context.getResources().getString(R.string.previous_directory))) {
                    String filePath = item.getPath();
                    playlist = MediaLibraryService.getFilesList(context, new File(filePath.substring(0, filePath.lastIndexOf("/"))));
                } else {
                    playlist = MediaLibraryService.getFilesList(context, new File(item.getPath()));
                }
                break;
            }
            case LIBRARY_TYPE_ARTISTS: {
                if (item.getTitle().equals(context.getResources().getString(R.string.previous_directory))) {
                    if (item.getPath().equals("root")) {
                        playlist = MediaLibraryService.getArtistList(context);
                    } else {
                        playlist = MediaLibraryService.getAlbumsOfArtist(context, item.getPath());
                    }
                } else {
                    if (!item.getPath().equals("root")) {
                        playlist = MediaLibraryService.getSongsOfAlbum(context, item.getTitle(), item.getPath());
                    } else {
                        playlist = MediaLibraryService.getAlbumsOfArtist(context, item.getTitle());
                    }
                }
                break;
            }
        }
        setPlaylist(playlist);
    }

    public void playNextSong() {
        int newPosition = playlist.getNextSongPosition();
        if (newPosition > 0) {
            setSelectedPosition(newPosition);
            scrollToPosition(newPosition);
        }
        play();
    }

    public void playPreviousSong() {
        int newPosition = playlist.getPreviousSongPosition();
        if (newPosition > 0) {
            setSelectedPosition(newPosition);
            scrollToPosition(newPosition);
        }
        play();
    }

    public void scrollToPosition(int position) {
        RecyclerView list = ((AppCompatActivity) listener).findViewById(R.id.playlist);
        LinearLayoutManager layoutManager = (LinearLayoutManager) list.getLayoutManager();
        layoutManager.scrollToPosition(position);
    }
}
