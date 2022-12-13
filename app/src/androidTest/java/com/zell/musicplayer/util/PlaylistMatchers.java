package com.zell.musicplayer.util;

import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

import static io.qameta.allure.Allure.step;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.matcher.BoundedMatcher;

import com.zell.musicplayer.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class PlaylistMatchers {

    public static Matcher<View> isSongAtPositionHasTitle(int position, String text){
        return itemAtPositionHasText(position, text, R.id.song_title);
    }

    public static Matcher<View> isSongAtPositionHasArtist(int position, String text){
        return itemAtPositionHasText(position, text, R.id.song_artist);
    }

    public static Matcher<View> isPlaylistHasSize(int size){

        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class){

            int playlistSize = -1;

            @Override
            public void describeTo(Description description) {
                description.appendText("Playlist extected size: " + size);
                if(playlistSize >=0){
                    description.appendText(", actuel size: " + playlistSize);
                }
            }

            @Override
            protected boolean matchesSafely(RecyclerView view) {
                playlistSize = view.getAdapter().getItemCount();
                return playlistSize == size;
            }
        };
    }

    public static Matcher<View> isItemAtPositionHighLighted(int position){
        String matcherDescription = new String ("Playlist item at position "+ String.valueOf(position) + " has selected item color");
        return isItemAtPositionHasBackground(position, R.color.selected_item, matcherDescription);
    }

    private static Matcher<View> isItemAtPositionHasBackground(int position, int color, String matcherDescription) {

        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {

            @Override
            public void describeTo(Description description) {
                description.appendText("Playlist item at position ")
                        .appendText(String.valueOf(position))
                        .appendText(" ")
                        .appendText(matcherDescription);
            }

            @Override
            protected boolean matchesSafely(RecyclerView view) {
                View itemView = view.findViewHolderForAdapterPosition(position).itemView;
                step("Item at position" + position + "is visible", () -> isDisplayed().matches(itemView));
                Drawable actualDrawable = itemView.getBackground();
                Drawable expectedDrawable = view.getContext().getResources().getDrawable(color, null);
                return actualDrawable.getConstantState().equals(expectedDrawable.getConstantState());
            }
        };
    }

    private static Matcher<View> itemAtPositionHasText(int position, String text, int field){

        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class){

            private String itemText;

            @Override
            public void describeTo(Description description) {
                description.appendText("Field with id: ")
                        .appendText(String.valueOf(field))
                        .appendText(" at position ")
                        .appendText(String.valueOf(position))
                        .appendText( " should has text: \"")
                        .appendText(text)
                        .appendText("\"");
                if(itemText != null){
                    description.appendText(", actual text: \"" + itemText + "\"");
                }
            }

            @Override
            protected boolean matchesSafely(RecyclerView view) {
                TextView item = view.findViewHolderForAdapterPosition(position).itemView.findViewById(field);
                Matcher<View> isDisplayed = isDisplayed();
                itemText = item.getText().toString();
                return itemText.contains(text) && isDisplayed.matches(item);
            }
        };
    }

}
