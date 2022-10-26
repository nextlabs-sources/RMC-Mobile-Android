package com.skydrm.rmc.dbbridge;

import java.util.List;

public interface IUser {
    int getUserTBPK();

    int getServerTBPK();

    String getName();

    String getEmail();

    int getRMSUserID();

    int getIdpType();

    long getTTL();

    String getTicket();

    String getTenantId();

    String getDefaultTenant();

    String getDefaultTenantURL();

    List<IMembership> getMemberships();

    boolean isProjectAdmin();

    boolean isTenantAdmin();

    IPreference getPreference();

    String getUserRawJson();

    interface IPreference {
        long getSyncWithRmsTimeMillis();
    }
}
