package com.etgames.alarmme;

import android.graphics.drawable.Drawable;

public class app_info {

    String itemTitle;
    String packageName;
    Drawable appIcon;
    boolean isToggled = false;

    public app_info(String _appName, String _packageName, Drawable _appIcon, boolean _isToggled) {
        itemTitle = _appName;
        packageName = _packageName;
        appIcon = _appIcon;
        isToggled = _isToggled;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public String getPackageName() {
        return packageName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public boolean isToggled() {
        return isToggled;
    }

    public void setToggled(boolean toggled) {
        isToggled = toggled;
    }


}
