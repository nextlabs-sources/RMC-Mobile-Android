package com.skydrm.rmc.ui.project.feature.service.core;

import com.skydrm.rmc.ui.project.feature.service.IMarkCallback;
import com.skydrm.rmc.ui.project.feature.service.IMarkerResponse;
import com.skydrm.rmc.ui.project.feature.service.IMarkerStatusDelivery;

public class MarkerResponse implements IMarkerResponse {
    private IMarkerStatusDelivery mDelivery;
    private MarkerStatus mStatus;

    public MarkerResponse(IMarkerStatusDelivery delivery, IMarkCallback callback) {
        this.mDelivery = delivery;
        this.mStatus = new MarkerStatus();
        mStatus.setCallback(callback);
    }

    @Override
    public void onMarkStart() {
        mStatus.setStatus(MarkerStatus.STATUS_MARK_START);
        mDelivery.post(mStatus);
    }

    @Override
    public void onMarkAllow() {
        mStatus.setStatus(MarkerStatus.STATUS_MARK_ALLOW);
        mDelivery.post(mStatus);
    }

    @Override
    public void onMarkFailed(MarkException e) {
        mStatus.setStatus(MarkerStatus.STATUS_MARK_FAILED);
        mStatus.setException(e);
        mDelivery.post(mStatus);
    }

    @Override
    public void onMarkCanceled() {
        mStatus.setStatus(MarkerStatus.STATUS_MARK_CANCEL);
        mDelivery.post(mStatus);
    }
}
