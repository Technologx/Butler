package com.pitchedapps.butler.library.icon.request;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Allan Wang on 2016-08-20.
 */
class IRLog {

    public static void log(@Nullable String tag, @NonNull String msg, @Nullable Object... args) {
        if(tag == null) tag = "IconRequest";
        if (args != null)
            msg = String.format(msg, args);
        Log.d(tag, msg);
    }

    private IRLog() {
    }
}