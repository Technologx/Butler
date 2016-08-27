package com.pitchedapps.butler.library.icon.request;

import android.support.annotation.Nullable;

import com.pitchedapps.butler.library.icon.request.App;

import java.util.ArrayList;

/**
 * Created by Allan Wang on 2016-08-27.
 */
public class AppLoadedEvent {

    private ArrayList<App> apps;
    private Exception e;

    AppLoadedEvent(@Nullable ArrayList<App> a, @Nullable Exception ee) {
        apps = a;
        e = ee;
    }

    public ArrayList<App> getApps() {
        return apps;
    }

    public Exception getException() {
        return e;
    }

    public boolean hasException() {
        return e != null;
    }
}
