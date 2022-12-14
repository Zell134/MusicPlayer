package com.zell.musicplayer.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static io.qameta.allure.Allure.step;

import com.zell.musicplayer.models.Item;
import com.zell.musicplayer.models.Song;
import com.zell.musicplayer.pages.ControlsPage;
import com.zell.musicplayer.pages.PlaylistPage;
import com.zell.musicplayer.pages.ToolbarPage;
import com.zell.musicplayer.services.MediaLibraryService;
import com.zell.musicplayer.util.DeviceHelper;

import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Random;

import io.qameta.allure.Step;

public class PlaylistTest extends  BaseTest {

    private ControlsPage controls;
    private PlaylistPage playlistPage;
    private MediaLibraryService mediaLibraryService;
    private List<Item> allMediaPlaylist;
    private final SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");

    @Before
    public void setup() {
        controls = new ControlsPage();
        playlistPage = new PlaylistPage();
        mediaLibraryService = new MediaLibraryService();
        allMediaPlaylist = mediaLibraryService.getAllMedia(getActivity());
    }

    @Step("Current song is visible in playlist")
    @Test
    public void currentSongIsVisibleInPlaylist() {
        setAllMediaLibrary();
        Song expectedSong = (Song) allMediaPlaylist.get(0);
        playlistPage
                .isItemAtPositionHasTitle(0, expectedSong.getTitle())
                .isItemAtPositionHasArtistName(0, expectedSong.getArtist());
    }

    @Step("Playlist has correct size")
    @Test
    public void playlistHasCorrectSize() {
        setAllMediaLibrary();
        playlistPage.IsPlaylistHasSize(allMediaPlaylist.size());
    }

    @Step("Correct song info is displayed in the controls on click on song in playlist")
    @Test
    public void correctSongInfoDisplayedInControls_onClickOnSongInPlaylist() {
        setAllMediaLibrary();
        List<Item> playlist = mediaLibraryService.getAllMedia(getActivity());
        int expectedPosition = getRandomPosition(playlist.size());
        Song song = (Song) playlist.get(expectedPosition);
        playlistPage.clickOnItemAtPosition(expectedPosition)
                .isItemAtPositionHasTitle(expectedPosition, song.getTitle())
                .isItemAtPositionHasArtistName(expectedPosition, song.getArtist());

        String songName = song.getTitle() + " (" + formatter.format(song.getDuration()) + ")";
        String songInfo = song.getArtist() + " - " + song.getAlbum();
        controls.isSongInfoIsDisplayed()
                .isSongInfoHasText(songInfo)
                .isSongNameHasText(songName);
    }

    @Step("Current song is highlighted in playlist")
    @Test
    public void currentSongIsHighlighted() {
        setAllMediaLibrary();
        playlistPage.isItemAtPositionHighlighted(0);
    }

    @Step("Next song is highlighted on play")
    @Test
    public void nextSongIsHighlighted() {
        setAllMediaLibrary();
        playlistPage.isItemAtPositionHighlighted(0);
        controls.nextSong();
        playlistPage
                .isItemAtPositionNOTHighlighted(0)
                .isItemAtPositionHighlighted(1);
    }

    @Step("Last song is highlighted on click \"Previous\" while first song is playing ")
    @Test
    public void lastSongIsHighlighted_onClickPreviousWhileFirstIsPlay() {
        setAllMediaLibrary();
        int playlistSize = allMediaPlaylist.size();
        playlistPage.isItemAtPositionHighlighted(0);
        controls.previousSong();
        waitForElementVisible();
        playlistPage
                .isItemAtPositionHighlighted(playlistSize - 1)
                .scrollToPosition(0)
                .isItemAtPositionNOTHighlighted(0);
    }

    @Step("First song is highlighted on click \"Next\" while last song is playing in all media library")
    @Test
    public void firstSongIsHighlighted_onClickNextWhileLastIsPlayAllMedia() {
        setAllMediaLibrary();
        int lastPosition = allMediaPlaylist.size() - 1;
        playlistPage.clickOnItemAtPosition(lastPosition)
                .isItemAtPositionHighlighted(lastPosition);
        controls.nextSong();
        waitForElementVisible();
        playlistPage.isItemAtPositionHighlighted(0)
                .scrollToPosition(lastPosition)
                .isItemAtPositionNOTHighlighted(lastPosition);
    }

    @Step("Artists List is displayed first of all in playlist if artists library selected")
    @Test
    public void artistsListDisplayedFirst() {
        setArtistsLibrary();
        waitForElementVisible();
        List<Item> playlist = mediaLibraryService.getArtistList(getActivity());
        int playlistSize = playlist.size();
        int expectedPosition = getRandomPosition(playlistSize);
        String expectedArtist = playlist.get(expectedPosition).getTitle();
        playlistPage.IsPlaylistHasSize(playlistSize)
                .isItemAtPositionHasTitle(expectedPosition, expectedArtist);
    }

    @Step("List of albums displayed on click artist in playlist if artists library selected")
    @Test
    public void listOfAlbumsDisplayed_OnClickAnyArtist() {
        Path path = createPathToSong();
        List<Item> playlist = mediaLibraryService.getAlbumsOfArtist(getActivity(), path.getArtist().getTitle());
        playlistPage.clickOnItemAtPosition(path.getArtist().getPosition())
                .IsPlaylistHasSize(playlist.size())
                .isItemAtPositionHasTitle(path.getAlbum().getPosition(),
                        playlist.get(path.getAlbum().getPosition()).getTitle()
                );
    }

