package com.pitchedapps.butler.iconrequest;

public abstract class RequestReadyCallback {
    public abstract void onRequestReady();

    public void onRequestLimited(long millis) {
    }
}