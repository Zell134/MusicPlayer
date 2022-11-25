package com.zell.musicplayer.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.zell.musicplayer.R;
import com.zell.musicplayer.pages.ControlsPage;
import com.zell.musicplayer.pages.Notification;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.function.Supplier;

import io.qameta.allure.Description;

public class NotificationsTest extends BaseTest{

    private String appName;
    private Notification notifications;

    interface Action{
        void execute();
    }

    @Before
    public void setup(){
        activityScenarioRule
                .getScenario()
                .onActivity(activity -> appName = activity.getResources().getString(R.string.app_name));
        new ControlsPage()
                .playPause();
        notifications = new Notification(appName);
    }

    @After
    public void complete(){
        notifications.closeNotifications();
    }

    @Description("Notification appears on start playing")
    @Test
    public void notificationAppears_onStartPlaying() {
        notifications
                .isNotificationShown();
    }

    @Description("Notification disappears on stop playing")
    @Test
    public void notificationDisappears_onStopPlaying() {
        notifications
                .isNotificationShown()
                .closeNotifications()
                .stop();
        notifications
                .isNotificationNotShown();
    }

    @Description("Clicking on \"Play/Pause\" button not change song info")
    @Test
    public void songInfoNotChanged_onClickingPlayPause() {

        Supplier getter = () -> notifications.getArtist() + notifications.getTitle();
        Action action = () -> notifications.playPause();
        checkValueAfterAction(getter, action, false);
    }

    @Description("Clicking on \"Play/Pause\" button change playing state")
    @Test
    public void playingStateChanged_onClickingPlayPause() {

        Supplier getter = () -> notifications.checkPlayingState().toString();
        Action action = () -> notifications.playPause();
        checkValueAfterAction(getter, action, true);
    }

    @Description("Clicking on \"Next\" button change song info")
    @Test
    public void songInfoChanged_onClickingNextButton() {
        Supplier getter = () -> notifications.getTitle();
        Action action = () -> notifications.next();
        checkValueAfterAction(getter, action, true);
    }

    @Description("Clicking on \"Previous\" button change song info")
    @Test
    public void songInfoChanged_onClickingPreviousButton() {
        notifications.next();
        Supplier getter = () -> notifications.getTitle();
        Action action = () -> notifications.previous();
        checkValueAfterAction(getter, action, true);
    }

    @Description("Song info stay the same after clicking on \"Next\" and then  \"Previous\"")
    @Test
    public void songInfoIsTheSame_onClickingNextAndPreviousButton() {
        Supplier getter = () -> notifications.getArtist() + notifications.getTitle();
        Action action = () -> {
            notifications.next();
            notifications.previous();
        };
        checkValueAfterAction(getter, action, false);

    }

    @Description("Application stop playing on clearing notifications")
    @Test
    public void stopPlaying_onClearingNotifications() {
        notifications
                .isNotificationShown()
                .clearNotifications()
                .isNotificationNotShown()
                .closeNotifications()
                .isInPausedState();
    }


    @Description("Clicking on seekbar change playing state")
    @Test
    public void playingStateChanged_onClickingOnSeekbar() {
        Supplier getter = () -> notifications.checkPlayingState().toString();
        Action action = () -> notifications.clickOnSeekbar();
        checkValueAfterAction(getter, action, true);
    }

    @Description("Song info stay the same after clicking on seekbar")
    @Test
    public void songInfoIsTheSame_onClickingOnSeekbar() {
        Supplier getter = () -> notifications.getTitle();
        Action action = () -> notifications.clickOnSeekbar();
        checkValueAfterAction(getter, action, false);
    }

    private void checkValueAfterAction(Supplier getValue, Action actions, boolean isShouldBeChanged){
        String expectedValue = (String) getValue.get();
        actions.execute();
        String actualValue = (String) getValue.get();
        String description = isShouldBeChanged ? "Value has been changed" : "Value has not been changed";
        assertThat(actualValue)
                .as(description)
                .matches(value -> isShouldBeChanged != value.equals(expectedValue));
    }
}