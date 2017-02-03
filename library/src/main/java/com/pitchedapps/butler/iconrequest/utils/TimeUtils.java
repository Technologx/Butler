package com.pitchedapps.butler.iconrequest.utils;

import java.util.Calendar;

public class TimeUtils {
    /**
     * This method returns current time in milliseconds
     *
     * @return time in milliseconds
     */
    public static long getCurrentTimeInMillis() {
        return Calendar.getInstance().getTimeInMillis();
    }

}