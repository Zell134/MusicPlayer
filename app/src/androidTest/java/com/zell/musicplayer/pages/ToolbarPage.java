package com.zell.musicplayer.pages;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.instanceOf;

import android.widget.ImageButton;

import androidx.test.espresso.ViewInteraction;

import com.zell.musicplayer.R;

import io.qameta.allure.Step;

public class ToolbarPage {
    private final ViewInteraction exitBtn = onView(withId(R.id.exit));
    private final ViewInteraction menuBtn = onView(allOf(withParent(withId(R.id.toolbar)), instanceOf(ImageButton.class)));

    @Step("Click \"exit\" button")
    public void exit() {
        exitBtn.perform(click());
    }

    @Step("Open menu")
    public MenuPage openMenu() {
        menuBtn.perform(click());
        return new MenuPage();
    }
}
