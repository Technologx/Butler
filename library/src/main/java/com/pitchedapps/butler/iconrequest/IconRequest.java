package com.pitchedapps.butler.iconrequest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.CallSuper;
import android.support.annotation.CheckResult;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.annotation.XmlRes;
import android.text.Html;

import com.pitchedapps.butler.R;
import com.pitchedapps.butler.iconrequest.events.AppLoadedEvent;
import com.pitchedapps.butler.iconrequest.events.AppLoadingEvent;
import com.pitchedapps.butler.iconrequest.events.AppSelectionEvent;
import com.pitchedapps.butler.iconrequest.events.EventState;
import com.pitchedapps.butler.iconrequest.events.OnRequestProgress;
import com.pitchedapps.butler.iconrequest.events.RequestEvent;
import com.pitchedapps.butler.iconrequest.events.RequestsCallback;
import com.pitchedapps.butler.iconrequest.logs.IRLog;
import com.pitchedapps.butler.iconrequest.logs.IRLogTree;
import com.pitchedapps.butler.iconrequest.utils.ComponentInfoUtil;
import com.pitchedapps.butler.iconrequest.utils.EventBusUtils;
import com.pitchedapps.butler.iconrequest.utils.FileUtil;
import com.pitchedapps.butler.iconrequest.utils.IRUtils;
import com.pitchedapps.butler.iconrequest.utils.ZipUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

/**
 * Created by Allan Wang on 2016-08-20.
 */
public final class IconRequest {

