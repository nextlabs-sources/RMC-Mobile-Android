package com.skydrm.rmc.ui.common;

import android.app.Activity;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by hhu on 5/16/2017.
 */

public class ActivityManager {
    private static volatile ActivityManager defaultInstance;
    private static ConcurrentLinkedQueue<Activity> mActivities = new ConcurrentLinkedQueue<>();

    private ActivityManager() {

    }

    public static ActivityManager getDefault() {
        if (defaultInstance == null) {
            synchronized (ActivityManager.class) {
                if (defaultInstance == null) {
                    defaultInstance = new ActivityManager();
                }
            }
        }
        return defaultInstance;
    }

    // add activity
    public void addActivity(Activity activity) {
        mActivities.add(activity);
    }

    // remove activity
    public void removeActivity(Activity activity) {
        mActivities.remove(activity);
    }

    // remove activity and finish specify activity
    public void finishActivity(Activity activity) {
        if (mActivities.contains(activity)) {
            mActivities.remove(activity);
            activity.finish();
        }
    }

    // remove and finish specify activity by its Class
    public void finishActivityByClass(Class<?> clazz) {
        String className = clazz.getName();
        for (Activity activity : mActivities) {
            if (activity.getClass().getName().equals(className)) {
                mActivities.remove(activity);
                activity.finish();
            }
        }
    }

    // remove and finish all activities
    public void clearAllActivity() {
        try {
            for (Activity activity : mActivities) {
                if (!activity.isFinishing()) {
                    activity.finish();
                }
            }
            mActivities.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean containClass(Class<?> clazz) {
        boolean result = false;
        String className = clazz.getName();
        for (Activity activity : mActivities) {
            if (activity.getClass().getName().equals(className)) {
                result = true;
                break;
            }
        }
        return result;
    }

}
