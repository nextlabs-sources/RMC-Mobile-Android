package com.skydrm.rmc.ui.service.offline.architecture;

import com.skydrm.rmc.ui.service.offline.exception.OfflineException;

public interface IOfflineResponse {
    void onStarted(long timillis);

    void onNetWorkConnected();

    void onAccepted();

    void onCheckPolicy(int type);

    void onCacheRights();

    void onDownloadStarted();

    void onDownloadProgress();

    void onDownloadComplete();

    void onTokenCached();

    void onMarkDone(long timillis);

    void onFailed(OfflineException e);
}
