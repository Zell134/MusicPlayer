package com.zell.musicplayer.pages;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.contrib.RecyclerViewActions;

import com.zell.musicplayer.R;

import io.qameta.allure.Step;

public class PlaylistPage {

    private final ViewInteraction playlist = onView(withId(R.id.playlist));

    @Step("Playlist is opened")
    public PlaylistPage isPlaylistOpened() {
        playlist.check(matches(isDisplayed()));
        return this;
    }

    public PlaylistPage clickOnItemAtPosition(int position) {
        playlist.perform(RecyclerViewActions.actionOnItemAtPosition(position, click()));
        return this;
    }

}