    @Step("Songs list displayed on click album of artist if artists library selected")
    @Test
    public void songsListDisplayedAfterAlbums_onArtistsLibrary() {

        Path path = createPathToSong();

        List<Item> playlist = mediaLibraryService.getSongsOfAlbum(getActivity(),
                path.getAlbum().getTitle(),
                path.getArtist().getTitle()
        );

        playlistPage.clickOnItemAtPosition(path.getArtist().getPosition())
                .isItemAtPositionHasTitle(path.getAlbum().getPosition(), path.getAlbum().getTitle())
                .clickOnItemAtPosition(path.getAlbum().getPosition())
                .IsPlaylistHasSize(playlist.size())
                .isItemAtPositionHasTitle(path.getSong().getPosition(), path.getSong().getTitle());
    }


    @Step("All subfolders in a playlist has root folder with title \"..\" to go back")
    @Test
    public void allSubFoldersHasRootFolder() {
        Path path = createPathToSong();
        playlistPage.clickOnItemAtPosition(path.getArtist().getPosition())
                .isItemAtPositionHasTitle(0, "..")
                .clickOnItemAtPosition(path.getAlbum().getPosition())
                .isItemAtPositionHasTitle(0, "..");
    }

    @Step("Root folder has no folder with title \"..\" to go back")
    @Test
    public void rootFolderHasNoRootFolder() {
        setArtistsLibrary();
        List<Item> playlist = mediaLibraryService.getArtistList(getActivity());
        assertThat(playlist.get(0).getTitle())
                .as("First folder in a root folder has title not equal to \"..\"")
                .isNotEqualTo("..");
        playlistPage
                .isItemAtPositionHasTitle(0, playlist.get(0).getTitle());
    }

    @Step("Previous folder is displayed on click on folder with title \"..\"")
    @Test
    public void previousFolderDisplayed_onClickOnFolderWithTwoDots() {
        Path path = createPathToSong();
        playlistPage.clickOnItemAtPosition(path.getArtist().getPosition())
                .clickOnItemAtPosition(path.getAlbum().getPosition())
                .isItemAtPositionHasTitle(0, "..")
                .clickOnItemAtPosition(0)
                .isItemAtPositionHasTitle(path.getAlbum().getPosition(), path.getAlbum().getTitle())
                .isItemAtPositionHasTitle(0, "..")
                .clickOnItemAtPosition(0)
                .isItemAtPositionHasTitle(path.getArtist().getPosition(), path.getArtist().getTitle());
    }

    @Step("Previous folder is displayed on press android back button")
    @Test
    public void previousFolderDisplayed_onPressAndroidBack() {
        Path path = createPathToSong();
        playlistPage.clickOnItemAtPosition(path.getArtist().getPosition())
                .clickOnItemAtPosition(path.getAlbum().getPosition())
                .isItemAtPositionHasTitle(0, "..");
        DeviceHelper.pressBack();
        playlistPage.isItemAtPositionHasTitle(path.getAlbum().getPosition(), path.getAlbum().getTitle())
                .isItemAtPositionHasTitle(0, "..");
        DeviceHelper.pressBack();
        playlistPage.isItemAtPositionHasTitle(path.getArtist().getPosition(), path.getArtist().getTitle());
    }

    private void setAllMediaLibrary() {
        step("Set all media library", () -> new ToolbarPage().openMenu().clickAllMedia());
        playlistPage.scrollToPosition(0);
        step("Play first song in playlist", () -> playlistPage.scrollToPosition(0).clickOnItemAtPosition(0));
    }

    private void setArtistsLibrary() {
        step("Set artists library", () -> new ToolbarPage().openMenu().clickArtists());
    }

    private Path createPathToSong() {
        Path path = new Path();
        setArtistsLibrary();

        List<Item> playlist = mediaLibraryService.getArtistList(getActivity());
        int expectedPosition = getRandomPosition(playlist.size());
        String expectedTitle = playlist.get(expectedPosition).getTitle();
        path.setArtist(new RandomItem(expectedTitle, expectedPosition));

        playlist = mediaLibraryService.getAlbumsOfArtist(getActivity(), expectedTitle);
        expectedPosition = getRandomPosition(playlist.size() - 1) + 1;
        expectedTitle = playlist.get(expectedPosition).getTitle();
        path.setAlbum(new RandomItem(expectedTitle, expectedPosition));

        playlist = mediaLibraryService.getSongsOfAlbum(getActivity(), expectedTitle, expectedTitle);
        expectedPosition = getRandomPosition(playlist.size());
        expectedTitle = playlist.get(expectedPosition).getTitle();
        path.setSong(new RandomItem(expectedTitle, expectedPosition));

        return path;
    }

    private int getRandomPosition(int listSize) {
        return Math.abs(new Random().nextInt(listSize));
    }

    private void waitForElementVisible() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    class Path {
        private RandomItem artist;
        private RandomItem album;
        private RandomItem song;

        public RandomItem getArtist() {
            return artist;
        }

        public void setArtist(RandomItem artist) {
            this.artist = artist;
        }

        public RandomItem getAlbum() {
            return album;
        }

        public void setAlbum(RandomItem album) {
            this.album = album;
        }

        public RandomItem getSong() {
            return song;
        }

        public void setSong(RandomItem song) {
            this.song = song;
        }
    }

    class RandomItem {
        private String title;
        private int position;

        public RandomItem(String title, int position) {
            this.title = title;
            this.position = position;
        }

        public String getTitle() {
            return title;
        }

        public int getPosition() {
            return position;
        }
    }
}
