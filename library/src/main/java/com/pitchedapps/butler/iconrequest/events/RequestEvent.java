package com.pitchedapps.butler.iconrequest.events;

import android.support.annotation.Nullable;

/**
 * Created by Allan Wang on 2016-08-27.
 */
public class RequestEvent {

    private boolean isPreparing, isSent;
    private Exception exception;

    public RequestEvent(boolean prep, boolean sent, @Nullable Exception e) {
        isPreparing = prep;
        isSent = sent;
        exception = e;
    }

    public boolean isPreparing() {
        return isPreparing;
    }

    public boolean isSent() {
        return isSent;
    }

    public Exception getException() {
        return exception;
    }
}
