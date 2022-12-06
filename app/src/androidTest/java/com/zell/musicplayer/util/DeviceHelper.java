package com.zell.musicplayer.util;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.assertj.core.api.Assertions.assertThat;

import android.app.UiAutomation;
import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import io.qameta.allure.Step;

public class DeviceHelper {

    public static int ROTATION_0 = UiAutomation.ROTATION_FREEZE_0;
    public static int ROTATION_90 = UiAutomation.ROTATION_FREEZE_90;
    public static int ROTATION_180 = UiAutomation.ROTATION_FREEZE_180;
    public static int ROTATION_270 = UiAutomation.ROTATION_FREEZE_270;

    public static final String PACKAGE = "com.zell.musicplayer";

    private static final UiDevice device = UiDevice.getInstance(getInstrumentation());

    public static UiObject findObject(UiSelector selector) {
        return device.findObject(selector);
    }

    public static void openNotifications() {
        device.openNotification();
    }

    @Step("Press back")
    public static void pressBack() {
        device.pressBack();
    }

    @Step("Change orientation")
    public static void changeOrientation(int rotation) {
        getInstrumentation().getUiAutomation().setRotation(rotation);
    }

    @Step("Restart application")
    public static void restartApplication() {

        device.pressHome();

        final String launcherPackage = device.getLauncherPackageName();
        assertThat(launcherPackage).isNotNull();
        device.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), 5000);

        Context context = ApplicationProvider.getApplicationContext();
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(PACKAGE);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);

        device.wait(Until.hasObject(By.pkg(PACKAGE).depth(0)), 5000);

    }

    public static void clearDatabase(){
        InstrumentationRegistry.getInstrumentation().getTargetContext().deleteDatabase("database");
    }
}
