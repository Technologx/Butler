package com.pitchedapps.butler.library.ui;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pitchedapps.butler.library.interfaces.IButlerPermissions;

/**
 * Created by Allan Wang on 2016-08-19.
 * <p>
 * Handles all fab related things
 */
public abstract class ButlerFragment extends Fragment {

    public abstract void onFabClick(View v);

    public abstract @StringRes int getTitleId();

    protected abstract
    @DrawableRes
    int getFabIcon();

    protected abstract boolean hasFab();

    protected void showFab() {
        butlerActivity().getFab().show();
    }

    protected void hideFab() {
        butlerActivity().getFab().hide();
    }

    private ButlerActivity butlerActivity() {
        if (!(getActivity() instanceof ButlerActivity)) {
            throw new RuntimeException("Context is not an instance of ButlerActivity");
        }
        return ((ButlerActivity) getActivity());
    }

    protected void setFabIcon(@DrawableRes int icon) {
        butlerActivity().getFab().setImageResource(icon);
    }

    protected void fabSnackbar(String text, int duration) {
        if (!hasFab()) {
            Log.d("Butler", "fab not attached, stopping snackbar");
            return; //TODO log
        }
        Snackbar.make(butlerActivity().getFab(), text, duration);
    }

    @CallSuper
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (hasFab()) {
            showFab();
            if (getFabIcon() != 0) {
                setFabIcon(getFabIcon());
            }
        } else {
            hideFab();
        }
        return null;
    }

    protected void getPermissions(@NonNull IButlerPermissions callback, @IntRange(from = 1, to = Integer.MAX_VALUE) int requestCode, @NonNull String... permissions) {
        butlerActivity().requestPermission(callback, requestCode, permissions);
    }

}
