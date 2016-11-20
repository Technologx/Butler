package com.pitchedapps.butler.request;

import android.Manifest;
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

import com.pitchedapps.butler.BuildConfig;
import com.pitchedapps.butler.R;
import com.pitchedapps.butler.library.icon.request.AppLoadedEvent;
import com.pitchedapps.butler.library.icon.request.AppLoadingEvent;
import com.pitchedapps.butler.library.icon.request.EventState;
import com.pitchedapps.butler.library.icon.request.IconRequest;
import com.pitchedapps.capsule.library.fragments.CapsuleFragment;
import com.pitchedapps.capsule.library.permissions.CPermissionCallback;
import com.pitchedapps.capsule.library.permissions.PermissionResult;
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.Locale;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View v = inflater.inflate(R.layout.icon_request_section, container, false);

        mViewGroup = (ViewGroup) v.findViewById(R.id.viewgroup);
        mText = (TextView) v.findViewById(R.id.text);
        mRV = (RecyclerView) v.findViewById(R.id.appsToRequestList);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        mRV.setLayoutManager(llm);
        mRV.setHasFixedSize(true);
        mLoadingView = (RelativeLayout) v.findViewById(R.id.loading_view);
        RecyclerFastScroller mFastScroller = (RecyclerFastScroller) v.findViewById(R.id.rvFastScroller);
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
                    // TODO: Add a callback if you want Allan.
                    IconRequest.get().send(null);
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

    private void setupRequest() {
        if (IconRequest.get() == null) {
            IconRequest.start(getActivity())
                    .withHeader("Hey, testing Icon Request!")
                    .withFooter("%s Version: %s", getString(R.string.app_name), BuildConfig.VERSION_NAME)
                    .withSubject("Icon Request - Just a Test")
                    .toEmail("fake-email@fake-website.com")
                    .saveDir(new File(Environment.getExternalStorageDirectory(), "Pitched_Apps/Capsule"))
                    .includeDeviceInfo(true)
                    .generateAppFilterXml(true)
                    .generateAppFilterJson(false)
                    .requestEvents(EventState.DISABLED)
                    .loadedEvents(EventState.ENABLED)
                    .loadingEvents(EventState.DISABLED)
                    .selectionEvents(EventState.DISABLED)
                    .filterOff()
                    .debugMode(true)
                    .build().loadApps();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAppsLoaded(AppLoadedEvent event) {
//        EventBus.getDefault().removeStickyEvent(event.getClass());
        mViewGroup.removeView(mLoadingView);
        snackbarCustom(String.format(Locale.getDefault(), "Loaded in %d milliseconds", System.currentTimeMillis() - start), Snackbar.LENGTH_LONG).show();
        RequestsAdapter mAdapter = new RequestsAdapter();
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