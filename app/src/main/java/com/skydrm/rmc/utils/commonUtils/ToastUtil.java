package com.skydrm.rmc.utils.commonUtils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by hhu on 11/18/2016.
 */

public class ToastUtil {
    private static String oldMsg;
    protected static Toast toast = null;
    private static long oneTime = 0;
    private static long twoTime = 0;

    /**
     * avoid memory leak use the applicationContext instead of context
     *
     * @param context The ApplicationContext
     * @param s       msg need display
     */
    public static void showToast(Context context, String s) {
        if (context == null) {
            return;
        }
        if (s == null || s.isEmpty()) {
            return;
        }
        if (toast == null) {
            toast = Toast.makeText(context.getApplicationContext(), s, Toast.LENGTH_SHORT);
            toast.show();
            oneTime = System.currentTimeMillis();
        } else {
            twoTime = System.currentTimeMillis();
            if (s.equals(oldMsg)) {
                if (twoTime - oneTime > Toast.LENGTH_SHORT) {
                    toast.show();
                }
            } else {
                oldMsg = s;
                toast.setText(s);
                toast.show();
            }
        }
        oneTime = twoTime;
    }


    public static void showToast(Context context, int resId) {
        if (context == null) {
            return;
        }
        showToast(context.getApplicationContext(), context.getString(resId));
    }
}
