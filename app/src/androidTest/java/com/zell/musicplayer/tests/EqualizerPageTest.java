package com.zell.musicplayer.tests;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.zell.musicplayer.activities.MainActivity;
import com.zell.musicplayer.pages.ControlsPage;
import com.zell.musicplayer.pages.EqualizerPage;
import com.zell.musicplayer.pages.PlaylistPage;
import com.zell.musicplayer.util.DeviceHelper;
import com.zell.musicplayer.util.PlayerHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;

import io.qameta.allure.Description;

@RunWith(AndroidJUnit4.class)
public class EqualizerPageTest{

    private final String DEFAULT_PRESET = "Пользовательский";
    private EqualizerPage equalizer;
    private ActivityScenario<MainActivity> scenario;

    @Before
    public void setup(){
        DeviceHelper.clearDatabase();
        scenario = ActivityScenario.launch(MainActivity.class).moveToState(Lifecycle.State.RESUMED);
        new ControlsPage().equalizerClick();
        equalizer = new EqualizerPage();
    }

    @After
    public void closeApp(){
        scenario.close();
    }

    @Description("Equalizer is closed on click \"Back\" button")
    @Test
    public void equalizerIsClosed_OnBackButtonClicked() {
        equalizer
                .isEqualizerOpened()
                .clickBack()
                .isPlaylistOpened();
    }

    @Description("Equalizer is closed on press android back button")
    @Test
    public void equalizerIsClosed_OnBackPressed() {
        equalizer.isEqualizerOpened();
        DeviceHelper.pressBack();
        new PlaylistPage().isPlaylistOpened();
    }

    @Description("Default preset is \"" + DEFAULT_PRESET + "\"")
    @Test
    public void usersPresetIsDefault() {
        equalizer.selectedPresetIs(DEFAULT_PRESET);
    }

    @Description("Preset list is populated by presets")
    @Test
    public void presetListIsPopulatedByPresets() {
        for (short i = 0; i < PlayerHelper.getEqualizerPresetsCount(); i++) {
            equalizer.isPresetWithTextExists(PlayerHelper.getEqualizerPresetNameAtPosition(i));
        }
    }

    @Description("\"" + DEFAULT_PRESET + "\" is present in the list")
    @Test
    public void firstPresetInListIsDefault() {
        equalizer.clickPresetList()
                .isPresetWithTextExists(DEFAULT_PRESET);
    }

    @Description("Selected preset name displays in the preset spinner")
    @Test
    public void selectedPresetDisplays() {
        String expectedPresetName = selectAnyPreset();
        equalizer.selectedPresetIs(expectedPresetName);
    }

    @Description("Selected preset name displays after application restart")
    @Test
    public void selectedPresetDisplays_afterAppRestart() {
        String expectedPresetName = selectAnyPreset();
        DeviceHelper.restartApplication();
        new ControlsPage()
                .equalizerClick()
                .selectedPresetIs(expectedPresetName);
    }

    @Description("Selected preset displays after application restart")
    @Test
    public void presetResetsToDefault_onClickOnAnySeekBar() {
        String selectedPresetName = selectAnyPreset();
        short numberOfBands = PlayerHelper.getNumberOfEqualizerBands();
        short seekbarTag =  (short) new Random().nextInt(numberOfBands);
        equalizer
                .selectedPresetIs(selectedPresetName)
                .clickOnSeekBarWithTag(seekbarTag)
                .selectedPresetIs(DEFAULT_PRESET);
    }

    private String selectAnyPreset(){
        int presetsCount = PlayerHelper.getEqualizerPresetsCount();
        short position = (short) (new Random().nextInt(presetsCount) + 1);
        String expectedPresetName = PlayerHelper.getEqualizerPresetNameAtPosition((short) (position - 1));
        equalizer.clickPresetList()
                .clickOnPresetAtPosition(position);
        return expectedPresetName;
    }
}
