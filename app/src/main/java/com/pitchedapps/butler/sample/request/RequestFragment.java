package com.pitchedapps.butler.sample.request;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pitchedapps.butler.iconrequest.IconRequest;
import com.pitchedapps.butler.iconrequest.events.AppLoadedEvent;
import com.pitchedapps.butler.iconrequest.events.AppLoadingEvent;
import com.pitchedapps.butler.iconrequest.events.EventState;
import com.pitchedapps.butler.iconrequest.events.OnRequestProgress;
import com.pitchedapps.butler.iconrequest.events.RequestsCallback;
import com.pitchedapps.butler.sample.BuildConfig;
import com.pitchedapps.butler.sample.R;
import com.pitchedapps.capsule.library.fragments.CapsuleFragment;
import com.pitchedapps.capsule.library.permissions.CPermissionCallback;
import com.pitchedapps.capsule.library.permissions.PermissionResult;
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.pitchedapps.butler.iconrequest.IconRequest.STATE_LIMITED;
import static com.pitchedapps.butler.iconrequest.IconRequest.STATE_TIME_LIMITED;

/**
 * Created by Allan Wang on 2016-08-20.
 */
public class RequestFragment extends CapsuleFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.builder()
                .logNoSubscriberMessages(false)
                .sendNoSubscriberEvent(false)
                .throwSubscriberException(false)
                .installDefaultEventBus();
        setupRequest();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View v = inflater.inflate(R.layout.icon_request_section, container, false);

        mViewGroup = (ViewGroup) v.findViewById(R.id.viewgroup);
        mText = (TextView) v.findViewById(R.id.text);
        mRV = (RecyclerView) v.findViewById(R.id.appsToRequestList);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        mRV.setLayoutManager(llm);
        mRV.setHasFixedSize(true);
        mLoadingView = (RelativeLayout) v.findViewById(R.id.loading_view);
        RecyclerFastScroller mFastScroller = (RecyclerFastScroller) v.findViewById(R.id
                .rvFastScroller);
        mFastScroller.attachRecyclerView(mRV);

        if (savedInstanceState != null)
            IconRequest.restoreInstanceState(getActivity(), savedInstanceState);

        start = System.currentTimeMillis();

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onFabClick(View v) {
        getPermissions(new CPermissionCallback() {
            @Override
            public void onResult(PermissionResult result) {
                if (result.isAllGranted()) {
                    if (IconRequest.get() != null) {
                        IconRequest.get().send(new OnRequestProgress() {
                            @Override
                            public void doWhenStarted() {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getActivity(), "Preparing request...",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void doWhenReady() {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mAdapter.unselectAllApps();
                                        Toast.makeText(getActivity(), "Request Ready. Starting " +
                                                "send intent...", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                }
            }
        }, 9, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @Override
    public int getTitleId() {
        return R.string.request_title;
    }

    @Override
    protected int getFabIcon() {
        return R.drawable.ic_email;
    }

    @Override
    protected boolean hasFab() {
        return true;
    }

    private RecyclerView mRV;
    private RelativeLayout mLoadingView;
    private long start;
    private ViewGroup mViewGroup;
    private TextView mText;
    private RequestsAdapter mAdapter;

    private void setupRequest() {
        if (IconRequest.get() == null) {
            IconRequest.start(getActivity())
                    .withHeader("Hey, testing Icon Request!")
                    .withFooter("%s Version: %s", getString(R.string.app_name), BuildConfig
                            .VERSION_NAME)
                    .withSubject("Icon Request - Just a Test")
                    .toEmail("fake-email@fake-website.com")
                    .saveDir(new File(Environment.getExternalStorageDirectory(),
                            "Pitched_Apps/Capsule"))
                    .maxSelectionCount(5)
                    .withTimeLimit(2, getActivity().getSharedPreferences("ButlerPrefs", Context
                            .MODE_PRIVATE))
                    .includeDeviceInfo(true)
                    .generateAppFilterXml(true)
                    .generateAppFilterJson(false)
                    .requestEvents(EventState.DISABLED)
                    .loadedEvents(EventState.ENABLED)
                    .loadingEvents(EventState.DISABLED)
                    .selectionEvents(EventState.DISABLED)
                    .filterOff()
                    .debugMode(BuildConfig.DEBUG)
                    .setCallback(new RequestsCallback() {
                        @Override
                        public void onRequestLimited(final Context context, @IconRequest.State
                        final int reason, final int requestsLeft, final long millis) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (reason == STATE_TIME_LIMITED && millis > 0) {
                                        Toast.makeText(context, "Request limited. Time " +
                                                "left: " + TimeUnit.MILLISECONDS.toSeconds
                                                (millis) + " seconds.", Toast.LENGTH_LONG).show();
                                    } else if (reason == STATE_LIMITED) {
                                        Toast.makeText(context, "Request limited. Requests " +
                                                "left: " + requestsLeft + ".", Toast.LENGTH_LONG)
                                                .show();
                                    }
                                }
                            });
                        }

                        @Override
                        public void onRequestEmpty(Context context) {
                            Toast.makeText(context, "No apps selected to request.", Toast
                                    .LENGTH_LONG).show();
                        }
                    })
                    .build().loadApps();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAppsLoaded(AppLoadedEvent event) {
//        EventBus.getDefault().removeStickyEvent(event.getClass());
        mViewGroup.removeView(mLoadingView);
        snackbarCustom(String.format(Locale.getDefault(), "Loaded in %d milliseconds", System
                .currentTimeMillis() - start), Snackbar.LENGTH_LONG).show();
        mAdapter = new RequestsAdapter();
        mRV.setAdapter(mAdapter);
//        IconRequest.get().loadHighResIcons();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAppsLoading(AppLoadingEvent event) {
//        EventBus.getDefault().removeStickyEvent(event.getClass());
        mText.setText(event.getString());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        IconRequest.saveInstanceState(outState);
    }
}