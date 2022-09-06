package com.zell.musicplayer;

import com.zell.musicplayer.models.Folder;
import com.zell.musicplayer.models.Item;
import com.zell.musicplayer.models.Song;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class PlaylistTest {

    private List<Item> list;
    private Playlist playlist;

    @BeforeEach
    public void setup(){
        list = new ArrayList<>();
        list.add(new Folder("foderPath1", "FolderTitle1"));
        list.add(new Folder("foderPath2", "FolderTitle2"));
        list.add(new Song("SongPath1", "SongTitle1", "SongAlbum1", "SongArtist1", 1, true));
        list.add(new Song("SongPath2", "SongTitle2", "SongAlbum2", "SongArtist2", 1, true));
        list.add(new Song("SongPath3", "SongTitle3", "SongAlbum3", "SongArtist3", 1, true));
        playlist = new Playlist();
        playlist.setPlaylist(list);
    }

    @Test
    public void getCurrentItemTest(){
        for(int i = 0; i< list.size(); i++) {
            playlist.setCurrentSongPosition(i);
            assertTrue(playlist.getCurrentItem().equals(list.get(i)));
        }
    }

    @Test
    public void getCurrentSongTest(){
        for(int i = 0; i < list.size(); i++) {
            playlist.setCurrentSongPosition(i);
            Item item = list.get(i);
            if(item.isAudioFile()) {
                    assertTrue(playlist.getCurrentSong().equals(list.get(i)));
            }else{
                assertNull(playlist.getCurrentSong());
            }
        }
    }

    @Test
    public void getPreviousSongPositionTest(){
        playlist.setCurrentSongPosition(2);
        int position = playlist.getPreviousSongPosition();
        assertEquals(4,position);
        playlist.setCurrentSongPosition(3);
        position = playlist.getPreviousSongPosition();
        assertEquals(2,position);
        playlist.setCurrentSongPosition(4);
        position = playlist.getPreviousSongPosition();
        assertEquals(3,position);
    }

    @Test
    public void getNextSongPositionTest(){
        playlist.setCurrentSongPosition(2);
        int position = playlist.getNextSongPosition();
        assertEquals(3,position);
        playlist.setCurrentSongPosition(3);
        position = playlist.getNextSongPosition();
        assertEquals(4,position);
        playlist.setCurrentSongPosition(4);
        position = playlist.getNextSongPosition();
        assertEquals(2,position);
    }

    @Test
    public void findSongIndexByPathTest(){
        for(int i = 2 ; i <= 3; i++) {
            int position = playlist.findSongIndexByPath(list.get(i).getPath());
            assertEquals(i, position);
        }
    }

}
