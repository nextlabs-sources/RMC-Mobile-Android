package com.skydrm.rmc.datalayer.repo.base;

import com.skydrm.rmc.datalayer.heartbeat.IHeartBeat;

public interface IBaseRepo extends IHeartBeat {
    void clearCache();

    long getCacheSize();

    void updateResetAllOperationStatus();
}