    @IntDef({STATE_NORMAL, STATE_LIMITED, STATE_TIME_LIMITED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
    }

    public static final int STATE_NORMAL = 0;
    public static final int STATE_LIMITED = 1;
    public static final int STATE_TIME_LIMITED = 2;

    public static final int INTENT_CODE = 99;

    @State
    private int state = STATE_NORMAL;

    private Builder mBuilder;
    private ArrayList<App> mApps;
    private ArrayList<App> mSelectedApps;

    private static final String KEY_SAVED_TIME_MILLIS = "saved_time_millis";
    private static final String MAX_APPS = "apps_to_request";

    private static IconRequest mRequest;

    private IconRequest() {
        mSelectedApps = new ArrayList<>();
    }

    private IconRequest(@NonNull Builder builder) {
        this();
        mBuilder = builder;
        mRequest = this;
        if (mBuilder.mDebugMode) {
            Timber.plant(new IRLogTree());
        }
    }

    public static class Builder implements Parcelable {

        protected transient Context mContext;
        protected File mSaveDir = null;
        protected int mFilterId = -1;
        protected String mAppName = "Default App";
        protected String mEmail = null;
        protected String mSubject = "Icon Request";
        protected String mHeader = "These apps aren't themed. Thanks in advance";
        protected String mFooter = null;
        protected int mMaxCount = 0;
        protected long mTimeLimit = -1;
        protected boolean mIsLoading = false;
        protected boolean mHasMaxCount = false;
        protected boolean mNoneSelectsAll = false;
        protected boolean mIncludeDeviceInfo = true;
        protected boolean mComments = true;
        protected boolean mGenerateAppFilterXml = true;
        protected boolean mGenerateAppMapXml = true;
        protected boolean mGenerateThemeResourcesXml = true;
        protected boolean mGenerateAppFilterJson = false;
        protected boolean mErrorOnInvalidAppFilterDrawable = true;
        protected boolean mDebugMode = false;
        protected SharedPreferences mPrefs = null;
        protected RequestsCallback mCallback = null;
        protected EventState mLoadingState = EventState.DISABLED,
                mLoadedState = EventState.STICKIED,
                mSelectionState = EventState.DISABLED,
                mRequestState = EventState.DISABLED;

        public Builder() {
        }

        public Builder(@NonNull Context context) {
            mContext = context;
            mSaveDir = new File(Environment.getExternalStorageDirectory(), "IconRequest");
        }

        public Builder filterXmlId(@XmlRes int resId) {
            mFilterId = resId;
            return this;
        }

        public Builder filterOff() {
            mFilterId = -1;
            return this;
        }

        public Builder saveDir(@NonNull File file) {
            mSaveDir = file;
            return this;
        }

        public Builder toEmail(@NonNull String email) {
            mEmail = email;
            return this;
        }

        public Builder withAppName(@Nullable String appName, @Nullable Object... args) {
            if (args != null && appName != null)
                appName = String.format(appName, args);
            mAppName = appName;
            return this;
        }

        public Builder withSubject(@Nullable String subject, @Nullable Object... args) {
            if (args != null && subject != null)
                subject = String.format(subject, args);
            mSubject = subject;
            return this;
        }

        public Builder withHeader(@Nullable String header, @Nullable Object... args) {
            if (args != null && header != null)
                header = String.format(header, args);
            mHeader = header;
            return this;
        }

        public Builder withFooter(@Nullable String footer, @Nullable Object... args) {
            if (args != null && footer != null)
                footer = String.format(footer, args);
            mFooter = footer;
            return this;
        }

        public Builder maxSelectionCount(@IntRange(from = 0) int count) {
            mMaxCount = count;
            mHasMaxCount = mMaxCount > 0;
            return this;
        }

        public Builder withTimeLimit(int minutes, SharedPreferences prefs) {
            mTimeLimit = TimeUnit.MINUTES.toMillis(minutes);
            mPrefs = prefs != null ? prefs : mContext.getSharedPreferences("ButlerPrefs", Context
                    .MODE_PRIVATE);
            return this;
        }

        public Builder withComments(boolean b) {
            mComments = b;
            return this;
        }

        public Builder noSelectionSelectsAll(boolean b) {
            mNoneSelectsAll = b;
            return this;
        }

        public Builder includeDeviceInfo(boolean include) {
            mIncludeDeviceInfo = include;
            return this;
        }

        public Builder generateAppFilterXml(boolean generate) {
            mGenerateAppFilterXml = generate;
            return this;
        }

        public Builder generateAppMapXml(boolean generate) {
            mGenerateAppMapXml = generate;
            return this;
        }

        public Builder generateThemeResourcesXml(boolean generate) {
            mGenerateThemeResourcesXml = generate;
            return this;
        }

        public Builder generateAppFilterJson(boolean generate) {
            mGenerateAppFilterJson = generate;
            return this;
        }

        public Builder errorOnInvalidFilterDrawable(boolean error) {
            mErrorOnInvalidAppFilterDrawable = error;
            return this;
        }

        public Builder debugMode(boolean debug) {
            mDebugMode = debug;
            return this;
        }

        public Builder loadingEvents(EventState state) {
            mLoadingState = state;
            return this;
        }

        public Builder loadedEvents(EventState state) {
            mLoadedState = state;
            return this;
        }

        public Builder selectionEvents(EventState state) {
            mSelectionState = state;
            return this;
        }

        public Builder requestEvents(EventState state) {
            mRequestState = state;
            return this;
        }

        public Builder setCallback(RequestsCallback callback) {
            mCallback = callback;
            return this;
        }

        public IconRequest build() {
            return new IconRequest(this);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeSerializable(mSaveDir);
            dest.writeInt(mFilterId);
            dest.writeString(mAppName);
            dest.writeString(mEmail);
            dest.writeString(mSubject);
            dest.writeString(mHeader);
            dest.writeString(mFooter);
            dest.writeInt(mMaxCount);
            dest.writeLong(mTimeLimit);
            dest.writeByte((byte) (mIsLoading ? 1 : 0));
            dest.writeByte((byte) (mHasMaxCount ? 1 : 0));
            dest.writeByte((byte) (mNoneSelectsAll ? 1 : 0));
            dest.writeByte((byte) (mIncludeDeviceInfo ? 1 : 0));
            dest.writeByte((byte) (mComments ? 1 : 0));
            dest.writeByte((byte) (mGenerateAppFilterXml ? 1 : 0));
            dest.writeByte((byte) (mGenerateAppMapXml ? 1 : 0));
            dest.writeByte((byte) (mGenerateThemeResourcesXml ? 1 : 0));
            dest.writeByte((byte) (mGenerateAppFilterJson ? 1 : 0));
            dest.writeByte((byte) (mErrorOnInvalidAppFilterDrawable ? 1 : 0));
            dest.writeByte((byte) (mDebugMode ? 1 : 0));
            dest.writeInt(mLoadingState == null ? -1 : mLoadingState.ordinal());
            dest.writeInt(mLoadedState == null ? -1 : mLoadedState.ordinal());
            dest.writeInt(mSelectionState == null ? -1 : mSelectionState.ordinal());
            dest.writeInt(mRequestState == null ? -1 : mRequestState.ordinal());
        }

        protected Builder(Parcel in) {
            mSaveDir = (File) in.readSerializable();
            mFilterId = in.readInt();
            mAppName = in.readString();
            mEmail = in.readString();
            mSubject = in.readString();
            mHeader = in.readString();
            mFooter = in.readString();
            mMaxCount = in.readInt();
            mTimeLimit = in.readLong();
            mIsLoading = in.readByte() != 0;
            mHasMaxCount = in.readByte() != 0;
            mNoneSelectsAll = in.readByte() != 0;
            mIncludeDeviceInfo = in.readByte() != 0;
            mComments = in.readByte() != 0;
            mGenerateAppFilterXml = in.readByte() != 0;
            mGenerateAppMapXml = in.readByte() != 0;
            mGenerateThemeResourcesXml = in.readByte() != 0;
            mGenerateAppFilterJson = in.readByte() != 0;
            mErrorOnInvalidAppFilterDrawable = in.readByte() != 0;
            mDebugMode = in.readByte() != 0;
            int tmpMLoadingState = in.readInt();
            mLoadingState = tmpMLoadingState == -1 ? null : EventState.values()[tmpMLoadingState];
            int tmpMLoadedState = in.readInt();
            mLoadedState = tmpMLoadedState == -1 ? null : EventState.values()[tmpMLoadedState];
            int tmpMSelectionState = in.readInt();
            mSelectionState = tmpMSelectionState == -1 ? null : EventState.values()
                    [tmpMSelectionState];
            int tmpMRequestState = in.readInt();
            mRequestState = tmpMRequestState == -1 ? null : EventState.values()[tmpMRequestState];
        }

        public static final Creator<Builder> CREATOR = new Creator<Builder>() {
            @Override
            public Builder createFromParcel(Parcel in) {
                return new Builder(in);
            }

            @Override
            public Builder[] newArray(int size) {
                return new Builder[size];
            }
        };
    }

    public static Builder start(Context context) {
        return new Builder(context);
    }

    public static IconRequest get() {
        return mRequest;
    }

    private StringBuilder mInvalidDrawables;

    @CallSuper
    @CheckResult
    @Nullable
    private HashSet<String> loadFilterApps() {
        IRUtils.startTimer("LFAXML");
        final HashSet<String> defined = new HashSet<>();
        if (mBuilder.mFilterId == -1) { //TODO add this
            IRUtils.stopTimer("LFAXML");
            return defined;
        }
        XmlResourceParser parser = null;
        try {
            parser = mBuilder.mContext.getResources().getXml(mBuilder.mFilterId);
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String mAppCode;
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        final String tagName = parser.getName();
                        if (tagName.equals("item")) {
                            try {
                                // Read package and activity name
                                mAppCode = parser.getAttributeValue(null, "component");
                                mAppCode = mAppCode.substring(14, mAppCode.length() - 1);
                                //wrapped in ComponentInfo{[Component]} TODO add checker?
                                //TODO check for valid drawable
                                // Add new info to our ArrayList and reset the object.
                                defined.add(mAppCode);
                            } catch (Exception e) {
                                // TODO Remove this
                                e.printStackTrace();
                                IRLog.d("Error adding parsed appfilter item! Due to Exception: "
                                        + e.getMessage());
                            }
                        }
                        break;
                }
                eventType = parser.next();
            }

        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        } finally {
            if (parser != null)
                parser.close();
        }
        IRUtils.stopTimer("LFAXML");
        return defined;
    }

