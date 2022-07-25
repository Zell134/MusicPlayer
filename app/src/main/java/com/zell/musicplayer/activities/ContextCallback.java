package com.zell.musicplayer.activities;

import android.content.Context;

public class ContextCallback {
    private static Context mContext;

    private static ContextCallback contextCallback = new ContextCallback();

    private ContextCallback(){
        super();
    }

    public static ContextCallback getInstance() {
        return contextCallback;
    }

    public void setContext(Context context) {
        if (mContext != null)
            return;

        mContext = context;
    }

    public boolean contextAssigned() {
        return mContext != null;
    }

    public Context getContext() {
        return mContext;
    }

    public static void freeContext() {
        mContext = null;
    }
}
