package com.skydrm.rmc.dbbridge;

import com.skydrm.rmc.datalayer.repo.base.IFileType;

import java.util.List;

public interface IDBProjectFileItem extends IFileType {
    int getProjectFileTBPK();

    int getProjectTBPK();

    String getId();

    String getDuid();

    String getPathDisplay();

    String getPathId();

    String getName();

    String getFileType();

    long getLastModified();

    long getCreationTime();

    long getSize();

    boolean isFolder();

    IOwner getOwner();

    IOwner getLastModifiedUser();

    boolean isFavorite();

    boolean isOffline();

    int getModifyRightsStatus();

    int getEditStatus();

    int getOperationStatus();

    String getLocalPath();

    int getOfflineRights();

    String getOfflineObligations();

    boolean isShared();

    boolean isRevoked();

    List<Integer> getShareWithProject();

    void setLocalPath(String localPath);

    void setOperationStatus(int status);

    void updateOfflineMarker(boolean offline);

    void updateLastModifiedTime(long lastModifiedTime);

    void cacheRights(int rights, String obligationRaw);

    void delete();

    void updateShareStatus(boolean isShared);

    void updateRevokeStatus(boolean isRevoked);

    void updateShareWithProject(List<Integer> data);
}
