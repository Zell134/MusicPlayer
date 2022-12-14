package com.zell.musicplayer.tests;

import static io.qameta.allure.Allure.step;

import com.zell.musicplayer.models.Item;
import com.zell.musicplayer.models.Song;
import com.zell.musicplayer.pages.MenuPage;
import com.zell.musicplayer.pages.PlaylistPage;
import com.zell.musicplayer.pages.ToolbarPage;
import com.zell.musicplayer.services.MediaLibraryService;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Random;

import io.qameta.allure.Step;

public class MenuTest extends BaseTest {

    private MenuPage menu;

    @Step("Open menu")
    @Before
    public void setup() {
        openMenu();
        menu = new MenuPage();
    }

    @Step("External storage is selected on click on this one")
    @Test
    public void externalStorageSelected() {
        menu.clickExternalStorage()
                .isPlaylistOpened();
        openMenu();
        menu.isExternalStorageSelected()
                .isAllMediaStorageNotSelected()
                .isArtistsStorageNotSelected();
    }

    @Step("All media storage is selected on click on this one")
    @Test
    public void allMediaStorageSelected() {
        menu.clickAllMedia()
                .isPlaylistOpened();
        openMenu();
        menu.isAllMediaStorageSelected()
                .isExternalStorageNotSelected()
                .isArtistsStorageNotSelected();
    }

    @Step("Artists storage is selected on click on this one")
    @Test
    public void artistsStorageSelected() {
        menu.clickArtists()
                .isPlaylistOpened();
        openMenu();
        menu.isArtistsStorageSelected()
                .isExternalStorageNotSelected()
                .isAllMediaStorageNotSelected();
    }

    @Step("Equalizer opens on click \"Equalizer\" item")
    @Test
    public void equalizerOpens() {
        menu.clickEqualizer()
                .isEqualizerOpened();
    }

    @Step("All media playlist displayed on click \"All media storage\" item")
    @Test
    public void allMediaPlaylistDisplayed_onclickAllMedia() {
        List<Item> playlist = new MediaLibraryService().getAllMedia(getActivity());
        int playlistSize = playlist.size();
        int position = getRandomPosition(playlistSize);
        Song expectedSong = (Song) playlist.get(position);
        openMenu();
        menu.clickAllMedia();
        new PlaylistPage().IsPlaylistHasSize(playlistSize)
                .scrollToPosition(position)
                .isItemAtPositionHasTitle(position, expectedSong.getTitle())
                .isItemAtPositionHasArtistName(position, expectedSong.getArtist());

    }

    @Step("Artists playlist displayed on click \"Artists storage\" item")
    @Test
    public void PlaylistDisplayed_onclickArtists() {
        List<Item> playlist = new MediaLibraryService().getArtistList(getActivity());
        int playlistSize = playlist.size();
        int position = getRandomPosition(playlistSize);
        Item expectedItem = playlist.get(position);
        openMenu();
        menu.clickArtists();
        new PlaylistPage().IsPlaylistHasSize(playlistSize)
                .scrollToPosition(position)
                .isItemAtPositionHasTitle(position, expectedItem.getTitle());

    }

    private void openMenu() {
        step("Open menu", new ToolbarPage()::openMenu);
    }

    private int getRandomPosition(int listSize) {
        return Math.abs(new Random().nextInt(listSize));
    }

}
