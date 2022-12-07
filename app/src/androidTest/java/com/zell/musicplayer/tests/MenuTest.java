package com.zell.musicplayer.tests;

import static io.qameta.allure.Allure.step;

import com.zell.musicplayer.pages.MenuPage;
import com.zell.musicplayer.pages.ToolbarPage;

import org.junit.Before;
import org.junit.Test;

import io.qameta.allure.Step;

public class MenuTest extends BaseTest{

    private MenuPage menu;

    @Step("Open menu")
    @Before
    public void setup(){
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

    private void openMenu(){
        step("Open menu",new ToolbarPage()::openMenu);
    }
}
