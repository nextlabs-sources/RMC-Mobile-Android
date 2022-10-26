package com.skydrm.rmc.ui.project.feature.service.share;

import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.project.feature.service.core.MarkException;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.exception.TokenAccessDenyException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Deprecated
public interface IShare {
    INxlFileFingerPrint getFingerPrint() throws RmsRestAPIException,
            IOException, TokenAccessDenyException, InvalidRMClientException, SessionInvalidException;

    String getName();

    int getProjectId();

    void doPolicyEvaluation(String membershipId, Map<String, Set<String>> tags, IPolicyCallback callback);

    interface IPolicyCallback {
        void onSuccess(List<String> rights, String obligations);

        void onFailed(MarkException e);
    }
}
