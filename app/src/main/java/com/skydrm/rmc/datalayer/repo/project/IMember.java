package com.skydrm.rmc.datalayer.repo.project;

import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.sdk.exception.RmsRestAPIException;

public interface IMember {
    String TITLE_ACTIVE = "Active";
    String TITLE_PENDING = "Pending";

    int getUserId();

    String getDisplayName();

    String getEmail();

    long getCreationTime();

    boolean isPending();

    boolean isOwner();

    String resendInvitation() throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException;

    String revokeInvitation() throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException;

    String remove() throws RmsRestAPIException, SessionInvalidException, InvalidRMClientException;

    IMemberDetail getDetail()
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException;
}
