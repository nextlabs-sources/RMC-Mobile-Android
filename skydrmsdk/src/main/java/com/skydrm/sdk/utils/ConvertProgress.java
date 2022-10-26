package com.skydrm.sdk.utils;

import android.os.SystemClock;
import android.util.Log;

import com.skydrm.sdk.rms.RestAPI;

import java.io.File;
import java.util.Random;

/**
 * Created by aning on 2/15/2017.
 * This class used to simulate convert progress of CAD and Office & PDF.
 */

public class ConvertProgress extends Thread {
    private static final String TAG = "ConvertProgress";
    private static final int NEED_AVERAGE_TIME_CONVERT_1M = 2680;
    private RestAPI.IConvertListener mConvertListener;
    private File mFile;

    public ConvertProgress(File file, RestAPI.IConvertListener convertListener) {
        mFile = file;
        mConvertListener = convertListener;
    }

    @Override
    public void run() {
        for (int i = 0; i <= 95; i++) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            mConvertListener.onConvertProgress(i, 100);
            int j = new Random().nextInt(5);
            i = i + j;
            SystemClock.sleep(mFile.length() / NEED_AVERAGE_TIME_CONVERT_1M);
            Log.d(TAG, "---- convert progress: " + i);
        }
    }

    public void cancel() {
        interrupt();
    }
}
