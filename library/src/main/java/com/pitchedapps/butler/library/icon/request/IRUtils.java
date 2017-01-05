package com.pitchedapps.butler.library.icon.request;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Allan Wang on 2016-08-20.
 */
public class IRUtils {

    public static boolean isEmpty(@Nullable String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean inClassPath(@NonNull String clsName) {
        try {
            Class.forName(clsName);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static String drawableName(String appName) {
        if (Character.isDigit(appName.charAt(0))) appName = "a_" + appName;
        return appName.toLowerCase(Locale.getDefault()).replace(" ", "_");
    }

    public static String getOSVersionName(int sdkInt) {
        switch (sdkInt) {
            default:
                return "";
            case Build.VERSION_CODES.CUPCAKE:
                return "Cupcake";
            case Build.VERSION_CODES.DONUT:
                return "Donut";
            case Build.VERSION_CODES.ECLAIR:
            case Build.VERSION_CODES.ECLAIR_0_1:
            case Build.VERSION_CODES.ECLAIR_MR1:
                return "Eclair";
            case Build.VERSION_CODES.FROYO:
                return "Froyo";
            case Build.VERSION_CODES.GINGERBREAD:
            case Build.VERSION_CODES.GINGERBREAD_MR1:
                return "Gingerbread";
            case Build.VERSION_CODES.HONEYCOMB:
            case Build.VERSION_CODES.HONEYCOMB_MR1:
            case Build.VERSION_CODES.HONEYCOMB_MR2:
                return "Honeycomb";
            case Build.VERSION_CODES.ICE_CREAM_SANDWICH:
            case Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1:
                return "Ice Cream Sandwich";
            case Build.VERSION_CODES.JELLY_BEAN:
            case Build.VERSION_CODES.JELLY_BEAN_MR1:
            case Build.VERSION_CODES.JELLY_BEAN_MR2:
                return "Jelly Bean";
            case Build.VERSION_CODES.KITKAT:
                return "KitKat";
            case Build.VERSION_CODES.KITKAT_WATCH:
                return "KitKat Watch";
            case Build.VERSION_CODES.LOLLIPOP:
            case Build.VERSION_CODES.LOLLIPOP_MR1:
                return "Lollipop";
            case Build.VERSION_CODES.M:
                return "Marshmallow";
        }
    }

    private IRUtils() {
    }

    private static HashMap<String, Long> mTimers;

    public static void startTimer(@NonNull String key) {
        if (mTimers == null) mTimers = new HashMap<>();
        if (key.isEmpty()) {
            IRLog.e("Invalid key. It's empty");
            return;
        }
        mTimers.put(key, System.currentTimeMillis());
    }

    public static void stopTimer(@NonNull String key) {
        if (key.isEmpty()) {
            IRLog.e("Invalid key. It's empty");
            return;
        }
        if (mTimers == null || !mTimers.containsKey(key)) {
            IRLog.e("Invalid timer", key);
            return;
        }
        long timeDiff = System.currentTimeMillis() - mTimers.get(key);
        IRLog.d("Timer " + key, "took " + timeDiff + "ms");
        mTimers.remove(key);
        if (mTimers.isEmpty()) mTimers = null;
    }

    public static void clearTimers() {
        mTimers = null;
    }

    public static boolean hasNetwork(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context
                .CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}