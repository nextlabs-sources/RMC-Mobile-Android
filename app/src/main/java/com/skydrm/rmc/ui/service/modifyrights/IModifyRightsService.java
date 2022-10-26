package com.skydrm.rmc.ui.service.modifyrights;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.sdk.exception.RmsRestAPIException;

public interface IModifyRightsService {
    String getServiceName(Context ctx);

    String getClassificationRaw();

    boolean modifyRights(@NonNull String fileName, @NonNull String parentPathId, @Nullable String fileTags)
            throws RmsRestAPIException, SessionInvalidException, InvalidRMClientException;
}
