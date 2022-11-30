package com.zell.musicplayer.pages;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.espresso.ViewInteraction;

import com.zell.musicplayer.R;

import io.qameta.allure.Step;

public class EqualizerPage {

    private final ViewInteraction backBtn = onView(withId(R.id.back_button));
    private final ViewInteraction presetList = onView(withId(R.id.preset_list));
    private final ViewInteraction slidersContainer = onView(withId(R.id.sliders_container));

    @Step("Equalizer is opened")
    public EqualizerPage isEqualizerOpened(){
        slidersContainer.check(matches(isDisplayed()));
        return this;
    }

    @Step("Click back button on equalizer page")
    public PlaylistPage clickBack(){
        backBtn.perform(click());
        return new PlaylistPage();
    }
}
