package com.skydrm.rmc.ui.service.offline.architecture;

public interface IUnMarker extends Runnable {
    interface OnDestroyListener {
        void onDestroy(String key, IUnMarker unMarker);
    }

    void start();

    void destroy();
}
