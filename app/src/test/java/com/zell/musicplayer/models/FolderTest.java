package com.zell.musicplayer.models;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class FolderTest {

    private static String PATH = "path";
    private static String TITLE = "title";

    @Test
    public void onFolderCreation_shouldSetCorrectValues(){
        Folder folder = new Folder(PATH, TITLE);
        assertThat(folder.getPath()).isEqualTo(PATH);
        assertThat(folder.getTitle()).isEqualTo(TITLE);
        assertThat(folder.isAudioFile()).isFalse();
    }

    @Test
    public void foldersWithTheSamePathAndTitle_shouldBeEquals(){
        Folder folder1 = new Folder(PATH, TITLE);
        Folder folder2 = new Folder(PATH, TITLE);
        assertThat(folder1.equals(folder2)).isTrue();
    }

    @Test
    public void toStringMethod_shouldReturnsTitle(){
        Folder folder = new Folder(PATH, TITLE);
        assertThat(folder.toString()).isEqualTo(TITLE);
    }

}