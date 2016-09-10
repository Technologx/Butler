package com.pitchedapps.butler.library.icon.request;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.CallSuper;
import android.support.annotation.CheckResult;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.annotation.XmlRes;
import android.text.Html;

import com.pitchedapps.butler.library.BuildConfig;
import com.pitchedapps.butler.library.R;

import org.greenrobot.eventbus.EventBus;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

import timber.log.Timber;

/**
 * Created by Allan Wang on 2016-08-20.
 */
public final class IconRequest {

    private Builder mBuilder;
    private ArrayList<App> mApps;
    private ArrayList<App> mSelectedApps;
    private transient Handler mHandler;

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
        protected String mEmail = null;
        protected String mSubject = "Icon Request";
        protected String mHeader = "These apps aren't themed. Thanks in advance";
        protected String mFooter = null;
        protected int mMaxCount = 0;
        protected boolean mIsLoading = false;
        protected boolean mHasMaxCount = false;
        protected boolean mNoneSelectsAll = true;
        protected boolean mIncludeDeviceInfo = true;
        protected boolean mComments = true;
        protected boolean mGenerateAppFilterXml = true;
        protected boolean mGenerateAppFilterJson = false;
        protected boolean mErrorOnInvalidAppFilterDrawable = true;
        protected boolean mDebugMode = false;
        protected EventState mLoadingState = EventState.DISABLED,
                mLoadedState = EventState.STICKIED,
                mSelectionState = EventState.DISABLED,
                mRequestState = EventState.DISABLED;

        public Builder() {
        }

