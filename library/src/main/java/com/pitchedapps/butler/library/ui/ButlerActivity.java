package com.pitchedapps.butler.library.ui;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

/**
 * Created by Allan Wang on 2016-08-19.
 * <p>
 * Handles all fab related things
 */
public abstract class ButlerActivity extends AppCompatActivity {

    private static final String libTag = "Butler";
    protected FloatingActionButton mFab;

    public FloatingActionButton getFab() {
        if (mFab == null)
            throw new RuntimeException("Fab not set in " + libTag + "Activity; use setupFab method");
        return mFab;
    }

    protected abstract
    @IdRes
    int getFragmentId();

    private ButlerFragment getCurrentBaseFragment() {
        Fragment current = getSupportFragmentManager().findFragmentById(getFragmentId());
        if (!(current instanceof ButlerFragment))
            throw new RuntimeException("Fragment does not extend " + libTag + "Fragment");
        return (ButlerFragment) current;
    }

    protected void setupFab(@IdRes int id) {
        mFab = (FloatingActionButton) findViewById(id);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCurrentBaseFragment().onFabClick(view);
            }
        });
    }

    public static void hideFab(Context context) {
        if (context instanceof ButlerActivity) {
            ((ButlerActivity)context).getFab().hide();
        } else {
            Log.e("hideFab", "context not instance of " + libTag + "Activity");
        }
    }
}
