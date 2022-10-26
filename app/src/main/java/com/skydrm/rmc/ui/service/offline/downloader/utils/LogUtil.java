package com.skydrm.rmc.ui.service.offline.downloader.utils;

import com.skydrm.rmc.ui.service.offline.downloader.config.Constant;

public class LogUtil {
    private static final String TAG = "Downloader-LOG";

    /**
     * d
     *
     * @param tag
     * @param msg
     */
    public static void d(String tag, String msg) {
        if (Constant.CONFIG.DEBUG) {
            android.util.Log.d(tag, msg);
        }
    }

    /**
     * d
     *
     * @param msg
     */
    public static void d(String msg) {
        d(TAG, msg);
    }

    /**
     * i
     *
     * @param tag
     * @param msg
     */
    public static void i(String tag, String msg) {
        if (Constant.CONFIG.DEBUG) {
            android.util.Log.i(tag, msg);
        }
    }

    /**
     * i
     *
     * @param msg
     */
    public static void i(String msg) {
        i(TAG, msg);
    }

    /**
     * e
     *
     * @param tag
     * @param msg
     */
    public static void e(String tag, String msg) {
        if (Constant.CONFIG.DEBUG) {
            android.util.Log.e(tag, msg);
        }
    }

    /**
     * w
     *
     * @param msg
     */
    public static void w(String msg) {
        w(TAG, msg);
    }

    /**
     * w
     *
     * @param tag
     * @param msg
     */
    public static void w(String tag, String msg) {
        if (Constant.CONFIG.DEBUG) {
            android.util.Log.w(tag, msg);
        }
    }
}
