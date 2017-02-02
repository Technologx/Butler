package com.pitchedapps.butler.iconrequest.utils;

import com.pitchedapps.butler.iconrequest.events.EventState;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Allan Wang on 2016-08-28.
 */
public class EventBusUtils {

    public static void post(Object o, EventState e) {
        switch (e) {
            case ENABLED:
                EventBus.getDefault().post(o);
                break;
            case STICKIED:
                EventBus.getDefault().postSticky(o);
            case DISABLED:
                break;
        }
    }

}