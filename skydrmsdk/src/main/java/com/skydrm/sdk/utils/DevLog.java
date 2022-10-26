package com.skydrm.sdk.utils;

import android.support.annotation.NonNull;
import android.util.Log;

import com.skydrm.sdk.Factory;

public class DevLog {
    private boolean turnOnLog = Factory.TURN_ON_LOG;
    private String TAG = "Nil";

    public DevLog(String TAG) {
        this.TAG = TAG;
    }

    public void v(@NonNull String msg) {
        if (!turnOnLog) {
            return;
        }

        try {
            Log.v(TAG, msg);
        } catch (Exception ignore) {
        }
    }

    public void d(@NonNull String msg) {
        if (!turnOnLog) {
            return;
        }
        try {
            Log.d(TAG, msg);
        } catch (Exception ignore) {
        }
    }

    public void i(@NonNull String msg) {
        if (!turnOnLog) {
            return;
        }
        try {
            Log.i(TAG, msg);
        } catch (Exception ignore) {
        }
    }

    public void w(@NonNull String msg) {
        if (!turnOnLog) {
            return;
        }
        try {
            Log.w(TAG, msg);
        } catch (Exception ignore) {
        }
    }

    public void e(@NonNull String msg) {
        if (!turnOnLog) {
            return;
        }
        try {
            Log.e(TAG, msg);
        } catch (Exception ignore) {
        }
    }

    public void e(@NonNull String msg, @NonNull Throwable e) {
        if (!turnOnLog) {
            return;
        }
        try {
            Log.e(TAG, msg);
            e.printStackTrace();
        } catch (Exception ignore) {
        }
    }

    public void e(@NonNull Throwable e) {
        if (!turnOnLog) {
            return;
        }
        try {
            e.printStackTrace();
        } catch (Exception ignore) {
        }
    }

}
