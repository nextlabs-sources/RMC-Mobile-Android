package com.skydrm.rmc.ui.service.modifyrights;

import android.os.Parcelable;

import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.project.feature.service.IMarkCallback;
import com.skydrm.rmc.ui.project.feature.service.share.IShare;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.exception.TokenAccessDenyException;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public interface IModifyRightsFile extends Parcelable {
    void modifyRights(IMarkCallback callback);

    INxlFileFingerPrint getFingerPrint() throws RmsRestAPIException,
            IOException, TokenAccessDenyException, InvalidRMClientException, SessionInvalidException;

    String getName();

    String getDuid();

    String getPathDisplay();

    String getParent();

    void doPolicyEvaluation(String membershipId, Map<String, Set<String>> tags, IShare.IPolicyCallback callback);

    void handleModifyFileRights();

    boolean checkRemoteRightsModifiedThenUpdate()
            throws SessionInvalidException, IOException, InvalidRMClientException,
            RmsRestAPIException, TokenAccessDenyException;
}
