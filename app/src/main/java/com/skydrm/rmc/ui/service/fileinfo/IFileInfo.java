package com.skydrm.rmc.ui.service.fileinfo;

import android.content.Context;

import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.exception.TokenAccessDenyException;

import java.io.IOException;
import java.util.List;

public interface IFileInfo {
    String getServiceName(Context ctx);

    String getName();

    String getPathDisplay();

    long getFileSize();

    long getLastModifiedTime();

    List<String> getRights();

    String getDuid();

    boolean isOffline();

    INxlFileFingerPrint getFingerPrint() throws RmsRestAPIException,
            SessionInvalidException, InvalidRMClientException, IOException, TokenAccessDenyException;

    /**
     * Run as a async way.
     *
     * @param callback
     */
    void doPolicyEvaluation(INxlFileFingerPrint fp, IPolicyCallback callback);


    interface IPolicyCallback {
        void onSuccess(List<String> rights, String obligations);

        void onFailed(Exception e);
    }
}
