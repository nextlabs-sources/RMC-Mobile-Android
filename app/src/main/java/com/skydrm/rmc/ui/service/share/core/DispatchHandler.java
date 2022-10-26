package com.skydrm.rmc.ui.service.share.core;

import com.skydrm.rmc.ui.base.IBaseHandlerRequest;
import com.skydrm.rmc.ui.project.feature.service.core.MarkBaseHandler;
import com.skydrm.rmc.ui.project.feature.service.core.MarkException;

public class DispatchHandler extends MarkBaseHandler {
    private ICallback mCallback;

    public DispatchHandler(ICallback callback) {
        this.mCallback = callback;
    }

    @Override
    public void handleRequest(IBaseHandlerRequest request) throws MarkException {
        if (mCallback != null) {
            mCallback.onMarkAllow();
        }
    }

    public interface ICallback {
        void onMarkAllow();
    }
}
