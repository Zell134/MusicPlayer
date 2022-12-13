package com.zell.musicplayer.tests;


import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.zell.musicplayer.activities.MainActivity;

import org.junit.Rule;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public abstract class  BaseTest {

    private MainActivity activity;

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule = new ActivityScenarioRule<>(MainActivity.class);

    protected MainActivity getActivity(){
        activityScenarioRule.getScenario().onActivity(activity -> {
            this.activity = activity;
        });
        return activity;
    }

}

