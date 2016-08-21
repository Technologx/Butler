package com.pitchedapps.butler.library.icon.request;

import java.util.ArrayList;

/**
 * Created by Allan Wang on 2016-08-20.
 */
public interface AppsLoadCallback {

    void onLoadingFilter();

    void onAppsLoaded(ArrayList<App> apps, Exception e);

    void onAppsLoadProgress(int percent);
}