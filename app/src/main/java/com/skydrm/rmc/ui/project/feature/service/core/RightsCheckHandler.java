package com.skydrm.rmc.ui.project.feature.service.core;

import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.base.IBaseHandlerRequest;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.exception.TokenAccessDenyException;

import java.io.IOException;

public abstract class RightsCheckHandler extends MarkBaseHandler {

    @Override
    public void handleRequest(IBaseHandlerRequest base) throws MarkException {
        IBaseHandlerRequest r = paramCheck(base);

        try {
            INxlFileFingerPrint fp = paramCheck(r.getFingerPrint());
            handleExactly(fp, r.getName());
            dispatchRequest(r);
        } catch (RmsRestAPIException e) {
            throw new MarkException(MarkerStatus.STATUS_FAILED_RMS_REST_API_EXCEPTION,
                    e.getMessage(), e);
        } catch (IOException e) {
            throw new MarkException(MarkerStatus.STATUS_FAILED_IO_EXCEPTION,
                    e.getMessage(), e);
        } catch (TokenAccessDenyException e) {
            throw new MarkException(MarkerStatus.STATUS_FAILED_TOKEN_ACCESS_DENY_EXCEPTION,
                    e.getMessage(), e);
        } catch (InvalidRMClientException e) {
            throw new MarkException(MarkerStatus.STATUS_FAILED_INVALID_RMC_CLIENT,
                    e.getMessage(), e);
        } catch (SessionInvalidException e) {
            throw new MarkException(MarkerStatus.STATUS_FAILED_SESSION_INVALID,
                    e.getMessage(), e);
        }
    }

    protected abstract void handleExactly(INxlFileFingerPrint fp, String name) throws MarkException;

    private void dispatchRequest(IBaseHandlerRequest request) throws MarkException {
        if (mSuccessor == null) {
            return;
        }
        mSuccessor.handleRequest(request);
    }
}