        public Builder(@NonNull Context context) {
            mContext = context;
            mSaveDir = new File(Environment.getExternalStorageDirectory(), "IconRequest");
            FileUtil.wipe(mSaveDir);
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
            mHasMaxCount = mMaxCount != 0;
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

        public IconRequest build() {
            return new IconRequest(this);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeSerializable(this.mSaveDir);
            dest.writeInt(this.mFilterId);
            dest.writeString(this.mEmail);
            dest.writeString(this.mSubject);
            dest.writeString(this.mHeader);
            dest.writeString(this.mFooter);
            dest.writeInt(this.mMaxCount);
            dest.writeByte(this.mIsLoading ? (byte) 1 : (byte) 0);
            dest.writeByte(this.mHasMaxCount ? (byte) 1 : (byte) 0);
            dest.writeByte(this.mNoneSelectsAll ? (byte) 1 : (byte) 0);
            dest.writeByte(this.mIncludeDeviceInfo ? (byte) 1 : (byte) 0);
            dest.writeByte(this.mComments ? (byte) 1 : (byte) 0);
            dest.writeByte(this.mGenerateAppFilterXml ? (byte) 1 : (byte) 0);
            dest.writeByte(this.mGenerateAppFilterJson ? (byte) 1 : (byte) 0);
            dest.writeByte(this.mErrorOnInvalidAppFilterDrawable ? (byte) 1 : (byte) 0);
            dest.writeByte(this.mDebugMode ? (byte) 1 : (byte) 0);
            dest.writeInt(this.mLoadingState == null ? -1 : this.mLoadingState.ordinal());
            dest.writeInt(this.mLoadedState == null ? -1 : this.mLoadedState.ordinal());
            dest.writeInt(this.mSelectionState == null ? -1 : this.mSelectionState.ordinal());
            dest.writeInt(this.mRequestState == null ? -1 : this.mRequestState.ordinal());
        }

        protected Builder(Parcel in) {
            this.mSaveDir = (File) in.readSerializable();
            this.mFilterId = in.readInt();
            this.mEmail = in.readString();
            this.mSubject = in.readString();
            this.mHeader = in.readString();
            this.mFooter = in.readString();
            this.mMaxCount = in.readInt();
            this.mIsLoading = in.readByte() != 0;
            this.mHasMaxCount = in.readByte() != 0;
            this.mNoneSelectsAll = in.readByte() != 0;
            this.mIncludeDeviceInfo = in.readByte() != 0;
            this.mComments = in.readByte() != 0;
            this.mGenerateAppFilterXml = in.readByte() != 0;
            this.mGenerateAppFilterJson = in.readByte() != 0;
            this.mErrorOnInvalidAppFilterDrawable = in.readByte() != 0;
            this.mDebugMode = in.readByte() != 0;
            int tmpMLoadingState = in.readInt();
            this.mLoadingState = tmpMLoadingState == -1 ? null : EventState.values()[tmpMLoadingState];
            int tmpMLoadedState = in.readInt();
            this.mLoadedState = tmpMLoadedState == -1 ? null : EventState.values()[tmpMLoadedState];
            int tmpMSelectionState = in.readInt();
            this.mSelectionState = tmpMSelectionState == -1 ? null : EventState.values()[tmpMSelectionState];
            int tmpMRequestState = in.readInt();
            this.mRequestState = tmpMRequestState == -1 ? null : EventState.values()[tmpMRequestState];
        }

        public static final Creator<Builder> CREATOR = new Creator<Builder>() {
            @Override
            public Builder createFromParcel(Parcel source) {
                return new Builder(source);
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
            String mAppCode = null;
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        final String tagName = parser.getName();
                        if (tagName.equals("item")) {
                            try {
                                // Read package and activity name
                                mAppCode = parser.getAttributeValue(null, "component");
                                mAppCode = mAppCode.substring(14, mAppCode.length() - 1); //wrapped in ComponentInfo{[Component]} TODO add checker?
                                //TODO check for valid drawable
                                // Add new info to our ArrayList and reset the object. Log commented out to reduce logcat spam.
                                defined.add(mAppCode);
                                //if(debugEnabled)
                                //	Log.d(LOG_TAG, "Added appfilter app:\n" + mAppCode);
                                mAppCode = null;

                            } catch (Exception e) {
                                IRLog.d("Error adding parsed appfilter item!", e);
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
//            EventBusUtils.post(new AppLoadedEvent(null, new Exception("Failed to open your filter: " + e.getLocalizedMessage(), e)), mBuilder.mLoadedState);
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
//                            mInvalidDrawables.append(String.format("Drawable for %s was null or empty.\n", component));
//                        }
//                    } else if (mBuilder.mContext != null) {
//                        final Resources r = mBuilder.mContext.getResources();
//                        int identifier;
//                        try {
//                            identifier = r.getIdentifier(drawable, "drawable", mBuilder.mContext.getPackageName());
//                        } catch (Throwable t) {
//                            identifier = 0;
//                        }
//                        if (identifier == 0) {
//                            IRLog.d("WARNING: Drawable %s (for %s) doesn't match up with a resource.", drawable, component);
//                            if (mBuilder.mErrorOnInvalidAppFilterDrawable) {
//                                if (mInvalidDrawables == null)
//                                    mInvalidDrawables = new StringBuilder();
//                                if (mInvalidDrawables.length() > 0) mInvalidDrawables.append("\n");
//                                mInvalidDrawables.append(String.format("Drawable %s (for %s) doesn't match up with a resource.\n", drawable, component));
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
//                EventBusUtils.post(new AppLoadedEvent(null, new Exception(mInvalidDrawables.toString())), mBuilder.mLoadedState);
//                mInvalidDrawables.setLength(0);
//                mInvalidDrawables.trimToSize();
//                mInvalidDrawables = null;
//            }
//            IRLog.d("Found %d total app(s) in your appfilter.", defined.size());
//        } catch (final Throwable e) {
//            e.printStackTrace();
//            mBuilder.mIsLoading = false;
//            EventBusUtils.post(new AppLoadedEvent(null, new Exception("Failed to read your filter: " + e.getMessage(), e)), mBuilder.mLoadedState);
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
        if (mHandler == null)
            mHandler = new Handler();
        EventBusUtils.post(new AppLoadingEvent(-2), mBuilder.mLoadingState);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mBuilder.mDebugMode) IRUtils.startTimer("IR_debug_auto");
                final HashSet<String> filter = loadFilterApps();
                if (filter == null) return;
                IRLog.d("Loading unthemed installed apps...");
                mApps = ComponentInfoUtil.getInstalledApps(mBuilder.mContext, filter, mBuilder.mLoadingState);
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
        if (mHandler == null)
            mHandler = new Handler();
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
            sb.append(String.format("Link: https://play.google.com/store/apps/details?id=%s<br/>", app.getPackage()));
        }

        if (mBuilder.mIncludeDeviceInfo) {
            sb.append("<br/><br/><br/>OS Version: ").append(System.getProperty("os.version")).append("(").append(Build.VERSION.INCREMENTAL).append(")");
            sb.append("<br/>OS API Level: ").append(Build.VERSION.SDK_INT);
            sb.append("<br/>Device: ").append(Build.DEVICE);
            sb.append("<br/>Manufacturer: ").append(Build.MANUFACTURER);
            sb.append("<br/>Model (and Product): ").append(Build.MODEL).append(" (").append(Build.PRODUCT).append(")");
            sb.append("<br/>App Version Name: ").append(BuildConfig.VERSION_NAME);
            sb.append("<br/>App Version Code: ").append(BuildConfig.VERSION_CODE);
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
        if (mBuilder.mHasMaxCount && mSelectedApps.size() >= mBuilder.mMaxCount) return false;
        if (!mSelectedApps.contains(app)) {
            mSelectedApps.add(app);
            EventBusUtils.post(new AppSelectionEvent(mSelectedApps.size()), mBuilder.mSelectionState);
            return true;
        }
        return false;
    }

    public boolean unselectApp(@NonNull App app) {
        final boolean result = mSelectedApps.remove(app);
        if (result)
            EventBusUtils.post(new AppSelectionEvent(mSelectedApps.size()), mBuilder.mSelectionState);
        return result;
    }

    public boolean toggleAppSelected(@NonNull App app) {
        final boolean result;
        if (isAppSelected(app))
            result = unselectApp(app);
        else result = selectApp(app);
        return result;
    }

    public boolean isAppSelected(@NonNull App app) {
        return mSelectedApps.contains(app);
    }

    public IconRequest selectAllApps() {
        if (mApps == null) return this;
        boolean changed = false;
        for (App app : mApps) {
            if (!mSelectedApps.contains(app)) {
                changed = true;
                mSelectedApps.add(app);
                if (mBuilder.mHasMaxCount && mSelectedApps.size() >= mBuilder.mMaxCount) break;
            }
        }
        if (changed)
            EventBusUtils.post(new AppSelectionEvent(mSelectedApps.size()), mBuilder.mSelectionState);
        return this;
    }

    public void unselectAllApps() {
        if (mSelectedApps == null || mSelectedApps.size() == 0) return;
        mSelectedApps.clear();
        EventBusUtils.post(new AppSelectionEvent(0), mBuilder.mSelectionState);
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

    private void post(Runnable runnable) {
        if (mBuilder.mContext == null ||
                (mBuilder.mContext instanceof Activity && ((Activity) mBuilder.mContext).isFinishing())) {
            return;
        } else if (mHandler == null) {
            return;
        }
        mHandler.post(runnable);
    }

    @WorkerThread
    private void postError(@NonNull final String msg, @Nullable final Exception baseError) {
        IRLog.e(msg, baseError);
        EventBusUtils.post(new RequestEvent(false, false, new Exception(msg, baseError)), mBuilder.mRequestState);
    }

    public void send() {
        IRLog.d("Preparing your request to send...");
        EventBusUtils.post(new RequestEvent(true, false, null), mBuilder.mRequestState);
        if (mHandler == null)
            mHandler = new Handler();

        if (mApps == null) {
            postError("No apps were loaded from this device.", null);
        } else if (IRUtils.isEmpty(mBuilder.mEmail)) {
            postError("The recipient email for the request cannot be empty.", null);
        } else if (mSelectedApps == null || mSelectedApps.size() == 0) {
            if (mBuilder.mNoneSelectsAll) {
                mSelectedApps = mApps;
            } else {
                postError("No apps have been selected for sending in the request.", null);
            }
        } else if (IRUtils.isEmpty(mBuilder.mSubject)) {
            mBuilder.mSubject = "Icon Request";
        }

        new Thread(new Runnable() {
            @SuppressWarnings("ResultOfMethodCallIgnored")
            @Override
            public void run() {
                final ArrayList<File> filesToZip = new ArrayList<>();
                mBuilder.mSaveDir.mkdirs();

                // Save app icons
                IRLog.d("Saving icons...");
                for (App app : mSelectedApps) {
                    final Drawable drawable = app.getHighResIcon(mBuilder.mContext);
                    if (!(drawable instanceof BitmapDrawable)) continue;
                    final BitmapDrawable bDrawable = (BitmapDrawable) drawable;
                    final Bitmap icon = bDrawable.getBitmap();
                    final File file = new File(mBuilder.mSaveDir,
                            String.format("%s.png", IRUtils.drawableName(app.getName())));
                    filesToZip.add(file);
                    try {
                        FileUtil.writeIcon(file, icon);
                    } catch (final Exception e) {
                        e.printStackTrace();
                        postError("Failed to save an icon: " + e.getMessage(), e);
                        return;
                    }
                }

                // Create appfilter
                IRLog.d("Creating appfilter...");
                StringBuilder xmlSb = null;
                StringBuilder jsonSb = null;
                if (mBuilder.mGenerateAppFilterXml) {
                    xmlSb = new StringBuilder("<resources>\n" +
                            "\t<iconback img1=\"iconback\" />\n" +
                            "\t<iconmask img1=\"iconmask\" />\n" +
                            "\t<iconupon img1=\"iconupon\" />\n" +
                            "\t<scale factor=\"1.0\" />");
                }
                if (mBuilder.mGenerateAppFilterJson) {
                    jsonSb = new StringBuilder("{\n" +
                            "\t\"components\": [");
                }
                int index = 0;
                for (App app : mSelectedApps) {
                    final String name = app.getName();
                    final String drawableName = IRUtils.drawableName(name);
                    if (xmlSb != null) {
                        xmlSb.append("\n\n");
                        if (mBuilder.mComments) {
                            xmlSb.append("\t<!-- ")
                                    .append(name)
                                    .append(" -->\n");
                        }
                        xmlSb.append(String.format("\t<item\n" +
                                        "\t\tcomponent=\"ComponentInfo{%s}\"\n" +
                                        "\t\tdrawable=\"%s\" />",
                                app.getCode(), drawableName));
                    }
                    if (jsonSb != null) {
                        if (index > 0) jsonSb.append(",");
                        jsonSb.append("\n        {\n")
                                .append(String.format("\t\t\t\"%s\": \"%s\",\n", "name", name))
                                .append(String.format("\t\t\t\"%s\": \"%s\",\n", "pkg", app.getPackage()))
                                .append(String.format("\t\t\t\"%s\": \"%s\",\n", "componentInfo", app.getCode()))
                                .append(String.format("\t\t\t\"%s\": \"%s\"\n", "drawable", drawableName))
                                .append("\t\t}");
                    }
                    index++;
                }

                String date = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

                if (xmlSb != null) {
                    xmlSb.append("\n\n</resources>");
                    final File newAppFilter = new File(mBuilder.mSaveDir, String.format("appfilter_%s.xml", date));
                    filesToZip.add(newAppFilter);
                    try {
                        FileUtil.writeAll(newAppFilter, xmlSb.toString());
                    } catch (final Exception e) {
                        e.printStackTrace();
                        postError("Failed to write your request appfilter.xml file: " + e.getMessage(), e);
                        return;
                    }
                }
                if (jsonSb != null) {
                    jsonSb.append("\n    ]\n}");
                    final File newAppFilter = new File(mBuilder.mSaveDir, String.format("appfilter_%s.json", date));
                    filesToZip.add(newAppFilter);
                    try {
                        FileUtil.writeAll(newAppFilter, jsonSb.toString());
                    } catch (final Exception e) {
                        e.printStackTrace();
                        postError("Failed to write your request appfilter.json file: " + e.getMessage(), e);
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
                    if (!fi.isDirectory() && (fi.getName().endsWith(".png") || fi.getName().endsWith(".xml")))
                        fi.delete();
                }

//                post(new Runnable() {
//                    @Override
//                    public void run() {
                // Send email intent
                IRLog.d("Launching intent!");
                final Uri zipUri = Uri.fromFile(zipFile);
                final Intent emailIntent = new Intent(Intent.ACTION_SEND)
                        .putExtra(Intent.EXTRA_EMAIL, new String[]{mBuilder.mEmail})
                        .putExtra(Intent.EXTRA_SUBJECT, mBuilder.mSubject)
                        .putExtra(Intent.EXTRA_TEXT, Html.fromHtml(getBody()))
                        .putExtra(Intent.EXTRA_STREAM, zipUri)
                        .setType("application/zip");
                mBuilder.mContext.startActivity(Intent.createChooser(
                        emailIntent, mBuilder.mContext.getString(R.string.send_using)));

                EventBusUtils.post(new RequestEvent(false, true, null), mBuilder.mRequestState);
//                    }
//                }); //TODO verify
            }
        }).start();
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
            EventBusUtils.post(new AppSelectionEvent(mRequest.mSelectedApps.size()), mRequest.mBuilder.mRequestState);
        return mRequest;
    }

    public static void cleanup() {
        if (mRequest == null) return;
        if (mRequest.mBuilder != null) {
            mRequest.mBuilder.mContext = null;
            mRequest.mBuilder = null;
        }
        mRequest.mHandler = null;
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