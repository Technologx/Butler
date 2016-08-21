package com.pitchedapps.butler.library.icon.request;

/**
 * Created by Allan Wang on 2016-08-20.
 */
public interface RequestSendCallback {

    void onRequestPreparing();

    void onRequestError(Exception e);

    void onRequestSent();
}