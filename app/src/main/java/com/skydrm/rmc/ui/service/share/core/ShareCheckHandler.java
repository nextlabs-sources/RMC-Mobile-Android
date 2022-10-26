package com.skydrm.rmc.ui.service.share.core;

import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.base.IBaseHandlerRequest;
import com.skydrm.rmc.ui.project.feature.service.core.MarkBaseHandler;
import com.skydrm.rmc.ui.project.feature.service.core.MarkException;
import com.skydrm.rmc.ui.project.feature.service.core.MarkerStatus;
import com.skydrm.rmc.ui.service.share.ISharingFile;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.exception.TokenAccessDenyException;

import java.io.IOException;

public class ShareCheckHandler extends MarkBaseHandler {

    @Override
    public void handleRequest(IBaseHandlerRequest base) throws MarkException {
        IBaseHandlerRequest r = paramCheck(base);
        if (r instanceof ShareRequest) {
            ShareRequest sr = (ShareRequest) r;
            ISharingFile file = paramCheck(sr.getSharingFile());
            try {
                if (file.isSharable()) {
                    dispatchRequest(r);
                    return;
                }
            } catch (InvalidRMClientException e) {
                throw new MarkException(MarkerStatus.STATUS_FAILED_INVALID_RMC_CLIENT,
                        e.getMessage(), e);
            } catch (SessionInvalidException e) {
                throw new MarkException(MarkerStatus.STATUS_FAILED_SESSION_INVALID,
                        e.getMessage(), e);
            } catch (IOException e) {
                throw new MarkException(MarkerStatus.STATUS_FAILED_IO_EXCEPTION,
                        e.getMessage(), e);
            } catch (TokenAccessDenyException e) {
                throw new MarkException(MarkerStatus.STATUS_FAILED_TOKEN_ACCESS_DENY_EXCEPTION,
                        e.getMessage(), e);
            } catch (RmsRestAPIException e) {
                throw new MarkException(MarkerStatus.STATUS_FAILED_RMS_REST_API_EXCEPTION,
                        e.getMessage(), e);
            }
            throw new MarkException(MarkerStatus.STATUS_FAILED_UNAUTHORIZED,
                    "You are not authorized to perform this action.");
        }
        throw new MarkException(MarkerStatus.STATUS_FAILED_COMMON,
                "Wrong request processed.");
    }

    private void dispatchRequest(IBaseHandlerRequest request) throws MarkException {
        if (mSuccessor == null) {
            return;
        }
        mSuccessor.handleRequest(request);
    }

}
