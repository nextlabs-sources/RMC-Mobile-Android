package com.skydrm.rmc.datalayer.repo.project;

import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.sdk.exception.RmsRestAPIException;

public interface IInvitePending {
    int getInvitationId();

    String getInviteeEmail();

    String getInviterDisplayName();

    String getInviterEmail();

    long getInviteTime();

    String getInviteCode();

    String getInviteMsg();

    boolean acceptInvitation()
            throws RmsRestAPIException, SessionInvalidException, InvalidRMClientException;

    boolean denyInvitation(String reason)
            throws RmsRestAPIException, SessionInvalidException, InvalidRMClientException;
}
