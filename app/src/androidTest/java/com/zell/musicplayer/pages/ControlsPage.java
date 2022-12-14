package com.zell.musicplayer.pages;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;

import androidx.test.espresso.ViewInteraction;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import com.zell.musicplayer.R;
import com.zell.musicplayer.util.DeviceHelper;

import io.qameta.allure.Step;

public class ControlsPage {

    private final ViewInteraction previousBtn = onView(withId(R.id.previous));
    private final ViewInteraction playBtn = onView(withId(R.id.play));
    private final ViewInteraction stopBtn = onView(withId(R.id.stop));
    private final ViewInteraction nextBtn = onView(withId(R.id.next));
    private final ViewInteraction equalizerBtn = onView(withId(R.id.equqlizer_button));
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
        equalizerBtn.check(matches(isDisplayed()));
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
    public EqualizerPage equalizerClick() {
        equalizerBtn.perform(click());
        return new EqualizerPage();
    }

    @Step("Click on seek bar")
    public ControlsPage seekBarClick() {
        seekBar.perform(click());
        return this;
    }

    @Step("Song info is displayed")
    public ControlsPage isSongInfoIsDisplayed() {
        songInfo.check(matches(isDisplayed()));
        songName.check(matches(isDisplayed()));
        return this;
    }

    @Step("Song info is not visible")
    public ControlsPage isSongInfoIsNotVisible() {
        songInfo.check(matches(not(isDisplayed())));
        songName.check(matches(not(isDisplayed())));
        return this;
    }

    @Step("Song info has text {0}")
    public ControlsPage isSongInfoHasText(String text) {
        songInfo.check(matches(withText(text)));
        return this;
    }

    @Step("Song name has text {0}")
    public ControlsPage isSongNameHasText(String text) {
        songName.check(matches(withText(text)));
        return this;
    }

    public String getSongInfo() {
        UiObject timer = DeviceHelper.findObject(new UiSelector().resourceId("com.zell.musicplayer:id/playing_song_info"));
        return getTextOfObject(timer);
    }

    public String getSongName() {
        UiObject timer = DeviceHelper.findObject(new UiSelector().resourceId("com.zell.musicplayer:id/playing_song_name"));
        return getTextOfObject(timer);
    }

    private String getTimerValue() {
        UiObject timer = DeviceHelper.findObject(new UiSelector().resourceId("com.zell.musicplayer:id/timer"));
        return getTextOfObject(timer);
    }

    public int getTimerProgress() {
        String timerText = getTimerValue().split("/")[0];

        if (timerText.equals("0")) {
            return 0;
        }
        String[] splittedTimer = timerText.split(":");
        return Integer.valueOf(splittedTimer[0].trim()) * 60 + Integer.valueOf(splittedTimer[1].trim());
    }

    private String getTextOfObject(UiObject object) {
        try {
            return object.getText();
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}