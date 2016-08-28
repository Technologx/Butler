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
        switch (percent) {
            case -2:
                return "Loading Appfilter...";
            case -1:
                return "Retrieving App List...";
            default:
                return String.format(Locale.getDefault(), "Loading %d%%", percent);
        }
    }
}
