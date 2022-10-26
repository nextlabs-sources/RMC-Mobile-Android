package com.skydrm.rmc.ui.service.offline.architecture;

import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.service.fileinfo.IFileInfo;
import com.skydrm.rmc.ui.service.offline.downloader.ICallback;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.exception.TokenAccessDenyException;

import java.io.IOException;

public interface IOffline {
    String getName();

    String getLocalPath();

    String getPathId();

    void downloadForOffline(ICallback callback);

    INxlFileFingerPrint getFingerPrint() throws RmsRestAPIException,
            SessionInvalidException, InvalidRMClientException, IOException, TokenAccessDenyException;

    void doPolicyEvaluation(final INxlFileFingerPrint fp, final IFileInfo.IPolicyCallback callback);

    void setRights(int rights, String obligationRaw);

    void updateOfflineStatus(boolean active);

    void cancel();
}
