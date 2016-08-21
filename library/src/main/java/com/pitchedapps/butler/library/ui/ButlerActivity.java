package com.pitchedapps.butler.library.ui;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.pitchedapps.butler.library.interfaces.IButlerPermissions;

import java.util.HashMap;

/**
 * Created by Allan Wang on 2016-08-19.
 * <p>
 * Handles all fab related things
 */
public abstract class ButlerActivity extends AppCompatActivity {

    protected FloatingActionButton mFab;
    protected Toolbar mToolbar;

    private HashMap<Integer, IButlerPermissions> mButlerPermissionCallbacks;

    public FloatingActionButton getFab() {
        if (mFab == null)
            throw new RuntimeException("Fab not set in ButlerActivity; use setupFab method");
        return mFab;
    }

    /**
     * Gets the Layout ID of the view that will be replaced by Fragments with the SupportFragmentManager
     *
     * @return
     */
    protected abstract
    @IdRes
    int getFragmentId();

    private ButlerFragment getCurrentBaseFragment() {
        Fragment current = getSupportFragmentManager().findFragmentById(getFragmentId());
        if (!(current instanceof ButlerFragment))
            throw new RuntimeException("Fragment does not extend ButlerFragment");
        return (ButlerFragment) current;
    }

    protected String s(@StringRes int id) {
        return getString(id);
    }

    protected void switchFragment(ButlerFragment fragment) {
        switchFragmentCustom(fragment).commit();
    }

    protected FragmentTransaction switchFragmentCustom(ButlerFragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                .beginTransaction();
        fragmentTransaction.replace(getFragmentId(), fragment, s(fragment.getTitleId()));
        return fragmentTransaction;
    }

    protected void butlerFab(@IdRes int id) {
        mFab = (FloatingActionButton) findViewById(id);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCurrentBaseFragment().onFabClick(view);
            }
        });
    }

    protected void butlerToolbar(@IdRes int id) {
        mToolbar = (Toolbar) findViewById(id);
        setSupportActionBar(mToolbar);
    }

    public static void hideFab(Context context) {
        if (context instanceof ButlerActivity) {
            ((ButlerActivity) context).getFab().hide();
        } else {
            Log.e("hideFab", "context not instance of ButlerActivity");
        }
    }

    public void requestPermission(@NonNull IButlerPermissions callback, @IntRange(from = 1, to = Integer.MAX_VALUE) int requestCode, @NonNull String... permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            callback.onSuccess();
            return;
        }
        if (mButlerPermissionCallbacks == null) mButlerPermissionCallbacks = new HashMap<>();
        mButlerPermissionCallbacks.put(requestCode, callback);
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    @CallSuper
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResult) {
        if (!mButlerPermissionCallbacks.containsKey(requestCode)) return;
        for (int i : grantResult) {
            if (i == PackageManager.PERMISSION_DENIED) {
                mButlerPermissionCallbacks.get(requestCode).onDenied();
                return;
            }
        }
        mButlerPermissionCallbacks.get(requestCode).onSuccess();
        mButlerPermissionCallbacks.remove(requestCode);
        if (mButlerPermissionCallbacks.isEmpty()) mButlerPermissionCallbacks = null;
    }
}
