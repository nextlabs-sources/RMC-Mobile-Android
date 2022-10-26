package com.skydrm.rmc.utils;

import android.view.View;

public class ViewUtils {
    public static boolean isVisible(View v) {
        return v != null && v.getVisibility() == View.VISIBLE;
    }

    public static boolean isInVisible(View v) {
        return v != null && v.getVisibility() == View.INVISIBLE;
    }

    public static boolean isGone(View v) {
        return v != null && v.getVisibility() == View.GONE;
    }
}
