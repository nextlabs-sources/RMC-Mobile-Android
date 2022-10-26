package com.skydrm.rmc.ui.service.offline.architecture;

import com.skydrm.rmc.ui.service.offline.exception.OfflineException;

public abstract class OfflineHandler {
    protected OfflineHandler successor;

    public void setSuccessor(OfflineHandler successor) {
        this.successor = successor;
    }

    public abstract void handleRequest(OfflineRequest request) throws OfflineException;

    protected <T> T paramCheck(T params) throws OfflineException {
        if (params == null) {
            throw new OfflineException(OfflineStatus.STATUS_FAILED, "fatal error:The params must not be null.");
        }
        return params;
    }
}
