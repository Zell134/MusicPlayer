package com.zell.musicplayer.pages;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withChild;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

import androidx.test.espresso.ViewInteraction;

import com.google.android.material.internal.NavigationMenuItemView;
import com.zell.musicplayer.R;

import io.qameta.allure.Step;

public class MenuPage {

    private final ViewInteraction externalStorage = onView(withId(R.id.external_storage));
    private final ViewInteraction allMedia = onView(withId(R.id.all_media));
    private final ViewInteraction artists = onView(withId(R.id.artists));
    private final ViewInteraction equalizer = onView(withId(R.id.menu_equalizer));
    private final ViewInteraction exit = onView(allOf(is(instanceOf(NavigationMenuItemView.class)), withId(R.id.exit)));

    @Step("Select external storage")
    public PlaylistPage clickExternalStorage() {
        externalStorage.perform(click());
        return new PlaylistPage();
    }

    @Step("Select all media storage")
    public PlaylistPage clickAllMedia() {
        allMedia.perform(click());
        return new PlaylistPage();
    }

    @Step("Select artists storage")
    public PlaylistPage clickArtists() {
        artists.perform(click());
        return new PlaylistPage();
    }

    @Step("Select equalizer")
    public EqualizerPage clickEqualizer() {
        equalizer.perform(click());
        return new EqualizerPage();
    }

    @Step("Select exit")
    public void clickExit() {
        exit.perform(click());
    }

    @Step("External storage is selected")
    public MenuPage isExternalStorageSelected() {
        externalStorage.check(matches(withChild(isChecked())));
        return this;
    }

    @Step("All media storage is selected")
    public MenuPage isAllMediaStorageSelected() {
        allMedia.check(matches(withChild(isChecked())));
        return this;
    }

    @Step("Artists storage is selected")
    public MenuPage isArtistsStorageSelected() {
        artists.check(matches(withChild(isChecked())));
        return this;
    }

    @Step("External storage is not selected")
    public MenuPage isExternalStorageNotSelected() {
        externalStorage.check(matches(withChild(isNotChecked())));
        return this;
    }

    @Step("All media storage is not selected")
    public MenuPage isAllMediaStorageNotSelected() {
        allMedia.check(matches(withChild(isNotChecked())));
        return this;
    }

    @Step("Artists storage is not selected")
    public MenuPage isArtistsStorageNotSelected() {
        artists.check(matches(withChild(isNotChecked())));
        return this;
    }

}
