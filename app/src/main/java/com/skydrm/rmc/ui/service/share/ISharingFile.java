package com.skydrm.rmc.ui.service.share;

import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.project.feature.service.IMarkCallback;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.exception.TokenAccessDenyException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ISharingFile {
    String getName();

    String getDuid();

    // for project key is project id(Integer numbers.)
    // values is project name lists.using for sharing page ui display.
    Map<String, String> getShareWith();

    long getFileSize();

    long getLastModifiedTime();

    String getPathDisplay();

    void share(IMarkCallback callback);

    boolean isRevokeable() throws RmsRestAPIException, InvalidRMClientException, SessionInvalidException;

    boolean isSharable() throws InvalidRMClientException, SessionInvalidException,
            IOException, TokenAccessDenyException, RmsRestAPIException;

    boolean isShared();

    boolean isRevoked();

    INxlFileFingerPrint getFingerPrint() throws RmsRestAPIException,
            IOException, TokenAccessDenyException, InvalidRMClientException, SessionInvalidException;

    String getPathId();

    void update(List<String> newRecipients, List<String> removedRecipients);

    void update(boolean revoked);
}
