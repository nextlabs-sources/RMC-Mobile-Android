package com.skydrm.rmc.ui.service.offline.core;

import com.skydrm.rmc.ui.service.offline.architecture.IOffline;
import com.skydrm.rmc.ui.service.offline.architecture.OfflineHandler;
import com.skydrm.rmc.ui.service.offline.architecture.OfflineRequest;
import com.skydrm.rmc.ui.service.offline.exception.OfflineException;

public class OfflineExpiryHandler extends OfflineHandler {
    IExpiryCallback mCallback;

    public OfflineExpiryHandler() {
    }

    public OfflineExpiryHandler(IExpiryCallback callback) {
        this.mCallback = callback;
        if (mCallback == null) {
            mCallback = useDefault();
        }
    }

    private IExpiryCallback useDefault() {
        return new IExpiryCallback() {
            @Override
            public void onTokenExpired() {

            }
        };
    }

    @Override
    public void handleRequest(OfflineRequest request) throws OfflineException {
        OfflineRequest offlineRequest = paramCheck(request);
        IOffline offlineFile = paramCheck(offlineRequest.getOffline());
        dispatchRequest(request);
    }

    private void sendExpirationMessage() {
        System.out.println("token expired,send expiration message to user.");
        mCallback.onTokenExpired();
    }

    private void dispatchRequest(OfflineRequest request) throws OfflineException {
        OfflineHandler handler = paramCheck(successor);
        handler.handleRequest(request);
    }

    public interface IExpiryCallback {
        void onTokenExpired();
    }
}
