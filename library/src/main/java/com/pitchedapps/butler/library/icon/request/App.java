package com.pitchedapps.butler.library.icon.request;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.pitchedapps.butler.library.icon.request.glide.AppIconLoader;

/**
 * Created by Allan Wang on 2016-08-20.
 */
public class App implements Parcelable {

    private String mName;
    private String mCode;
    private String mPkg;
    private boolean mRequested;

    private transient Drawable mIcon;

    public App() {
    }

    App(String name, String code, String pkg, boolean requested) {
        mName = name;
        mCode = code;
        mPkg = pkg;
        mRequested = requested;
    }

    public Drawable getIcon(Context context) {
        if (mIcon == null) {
            final ApplicationInfo ai = getAppInfo(context);
            if (ai != null)
                mIcon = ai.loadIcon(context.getPackageManager());
        }
        return mIcon;
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
        mRequested = in.readByte() != 0x00;
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
        dest.writeByte((byte) (mRequested ? 0x01 : 0x00));
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