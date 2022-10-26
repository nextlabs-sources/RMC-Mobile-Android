package com.skydrm.rmc.dbbridge;

import android.os.Parcelable;

import java.util.List;

public interface IDBProjectItem extends Parcelable {
    int getProjectTBPK();

    int getUserTBPK();

    int getId();

    String getParentTenantId();

    String getParentTenantName();

    String getTokenGroupName();

    String getName();

    String getDescription();

    String getDisplayName();

    long getCreationTime();

    long getConfigurationModified();

    int getTotalMembers();

    int getTotalFiles();

    boolean isOwnedByMe();

    IOwner getOwner();

    String getAccountType();

    long getTrialEndTime();

    String getExpiry();

    String getWatermark();

    String getClassificationRaw();

    List<IMember> getMember();

    long getUsage();

    long getQuota();

    long getLastRefreshMillis();

    void setClassification(String raw);

    void updateAccessCount();

    void updateTrialEndTime(long time);

    void updateLastAccessTime(long time);

    void updateLastRefreshTime();

    void increaseTotalCount();

    long getLastAccessTime();

    void delete();

    interface IMember {
        int getProjectMemberTBPK();

        int getProjectTBPK();

        int getUserId();

        String getDisplayName();

        String getEmail();

        long getCreationTime();
    }
}
