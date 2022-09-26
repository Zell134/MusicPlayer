package com.zell.musicplayer.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;

import com.zell.musicplayer.R;
import com.zell.musicplayer.helpers.cursors.FoldersForMediaLibraryService;
import com.zell.musicplayer.helpers.cursors.SongsForMediaLibraryService;
import com.zell.musicplayer.models.Folder;
import com.zell.musicplayer.models.Item;
import com.zell.musicplayer.models.Song;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class MediaLibraryServiceTest {

    private Context context;
    private MediaLibraryService mediaLibraryService;
    private ContentResolver contentResolver;

    @Before
    public void setup() {
        mediaLibraryService = new MediaLibraryService();

        contentResolver = mock(ContentResolver.class);


        Resources resources = mock(Resources.class);
        when(resources.getString(R.string.previous_directory)).thenReturn("..");

        context = mock(Context.class);
        when(context.getContentResolver()).thenReturn(contentResolver);
        when(context.getResources()).thenReturn(resources);
    }

    @Test
    public void getAllMedia_shouldReturnAllFoundMedia() {
        when(contentResolver.query(any(), any(), any(), any(), any())).thenReturn(new SongsForMediaLibraryService());

        SongsForMediaLibraryService cursor = new SongsForMediaLibraryService();
        List<Item> expectedList = cursor.getAllRecords();
        List<Item> list = mediaLibraryService.getAllMedia(context);
        assertThat(list.size()).isEqualTo(expectedList.size());
        for (Item item : expectedList) {
            assertThat(list.contains(item)).isTrue();
        }
    }

    @Test
    public void getArtistList_shouldReturnFoundMedia() {
        when(contentResolver.query(any(), any(), any(), any(), any())).thenReturn(new FoldersForMediaLibraryService());

        FoldersForMediaLibraryService cursor = new FoldersForMediaLibraryService();
        List<Item> expectedList = cursor.getAllRecords("root");
        List<Item> list = mediaLibraryService.getArtistList(context);
        assertThat(list.size()).isEqualTo(expectedList.size());
        for (Item item : expectedList) {
            assertThat(list.contains(item)).isTrue();
        }
    }

    @Test
    public void getAlbumsOfArtist_shouldReturnFoundMedia() {
        when(contentResolver.query(any(), any(), any(), any(), any())).thenReturn(new FoldersForMediaLibraryService());

        FoldersForMediaLibraryService cursor = new FoldersForMediaLibraryService();
        List<Item> expectedList = new ArrayList<>();
        expectedList.add(new Folder("root", context.getResources().getString(R.string.previous_directory)));
        expectedList.addAll(cursor.getAllRecords("artist"));
        List<Item> list = mediaLibraryService.getAlbumsOfArtist(context, "artist");
        assertThat(list.size()).isEqualTo(expectedList.size());
        for (Item item : expectedList) {
            assertThat(list.contains(item)).isTrue();
        }
    }

    @Test
    public void getSongsOfAlbum_shouldReturnFoundMedia() {
        when(contentResolver.query(any(), any(), any(), any(), any())).thenReturn(new SongsForMediaLibraryService());

        SongsForMediaLibraryService cursor = new SongsForMediaLibraryService();
        List<Item> expectedList = new ArrayList<>();
        expectedList.add(new Folder("artist", context.getResources().getString(R.string.previous_directory)));
        expectedList.addAll(cursor.getAllRecords());
        List<Item> list = mediaLibraryService.getSongsOfAlbum(context, "album", "artist");
        assertThat(list.size()).isEqualTo(expectedList.size());
        for (Item item : expectedList) {
            assertThat(list.contains(item)).isTrue();
        }
    }

    @Test
    public void getSongByPath_shouldReturnFoundMedia() {
        when(contentResolver.query(any(), any(), any(), any(), any())).thenReturn(new SongsForMediaLibraryService());

        SongsForMediaLibraryService cursor = new SongsForMediaLibraryService();
        Song expectedSong = (Song) cursor.getAllRecords().get(0);
        Song song = mediaLibraryService.getSongByPath(context, "path");
        assertThat(song).isEqualTo(expectedSong);
    }
}