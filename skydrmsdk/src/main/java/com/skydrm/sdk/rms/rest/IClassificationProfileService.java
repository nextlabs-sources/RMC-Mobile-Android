package com.skydrm.sdk.rms.rest;

import android.support.annotation.NonNull;

import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.types.ClassificationProfileRetrieveResult;

public interface IClassificationProfileService {
    String getClassificationProfile(@NonNull String tenantId) throws RmsRestAPIException;

    void updateClassificationProfile(@NonNull String tenantId) throws RmsRestAPIException;
}
