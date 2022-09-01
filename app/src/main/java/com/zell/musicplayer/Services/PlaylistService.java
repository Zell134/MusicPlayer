package com.zell.musicplayer.Services;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zell.musicplayer.R;
import com.zell.musicplayer.activities.MainActivity;
import com.zell.musicplayer.adapters.SongAdapter;
import com.zell.musicplayer.db.LibraryType;
import com.zell.musicplayer.models.Item;
import com.zell.musicplayer.models.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlaylistService implements SongAdapter.Listener{

    private RecyclerView listView;
    private final Context context;
    private SongAdapter adapter;
    private LibraryType libraryType;
    private boolean isFirstStart = true;
    private List<Item> playlist = new ArrayList<>();
    private int currentSongPosition;

    public PlaylistService(Context context, LibraryType libraryType, String songPath) {
        this.context = context;
        this.libraryType = libraryType;
        setup(songPath);
    }

    private void setup(String songPath){
        setPlaylist(getPlaylistBySongPathAndLibraryType(songPath));
        adapter = new SongAdapter(this, playlist);
        setAdapter();

        int position = getItemIndexFromPlaylist(playlist, songPath);

        if(position >= 0) {
            currentSongPosition = position;
            scrollToPosition(position - 2);
            if(isFirstStart) {
                play();
                isFirstStart = false;
            }
        }else{
            currentSongPosition = 0;
        }
    }

    public void setAdapter(){
        listView = ((AppCompatActivity)context).findViewById(R.id.playlist);
        listView.setLayoutManager(new LinearLayoutManager(context));
        listView.setAdapter(adapter);
    }

    private void setPlaylist(List<Item> playlist){
        this.playlist = playlist;
        if(this.playlist.get(0).getTitle().equals(context.getResources().getString(R.string.previous_directory))&&
                playlist.size() > 1)
        {
            this.currentSongPosition = 1;
        }else {
            this.currentSongPosition = 0;
        }
    }

    public void setLibraryType(LibraryType libraryType) {
        this.libraryType = libraryType;
        setPlaylist(getPlaylistBySongPathAndLibraryType(null));
        adapter.setPlaylist(playlist);
        ((MainActivity)context).stopPlaying();
    }

    @SuppressLint("NewApi")
    public List<Item> getPlaylistBySongPathAndLibraryType(String songPath){

        List<Item> playlist = new ArrayList<>();

        switch (libraryType) {
            case LIBRARY_TYPE_EXTERNAL_STORAGE:
                if(songPath!=null && new File(songPath).exists()) {
                    playlist = MediaLibraryService.getFilesList(context, new File(songPath.substring(0, songPath.lastIndexOf("/"))));
                }else{
                    playlist = MediaLibraryService.getFilesList(context, new File(Environment.getStorageDirectory().getAbsolutePath()));
                }
                break;
            case LIBRARY_TYPE_MEDIA_LIBRARY:
                playlist = MediaLibraryService.getAllMedia(context);
                break;
            case LIBRARY_TYPE_ARTISTS:
                if(songPath!=null && new File(songPath).exists()) {
                    Song song = MediaLibraryService.getSongByPath(context, songPath);
                    playlist = MediaLibraryService.getSongsOfAlbum(context, song.getAlbum(), song.getArtist());
                }else{
                    playlist = MediaLibraryService.getArtistList(context);
                }
                break;
        }
        return playlist;
    }

    private List<Item> getPlayListByFolderPath(String path){
        List<Item> playlist = new ArrayList<>();
        if(path!=null && new File(path).exists()) {
            if (libraryType == LibraryType.LIBRARY_TYPE_EXTERNAL_STORAGE){
                playlist = MediaLibraryService.getFilesList(context, new File(path));
            }
        }
        return playlist;
    }

    private int getItemIndexFromPlaylist(List <Item> playlist, String songPath){
        for (int i = 0; i <playlist.size(); i++){
            if(playlist.get(i).getPath().equals(songPath)){
                return i;
            }
        }
        return -1;
    }

    public void play() {
        Song song = getCurrentSong();
        if (song != null) {
            ((MainActivity)context).playSong(song);
        }
    }

    private Song getCurrentSong(){
        Item item = playlist.get(currentSongPosition);
        if(item.isAudioFile()){
            return (Song) item;
        }
        return null;
    }

    private Item getCurrentItem() {
        return playlist.get(currentSongPosition);
    }

    @Override
    public int getCurrentSongPosition(){
        return currentSongPosition;
    }

    private int getPreviousSongPosition(){
        int current = currentSongPosition;
        if(playlist!= null) {
            int i = 0;
            while (true) {
                current--;
                if (current < 0) {
                    current = playlist.size() - 1;
                }
                Item item = playlist.get(current);
                if (item.isAudioFile()) {
                    return current;
                }
                i++;
                if (i >= playlist.size()) {
                    break;
                }
            }
        }
        return -1;
    }

    private int findItemIndexByPath(String songPath){
        for(int i = 0; i<playlist.size(); i++){
            if(playlist.get(i).getPath().equals(songPath)){
                return i;
            }
        }
        return -1;
    }

    private int findItemIndexByTitle(String title){
        for(int i = 0; i<playlist.size(); i++){
            if(playlist.get(i).getTitle().equals(title)){
                return i;
            }
        }
        return -1;
    }

    @Override
    public void itemSelected(int position) {
        Item item = playlist.get(position);
        int oldPosition = currentSongPosition;
        if (item.isAudioFile()) {
            currentSongPosition = position;
            adapter.setSelectedPosition(oldPosition, position);
            play();
        } else {
            currentSongPosition = position;
            onItemSelect();
            adapter.setPlaylist(playlist);
        }
    }

    public void onItemSelect() {
        Item item = getCurrentItem();
        List<Item> playlist = new ArrayList<>();
        switch (libraryType) {
            case LIBRARY_TYPE_EXTERNAL_STORAGE: {
                if (item.getTitle().equals(context.getResources().getString(R.string.previous_directory))) {
                    String filePath = item.getPath();
                    playlist = MediaLibraryService.getFilesList(context, new File(getSongFolder(filePath)));
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

    public void onBackPressed() {
        getPreviousDirectory();
        adapter.setPlaylist(playlist);
    }

    public void playPreviousSong() {
        int oldPosition = currentSongPosition;
        int newPosition = getPreviousSongPosition();
        setNewPositionAndPlay(oldPosition, newPosition);
    }

    private void setNewPositionAndPlay(int oldPosition, int newPosition){
        if (newPosition >= 0) {
            currentSongPosition = newPosition;
            adapter.setSelectedPosition(oldPosition, newPosition);
            scrollToPosition(newPosition);
        }
        play();
    }

    public void scrollToPosition(int position) {
        if(listView != null) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) listView.getLayoutManager();
            layoutManager.scrollToPosition(position);
        }
    }

    public void scrollToCurrentPosition(){
        scrollToPosition(currentSongPosition);
    }

    public void playNextSong() {
        switch (libraryType){
            case LIBRARY_TYPE_EXTERNAL_STORAGE: {
                playNextSongOnExternalStorageLibrary();
                break;
            }
            case LIBRARY_TYPE_ARTISTS: {
                playNextSongOnArtistTypeLibrary();
                break;
            }
            default: {
                int oldPosition = currentSongPosition;
                int newPosition;
                if (oldPosition < playlist.size() - 1) {
                    newPosition = oldPosition + 1;
                }else{
                    newPosition = 0;
                }
                setNewPositionAndPlay(oldPosition, newPosition);
            }
        }
    }

    private void playNextSongOnArtistTypeLibrary(){
        int oldPosition = currentSongPosition;
        if (oldPosition < playlist.size() - 1) {
            int newPosition = oldPosition + 1;
            setNewPositionAndPlay(oldPosition, newPosition);
        }else {
            getPreviousDirectory();
            if (playlist.size() <= 2 || currentSongPosition == playlist.size() - 1) {
                getPreviousDirectory();
            }
            if(currentSongPosition == playlist.size() - 1){
                currentSongPosition = 0;
                onItemSelect();
                onItemSelect();
                int position = currentSongPosition;
                setAdapterAndPlay(position);
            }else {
                int position = currentSongPosition;
                position++;
                if (position < playlist.size()) {
                    currentSongPosition = position;
                    onItemSelect();
                    Item currentItem = getCurrentItem();
                    if (!currentItem.isAudioFile()) {
                        onItemSelect();
                    }
                    position = currentSongPosition;
                    setAdapterAndPlay(position);
                }
            }
        }
    }

    private void playNextSongOnExternalStorageLibrary(){
        int oldPosition = currentSongPosition;
        if (oldPosition < playlist.size() - 1) {
            int newPosition = oldPosition + 1;
            Item currentItem = playlist.get(newPosition);
            if (currentItem.isAudioFile()) {
                setNewPositionAndPlay(oldPosition, newPosition);
                return;
            }else {
                goInToFolder(currentItem.getPath());
                if (getCurrentItem().isAudioFile()) {
                    setAdapterAndPlay(currentSongPosition);
                } else {
                    playNextSong();
                }
            }
        }else {
            if (getPreviousDirectory()) {
                int position = currentSongPosition;
                if(position < playlist.size() - 1){
                    position ++;
                }
                Item currentItem = playlist.get(position);
                if(currentItem.isAudioFile()){
                    setAdapterAndPlay(position);
                }else {
                    getNextFolder();
                    if (playlist.size() == 1) {
                        getPreviousDirectory();
                        playNextSong();
                    } else if (getCurrentItem().isAudioFile()) {
                        setAdapterAndPlay(currentSongPosition);
                    } else {
                        getNextFolder();
                        if(getCurrentItem().isAudioFile()){
                            position = currentSongPosition;
                            setAdapterAndPlay(position);
                        }else {
                            playNextSong();
                        }
                    }
                }
            }else{
                playlist = adapter.getPlaylist();
            }
        }
    }

    private void setAdapterAndPlay(int newPosition){
        adapter = new SongAdapter(this, playlist);
        setAdapter();
        setNewPositionAndPlay(newPosition, newPosition);
    }

    private String getSongFolder(String filePath){
        return filePath.substring(0, filePath.lastIndexOf("/"));
    }

    public boolean getPreviousDirectory(){
        Item currentItem = getCurrentItem();

        if (libraryType == LibraryType.LIBRARY_TYPE_EXTERNAL_STORAGE) {
            String folderPath = playlist.get(0).getPath();

            File folder = new File(folderPath);
            if(folder.exists() && folder.canRead()) {
                currentSongPosition = 0;
                if(getCurrentItem().getTitle().equals(context.getResources().getString(R.string.previous_directory)))
                {
                    onItemSelect();
                    if(!playlist.get(0).getTitle().equals(context.getResources().getString(R.string.previous_directory))){
                        return false;
                    }
                    if(playlist.size() == 1){
                        getPreviousDirectory();
                    }
                    if(currentItem.getTitle().equals(context.getResources().getString(R.string.previous_directory))){
                        currentSongPosition = findItemIndexByPath(currentItem.getPath());
                    }else {
                        currentSongPosition = findItemIndexByPath(getSongFolder(currentItem.getPath()));
                    }
                }else {
                    return false;
                }
            }
        }else if(libraryType == LibraryType.LIBRARY_TYPE_ARTISTS){
            Item rootFolder = playlist.get(0);
            if(rootFolder.getTitle().equals(context.getResources().getString(R.string.previous_directory))) {
                currentSongPosition = 0;
                onItemSelect();
                if(currentItem.isAudioFile()) {
                    currentSongPosition = findItemIndexByTitle(((Song) currentItem).getAlbum());
                }else{
                    currentSongPosition = findItemIndexByTitle(currentItem.getPath());
                }
            }
        }
        return true;
    }

    private void getNextFolder(){
        int position = currentSongPosition;
        if(position < playlist.size() - 1) {
            position++;
            int playlistSize = playlist.size();
            if (position < playlistSize) {
                goInToFolder(playlist.get(position).getPath());
            }
        }else{
            getPreviousDirectory();
        }
    }

    private void goInToFolder(String path){
        if (libraryType == LibraryType.LIBRARY_TYPE_EXTERNAL_STORAGE) {
            List<Item> list = getPlayListByFolderPath(path);
            setPlaylist(list);
            if (playlist.size() == 1) {
                return;
            }
            Item curentItem = getCurrentItem();
            if (!curentItem.isAudioFile()) {
                goInToFolder(curentItem.getPath());
            }
        }
    }
}
