package com.zell.musicplayer.pages;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.CoordinatesProvider;
import androidx.test.espresso.action.GeneralClickAction;
import androidx.test.espresso.action.GeneralLocation;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Tap;

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

    @Step("Selected preset is {0}")
    public EqualizerPage selectedPresetIs(String name){
        presetList.check(matches(withSpinnerText(containsString(name))));
        return this;
    }

    @Step("Click preset list")
    public EqualizerPage clickPresetList(){
        presetList.perform(click());
        return this;
    }

    @Step("Preset with text \"{0}\" exists")
    public EqualizerPage isPresetWithTextExists(String text){
        onData(allOf(is(instanceOf(String.class)), is(text)))
                .check(matches(isDisplayed()));
        return this;
    }

    @Step("Click on preset at position {0}")
    public EqualizerPage clickOnPresetAtPosition(int position){
        onData(allOf())
                .atPosition(position)
                .perform(click());
        return this;
    }

    @Step("Click on {0} seekbar")
    public EqualizerPage clickOnSeekBarWithTag(short tag){
        onView(withTagValue(is((Object) tag)))
                .perform(click());
        return this;
    }
}
