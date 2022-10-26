package com.skydrm.rmc.ui.service.offline.core;

import com.skydrm.rmc.ui.service.offline.architecture.IOffline;
import com.skydrm.rmc.ui.service.offline.architecture.IUnMarker;
import com.skydrm.rmc.ui.service.offline.architecture.OfflineRequest;
import com.skydrm.rmc.ui.service.offline.exception.OfflineException;

public class OfflineUnMarker implements IUnMarker, OfflineClearHandler.ICallback {
    private IOffline mOffline;
    private String mTag;
    private IUnMarker.OnDestroyListener mDestroyListener;

    public OfflineUnMarker(IOffline file, String tag, IUnMarker.OnDestroyListener listener) {
        this.mOffline = file;
        this.mTag = tag;
        this.mDestroyListener = listener;
    }

    @Override
    public void run() {
        OfflineClearHandler clearHandler = new OfflineClearHandler(this);
        OfflineRequest request = new OfflineRequest.Builder()
                .setOffline(mOffline)
                .build();
        try {
            clearHandler.handleRequest(request);
        } catch (OfflineException e) {
            e.printStackTrace();
        }
        destroy();
    }

    @Override
    public void start() {
        run();
    }

    @Override
    public void destroy() {
        mDestroyListener.onDestroy(mTag, this);
    }

    @Override
    public void onClear() {

    }
}
