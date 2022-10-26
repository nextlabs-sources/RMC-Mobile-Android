package com.skydrm.rmc.utils.commonUtils;

import android.content.Context;

/**
 * Created by aning on 11/15/2016.
 */

public class DensityHelper {
    /**
     * dp convert px
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * px convert dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
