package com.pitchedapps.butler.library.icon.request;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import timber.log.Timber;

/**
 * Created by Allan Wang on 2016-08-20.
 */
class ComponentInfoUtil {

    private static class NameComparator implements Comparator<ApplicationInfo> {

        private PackageManager mPM;

        public NameComparator(PackageManager pm) {
            mPM = pm;
        }

        @Override
        public int compare(ApplicationInfo aa, ApplicationInfo ab) {
            CharSequence sa = mPM.getApplicationLabel(aa);
            if (sa == null) {
                sa = aa.packageName;
            }
            CharSequence sb = mPM.getApplicationLabel(ab);
            if (sb == null) {
                sb = ab.packageName;
            }
            return sa.toString().compareTo(sb.toString());
        }
    }

    public static ArrayList<App> getInstalledApps(final Context context,
                                                  final HashSet<String> filter,
                                                  final AppsLoadCallback cb,
                                                  final Handler handler) {
        IRUtils.startTimer("getInstalledApps");
        final PackageManager pm = context.getPackageManager();
        final List<ApplicationInfo> appInfos = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        try {
            Collections.sort(appInfos, new NameComparator(pm));
        } catch (Throwable t) {
            t.printStackTrace();
        }
        final ArrayList<App> apps = new ArrayList<>();

        int loaded = 0;
        int filtered = 0;
        for (ApplicationInfo ai : appInfos) {
            final Intent launchIntent = pm.getLaunchIntentForPackage(ai.packageName);
            if (launchIntent == null)
                continue;

            String launchStr = launchIntent.toString();
            launchStr = launchStr.substring(launchStr.indexOf("cmp=") + "cmp=".length());
            launchStr = launchStr.substring(0, launchStr.length() - 2);

            final String[] splitCode = launchStr.split("/");
            if (splitCode[1].startsWith("."))
                launchStr = splitCode[0] + "/" + splitCode[0] + splitCode[1];

            if (filter.contains(launchStr)) {
                filtered++;
                IRLog.d("Filtered %s", launchStr);
                continue;
            }

//            IRLog.d("Loaded %s", launchStr);
            final String name = ai.loadLabel(pm).toString();
            apps.add(new App(name, launchStr, ai.packageName));

            loaded++;
            final int percent = (loaded / appInfos.size()) * 100;
            if (cb != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        cb.onAppsLoadProgress(percent);
                    }
                });
            }
        }

        IRLog.d("Loaded %d total app(s), filtered out %d app(s).", apps.size(), filtered);
        IRUtils.stopTimer("getInstalledApps");
        return apps;
    }

    public static ArrayList<App> getInstalledApps2(final Context context,
                                                   final HashSet<String> filter,
                                                   final AppsLoadCallback cb,
                                                   final Handler handler) {
        IRUtils.startTimer("getInstalledApps2");
        final PackageManager pm = context.getPackageManager();
        final List<ResolveInfo> packageList =
                pm.queryIntentActivities(
                        new Intent("android.intent.action.MAIN")
                                .addCategory("android.intent.category.LAUNCHER"), 0);
        Collections.sort(packageList, new Comparator<ResolveInfo>() {
            @Override
            public int compare(ResolveInfo a, ResolveInfo b) {
                String initialName = pm.getApplicationLabel(a.activityInfo.applicationInfo).toString();
                String finalName = pm.getApplicationLabel(b.activityInfo.applicationInfo).toString();
                return initialName.compareToIgnoreCase(finalName);
            }
        });

        final ArrayList<App> apps = new ArrayList<>();

        int loaded = 0;
        int filtered = 0;
        for (ResolveInfo ri : packageList) {
            String launchStr = ri.activityInfo.packageName + "/" + ri.activityInfo.name;

            if (filter.contains(launchStr)) {
                filtered++;
                IRLog.d("Filtered %s", launchStr);
                continue;
            }

//            IRLog.d("Loaded %s", launchStr);
            final String name = ri.loadLabel(pm).toString();
            apps.add(new App(name, launchStr, ri.activityInfo.packageName));

            loaded++;
            final int percent = (loaded / packageList.size()) * 100;
            if (cb != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        cb.onAppsLoadProgress(percent);
                    }
                });
            }
        }

        IRLog.d("Loaded %d total app(s), filtered out %d app(s).", apps.size(), filtered);
        IRUtils.stopTimer("getInstalledApps2");
        return apps;
    }

    private ComponentInfoUtil() {
    }
}