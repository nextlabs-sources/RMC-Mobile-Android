package com.skydrm.rmc.ui.project.feature.service.core;

import com.skydrm.rmc.ui.base.IBaseHandlerRequest;

public abstract class MarkBaseHandler {
    protected MarkBaseHandler mSuccessor;

    public void setSuccessor(MarkBaseHandler successor) {
        this.mSuccessor = successor;
    }

    public abstract void handleRequest(IBaseHandlerRequest p) throws MarkException;

    protected <T> T paramCheck(T params) throws MarkException {
        if (params == null) {
            throw new MarkException(MarkerStatus.STATUS_FAILED_COMMON, "Fatal error,params must not be null.");
        }
        return params;
    }
}
