package com.pitchedapps.butler;

import android.os.Bundle;

import com.pitchedapps.butler.library.icon.request.IconRequest;
import com.pitchedapps.butler.library.ui.ButlerActivity;
import com.pitchedapps.butler.request.RequestFragment;

public class MainActivity extends ButlerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        butlerToolbar(R.id.toolbar);
        butlerFab(R.id.fab);

        switchFragment(new RequestFragment());
    }

    @Override
    protected int getFragmentId() {
        return R.id.main;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing())
            IconRequest.cleanup();
    }
}
