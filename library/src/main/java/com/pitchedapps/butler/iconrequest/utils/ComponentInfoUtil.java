package com.pitchedapps.butler.iconrequest.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.pitchedapps.butler.iconrequest.App;
import com.pitchedapps.butler.iconrequest.events.AppLoadingEvent;
import com.pitchedapps.butler.iconrequest.events.EventState;
import com.pitchedapps.butler.iconrequest.logs.IRLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Allan Wang on 2016-08-20.
 */
public class ComponentInfoUtil {

    private static class NameComparator implements Comparator<ResolveInfo> {
        private final PackageManager mPM;

        public NameComparator(PackageManager pm) {
            mPM = pm;
        }

        @Override
        public int compare(ResolveInfo ra, ResolveInfo rb) {
            CharSequence sa = ra.loadLabel(mPM);
            if (sa == null) {
                sa = ra.activityInfo.packageName;
            }
            CharSequence sb = rb.loadLabel(mPM);
            if (sb == null) {
                sb = rb.activityInfo.packageName;
            }
            return sa.toString().compareTo(sb.toString());
        }
    }

    public static ArrayList<App> getInstalledApps(final Context context,
                                                  final HashSet<String> filter,
                                                  final EventState loadingState) {
        IRUtils.startTimer("getInstalledApps");
        final PackageManager pm = context.getPackageManager();
        EventBusUtils.post(new AppLoadingEvent(-1), loadingState);
        final List<ResolveInfo> packageList =
                pm.queryIntentActivities(
                        new Intent("android.intent.action.MAIN")
                                .addCategory("android.intent.category.LAUNCHER"), 0);

        // TODO: This throws IllegalArgumentException: Comparison method violates its general
        // contract!
        Collections.sort(packageList, new NameComparator(pm));

        final ArrayList<App> apps = new ArrayList<>();

        int loaded = 0;
        int filtered = 0;
        for (ResolveInfo ri : packageList) {
            String riPkg = ri.activityInfo.packageName;
            String launchStr = riPkg + "/" + ri.activityInfo.name;

            if ((filter.contains(launchStr)) || (context.getPackageName().equals(riPkg))) {
                filtered++;
                IRLog.d("Filtered %s", launchStr);
                continue;
            }

//            IRLog.d("Loaded %s", launchStr);
            CharSequence name = ri.loadLabel(pm);
            if (name == null) name = ri.activityInfo.packageName;
            apps.add(new App(IRUtils.getLocalizedName(context, launchStr, name.toString()),
                    launchStr, ri.activityInfo.packageName));
            loaded++;
            final int percent = (loaded * 100 / packageList.size());
            EventBusUtils.post(new AppLoadingEvent(percent), loadingState);
        }

        IRLog.d("Loaded %d total app(s), filtered out %d app(s).", apps.size(), filtered);
        IRUtils.stopTimer("getInstalledApps");
        return apps;
    }

    private ComponentInfoUtil() {
    }
}