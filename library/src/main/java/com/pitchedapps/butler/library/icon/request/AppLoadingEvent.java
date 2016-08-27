package com.pitchedapps.butler.library.icon.request;

import java.util.Locale;

/**
 * Created by Allan Wang on 2016-08-27.
 */
public class AppLoadingEvent {

    private int percent;

    AppLoadingEvent(int i) {
        percent = i;
    }

    /**
     * Gets percentage loaded; -1 refers to a loading AppFilter
     * @return
     */
    public int getPercent() {
        return percent;
    }

    public String getString() {
        if (percent == -1) {
            return "Loading Appfilter...";
        }
        return String.format(Locale.getDefault(), "Loading %d%%", percent);
    }
}
