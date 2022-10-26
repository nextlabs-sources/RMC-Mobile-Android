package com.skydrm.rmc.ui.service.share;

import android.content.Context;
import android.support.annotation.NonNull;

import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.sdk.exception.RmsRestAPIException;

import java.util.List;

public interface ISharingService {
    int getId();

    String getServiceName(@NonNull Context ctx);

    boolean shareToProject(@NonNull ISharingFile file,
                           @NonNull List<Integer> recipients,
                           String comments)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException;

    boolean shareToPerson(@NonNull ISharingFile file,
                          @NonNull List<String> recipients,
                          String comments)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException;

    boolean updateRecipients(@NonNull ISharingFile file,
                             List<String> newRecipients,
                             List<String> removedRecipients,
                             String comments)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException;

    boolean revokeAllRights(@NonNull ISharingFile file)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException;
}
