package com.skydrm.rmc.ui.service.offline.core;

import android.content.Context;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.ui.service.offline.architecture.OfflineHandler;
import com.skydrm.rmc.ui.service.offline.architecture.OfflineRequest;
import com.skydrm.rmc.ui.service.offline.exception.OfflineException;

public class OfflineViewHandler extends OfflineHandler {
    private final Context mContext;

    public OfflineViewHandler(Context context) {
        mContext = context;
    }

    @Override
    public void handleRequest(OfflineRequest request) throws OfflineException {

    }


    private boolean isNetworkConnected() {
        return SkyDRMApp.getInstance().isNetworkAvailable();
    }
}
