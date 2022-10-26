package com.skydrm.rmc.datalayer.heartbeat;

public interface IHeartBeatListener {
    void onTaskBegin();

    void onTaskProgress(int progress);

    void onTaskFinish();

    void onTaskFailed(Exception e);
}
