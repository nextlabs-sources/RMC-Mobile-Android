package com.skydrm.rmc.dbbridge;

import com.skydrm.rmc.datalayer.repo.base.IFileType;

import java.util.List;

public interface IDBSharedWithMeItem extends IFileType {
    int getSharedWithMeFileTBPK();

    int getUserTBPK();

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

    String getComment();

    boolean isOwner();

    int getProtectionType();

    boolean isFavorite();

    boolean isOffline();

    int getModifyRightsStatus();

    int getEditStatus();

    int getOperationStatus();

    String getLocalPath();

    void setLocalPath(String localPath);

    void setOfflineStatus(boolean offline);

    void setOperationStatus(int status);

    void setRightsAndObligation(int rights, String obligation);

    int getOfflineRights();

    String getOfflineObligations();
}
