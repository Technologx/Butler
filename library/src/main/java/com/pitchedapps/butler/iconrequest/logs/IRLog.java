package com.pitchedapps.butler.iconrequest.logs;

import timber.log.Timber;

/**
 * Created by Allan Wang on 2016-08-21.
 */
public class IRLog {

    public static final int DEBUG = 11, ERROR = 66;

    public static void d(String message, Object... o ) {
        Timber.log(DEBUG, message, o);
    }

    public static void e(String message, Object... o ) {
        Timber.log(ERROR, message, o);
    }
}
