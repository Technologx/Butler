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
import android.widget.ProgressBar;

import com.pitchedapps.butler.BuildConfig;
import com.pitchedapps.butler.R;
import com.pitchedapps.butler.library.icon.request.AppLoadedEvent;
import com.pitchedapps.butler.library.icon.request.IconRequest;
import com.pitchedapps.capsule.library.fragments.CapsuleFragment;
import com.pitchedapps.capsule.library.permissions.CPermissionCallback;
import com.pitchedapps.capsule.library.permissions.PermissionResult;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Allan Wang on 2016-08-20.
 */
public class RequestFragment extends CapsuleFragment {

    @Override
    public void onFabClick(View v) {
        getPermissions(new CPermissionCallback() {
            @Override
            public void onResult(PermissionResult result) {
                if (result.isAllGranted()) {
                    IconRequest.get().send();
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

    private RecyclerView mRV;
    private ProgressBar mProgress;
    private RequestsAdapter mAdapter;
    private long start;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (IconRequest.get() == null) {
            IconRequest request = IconRequest.start(getActivity())
                    .withHeader("Hey, testing Icon Request!")
                    .withFooter("%s Version: %s", getString(R.string.app_name), BuildConfig.VERSION_NAME)
                    .withSubject("Icon Request - Just a Test")
                    .toEmail("fake-email@fake-website.com")
                    .saveDir(new File(Environment.getExternalStorageDirectory(), "Pitched_Apps/Capsule"))
                    .includeDeviceInfo(true)
                    .generateAppFilterXml(true)
                    .generateAppFilterJson(false)
                    .filterOff()
                    .debugMode(true)
                    .build();
            request.loadApps();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View v = inflater.inflate(R.layout.content_main, container, false);

        mRV = (RecyclerView) v.findViewById(R.id.rv);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        mRV.setLayoutManager(llm);
        mRV.setHasFixedSize(true);
        mAdapter = new RequestsAdapter();
        mRV.setAdapter(mAdapter);
        mProgress = (ProgressBar) v.findViewById(R.id.progress);

        if (savedInstanceState != null)
            IconRequest.restoreInstanceState(getActivity(), savedInstanceState);

        start = System.currentTimeMillis();

        return v;
    }




    @Subscribe
    public void onAppsLoaded(AppLoadedEvent event) {
        snackbarCustom(String.format(Locale.getDefault(), "Loaded in %d milliseconds", System.currentTimeMillis() - start), Snackbar.LENGTH_LONG).show();
        mProgress.setVisibility(View.GONE);
        mRV.setVisibility(View.VISIBLE);
        mAdapter.notifyDataSetChanged();
        IconRequest.get().loadHighResIcons();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        IconRequest.saveInstanceState(outState);
    }
}
