package com.zell.musicplayer.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Supplier;

public class AssertionsHelper {

    public interface Action{
        void execute();
    }

    /**
     * Compare required value before and after some actions.
     *
     *@param getValue getter for the required value
     *@param actions actions to execute
     *@param isShouldBeChanged should actual and expected values be equals or not
     */
    public static void checkValueAfterAction(Supplier getValue, Action actions, boolean isShouldBeChanged){
        String expectedValue = (String) getValue.get();
        actions.execute();
        String actualValue = (String) getValue.get();
        String description = isShouldBeChanged ? "Value has been changed" : "Value has not been changed";
        assertThat(actualValue)
                .as(description)
                .matches(value -> isShouldBeChanged != value.equals(expectedValue));
    }
}
