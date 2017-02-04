package com.pitchedapps.butler.iconrequest.events;

import android.content.Context;

import com.pitchedapps.butler.iconrequest.IconRequest;

public abstract class RequestsCallback {
    public abstract void onRequestLimited(Context context, @IconRequest.State int reason, int
            requestsLeft, long millis);

    public abstract void onRequestEmpty(Context context);
}