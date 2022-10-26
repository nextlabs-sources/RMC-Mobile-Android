package com.skydrm.rmc.dbbridge;

import com.skydrm.rmc.datalayer.repo.base.IFileType;

import java.util.List;

public interface IDBSharedWithProjectItem extends IFileType {
    int getSharedWithProjectFileTBPK();

    int getProjectTBPK();

    String getDuid();

    String getName();

    long getSize();

    String getFileType();

    long getSharedDate();

    String getSharedBy();

    String getTransactionId();

    String getTransactionCode();

    String getSharedLink();

    List<String> getRights();

    boolean isOwner();

    int getProtectionType();

    String getSharedBySpace();

    boolean isFavorite();

    boolean isOffline();

    int getOfflineRights();

    String getOfflineObligations();

    int getModifyRightsStatus();

    int getEditStatus();

    int getOperationStatus();

    String getLocalPath();

    String getComment();

    void setLocalPath(String path);

    void setOperationStatus(int status);

    void updateOfflineMarker(boolean offline);

    void cacheRights(int rights, String obligationRaw);

    void delete();
}
