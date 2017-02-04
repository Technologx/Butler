package com.pitchedapps.butler.iconrequest.events;

public abstract class OnRequestProgress {
    public abstract void doWhenStarted();

    public void updateWithProgress(int progress) {
    }

    public abstract void doWhenReady();
}