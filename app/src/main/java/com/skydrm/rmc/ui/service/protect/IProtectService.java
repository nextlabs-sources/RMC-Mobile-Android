package com.skydrm.rmc.ui.service.protect;

import android.content.Context;
import android.support.annotation.NonNull;

import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.policy.Expiry;
import com.skydrm.sdk.policy.Obligations;
import com.skydrm.sdk.policy.Rights;
import com.skydrm.sdk.rms.rest.user.User;

import java.io.File;
import java.util.Map;
import java.util.Set;

public interface IProtectService {
    String getServiceName(@NonNull Context ctx);

    String getClassificationRaw();

    User.IExpiry getIExpiry();

    String getWatermark();

    void protect(String normalPath,
                 Rights rights, Obligations obligations, Expiry expiry,
                 String parentPathId,
                 IProtectCallback callback);

    void protect(String normalPath, Map<String, Set<String>> tags,
                 String parentPathId,
                 IProtectCallback callback);

    boolean upload(File nxlFile, String parentPathId)
            throws InvalidRMClientException, SessionInvalidException, RmsRestAPIException;

    interface IProtectCallback {
        void onPreProtect();

        void onProtectSuccess();

        void onProtectFailed(Exception e);
    }

}
