package com.zell.musicplayer.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zell.musicplayer.R;
import com.zell.musicplayer.activities.MainActivitySetup;
import com.zell.musicplayer.db.LibraryType;
import com.zell.musicplayer.helpers.FakeMediaLibraryService;
import com.zell.musicplayer.models.Item;
import com.zell.musicplayer.models.Song;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Random;


@RunWith(RobolectricTestRunner.class)
public class PlaylistServiceTest extends MainActivitySetup {

    private PlaylistService playlistService;
    private FakeMediaLibraryService mediaLibraryService;
    private int currentSongPosition;
    private List<Item> playlist;

    @Before
    public void setupMediaLibraryService(){
        mediaLibraryService = new FakeMediaLibraryService(activity);
    }

    private void setupPlaylistService(LibraryType libraryType){
        String path = mediaLibraryService.getRandomSong().getPath();

        playlistService = new PlaylistService(activity,
                libraryType,
                path,
                mediaLibraryService
        );

        playlist = playlistService.getPlaylist();
        currentSongPosition = mediaLibraryService.findSongPosition(activity, path, libraryType);
    }

    @Test
    public void nextSongIsSelectedAndPlaying_WhenNextSongMethodInvoked_InTheCenterOfPlaylist(){
        Arrays.stream(LibraryType.values()).forEach(lType ->{
            setupPlaylistService(lType);
            playlistService = spy(playlistService);
            int foldersCount = getFoldersCountOfPlaylist();
            int position = (playlist.size() - foldersCount) / 2 + foldersCount;
            playlistService.itemSelected(position);
            assertThat(playlistService.getCurrentSongPosition()).isEqualTo(position);
            verify(playlistService).play();
            playlistService.playNextSong();
            if(position < playlist.size()) {
                assertThat(playlistService.getCurrentSongPosition()).isEqualTo(position + 1);
                verify(playlistService, times(2)).play();
            }
        });
    }

    @Test
    public void previousSongIsSelectedAndPlaying_WhenPreviousSongMethodInvoked_InTheCenterOfPlaylist() {
        Arrays.stream(LibraryType.values()).forEach(lType -> {
            setupPlaylistService(lType);
            playlistService = spy(playlistService);
            int foldersCount = getFoldersCountOfPlaylist();
            int position = (playlist.size() - foldersCount) / 2 + foldersCount;
            playlistService.itemSelected(position);
            assertThat(playlistService.getCurrentSongPosition()).isEqualTo(position);
            verify(playlistService).play();
            playlistService.playPreviousSong();
            if (position < playlist.size()) {
                assertThat(playlistService.getCurrentSongPosition()).isEqualTo(position - 1);
                verify(playlistService, times(2)).play();
            }
        });
    }

    @Test
    public void lastSongIsSelectedAndPlaying_WhenPreviousSongMethodInvoked_InTheStartOfPlaylist() {
        Arrays.stream(LibraryType.values()).forEach(lType -> {
            setupPlaylistService(lType);
            playlistService = spy(playlistService);
            int foldersCount = getFoldersCountOfPlaylist();
            playlistService.itemSelected(foldersCount);
            assertThat(playlistService.getCurrentSongPosition()).isEqualTo(foldersCount);
            verify(playlistService).play();
            playlistService.playPreviousSong();
            assertThat(playlistService.getCurrentSongPosition()).isEqualTo(playlist.size() - 1);
            verify(playlistService, times(2)).play();

        });
    }

    @Test
    public void savedSongShoudlBeSelected_OnStartApplication() {
        Arrays.stream(LibraryType.values()).forEach(lType ->{
            setupPlaylistService(lType);
            assertThat(playlistService.getCurrentSongPosition()).isEqualTo(currentSongPosition);
        });
    }

    @Test
    public void playlistViewShouldFillsCorrectly_OnStartApplication() {
        Arrays.stream(LibraryType.values()).forEach(lType ->{
            setupPlaylistService(lType);
            verificationOFEqualsOfPlaylistAndListView();
        });
    }

    @Test
    public void selectedSongShouldPlay_OnThisOneIsChoosedPlaylist(){
        for(LibraryType lType :LibraryType.values()) {
            setupPlaylistService(lType);
            playlistService = spy(playlistService);
            int counter = 1;
            int size = playlist.size();
            for (int i = 0; i < size; i++) {
                if (playlist.get(i).isAudioFile()) {
                    playlistService.itemSelected(i);
                    verify(playlistService, times(counter)).play();
                    counter ++;
                    assertThat(playlistService.getCurrentSongPosition()).isEqualTo(i);
                }
            }
        }
    }

    private void verificationOFEqualsOfPlaylistAndListView(){
        RecyclerView listView = activity.findViewById(R.id.playlist);
        assertThat(listView).isNotNull();
        LinearLayoutManager layoutManager = (LinearLayoutManager) listView.getLayoutManager();
        listView.measure(0,0);
        int playlistSize = playlist.size();
        assertThat(listView.getAdapter().getItemCount()).isEqualTo(playlistSize);
        for(int i = 0; i < playlistSize ; i++){
            layoutManager.scrollToPosition(i);
            TextView songTitle = listView.getChildAt(i).findViewById(R.id.song_title);
            TextView songArtist = listView.getChildAt(i).findViewById(R.id.song_artist);
            Item currentItem = playlist.get(i);
            assertThat(songTitle.getText()).contains(currentItem.getTitle());
            if(currentItem.isAudioFile()){
                assertThat(songArtist.getText()).contains(((Song)currentItem).getArtist());
            }else{
                assertThat(songArtist.getText()).isBlank();
            }
        }
    }

    private int getFoldersCountOfPlaylist(){
        for(int i = 0; i < playlist.size(); i++){
            if(playlist.get(i).isAudioFile()){
                return i;
            }
        }
        return  -1;
    }
}
