package com.pitchedapps.butler.library.ui;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.DrawableRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Allan Wang on 2016-08-19.
 *
 * Handles all fab related things
 */
public abstract class ButlerFragment extends Fragment {

    public abstract void onFabClick(View v);

    abstract
    @DrawableRes
    int getFabIcon();

    abstract boolean hasFab();

    protected void showFab() {
        ((ButlerActivity) getActivity()).getFab().show();
    }

    protected void hideFab() {
        ((ButlerActivity) getActivity()).getFab().hide();
    }

    protected void setFabIcon(@DrawableRes int icon) {
        ((ButlerActivity) getActivity()).getFab().setImageResource(icon);
    }

    @CallSuper
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (hasFab()) {
            showFab();
            setFabIcon(getFabIcon());
        } else {
            hideFab();
        }
        return null;
    }

}
