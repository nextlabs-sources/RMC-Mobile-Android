package com.skydrm.rmc.ui.service.offline.architecture;

public interface IMarker {
    interface OnDestroyListener {
        void onDestroy(String key, IMarker marker);
    }

    void start();

    void cancel();

    boolean isRunning();

    void destroy();
}
