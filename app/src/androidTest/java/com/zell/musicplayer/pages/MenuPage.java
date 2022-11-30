package com.zell.musicplayer.pages;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.espresso.ViewInteraction;

import com.zell.musicplayer.R;

import io.qameta.allure.Step;

public class MenuPage {

    private final ViewInteraction externalStorage = onView(withId(R.id.external_storage));
    private final ViewInteraction allMedia = onView(withId(R.id.all_media));
    private final ViewInteraction artists = onView(withId(R.id.artists));
    private final ViewInteraction equalizer = onView(withId(R.id.menu_equalizer));
    private final ViewInteraction exit = onView(withId(R.id.exit));

    @Step("Select external storage")
    public PlaylistPage externalStorageClick() {
        externalStorage.perform(click());
        return new PlaylistPage();
    }

    @Step("Select all media storage")
    public PlaylistPage allMediaClick() {
        allMedia.perform(click());
        return new PlaylistPage();
    }

    @Step("Select artists storage")
    public PlaylistPage artistsClick() {
        artists.perform(click());
        return new PlaylistPage();
    }

    @Step("Select equalizer")
    public EqualizerPage equalizerClick() {
        equalizer.perform(click());
        return new EqualizerPage();
    }

    @Step("Select exit")
    public void exitClick() {
        exit.perform(click());
    }
}
