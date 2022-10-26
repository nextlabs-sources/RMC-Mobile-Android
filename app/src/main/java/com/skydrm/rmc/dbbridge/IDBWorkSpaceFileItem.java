package com.skydrm.rmc.dbbridge;

import com.skydrm.rmc.datalayer.repo.base.IFileType;

public interface IDBWorkSpaceFileItem extends IFileType {
    int getWorkSpaceFileTBPK();

    int getUserTBPK();

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

    IOwner getUploader();

    IOwner getLastModifiedUser();

    boolean isFavorite();

    boolean isOffline();

    int getModifyRightsStatus();

    int getEditStatus();

    int getOperationStatus();

    String getLocalPath();

    void delete();

    void setLocalPath(String path);

    void cacheRights(int rights, String obligationRaw);

    void updateOfflineMarker(boolean offline);

    void setOperationStatus(int status);

    int getOfflineRights();

    String getOfflineObligations();

    void updateLastModifiedTime(long lastModifiedTime);
}
