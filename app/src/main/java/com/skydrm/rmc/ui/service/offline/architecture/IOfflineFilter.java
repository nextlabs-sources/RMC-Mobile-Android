package com.skydrm.rmc.ui.service.offline.architecture;

import com.skydrm.rmc.ui.service.offline.exception.OfflineException;

public interface IOfflineFilter {
    interface ICallback {
        void onAccepted();
    }

    boolean accept(String filename) throws OfflineException;
}
