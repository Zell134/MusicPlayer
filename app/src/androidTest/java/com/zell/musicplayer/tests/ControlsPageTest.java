package com.zell.musicplayer.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.zell.musicplayer.pages.ControlsPage;
import com.zell.musicplayer.util.PlayerHelper;

import org.junit.Test;

import io.qameta.allure.Description;

public class ControlsPageTest extends BaseTest{

    private ControlsPage controlsPage = new ControlsPage();

    @Description("All elements is visible on start application")
    @Test
    public void isAllElementsVisible_OnStart() {
        controlsPage.isAllElementsVisible();
    }

    @Description("Player start playing on click \"Play/Pause\" button")
    @Test
    public void startPlaying_onClickPlay() {

        if(PlayerHelper.isPlaying()){
            controlsPage.playPause();
        }
        assertThat(PlayerHelper.isPlaying())
                .as("Player is in paused state")
                .isFalse();
        controlsPage.playPause();
        assertThat(PlayerHelper.isPlaying())
                .as("Player is in playing state")
                .isTrue();
    }

}
