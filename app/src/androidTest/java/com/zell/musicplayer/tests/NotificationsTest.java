package com.zell.musicplayer.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.zell.musicplayer.R;
import com.zell.musicplayer.pages.ControlsPage;
import com.zell.musicplayer.pages.Notification;
import com.zell.musicplayer.util.PlayerHelper;

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
        notifications
                .closeNotifications();
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

    @Description("Song info is not changed on click \"Play/Pause\" button")
    @Test
    public void songInfoNotChanged_onPlayPauseBtnClick() {

        Supplier getter = () -> notifications.getArtist() + notifications.getTitle();
        Action action = () -> notifications.playPause();
        checkValueAfterAction(getter, action, false);
    }

    @Description("Playing state is changed on click \"Play/Pause\" button")
    @Test
    public void playingStateChanged_onPlayPauseBtnClick() {

        Supplier getter = () -> String.valueOf(PlayerHelper.isPlaying());
        Action action = () -> notifications.playPause();
        checkValueAfterAction(getter, action, true);
    }

    @Description("Song info is changed on click \"Next\" button")
    @Test
    public void songInfoChanged_onNextBtnClick() {
        Supplier getter = () -> notifications.getTitle();
        Action action = () -> notifications.next();
        checkValueAfterAction(getter, action, true);
    }

    @Description("Song info is changed on click \"Previous\" button")
    @Test
    public void songInfoChanged_onPreviousBtnClick() {
        notifications.next();
        Supplier getter = () -> notifications.getTitle();
        Action action = () -> notifications.previous();
        checkValueAfterAction(getter, action, true);
    }

    @Description("Song info stay the same on click \"Next\" and then  \"Previous\"")
    @Test
    public void songInfoIsTheSame_onClickNextAndPreviousBtn() {
        Supplier getter = () -> notifications.getArtist() + notifications.getTitle();
        Action action = () -> {
            notifications.next();
            notifications.previous();
        };
        checkValueAfterAction(getter, action, false);

    }

    @Description("Stop playing on notifications clearing")
    @Test
    public void stopPlaying_onNotificationsClearing() {
        notifications
                .isNotificationShown()
                .clearNotifications()
                .isNotificationNotShown()
                .closeNotifications()
                .isInPausedState();
    }


    @Description("Playing state is changed on click on seekbar")
    @Test
    public void playingStateChanged_onOnSeekbarClick() {
        Supplier getter = () -> String.valueOf(PlayerHelper.isPlaying());
        Action action = () -> notifications.clickOnSeekbar();
        checkValueAfterAction(getter, action, true);
    }

    @Description("Song info stay the same on click on seekbar")
    @Test
    public void songInfoIsTheSame_onOnSeekbarClick() {
        Supplier getter = () -> notifications.getTitle();
        Action action = () -> notifications.clickOnSeekbar();
        checkValueAfterAction(getter, action, false);
    }

    /**
     * Compare required value before and after some actions.
     *
     *@param getValue getter for the required value
     *@param actions actions to execute
     *@param isShouldBeChanged should actual and expected values be equals or not
     */

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