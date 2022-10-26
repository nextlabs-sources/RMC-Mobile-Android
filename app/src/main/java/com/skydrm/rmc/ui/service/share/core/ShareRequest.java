package com.skydrm.rmc.ui.service.share.core;

import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.base.IBaseHandlerRequest;
import com.skydrm.rmc.ui.service.share.ISharingFile;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.exception.TokenAccessDenyException;

import java.io.IOException;

public class ShareRequest implements IBaseHandlerRequest {
    private ISharingFile mFile;

    ShareRequest(ISharingFile file) {
        this.mFile = file;
    }

    ISharingFile getSharingFile() {
        return mFile;
    }

    @Override
    public INxlFileFingerPrint getFingerPrint()
            throws RmsRestAPIException, IOException, TokenAccessDenyException,
            InvalidRMClientException, SessionInvalidException {
        return mFile.getFingerPrint();
    }

    @Override
    public String getName() {
        return mFile.getName();
    }

}
