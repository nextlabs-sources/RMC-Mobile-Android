package com.skydrm.rmc.ui.base;

import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.exception.TokenAccessDenyException;

import java.io.IOException;

public interface IBaseHandlerRequest {
    INxlFileFingerPrint getFingerPrint() throws RmsRestAPIException,
            IOException, TokenAccessDenyException, InvalidRMClientException, SessionInvalidException;

    String getName();
}
