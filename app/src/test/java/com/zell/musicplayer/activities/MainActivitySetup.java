package com.zell.musicplayer.activities;

import android.app.Application;

import androidx.test.core.app.ApplicationProvider;

import com.zell.musicplayer.services.PermissionsService;

import org.junit.Before;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowApplication;

public class MainActivitySetup {

    protected ActivityController controller;
    protected MainActivity activity;

    @Before
    public void setupEnvironment() {
        ShadowApplication app = Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext());
        app.grantPermissions(PermissionsService.PERMISSION_STRING);

        controller = Robolectric.buildActivity(MainActivity.class);

        activity = (MainActivity) controller
                .create()
                .resume()
                .visible()
                .get();
    }
}
