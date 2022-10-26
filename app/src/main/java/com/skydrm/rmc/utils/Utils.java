package com.skydrm.rmc.utils;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;

/**
 * Created by oye on 11/15/2016.
 */

public final class Utils {
    static public interface IScreenSize {
        int getHeight();

        int getWidth();
    }

    static IScreenSize getWindowSize(@NonNull Activity activity) {

        final DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return new IScreenSize() {
            @Override
            public int getHeight() {
                return dm.heightPixels;
            }

            @Override
            public int getWidth() {
                return dm.widthPixels;
            }
        };

    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

}
