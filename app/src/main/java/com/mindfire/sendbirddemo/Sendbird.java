package com.mindfire.sendbirddemo;

import android.app.Application;
import android.content.Context;

import com.sendbird.android.SendBird;

import static com.mindfire.sendbirddemo.constants.AppDetails.APP_ID;

/**
 * Application Class
 * Created by Vyom on 1/3/2017.
 */

public class Sendbird extends Application {
    private static Context sContext;

    public static Context getContext() {
        return sContext;
    }

    private static void setContext(Context sContext) {
        Sendbird.sContext = sContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setContext(this);
        SendBird.init(APP_ID, sContext);
    }
}
