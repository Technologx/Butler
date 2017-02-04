package com.pitchedapps.butler.iconrequest;

public abstract class RequestsCallback {
    public abstract void onRequestLimited(@IconRequest.State int reason, int requestsLeft, long
            millis);

    public abstract void onRequestEmpty();
}