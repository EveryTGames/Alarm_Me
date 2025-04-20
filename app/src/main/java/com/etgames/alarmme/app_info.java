package com.etgames.alarmme;

import android.graphics.drawable.Drawable;

public class app_info {

    String appName;
    String packageName;
    Drawable appIcon;
    boolean isToggled = false;

    public app_info(String _appName, String _packageName, Drawable _appIcon, boolean _isToggled) {
        appName = _appName;
        packageName = _packageName;
        appIcon = _appIcon;
        isToggled = _isToggled;
    }


}
