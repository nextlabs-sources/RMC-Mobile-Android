package com.skydrm.rmc.ui.service.offline.core;

import com.skydrm.rmc.ui.service.offline.IOfflineCallback;
import com.skydrm.rmc.ui.service.offline.architecture.IOfflineResponse;
import com.skydrm.rmc.ui.service.offline.architecture.IOfflineStatusDelivery;
import com.skydrm.rmc.ui.service.offline.architecture.OfflineStatus;
import com.skydrm.rmc.ui.service.offline.exception.OfflineException;

public class OfflineResponse implements IOfflineResponse {
    private IOfflineStatusDelivery mDelivery;
    private OfflineStatus mStatus;

    public OfflineResponse(IOfflineStatusDelivery delivery, IOfflineCallback callback) {
        mDelivery = delivery;
        mStatus = new OfflineStatus();
        mStatus.setCallback(callback);
    }

    @Override
    public void onStarted(long timillis) {
        mStatus.setStartTime(timillis);
        mStatus.setStatus(OfflineStatus.STATUS_STARTED);
        mDelivery.post(mStatus);
    }

    @Override
    public void onNetWorkConnected() {
        mStatus.setStatus(OfflineStatus.STATUS_PROGRESS);
        mDelivery.post(mStatus);
    }

    @Override
    public void onAccepted() {
        mStatus.setStatus(OfflineStatus.STATUS_PROGRESS);
        mDelivery.post(mStatus);
    }

    @Override
    public void onCheckPolicy(int type) {
        mStatus.setStatus(OfflineStatus.STATUS_PROGRESS);
        mDelivery.post(mStatus);
    }

    @Override
    public void onCacheRights() {
        mStatus.setStatus(OfflineStatus.STATUS_PROGRESS);
        mDelivery.post(mStatus);
    }

    @Override
    public void onDownloadStarted() {
        mStatus.setStatus(OfflineStatus.STATUS_PROGRESS);
        mDelivery.post(mStatus);
    }

    @Override
    public void onDownloadProgress() {
        mStatus.setStatus(OfflineStatus.STATUS_PROGRESS);
        mDelivery.post(mStatus);
    }

    @Override
    public void onDownloadComplete() {
        mStatus.setStatus(OfflineStatus.STATUS_PROGRESS);
        mDelivery.post(mStatus);
    }

    @Override
    public void onTokenCached() {
        mStatus.setStatus(OfflineStatus.STATUS_PROGRESS);
        mDelivery.post(mStatus);
    }

    @Override
    public void onMarkDone(long timillis) {
        mStatus.setEndTime(timillis);
        mStatus.setStatus(OfflineStatus.STATUS_MARK_SUCCESS);
        mDelivery.post(mStatus);
    }

    @Override
    public void onFailed(OfflineException e) {
        mStatus.setStatus(OfflineStatus.STATUS_FAILED);
        mStatus.setException(e);
        mDelivery.post(mStatus);
    }
}