//    @CheckResult
//    @Nullable
//    private HashSet<String> loadFilterApps2() {
//        IRUtils.startTimer("LFAReader");
//        final HashSet<String> defined = new HashSet<>();
//        if (IRUtils.isEmpty(mBuilder.mFilterName)) {
//            IRUtils.stopTimer("LFAReader");
//            return defined;
//        }
//
//        InputStream is;
//        try {
//            final AssetManager am = mBuilder.mContext.getAssets();
//            IRLog.d("Loading your appfilter, opening: %s", mBuilder.mFilterName);
//            is = am.open(mBuilder.mFilterName);
//        } catch (final Throwable e) {
//            e.printStackTrace();
//            mBuilder.mIsLoading = false;
//            EventBusUtils.post(new AppLoadedEvent(null, new Exception("Failed to open your
// filter: " + e.getLocalizedMessage(), e)), mBuilder.mLoadedState);
//            IRUtils.stopTimer("LFAReader");
//            return null;
//        }
//
//        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
//        try {
//            final String itemEndStr = "/>";
//            final String componentStartStr = "component=\"ComponentInfo";
//            final String drawableStartStr = "drawable=\"";
//            final String endStr = "\"";
//            final String commentStart = "<!--";
//            final String commentEnd = "-->";
//
//            String component = null;
//            String drawable = null;
//
//            String line;
//            boolean inComment = false;
//
//            while ((line = reader.readLine()) != null) {
//                final String trimmedLine = line.trim();
//                if (!inComment && trimmedLine.startsWith(commentStart)) {
//                    inComment = true;
//                }
//                if (inComment && trimmedLine.endsWith(commentEnd)) {
//                    inComment = false;
//                    continue;
//                }
//
//                if (inComment) continue;
//                int start;
//                int end;
//
//                start = line.indexOf(componentStartStr);
//                if (start != -1) {
//                    start += componentStartStr.length();
//                    end = line.indexOf(endStr, start);
//                    String ci = line.substring(start, end);
//                    if (ci.startsWith("{"))
//                        ci = ci.substring(1);
//                    if (ci.endsWith("}"))
//                        ci = ci.substring(0, ci.length() - 1);
//                    component = ci;
//                }
//
//                start = line.indexOf(drawableStartStr);
//                if (start != -1) {
//                    start += drawableStartStr.length();
//                    end = line.indexOf(endStr, start);
//                    drawable = line.substring(start, end);
//                }
//
//                start = line.indexOf(itemEndStr);
//                if (start != -1 && (component != null || drawable != null)) {
//                    IRLog.d("Found: %s (%s)", component, drawable);
//                    if (drawable == null || drawable.trim().isEmpty()) {
//                        IRLog.d("WARNING: Drawable shouldn't be null.");
//                        if (mBuilder.mErrorOnInvalidAppFilterDrawable) {
//                            if (mInvalidDrawables == null)
//                                mInvalidDrawables = new StringBuilder();
//                            if (mInvalidDrawables.length() > 0) mInvalidDrawables.append("\n");
//                            mInvalidDrawables.append(String.format("Drawable for %s was null or
// empty.\n", component));
//                        }
//                    } else if (mBuilder.mContext != null) {
//                        final Resources r = mBuilder.mContext.getResources();
//                        int identifier;
//                        try {
//                            identifier = r.getIdentifier(drawable, "drawable", mBuilder
// .mContext.getPackageName());
//                        } catch (Throwable t) {
//                            identifier = 0;
//                        }
//                        if (identifier == 0) {
//                            IRLog.d("WARNING: Drawable %s (for %s) doesn't match up with a
// resource.", drawable, component);
//                            if (mBuilder.mErrorOnInvalidAppFilterDrawable) {
//                                if (mInvalidDrawables == null)
//                                    mInvalidDrawables = new StringBuilder();
//                                if (mInvalidDrawables.length() > 0) mInvalidDrawables.append
// ("\n");
//                                mInvalidDrawables.append(String.format("Drawable %s (for %s)
// doesn't match up with a resource.\n", drawable, component));
//                            }
//                        }
//                    }
//                    defined.add(component);
//                }
//            }
//
//            if (mInvalidDrawables != null && mInvalidDrawables.length() > 0 &&
//                    mBuilder.mErrorOnInvalidAppFilterDrawable) {
//                mBuilder.mIsLoading = false;
//                EventBusUtils.post(new AppLoadedEvent(null, new Exception(mInvalidDrawables
// .toString())), mBuilder.mLoadedState);
//                mInvalidDrawables.setLength(0);
//                mInvalidDrawables.trimToSize();
//                mInvalidDrawables = null;
//            }
//            IRLog.d("Found %d total app(s) in your appfilter.", defined.size());
//        } catch (final Throwable e) {
//            e.printStackTrace();
//            mBuilder.mIsLoading = false;
//            EventBusUtils.post(new AppLoadedEvent(null, new Exception("Failed to read your
// filter: " + e.getMessage(), e)), mBuilder.mLoadedState);
//
//            IRUtils.stopTimer("LFAReader");
//            return null;
//        } finally {
//            FileUtil.closeQuietely(reader);
//            FileUtil.closeQuietely(is);
//        }
//        IRUtils.stopTimer("LFAReader");
//        return defined;
//    }

    public int getMaxSelectable() {
        return mBuilder.mMaxCount;
    }

    public void loadApps() {
        mBuilder.mIsLoading = true;
        EventBusUtils.post(new AppLoadingEvent(-2), mBuilder.mLoadingState);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mBuilder.mDebugMode) IRUtils.startTimer("IR_debug_auto");
                final HashSet<String> filter = loadFilterApps();
                if (filter == null) return;
                IRLog.d("Loading unthemed installed apps...");
                mApps = ComponentInfoUtil.getInstalledApps(mBuilder.mContext, filter, mBuilder
                        .mLoadingState);
                if (mBuilder.mDebugMode) IRUtils.stopTimer("IR_debug_auto");
                mBuilder.mIsLoading = false;
                EventBusUtils.post(new AppLoadedEvent(mApps, null), mBuilder.mLoadedState);
            }
        }).start();
    }

    public void loadHighResIcons() {
        if (mApps == null) {
            IRLog.d("High res load failed; app list is empty");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                IRLog.d("Getting high res icons for all apps...");
                for (App app : mApps) {
                    app.getHighResIcon(mBuilder.mContext);
                }
                IRLog.d("High res icon retrieval finished...");
            }
        }).start();
    }

    @SuppressWarnings("MalformedFormatString")
    private String getBody() {
        StringBuilder sb = new StringBuilder();
        if (!IRUtils.isEmpty(mBuilder.mHeader)) {
            sb.append(mBuilder.mHeader.replace("\n", "<br/>"));
            sb.append("<br/><br/>");
        }

        for (int i = 0; i < mSelectedApps.size(); i++) {
            if (i > 0) sb.append("<br/><br/>");
            final App app = mSelectedApps.get(i);
            sb.append(String.format("Name: <b>%s</b><br/>", app.getName()));
            sb.append(String.format("Code: <b>%s</b><br/>", app.getCode()));
            sb.append(String.format("Link: https://play.google" +
                    ".com/store/apps/details?id=%s<br/>", app.getPackage()));
        }

        if (mBuilder.mIncludeDeviceInfo) {
            sb.append("<br/><br/><br/>OS Version: ").append(System.getProperty("os.version"))
                    .append("(").append(Build.VERSION.INCREMENTAL).append(")");
            sb.append("<br/>OS API Level: ").append(Build.VERSION.SDK_INT);
            sb.append("<br/>Device: ").append(Build.MODEL);
            sb.append("<br/>Manufacturer: ").append(Build.MANUFACTURER);
            sb.append("<br/>Model (and Product): ").append(Build.DEVICE).append(" (").append
                    (Build.PRODUCT).append(")");
            PackageInfo appInfo;
            try {
                appInfo = mBuilder.mContext.getPackageManager().getPackageInfo(mBuilder.mContext
                        .getPackageName(), 0);
                sb.append("<br/>App Version Name: ").append(appInfo.versionName);
                sb.append("<br/>App Version Code: ").append(appInfo.versionCode);
            } catch (PackageManager.NameNotFoundException e) {
                sb.append("<br/>There was an error getting application version.");
            }
            if (mBuilder.mFooter != null) {
                sb.append("<br/>");
                sb.append(mBuilder.mFooter.replace("\n", "<br/>"));
            }
        } else {
            sb.append("<br/><br/>");
            sb.append(mBuilder.mFooter.replace("\n", "<br/>"));
        }
        return sb.toString();
    }

    public boolean selectApp(@NonNull App app) {
        if (mBuilder.mHasMaxCount && mSelectedApps.size() >= getRequestsLeft()) return false;
        if (!mSelectedApps.contains(app)) {
            mSelectedApps.add(app);
            EventBusUtils.post(new AppSelectionEvent(mSelectedApps.size()), mBuilder
                    .mSelectionState);
            return true;
        }
        return false;
    }

    public boolean unselectApp(@NonNull App app) {
        final boolean result = mSelectedApps.remove(app);
        if (result)
            EventBusUtils.post(new AppSelectionEvent(mSelectedApps.size()), mBuilder
                    .mSelectionState);
        return result;
    }

    public boolean toggleAppSelected(@NonNull App app) {
        final boolean result;
        if (isAppSelected(app)) {
            result = unselectApp(app);
        } else {
            if (getSelectedApps().size() >= getRequestsLeft()) {
                if (mBuilder.mCallback != null) {
                    mBuilder.mCallback.onRequestLimited(mBuilder.mContext, STATE_LIMITED,
                            getRequestsLeft(), -1);
                }
                return false;
            } else {
                result = selectApp(app);
            }
        }
        return result;
    }

    public boolean isAppSelected(@NonNull App app) {
        return mSelectedApps.contains(app);
    }

    public boolean selectAllApps() {
        if (mApps == null) return false;
        boolean changed = false;
        boolean limited = false;
        for (App app : mApps) {
            if (!mSelectedApps.contains(app)) {
                if (mBuilder.mHasMaxCount && mSelectedApps.size() >= getRequestsLeft()) {
                    limited = true;
                } else {
                    changed = true;
                    mSelectedApps.add(app);
                }
            }
        }
        if (changed)
            EventBusUtils.post(new AppSelectionEvent(mSelectedApps.size()), mBuilder
                    .mSelectionState);
        if (limited && mBuilder.mCallback != null)
            mBuilder.mCallback.onRequestLimited(mBuilder.mContext, STATE_LIMITED,
                    getRequestsLeft(), -1);
        return changed;
    }

    public boolean unselectAllApps() {
        if (mSelectedApps == null || mSelectedApps.size() == 0) return false;
        mSelectedApps.clear();
        EventBusUtils.post(new AppSelectionEvent(0), mBuilder.mSelectionState);
        return true;
    }

    public boolean isNotEmpty() {
        return getApps() != null && getApps().size() > 0;
    }

    public boolean isLoading() {
        return mBuilder.mIsLoading;
    }

    @Nullable
    public ArrayList<App> getApps() {
        return mApps;
    }

    @NonNull
    public ArrayList<App> getSelectedApps() {
        if (mSelectedApps == null)
            mSelectedApps = new ArrayList<>();
        return mSelectedApps;
    }

    @WorkerThread
    private void postError(@NonNull final String msg, @Nullable final Exception baseError) {
        IRLog.e(msg, baseError);
        EventBusUtils.post(new RequestEvent(false, false, new Exception(msg, baseError)),
                mBuilder.mRequestState);
    }

    public void send(final OnRequestProgress onRequestProgress) {
        IRLog.d("Preparing your request to send...");
        EventBusUtils.post(new RequestEvent(true, false, null), mBuilder.mRequestState);

        boolean requestError = false;

        if (mApps == null) {
            requestError = true;
            postError("No apps were loaded from this device.", null);
        } else if (IRUtils.isEmpty(mBuilder.mEmail)) {
            requestError = true;
            postError("The recipient email for the request cannot be empty.", null);
        } else if (getSelectedApps().size() <= 0) {
            if (mBuilder.mNoneSelectsAll) {
                mSelectedApps = mApps;
                requestError = false;
            } else {
                requestError = true;
                if (mBuilder.mCallback != null)
                    mBuilder.mCallback.onRequestEmpty(mBuilder.mContext);
                postError("No apps have been selected for sending in the request.", null);
            }
        } else if (IRUtils.isEmpty(mBuilder.mSubject)) {
            mBuilder.mSubject = "Icon Request";
            requestError = false;
        }

        if (requestError) return;

        @State int currentState = getRequestState();

        if (currentState == STATE_NORMAL) {
            new Thread(new Runnable() {
                @SuppressWarnings({"ResultOfMethodCallIgnored", "deprecation"})
                @Override
                public void run() {
                    if (onRequestProgress != null)
                        onRequestProgress.doWhenStarted();

                    final ArrayList<File> filesToZip = new ArrayList<>();

                    FileUtil.wipe(mBuilder.mSaveDir);
                    mBuilder.mSaveDir.mkdirs();

                    // Save app icons
                    IRLog.d("Saving icons...");
                    ArrayList<String> appNames = new ArrayList<>();
                    int i = 1;
                    for (App app : mSelectedApps) {
                        String iconName = app.getName();
                        if (appNames.contains(iconName)) {
                            iconName += String.valueOf(i);
                            i += 1;
                        }
                        final Drawable drawable = app.getHighResIcon(mBuilder.mContext);
                        if (!(drawable instanceof BitmapDrawable)) continue;
                        final BitmapDrawable bDrawable = (BitmapDrawable) drawable;
                        final Bitmap icon = bDrawable.getBitmap();
                        final File file = new File(mBuilder.mSaveDir,
                                String.format("%s.png", IRUtils.drawableName(iconName)));
                        appNames.add(iconName);
                        filesToZip.add(file);
                        try {
                            FileUtil.writeIcon(file, icon);
                        } catch (final Exception e) {
                            e.printStackTrace();
                            postError("Failed to save an icon: " + e.getMessage(), e);
                            return;
                        }
                    }

                    // Create request files
                    IRLog.d("Creating request files...");
                    StringBuilder xmlSb = null;
                    StringBuilder amSb = null;
                    StringBuilder trSb = null;
                    StringBuilder jsonSb = null;

                    if (mBuilder.mGenerateAppFilterXml) {
                        xmlSb = new StringBuilder("<resources>\n" +
                                "\t<iconback img1=\"iconback\"/>\n" +
                                "\t<iconmask img1=\"iconmask\"/>\n" +
                                "\t<iconupon img1=\"iconupon\"/>\n" +
                                "\t<scale factor=\"1.0\"/>");
                    }

                    if (mBuilder.mGenerateAppMapXml) {
                        amSb = new StringBuilder("<appmap>");
                    }

                    if (mBuilder.mGenerateThemeResourcesXml) {
                        trSb = new StringBuilder("<Theme version=\"1\">\n" +
                                "\t<Label value=\"" + mBuilder.mAppName + "\"/>\n" +
                                "\t<Wallpaper image=\"wallpaper_01\"/>\n" +
                                "\t<LockScreenWallpaper image=\"wallpaper_02\"/>\n" +
                                "\t<ThemePreview image=\"preview1\"/>\n" +
                                "\t<ThemePreviewWork image=\"preview1\"/>\n" +
                                "\t<ThemePreviewMenu image=\"preview1\"/>\n" +
                                "\t<DockMenuAppIcon selector=\"drawer\"/>");
                    }

                    if (mBuilder.mGenerateAppFilterJson) {
                        jsonSb = new StringBuilder("{\n" +
                                "\t\"components\": [");
                    }

                    int index = 0;
                    int n = 1;
                    appNames.clear();
                    for (App app : mSelectedApps) {
                        final String name = app.getName();
                        String iconName = name;
                        if (appNames.contains(iconName)) {
                            iconName += String.valueOf(n);
                            n += 1;
                        }
                        final String drawableName = IRUtils.drawableName(iconName);
                        if (xmlSb != null) {
                            xmlSb.append("\n\n");
                            if (mBuilder.mComments) {
                                xmlSb.append("\t<!-- ")
                                        .append(name)
                                        .append(" -->\n");
                            }
                            xmlSb.append(String.format("\t<item\n" +
                                            "\t\tcomponent=\"ComponentInfo{%s}\"\n" +
                                            "\t\tdrawable=\"%s\"/>",
                                    app.getCode(), drawableName));
                        }
                        if (amSb != null) {
                            amSb.append("\n\n");
                            if (mBuilder.mComments) {
                                amSb.append("\t<!-- ")
                                        .append(name)
                                        .append(" -->\n");
                            }
                            amSb.append(String.format("\t<item\n" +
                                            "\t\tclass=\"%s\"\n" +
                                            "\t\tname=\"%s\"/>",
                                    app.getCode().split("/")[1], drawableName));
                        }
                        if (trSb != null) {
                            trSb.append("\n\n");
                            if (mBuilder.mComments) {
                                trSb.append("\t<!-- ")
                                        .append(name)
                                        .append(" -->\n");
                            }
                            trSb.append(String.format("\t<AppIcon\n" +
                                            "\t\tname=\"%s\"\n" +
                                            "\t\timage=\"%s\"/>",
                                    app.getCode(), drawableName));
                        }
                        if (jsonSb != null) {
                            if (index > 0) jsonSb.append(",");
                            jsonSb.append("\n        {\n")
                                    .append(String.format("\t\t\t\"%s\": \"%s\",\n", "name", name))
                                    .append(String.format("\t\t\t\"%s\": \"%s\",\n", "pkg", app
                                            .getPackage()))
                                    .append(String.format("\t\t\t\"%s\": \"%s\",\n",
                                            "componentInfo",
                                            app.getCode()))
                                    .append(String.format("\t\t\t\"%s\": \"%s\"\n", "drawable",
                                            drawableName))
                                    .append("\t\t}");
                        }
                        index++;
                        appNames.add(iconName);
                    }

                    final String date = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                            .format
                                    (new Date());

                    if (xmlSb != null) {
                        xmlSb.append("\n\n</resources>");
                        final File newAppFilter = new File(mBuilder.mSaveDir, String.format
                                ("appfilter_%s.xml", date));
                        filesToZip.add(newAppFilter);
                        try {
                            FileUtil.writeAll(newAppFilter, xmlSb.toString());
                        } catch (final Exception e) {
                            e.printStackTrace();
                            postError("Failed to write your request appfilter.xml file: " + e
                                    .getMessage(), e);
                            return;
                        }
                    }

                    if (amSb != null) {
                        amSb.append("\n\n</appmap>");
                        final File newAppFilter = new File(mBuilder.mSaveDir, String.format
                                ("appmap_%s.xml", date));
                        filesToZip.add(newAppFilter);
                        try {
                            FileUtil.writeAll(newAppFilter, amSb.toString());
                        } catch (final Exception e) {
                            e.printStackTrace();
                            postError("Failed to write your request appmap.xml file: " + e
                                    .getMessage
                                            (), e);
                            return;
                        }
                    }
                    if (trSb != null) {
                        trSb.append("\n\n</Theme>");
                        final File newAppFilter = new File(mBuilder.mSaveDir, String.format
                                ("theme_resources_%s.xml", date));
                        filesToZip.add(newAppFilter);
                        try {
                            FileUtil.writeAll(newAppFilter, trSb.toString());
                        } catch (final Exception e) {
                            e.printStackTrace();
                            postError("Failed to write your request theme_resources.xml file: " + e
                                    .getMessage(), e);
                            return;
                        }
                    }

                    if (jsonSb != null) {
                        jsonSb.append("\n    ]\n}");
                        final File newAppFilter = new File(mBuilder.mSaveDir, String.format
                                ("appfilter_%s.json", date));
                        filesToZip.add(newAppFilter);
                        try {
                            FileUtil.writeAll(newAppFilter, jsonSb.toString());
                        } catch (final Exception e) {
                            e.printStackTrace();
                            postError("Failed to write your request appfilter.json file: " + e
                                    .getMessage(), e);
                            return;
                        }
                    }

                    if (filesToZip.size() == 0) {
                        postError("There are no files to put into the ZIP archive.", null);
                        return;
                    }

                    // Zip everything into an archive
                    IRLog.d("Creating ZIP...");
                    final File zipFile = new File(mBuilder.mSaveDir,
                            String.format("IconRequest-%s.zip", date));
                    try {
                        ZipUtil.zip(zipFile, filesToZip.toArray(new File[filesToZip.size()]));
                    } catch (final Exception e) {
                        e.printStackTrace();
                        postError("Failed to create the request ZIP file: " + e.getMessage(), e);
                        return;
                    }

                    // Cleanup files
                    IRLog.d("Cleaning up files...");
                    final File[] files = mBuilder.mSaveDir.listFiles();
                    for (File fi : files) {
                        if (!fi.isDirectory() && (fi.getName().endsWith(".png") || fi.getName()
                                .endsWith(".xml"))) {
                            fi.delete();
                        }
                    }

                    // post(new Runnable() {
                    // @Override
                    // public void run() {
                    // Send email intent
                    IRLog.d("Launching intent!");
                    final Uri zipUri = Uri.fromFile(zipFile);
                    final Intent emailIntent = new Intent(Intent.ACTION_SEND)
                            .putExtra(Intent.EXTRA_EMAIL, new String[]{mBuilder.mEmail})
                            .putExtra(Intent.EXTRA_SUBJECT, mBuilder.mSubject)
                            .putExtra(Intent.EXTRA_TEXT, Html.fromHtml(getBody()))
                            .putExtra(Intent.EXTRA_STREAM, zipUri)
                            .setType("application/zip");

                    saveRequestsLeft((getRequestsLeft() - getSelectedApps().size()) < 0 ? -1 :
                            (getRequestsLeft() - getSelectedApps().size()));
                    saveRequestMoment();

                    if (onRequestProgress != null)
                        onRequestProgress.doWhenReady();

                    if (mBuilder.mContext instanceof Activity) {
                        ((Activity) mBuilder.mContext).startActivityForResult(Intent.createChooser(
                                emailIntent, mBuilder.mContext.getString(R.string.send_using)),
                                INTENT_CODE);
                    } else {
                        mBuilder.mContext.startActivity(Intent.createChooser(
                                emailIntent, mBuilder.mContext.getString(R.string.send_using)));
                    }
                    EventBusUtils.post(new RequestEvent(false, true, null), mBuilder.mRequestState);
                }
            })
                    .start();
        } else {
            if (mBuilder.mCallback != null)
                mBuilder.mCallback.onRequestLimited(mBuilder.mContext, currentState,
                        getRequestsLeft(),
                        getMillisToFinish());
        }
    }

    @State
    private int getRequestState() {
        if ((mBuilder.mMaxCount <= 0) || (mBuilder.mTimeLimit <= 0)) return STATE_NORMAL;
        IRLog.d("Timer: Millis to finish: " + getMillisToFinish() + " - Request limit: " +
                mBuilder.mTimeLimit);
        if (getMillisToFinish() > 0) {
            return STATE_TIME_LIMITED;
        } else if (getSelectedApps().size() > getRequestsLeft()) {
            return STATE_LIMITED;
        }
        return STATE_NORMAL;
    }

    private void saveRequestMoment() {
        mBuilder.mPrefs.edit().putLong(KEY_SAVED_TIME_MILLIS, IRUtils.getCurrentTimeInMillis())
                .apply();
    }

    @SuppressLint("SimpleDateFormat")
    private long getMillisToFinish() {
        long savedTime = mBuilder.mPrefs.getLong(KEY_SAVED_TIME_MILLIS, -1);
        if (savedTime == -1) return -1;
        long elapsedTime = IRUtils.getCurrentTimeInMillis() - savedTime;
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
        IRLog.d("Timer: [Last request was on: " + sdf.format(savedTime) + "] - [Right" +
                " now is: " + sdf.format(new Date(IRUtils.getCurrentTimeInMillis())) + "] - " +
                "[Time Left: ~" + ((mBuilder.mTimeLimit - elapsedTime) / 1000) + " secs.]");
        return mBuilder.mTimeLimit - elapsedTime;
    }

    private int getRequestsLeft() {
        int requestsLeft = mBuilder.mPrefs.getInt(MAX_APPS, -1);
        if (requestsLeft > -1) {
            return requestsLeft;
        } else {
            saveRequestsLeft(mBuilder.mMaxCount);
            return mBuilder.mPrefs.getInt(MAX_APPS, mBuilder.mMaxCount);
        }
    }

    private void saveRequestsLeft(int requestsLeft) {
        mBuilder.mPrefs.edit().putInt(MAX_APPS, requestsLeft).apply();
    }

    public static void saveInstanceState(Bundle outState) {
        if (mRequest == null || outState == null) return;
        outState.putParcelable("butler_builder", mRequest.mBuilder);
        outState.putParcelableArrayList("apps", mRequest.mApps);
        outState.putParcelableArrayList("selected_apps", mRequest.mSelectedApps);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static IconRequest restoreInstanceState(Context context, Bundle inState) {
        if (inState == null || !inState.containsKey("butler_builder"))
            return null;
        mRequest = new IconRequest();
        mRequest.mBuilder = inState.getParcelable("butler_builder");
        if (mRequest.mBuilder != null) {
            mRequest.mBuilder.mContext = context;
        }
        if (inState.containsKey("apps"))
            mRequest.mApps = inState.getParcelableArrayList("apps");
        if (inState.containsKey("selected_apps"))
            mRequest.mSelectedApps = inState.getParcelableArrayList("selected_apps");
        if (mRequest.mApps == null)
            mRequest.mApps = new ArrayList<>();
        if (mRequest.mSelectedApps == null)
            mRequest.mSelectedApps = new ArrayList<>();
        else if (mRequest.mSelectedApps.size() > 0 && mRequest.mBuilder != null)
            EventBusUtils.post(new AppSelectionEvent(mRequest.mSelectedApps.size()), mRequest
                    .mBuilder.mRequestState);
        return mRequest;
    }

    public static void cleanup() {
        if (mRequest == null) return;
        if (mRequest.mBuilder != null) {
            mRequest.mBuilder.mContext = null;
            mRequest.mBuilder = null;
        }
        if (mRequest.mApps != null) {
            mRequest.mApps.clear();
            mRequest.mApps = null;
        }
        if (mRequest.mSelectedApps != null) {
            mRequest.mSelectedApps.clear();
            mRequest.mSelectedApps = null;
        }
        IRUtils.clearTimers();
        mRequest = null;
    }

}