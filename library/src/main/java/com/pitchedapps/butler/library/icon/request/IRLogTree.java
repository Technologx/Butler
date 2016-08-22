package com.pitchedapps.butler.library.icon.request;

import android.util.Log;

import timber.log.Timber;

/**
 * Created by Allan Wang on 2016-08-21.
 */
public class IRLogTree extends Timber.DebugTree {

    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        switch (priority) {
            case IRLog.DEBUG:
                priority = Log.DEBUG;
                break;
            case IRLog.ERROR:
                priority = Log.ERROR;
                break;
            default:
                return;
        }
        super.log(priority, tag, message, t);
    }
}
