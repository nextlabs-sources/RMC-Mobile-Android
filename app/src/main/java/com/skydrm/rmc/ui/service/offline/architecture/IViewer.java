package com.skydrm.rmc.ui.service.offline.architecture;

import com.skydrm.rmc.ui.service.offline.exception.OfflineException;

public interface IViewer {
    interface ICallback {
        void onTokenCancelRecovery();

        void onError(OfflineException e);
    }

    void view();
}
