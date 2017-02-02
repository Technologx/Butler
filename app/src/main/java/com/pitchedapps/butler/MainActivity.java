package com.pitchedapps.butler;

import android.os.Bundle;

import com.pitchedapps.butler.iconrequest.IconRequest;
import com.pitchedapps.butler.request.RequestFragment;
import com.pitchedapps.capsule.library.activities.CapsuleActivity;

public class MainActivity extends CapsuleActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        capsulate().toolbar(R.id.toolbar);
        switchFragment(new RequestFragment());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing())
            IconRequest.cleanup();
    }

    @Override
    protected int getFragmentId() {
        return R.id.main;
    }

    @Override
    protected int getFabId() {
        return R.id.fab;
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_main;
    }

}