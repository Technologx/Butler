package com.pitchedapps.butler.library.icon.request;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.pitchedapps.butler.library.icon.request.glide.AppIconLoader;

/**
 * Created by Allan Wang on 2016-08-20.
 */
public class App implements Parcelable {

    private String mName;
    private String mCode;
    private String mPkg;

    private transient Drawable mIcon;
    private transient Drawable mIconHighRes;

    App(String name, String code, String pkg) {
        mName = name;
        mCode = code;
        mPkg = pkg;
    }

    public Drawable getIcon(Context context) {
        if (mIcon == null) {
            final ApplicationInfo ai = getAppInfo(context);
            if (ai != null)
                mIcon = ai.loadIcon(context.getPackageManager());
        }
        return mIcon;
    }

    public Drawable getHighResIcon(Context context) {
        if (mIconHighRes == null) {
            final ApplicationInfo ai = getAppInfo(context);
            if (ai == null || ai.icon == 0) return getIcon(context);
            final Resources mRes = getResources(context, ai);
            if (mRes == null) return getIcon(context);
            int iconDpi;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                iconDpi = DisplayMetrics.DENSITY_XXXHIGH;
            } else {
                iconDpi = DisplayMetrics.DENSITY_XXHIGH;
            }
            mIconHighRes = ResourcesCompat.getDrawableForDensity(mRes, ai.icon, iconDpi, null);
        }
        return mIconHighRes;
    }

    public void loadIcon(ImageView into) {
        if (IRUtils.inClassPath("com.bumptech.glide.load.model.ModelLoader")) {
            AppIconLoader.display(into, this);
        } else {
            into.setImageDrawable(getIcon(into.getContext()));
        }
    }

    public String getName() {
        return mName;
    }

    public String getCode() {
        return mCode;
    }

    public String getPackage() {
        return mPkg;
    }

    @Nullable
    public ApplicationInfo getAppInfo(Context context) {
        try {
            return context.getPackageManager().getApplicationInfo(mPkg, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    @Nullable
    public ActivityInfo getActivityInfo(Context context) {
        try {
            return context.getPackageManager().getActivityInfo(new ComponentName(mCode.split("/")[0], mCode.split("/")[1]), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    @Nullable
    public Resources getResources(Context context, ApplicationInfo ai) {
        try {
            return context.getPackageManager().getResourcesForApplication(ai);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return mCode;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof App && ((App) o).getCode().equals(getCode());
    }

    protected App(Parcel in) {
        mName = in.readString();
        mCode = in.readString();
        mPkg = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mCode);
        dest.writeString(mPkg);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<App> CREATOR = new Parcelable.Creator<App>() {
        @Override
        public App createFromParcel(Parcel in) {
            return new App(in);
        }

        @Override
        public App[] newArray(int size) {
            return new App[size];
        }
    };
}