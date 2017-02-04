package com.pitchedapps.butler.iconrequest;

import android.content.Context;

public abstract class RequestsCallback {
    public abstract void onRequestLimited(Context context, @IconRequest.State int reason, int
            requestsLeft, long millis);

    public abstract void onRequestEmpty(Context context);
}