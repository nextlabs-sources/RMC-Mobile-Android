package com.skydrm.rmc.ui.service.offline.core;

import com.skydrm.rmc.ui.service.offline.architecture.IOffline;
import com.skydrm.rmc.ui.service.offline.architecture.IOfflineFilter;
import com.skydrm.rmc.ui.service.offline.architecture.OfflineHandler;
import com.skydrm.rmc.ui.service.offline.architecture.OfflineStatus;
import com.skydrm.rmc.ui.service.offline.architecture.OfflineRequest;
import com.skydrm.rmc.ui.service.offline.exception.OfflineException;

public class OfflineFilterHandler extends OfflineHandler {
    private IOfflineFilter mOfflineFilter;
    private IOfflineFilter.ICallback mCallback;

    OfflineFilterHandler(IOfflineFilter filter, IOfflineFilter.ICallback callback) {
        this.mOfflineFilter = filter;
        this.mCallback = callback;
    }

    @Override
    public void handleRequest(OfflineRequest request) throws OfflineException {
        if (mOfflineFilter == null) {
            throw new OfflineException(OfflineStatus.STATUS_FAILED, "OfflineFilterHandler mOfflineFilter must not be null.");
        }
        IOffline offline = request.getOffline();
        if (mOfflineFilter.accept(offline.getName())) {
            if (mCallback != null) {
                mCallback.onAccepted();
            }
            if (successor != null) {
                successor.handleRequest(request);
            }
        } else {
            throw new OfflineException(OfflineStatus.STATUS_FILTER_FAILED_NOT_SUPPORTED_TYPE,
                    "This file type is not supported marking as offline currently.");
        }
    }
}
