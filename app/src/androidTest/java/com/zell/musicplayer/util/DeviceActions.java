package com.zell.musicplayer.util;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiSelector;

public class DeviceActions {

    private static UiDevice device = UiDevice.getInstance(getInstrumentation());

    public static UiObject findObject(UiSelector selector){
        return device.findObject(selector);
    }

    public static void openNotifications() {
        device.openNotification();
    }

    public static void pressBack() {
        device.pressBack();
    }
}
