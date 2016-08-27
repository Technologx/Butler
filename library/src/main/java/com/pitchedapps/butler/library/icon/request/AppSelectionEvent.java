package com.pitchedapps.butler.library.icon.request;

import com.pitchedapps.butler.library.icon.request.App;
import com.pitchedapps.butler.library.icon.request.IconRequest;

import java.util.ArrayList;

/**
 * Created by Allan Wang on 2016-08-27.
 */
public class AppSelectionEvent {

    private int count;

    AppSelectionEvent(int i) {
        count = i;
    }

    public int getCount() {
        return count;
    }

    public boolean isAtMax() {
        int max = IconRequest.get().getMaxSelectable();
        return max > 0 && count == max;
    }

    public boolean isAllSelected() {
        ArrayList<App> apps = IconRequest.get().getApps();
        return apps !=null && count == apps.size();
    }
}
