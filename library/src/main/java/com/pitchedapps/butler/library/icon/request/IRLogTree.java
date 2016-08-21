package com.pitchedapps.butler.library.icon.request;

import timber.log.Timber;

/**
 * Created by Allan Wang on 2016-08-21.
 */
public class IRLogTree extends Timber.DebugTree {
    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        if (tag == null || !tag.equals("IR")) return;
        super.log(priority, tag, message, t);
    }
}
