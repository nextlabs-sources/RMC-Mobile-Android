package com.skydrm.rmc.utils.commonUtils;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by hhu on 1/17/2017.
 */

public class TimeUtil {
    public static String getProtectTime(long protectedTimeMillis, long currentTimeMillis) {
        long day;
        long hour;
        long min;
        long diff;
        String flag;
        if (protectedTimeMillis < currentTimeMillis) {
            diff = currentTimeMillis - protectedTimeMillis;
            flag = "ago";
        } else {
            diff = protectedTimeMillis - currentTimeMillis;
            flag = "later";
        }
        day = diff / (24 * 60 * 60 * 1000);
        hour = (diff / (60 * 60 * 1000) - day * 24);
        min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
        if (day != 0) {
            return formatData(protectedTimeMillis);
        }
        if (hour != 0) {
            if (hour < 12) {
                return "today," + hour + " hours " + flag;
            } else {
                return "yesterday," + format(protectedTimeMillis);
            }
        }
        if (min != 0) {
            return min + " minutes " + flag;
        }
        return "Just now";
    }

    public static String getLogTime(long accessTime, long currentTimeMillis) {
        long day;
        long hour;
        long min;
        long diff;
        String flag;
        if (accessTime < currentTimeMillis) {
            diff = currentTimeMillis - accessTime;
            flag = "ago";
        } else {
            diff = accessTime - currentTimeMillis;
            flag = "later";
        }
        day = diff / (24 * 60 * 60 * 1000);
        hour = (diff / (60 * 60 * 1000) - day * 24);
        min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
        if (day != 0) {
            return formatLogData(accessTime);
        }
        if (hour != 0) {
            if (hour < 12) {
                return "today," + hour + " hours " + flag;
            } else {
                return "yesterday," + format(accessTime);
            }
        }
        if (min != 0) {
            return min + " minutes " + flag;
        }
        return "just now";
    }

    public static String formatData(long timemillis) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateformat = new SimpleDateFormat("d MMM yyyy,hh:mm aaa", Locale.getDefault());
        return dateformat.format(timemillis);
    }

    public static String formatLibraryFileDate(long timemillis) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateformat = new SimpleDateFormat("d MMM", Locale.getDefault());
        return dateformat.format(timemillis);
    }

    private static String formatLogData(long timemillis) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateformat = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());
        return dateformat.format(timemillis);
    }

    private static String format(long timemillis) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateformat = new SimpleDateFormat("hh:mm aaa", Locale.getDefault());
        return dateformat.format(timemillis);
    }
}
