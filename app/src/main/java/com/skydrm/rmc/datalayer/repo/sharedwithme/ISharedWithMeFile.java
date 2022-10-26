package com.skydrm.rmc.datalayer.repo.sharedwithme;

import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.rest.sharewithme.SharedWithMeReshareResult;

import java.util.List;

public interface ISharedWithMeFile {
    long getSharedDate();

    String getSharedBy();

    String getTransactionId();

    String getTransactionCode();

    String getSharedLink();

    List<String> getRights();

    String getComment();

    boolean isOwner();

    int getProtectionType();

    SharedWithMeReshareResult reShare(List<String> members, String comments) throws
            SessionInvalidException, InvalidRMClientException, RmsRestAPIException;
}
