package com.zell.musicplayer.pages;

import static org.assertj.core.api.Assertions.assertThat;

import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import com.zell.musicplayer.util.DeviceHelper;

import io.qameta.allure.Step;

public class Notification {

    private String appNameText;

    private final UiObject appName;
    private final UiObject title;
    private final UiObject artist;
    private final UiObject previousBtn;
    private final UiObject nextBtn;
    private final UiObject playPauseBtn;
    private final UiObject seekBar;
    private final UiObject clearNotificationsBtn;
    private final UiObject timer;

    public Notification(String name) {
        appNameText = name;
        DeviceHelper.openNotifications();
        appName = DeviceHelper.findObject(new UiSelector().resourceId("android:id/app_name_text"));
        title = DeviceHelper.findObject(new UiSelector().resourceId("android:id/title"));
        artist = DeviceHelper.findObject(new UiSelector().resourceId("android:id/text"));
        previousBtn = DeviceHelper.findObject(new UiSelector().resourceId("android:id/action0"));
        nextBtn = DeviceHelper.findObject(new UiSelector().resourceId("android:id/action1"));
        playPauseBtn = DeviceHelper.findObject(new UiSelector().resourceId("android:id/action2"));
        seekBar = DeviceHelper.findObject(new UiSelector().resourceId("com.android.systemui:id/media_notification_progress_bar"));
        clearNotificationsBtn = DeviceHelper.findObject(new UiSelector().resourceId("com.android.systemui:id/dismiss_view"));
        timer = DeviceHelper.findObject(new UiSelector().resourceId("com.android.systemui:id/media_notification_elapsed_time"));
    }

    @Step("Notification is shown")
    public Notification isNotificationShown() {
        assertThat(getTextOfObject(appName))
                .as("Notification is shown")
                .isNotNull()
                .isEqualTo(appNameText);
        return this;
    }

    @Step("Notification is not shown")
    public Notification isNotificationNotShown() {
        assertThat(appName)
                .as("Notification is not shown")
                .isNotNull()
                .isNotEqualTo(appNameText);
        return this;
    }

    @Step("Click play/pause button")
    public Notification playPause(){
       click(playPauseBtn);
        return this;
    }

    @Step("Click previous button")
    public Notification previous(){
        click(previousBtn);
        return this;
    }

    @Step("Click next button")
    public Notification next(){
        click(nextBtn);
        return this;
    }

    @Step("Click on seekbar")
    public Notification clickOnSeekbar(){
        click(seekBar);
        return this;
    }

    @Step("Click \"clear notifications\" button")
    public Notification clearNotifications(){
        click(clearNotificationsBtn);
        return this;
    }

    @Step("Close notifications")
    public ControlsPage closeNotifications(){
        DeviceHelper.pressBack();
        return new ControlsPage();
    }

    public String getArtist(){
        return getTextOfObject(artist);
    }

    public String getTitle(){
        return getTextOfObject(artist);
    }

    private void click(UiObject object){
        try {
            object.click();
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String getTextOfObject(UiObject object){
        try {
            return object.getText();
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
