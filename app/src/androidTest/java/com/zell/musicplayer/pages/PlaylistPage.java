package com.zell.musicplayer.pages;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.hamcrest.CoreMatchers.not;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.contrib.RecyclerViewActions;

import com.zell.musicplayer.R;
import com.zell.musicplayer.util.PlaylistMatchers;

import io.qameta.allure.Step;

public class PlaylistPage {



    private final ViewInteraction playlist = onView(withId(R.id.playlist));

    @Step("Playlist is opened")
    public PlaylistPage isPlaylistOpened() {
        playlist.check(matches(isDisplayed()));
        return this;
    }

    @Step("Click on playlist item at position {0}")
    public PlaylistPage clickOnItemAtPosition(int position) {
        playlist.perform(RecyclerViewActions.scrollToPosition(position), RecyclerViewActions.actionOnItemAtPosition(position, click()));
        return this;
    }

    @Step("Song at position {0} has title \"{1}\" and visible")
    public PlaylistPage isItemAtPositionHasTitle(int position, String text){
        playlist.check(matches(PlaylistMatchers.isSongAtPositionHasTitle(position, text)));
        return this;
    }

    @Step("Song at position {0} has artist name \"{1}\" and visible")
    public PlaylistPage isItemAtPositionHasArtistName(int position, String text){
        playlist.check(matches(PlaylistMatchers.isSongAtPositionHasArtist(position, text)));
        return this;
    }

    @Step("Playlist has size - {0}")
    public PlaylistPage IsPlaylistHasSize(int size){
        playlist.check(matches(PlaylistMatchers.isPlaylistHasSize(size)));
        return this;
    }

    @Step("Playlist item at position {0} is highlighted")
    public PlaylistPage isItemAtPositionHighlighted(int position){
        playlist
                .check(matches(PlaylistMatchers.isItemAtPositionHighLighted(position)));
        return this;
    }

    @Step("Playlist item at position {0} is not highlighted")
    public PlaylistPage isItemAtPositionNOTHighlighted(int position){
        playlist
                .check(matches(not(PlaylistMatchers.isItemAtPositionHighLighted(position))));
        return this;
    }

    @Step("Scroll to {0} position")
    public PlaylistPage scrollToPosition(int position){
        playlist.perform(RecyclerViewActions.scrollToPosition(position));
        return this;
    }
}
