package com.pitchedapps.butler.request;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.pitchedapps.butler.BuildConfig;
import com.pitchedapps.butler.R;
import com.pitchedapps.butler.library.icon.request.App;
import com.pitchedapps.butler.library.icon.request.AppsLoadCallback;
import com.pitchedapps.butler.library.icon.request.AppsSelectionListener;
import com.pitchedapps.butler.library.icon.request.IconRequest;
import com.pitchedapps.butler.library.icon.request.RequestSendCallback;
import com.pitchedapps.capsule.library.CapsuleFragment;
import com.pitchedapps.capsule.library.permissions.CPermissionCallback;
import com.pitchedapps.capsule.library.permissions.PermissionResult;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Allan Wang on 2016-08-20.
 */
public class RequestFragment extends CapsuleFragment implements AppsLoadCallback, RequestSendCallback, AppsSelectionListener {
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

    private RecyclerView mRV;
    private ProgressBar mProgress;
    private RequestsAdapter mAdapter;
    private long start, end;

    private void log(String s, @Nullable Object... o) {
        Log.e("ButlerSample", String.format(Locale.getDefault(), s, o));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (IconRequest.get() == null) {
            log("HERE");
            IconRequest request = IconRequest.start(getActivity())
                    .withHeader("Hey, testing Icon Request!")
                    .withFooter("%s Version: %s", getString(R.string.app_name), BuildConfig.VERSION_NAME)
                    .withSubject("Icon Request - Just a Test")
                    .toEmail("fake-email@fake-website.com")
                    .saveDir(new File(Environment.getExternalStorageDirectory(), "Pitched_Apps/Capsule"))
                    .includeDeviceInfo(true) // defaults to true anyways
                    .generateAppFilterXml(true) // defaults to true anyways
                    .generateAppFilterJson(true)
                    .loadCallback(this)
                    .sendCallback(this)
                    .selectionCallback(this)
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
            IconRequest.restoreInstanceState(getActivity(), savedInstanceState, this, this, this);

        start = System.currentTimeMillis();
        log("Start", start);

        return v;
    }


    @Override
    public void onLoadingFilter() {
    }

    @Override
    public void onAppsLoaded(ArrayList<App> apps, Exception e) {
        end = System.currentTimeMillis();
        log("LOAD TIME %d MS", end - start);
        fabSnackbar(String.format(Locale.getDefault(), "Loaded in %d milliseconds", end - start), Snackbar.LENGTH_LONG);
        mProgress.setVisibility(View.GONE);
        mRV.setVisibility(View.VISIBLE);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAppsLoadProgress(int percent) {
    }

    @Override
    public void onRequestPreparing() {

    }

    @Override
    public void onRequestError(Exception e) {

    }

    @Override
    public void onRequestSent() {

    }

    @Override
    public void onAppSelectionChanged(int selectedCount) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        IconRequest.saveInstanceState(outState);
    }
}
