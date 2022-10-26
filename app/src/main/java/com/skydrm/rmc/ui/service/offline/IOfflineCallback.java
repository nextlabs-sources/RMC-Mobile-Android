package com.skydrm.rmc.ui.service.offline;

import com.skydrm.rmc.ui.service.offline.exception.OfflineException;

public interface IOfflineCallback {
    void onStarted();

    void onProgress();

    void onMarkDone();

    void onMarkFailed(OfflineException e);
}
