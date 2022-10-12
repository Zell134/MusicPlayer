package com.zell.musicplayer;

import static org.assertj.core.api.Assertions.assertThat;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import io.appium.java_client.android.AndroidDriver;

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Test
    public void useAppContext() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertThat(appContext.getPackageName()).isEqualTo("com.zell.musicplayer");
    }

    @Test
    public void testAppium() throws InterruptedException {
        AndroidDriver driver = new DriverManager().getInstance();
        WebElement el = driver.findElement(By.id("com.zell.musicplayer:id/play"));
        assertThat(el).isNotNull();
        el.click();
        Thread.sleep(2000);
        driver.quit();
    }
}