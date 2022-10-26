package com.skydrm.sdk;

/**
 * Created by oye on 12/1/2016.
 */

public interface INxlFileFingerPrint extends INxlRights, INxlObligations, INxlExpiry, INxlTags {
    String getDUID();

    String getOwnerID();

    int getMaintenanceLevel();

    String getRootAgreementKey();

    String getICAAgreementKey();

    String getOtp();

    boolean hasRights();

    boolean hasObligations();

    boolean hasExpiry();

    boolean hasTags();

    String getADHocSectionRaw();

    String getCentralSectionRaw();

    long getLastModifiedTime();

    String getNormalFileName();

    String getDisplayWatermark();
}
