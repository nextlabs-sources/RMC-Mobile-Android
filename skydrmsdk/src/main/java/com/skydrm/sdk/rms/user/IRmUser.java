package com.skydrm.sdk.rms.user;

import com.skydrm.sdk.rms.user.membership.IMemberShip;

import java.util.List;

public interface IRmUser {
    int getUserId();

    String getUserIdStr();

    String getTicket();

    String getMembershipId();

    String getTenantId();

    String getName();

    String getEmail();

    long getTtl();

    int getIdpType();

    String getDefaultTenant();

    String getDefaultTenantUrl();

    String getTokenGroupName();

    boolean isOwner(String ownerId);

    boolean isProjectAdmin();

    boolean isTenantAdmin();

    boolean isADHocEnabled();

    List<IMemberShip> getMemberships();

    void setName(String name);

    void updateOrInsertMembershipItem(IMemberShip memberShip);
}
