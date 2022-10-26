package com.skydrm.rmc.ui.service.modifyrights.core;

import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.base.IBaseHandlerRequest;
import com.skydrm.rmc.ui.service.modifyrights.IModifyRightsFile;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.exception.TokenAccessDenyException;

import java.io.IOException;

public class ModifyRightsRequest implements IBaseHandlerRequest {
    private IModifyRightsFile mFile;

    ModifyRightsRequest(IModifyRightsFile file) {
        this.mFile = file;
    }

    @Override
    public INxlFileFingerPrint getFingerPrint() throws RmsRestAPIException,
            IOException, TokenAccessDenyException, InvalidRMClientException, SessionInvalidException {
        return mFile.getFingerPrint();
    }

    @Override
    public String getName() {
        return mFile.getName();
    }
}
