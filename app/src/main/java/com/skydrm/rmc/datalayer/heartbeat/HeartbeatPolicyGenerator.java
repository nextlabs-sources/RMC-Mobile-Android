package com.skydrm.rmc.datalayer.heartbeat;

import android.support.annotation.IntDef;

import java.util.HashMap;
import java.util.Map;

public class HeartbeatPolicyGenerator {
    public static final int TYPE_COMMON = 0;
    public static final int TYPE_MYVAULT = 1;
    public static final int TYPE_SHARED_WITH_ME = 2;
    public static final int TYPE_PROJECT = 3;
    private static final Object mPoolLock = new Object();
    private static final int DEFAULT_INTERVAL_SECONDS = 120;
    private static final Map<Integer, IHeartBeatPolicy> mPolicyPool = new HashMap<>();

    public static IHeartBeatPolicy getOne(@PolicyType int policyType) {
        IHeartBeatPolicy ret;
        synchronized (mPoolLock) {
            if (mPolicyPool.containsKey(policyType)) {
                ret = mPolicyPool.get(policyType);
            } else {
                //Using common heart beat type
                int heartBeatType = mPolicyPool.get(0).getType();
                ret = newOne(policyType, heartBeatType, DEFAULT_INTERVAL_SECONDS);
                mPolicyPool.put(policyType, ret);
            }
            return ret;
        }
    }

    public static IHeartBeatPolicy getOne(@PolicyType int policyType, int heartBeatType, long interval) {
        IHeartBeatPolicy ret;
        synchronized (mPoolLock) {
            if (mPolicyPool.containsKey(policyType)) {
                ret = mPolicyPool.get(policyType);
            } else {
                ret = newOne(policyType, heartBeatType, interval);
                mPolicyPool.put(policyType, ret);
            }
            return ret;
        }
    }

    public static void configureOne(@PolicyType int type, IHeartBeatPolicy p) {
        synchronized (mPoolLock) {
            mPolicyPool.put(type, p);
        }
    }

    private static IHeartBeatPolicy newOne(int policyType, int heartBeatType, long interval) {
        if (policyType == TYPE_COMMON) {
            return new CommonPolicy(heartBeatType, interval);
        }
        if (policyType == TYPE_MYVAULT) {
            return new MyVaultHeartBeatPolicy(heartBeatType, interval);
        }
        if (policyType == TYPE_SHARED_WITH_ME) {
            return new SharedWithMeHeartBeatPolicy(heartBeatType, interval);
        }
        if (policyType == TYPE_PROJECT) {
            return new ProjectHeartbeatPolicy(heartBeatType, interval);
        }
        throw new IllegalArgumentException("Unrecognized heart beat policy type " + policyType + " performed.");
    }

    @IntDef({TYPE_COMMON, TYPE_MYVAULT, TYPE_SHARED_WITH_ME, TYPE_PROJECT})
    @interface PolicyType {

    }
}
