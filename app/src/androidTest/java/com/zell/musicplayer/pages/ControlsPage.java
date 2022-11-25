package com.zell.musicplayer.pages;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.assertj.core.api.Assertions.assertThat;

import androidx.test.espresso.ViewInteraction;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import com.zell.musicplayer.R;
import com.zell.musicplayer.util.DeviceActions;

import io.qameta.allure.Step;

public class ControlsPage {

    private final ViewInteraction previousBtn = onView(withId(R.id.previous));
    private final ViewInteraction playBtn = onView(withId(R.id.play));
    private final ViewInteraction stopBtn = onView(withId(R.id.stop));
    private final ViewInteraction nextBtn = onView(withId(R.id.next));
    private final ViewInteraction equqlizerBtn = onView(withId(R.id.equqlizer_button));
    private final ViewInteraction seekBar = onView(withId(R.id.seekbar));
    private final ViewInteraction albumImage = onView(withId(R.id.album_art));
    private final ViewInteraction timer = onView(withId(R.id.timer));
    private final ViewInteraction songInfo = onView(withId(R.id.playing_song_info));
    private final ViewInteraction songName = onView(withId(R.id.playing_song_name));

    @Step("Click \"Stop\" button")
    public ControlsPage stop() {
        stopBtn.perform(click());
        return this;
    }

    @Step("All elements is visible")
    public ControlsPage isAllElementsVisible() {
        previousBtn.check(matches(isDisplayed()));
        playBtn.check(matches(isDisplayed()));
        stopBtn.check(matches(isDisplayed()));
        nextBtn.check(matches(isDisplayed()));
        equqlizerBtn.check(matches(isDisplayed()));
        seekBar.check(matches(isDisplayed()));
        albumImage.check(matches(isDisplayed()));
        timer.check(matches(isDisplayed()));
        return this;
    }

    @Step("Click \"Play/Pause\" button")
    public ControlsPage playPause() {
        playBtn.perform(click());
        return this;
    }

     @Step("Click \"Next\" button")
    public ControlsPage nextSong() {
        nextBtn.perform(click());
         return this;
    }

    @Step("Click \"Previous\" button")
    public ControlsPage previousSong() {
        previousBtn.perform(click());
        return this;
    }

    @Step("Click \"Equalizer\" button")
    public ControlsPage equalizerClick() {
        equqlizerBtn.perform(click());
        return this;
    }

    @Step("Click on seek bar")
    public ControlsPage seekBarClick() {
        seekBar.perform(click());
        return this;
    }

    @Step("Player is in playing state")
    public ControlsPage isInPlayingState() {
        assertThat(checkIsInPlayinState())
                .as("Timer is running")
                .isTrue();

        return this;
    }

    @Step("Player is in paused state")
    public ControlsPage isInPausedState() {
        assertThat(checkIsInPlayinState())
                .as("Timer is stopped")
                .isNotNull()
                .isFalse();
        return this;
    }

    private int getTimerProgress() {

        String timerText = getTimerValue().split("/")[0];

        if (timerText.equals("0")) {
            return 0;
        }
        String[] splittedTimer = timerText.split(":");
        return Integer.valueOf(splittedTimer[0].trim()) * 60 + Integer.valueOf(splittedTimer[1].trim());
    }

    private boolean checkIsInPlayinState(){
        int start = getTimerProgress();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        int end = getTimerProgress();
        return start != end;
    }

    private String getTimerValue(){
        UiObject timer = DeviceActions.findObject(new UiSelector().resourceId("com.zell.musicplayer:id/timer"));
        try {
            return timer.getText();
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
