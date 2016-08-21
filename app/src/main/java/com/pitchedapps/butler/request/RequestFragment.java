package com.pitchedapps.butler.request;

import android.Manifest;
import android.view.View;

import com.pitchedapps.butler.R;
import com.pitchedapps.butler.library.interfaces.IButlerPermissions;
import com.pitchedapps.butler.library.ui.ButlerFragment;

/**
 * Created by Allan Wang on 2016-08-20.
 */
public class RequestFragment extends ButlerFragment {
    @Override
    public void onFabClick(View v) {
        getPermissions(new IButlerPermissions() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onDenied() {

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


}
