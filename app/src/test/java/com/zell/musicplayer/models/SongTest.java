package com.zell.musicplayer.models;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class SongTest {

    private String PATH = "path";
    private String TITLE = "title";
    private String ALBUM = "album";
    private String ARTIST = "artist";
    private long DURATION = 123;

    @Test
    public void onSongCreation_shouldSetCorrectValues(){
        Song song = new Song(PATH, TITLE,  ALBUM, ARTIST, DURATION);
        assertThat(song.getPath()).isEqualTo(PATH);
        assertThat(song.getTitle()).isEqualTo(TITLE);
        assertThat(song.getAlbum()).isEqualTo(ALBUM);
        assertThat(song.getArtist()).isEqualTo(ARTIST);
        assertThat(song.getDuration()).isEqualTo(DURATION);
        assertThat(song.isAudioFile()).isTrue();
    }

    @Test
    public void songsWithTheSameParametest_shouldBeEquals(){
        Song song1 = new Song(PATH, TITLE,  ALBUM, ARTIST, DURATION);
        Song song2 = new Song(PATH, TITLE,  ALBUM, ARTIST, DURATION);
        assertThat(song1.equals(song2)).isTrue();
    }

    @Test
    public void toStringMethod_shouldReturnsFormattedValue(){
        Song song = new Song(PATH, TITLE,  ALBUM, ARTIST, DURATION);
        assertThat(song.toString()).isEqualTo(ARTIST + " - " + TITLE + " (" + ALBUM + ")");
    }
}