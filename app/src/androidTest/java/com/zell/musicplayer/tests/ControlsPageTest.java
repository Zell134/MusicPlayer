package com.zell.musicplayer.tests;

import static org.assertj.core.api.Assertions.assertThat;

import static io.qameta.allure.Allure.step;

import com.zell.musicplayer.pages.ControlsPage;
import com.zell.musicplayer.pages.PlaylistPage;
import com.zell.musicplayer.util.AssertionsHelper;
import com.zell.musicplayer.util.DeviceHelper;
import com.zell.musicplayer.util.PlayerHelper;

import org.junit.Before;
import org.junit.Test;

import java.util.function.Supplier;

import io.qameta.allure.Description;

public class ControlsPageTest extends BaseTest {

    private final ControlsPage controlsPage = new ControlsPage();
    Supplier<String> getSongInfoFunc = () -> controlsPage.getSongInfo() + controlsPage.getSongName();

    @Before
    public void setOnPlayingStateOnStart() {
        step("Set player on playing state on start application", () ->
        {
            if (!PlayerHelper.isPlaying()) {
                controlsPage.playPause();
            }
        });
    }

    @Description("All elements is visible on start application")
    @Test
    public void isAllElementsVisible_OnStart() {
        controlsPage.isAllElementsVisible();
    }

    @Description("Player start playing on click \"Play/Pause\" button")
    @Test
    public void startPlaying_onClickPlay() {

        assertThat(PlayerHelper.isPlaying())
                .as("Player is in playing state")
                .isTrue();
        controlsPage.playPause();
        assertThat(PlayerHelper.isPlaying())
                .as("Player is in paused state")
                .isFalse();
    }

    @Description("Song name and song info fields should displayed on playing")
    @Test
    public void songInfoIsDisplayed_OnPlaying() {
        controlsPage
                .playPause()
                .isSongInfoIsDisplayed();
    }

    @Description("Song name and song info fields should disappears on stop playing")
    @Test
    public void songInfoIsInvisible_OnStopPlaying() {
        controlsPage
                .stop()
                .isSongInfoIsNotVisible();
    }

    @Description("The same song info is displayed on pause and resume playing")
    @Test
    public void theSameSongInfoIsDisplayed_OnResumePlaying() {
        AssertionsHelper.checkValueAfterAction(
                getSongInfoFunc,
                controlsPage::playPause,
                false);
        AssertionsHelper.checkValueAfterAction(
                getSongInfoFunc,
                controlsPage::playPause,
                false);
    }

    @Description("The same song info and song name is displayed on change screen orientation")
    @Test
    public void theSameSongInfoIsDisplayed_OnScreenOrientationChanged() {
        AssertionsHelper.checkValueAfterAction(
                getSongInfoFunc,
                () -> DeviceHelper.changeOrientation(DeviceHelper.ROTATION_0),
                false
        );
        AssertionsHelper.checkValueAfterAction(
                getSongInfoFunc,
                () -> DeviceHelper.changeOrientation(DeviceHelper.ROTATION_90),
                false
        );
        AssertionsHelper.checkValueAfterAction(
                getSongInfoFunc,
                () -> DeviceHelper.changeOrientation(DeviceHelper.ROTATION_180),
                false
        );
        AssertionsHelper.checkValueAfterAction(
                getSongInfoFunc,
                () -> DeviceHelper.changeOrientation(DeviceHelper.ROTATION_270),
                false
        );
    }

    @Description("The same song is playing on click \"Next\" and than \"Previous\" button")
    @Test
    public void theSameSongIsPlaying_OnClickNextAndPreviousButton() {
        AssertionsHelper.Action action = () -> {
            controlsPage.nextSong();
            controlsPage.previousSong();
        };
        AssertionsHelper.checkValueAfterAction(
                getSongInfoFunc,
                action,
                false);
    }

    @Description("Another song is playing on click \"Next\" button")
    @Test
    public void anotherSongIsPlaying_OnClickNextButton() {
        new PlaylistPage().clickOnItemAtPosition(0);
        AssertionsHelper.checkValueAfterAction(
                controlsPage::getSongName,
                controlsPage::nextSong,
                true);
    }

    @Description("Another song is playing on click \"Previous\" button and song has been played greater then 10 seconds")
    @Test
    public void anotherSongIsPlaying_OnClickPreviousButtonGreaterTenSecs() {
        new PlaylistPage().clickOnItemAtPosition(1);
        controlsPage.seekBarClick();
        AssertionsHelper.checkValueAfterAction(
                controlsPage::getSongName,
                controlsPage::previousSong,
                false);
    }

    @Description("The same song is playing on click \"Previous\" button and song has been played less then 10 seconds")
    @Test
    public void anotherSongIsPlaying_OnClickPreviousButtonLessTenSecs() {
        new PlaylistPage().clickOnItemAtPosition(1);
        AssertionsHelper.checkValueAfterAction(
                getSongInfoFunc,
                controlsPage::previousSong,
                true);
    }

    @Description("Equalizer opens and closes after clicking \"Equalizer\" button")
    @Test
    public void equalizerOpensAndCloses_OnClickEqualizerButton() {
        controlsPage
                .equalizerClick()
                .isEqualizerOpened()
                .clickBack()
                .isPlaylistOpened();
    }

    @Description("Clicking on seek bar on paused state change current song progress")
    @Test
    public void progressChanged_OnSeekbarClickOnPausedState() {

        controlsPage.playPause();
        int startPosition = PlayerHelper.getCurrentPosition();
        controlsPage
                .seekBarClick();
        int endPosition = PlayerHelper.getCurrentPosition();
        assertThat(endPosition)
                .as("End position is greater then start position")
                .isGreaterThan(startPosition);
    }

    @Description("Clicking on seek bar on playing state change current song progress")
    @Test
    public void progressChanged_OnSeekbarClickOnPlayingState() {
        int startPosition = PlayerHelper.getCurrentPosition();
        controlsPage
                .seekBarClick();
        int endPosition = PlayerHelper.getCurrentPosition();
        assertThat(endPosition)
                .as("End position is greater then start position")
                .isGreaterThan(startPosition);
    }

    @Description("App do not crashed on clicking on seek bar in stopped state")
    @Test
    public void appNotCrashed_OnSeekbarClickOnStoppedState() {
        controlsPage
                .stop()
                .isAllElementsVisible();
    }

    @Description("The same song is playing after restart application")
    @Test
    public void theSameSongIsPlaying_AfterRestartApp() {
        String expectedSongInfo = getSongInfoFunc.get();
        DeviceHelper.restartApplication();
        assertThat(PlayerHelper.isPlaying())
                .as("Player on playing state")
                .isTrue();
        assertThat(getSongInfoFunc.get())
                .as("Song info is the same")
                .isEqualTo(expectedSongInfo);
    }
}
