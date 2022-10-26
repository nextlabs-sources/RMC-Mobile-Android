package com.skydrm.rmc.datalayer.heartbeat;

public abstract class  HeartBeatBasePolicy implements IHeartBeatPolicy {
    private int mType;
    private long mInterval;

    public HeartBeatBasePolicy(int type, long interval) {
        this.mType = type;
        this.mInterval = interval;
    }

    protected void setType(int type) {
        this.mType = type;
    }

    protected void setInterval(long interval) {
        this.mInterval = interval;
    }

    @Override
    public int getType() {
        return mType;
    }

    @Override
    public long getInterval() {
        return mInterval;
    }
}
