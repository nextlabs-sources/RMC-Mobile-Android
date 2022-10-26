package com.skydrm.rmc.datalayer.repo.project;

public interface IPendingMember {
    int getInvitationId();

    String getInviteeEmail();

    String getInviterDisplayName();

    String getInviterEmail();

    long getInviteTime();
}
