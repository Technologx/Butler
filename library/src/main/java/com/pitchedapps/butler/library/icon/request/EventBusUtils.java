package com.pitchedapps.butler.library.icon.request;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Allan Wang on 2016-08-28.
 */
class EventBusUtils {

    static void post(Object o, EventState e) {
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
