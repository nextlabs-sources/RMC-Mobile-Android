package com.skydrm.rmc.dbbridge;

import java.util.List;

public interface IDBMyVaultItem {
    int getMyVaultFileTBPK();

    int getUserTBPK();

    String getPathId();

    String getPathDisplay();

    String getRepoId();

    long getSharedOn();

    List<String> getSharedWith();

    List<String> getRights();

    String getName();

    String getFileType();

    String getDuid();

    boolean isRevoked();

    boolean isDeleted();

    boolean isShared();

    long getSize();

    boolean isFavorite();

    boolean isOffline();

    String getSourceFilePathDisplay();

    String getSourcePathId();

    String getSourceRepoType();

    String getSourceRepoName();

    String getSourceRepoId();

    int getModifyRightsStatus();

    int getEditStatus();

    int getOperationStatus();

    String getLocalPath();

    void setLocalPath(String localPath);

    void setShared(List<String> emails);

    void setRevoked();

    void setDeleted();

    void setOperationStatus(int status);

    void setFavoriteStatus(boolean favorite);

    void setOfflineStatus(boolean offline);
}
