package com.skydrm.rmc.dbbridge.myvault;

import android.os.Parcel;
import android.os.Parcelable;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.database.table.myvault.MyVaultFileBean;
import com.skydrm.rmc.dbbridge.IDBMyVaultItem;
import com.skydrm.rmc.utils.commonUtils.StringUtils;

import java.util.Arrays;
import java.util.List;

public class DBMyVaultItem implements IDBMyVaultItem, Parcelable {
    private MyVaultFileBean mRaw;

    public DBMyVaultItem(MyVaultFileBean raw) {
        this.mRaw = raw;
    }

    private DBMyVaultItem(Parcel in) {
        mRaw = in.readParcelable(MyVaultFileBean.class.getClassLoader());
    }

    public static final Creator<DBMyVaultItem> CREATOR = new Creator<DBMyVaultItem>() {
        @Override
        public DBMyVaultItem createFromParcel(Parcel in) {
            return new DBMyVaultItem(in);
        }

        @Override
        public DBMyVaultItem[] newArray(int size) {
            return new DBMyVaultItem[size];
        }
    };

    @Override
    public int getMyVaultFileTBPK() {
        return mRaw._id;
    }

    @Override
    public int getUserTBPK() {
        return mRaw._user_id;
    }

    @Override
    public String getPathId() {
        return mRaw.pathId;
    }

    @Override
    public String getPathDisplay() {
        return mRaw.pathDisplay;
    }

    @Override
    public String getRepoId() {
        return mRaw.repoId;
    }

    @Override
    public long getSharedOn() {
        return mRaw.sharedOn;
    }

    @Override
    public List<String> getSharedWith() {
        return Arrays.asList(mRaw.sharedWith.split(","));
    }

    @Override
    public List<String> getRights() {
        return Arrays.asList(mRaw.rights.split(","));
    }

    @Override
    public String getName() {
        return mRaw.name;
    }

    @Override
    public String getFileType() {
        return mRaw.fileType;
    }

    @Override
    public String getDuid() {
        return mRaw.duid;
    }

    @Override
    public boolean isRevoked() {
        return mRaw.isRevoked;
    }

    @Override
    public boolean isDeleted() {
        return mRaw.isDeleted;
    }

    @Override
    public boolean isShared() {
        return mRaw.isShared;
    }

    @Override
    public long getSize() {
        return mRaw.size;
    }

    @Override
    public boolean isFavorite() {
        return mRaw.isFavorite;
    }

    @Override
    public boolean isOffline() {
        return mRaw.isOffline;
    }

    @Override
    public String getSourceFilePathDisplay() {
        return mRaw.metadata.sourceFilePathDisplay;
    }

    @Override
    public String getSourcePathId() {
        return mRaw.metadata.sourcePathId;
    }

    @Override
    public String getSourceRepoType() {
        return mRaw.metadata.sourceRepoType;
    }

    @Override
    public String getSourceRepoName() {
        return mRaw.metadata.sourceRepoName;
    }

    @Override
    public String getSourceRepoId() {
        return mRaw.metadata.sourceRepoId;
    }

    @Override
    public int getModifyRightsStatus() {
        return mRaw.modifyRightsStatus;
    }

    @Override
    public int getEditStatus() {
        return mRaw.editStatus;
    }

    @Override
    public int getOperationStatus() {
        return mRaw.operationStatus;
    }

    @Override
    public String getLocalPath() {
        return mRaw.localNxlPath;
    }

    @Override
    public void setLocalPath(String localPath) {
        SkyDRMApp.getInstance().getDBProvider().
                updateMyVaultItemLocalPath(mRaw._id, localPath);
        mRaw.localNxlPath = localPath;
    }

    @Override
    public void setShared(List<String> emails) {
        String shareStr = StringUtils.list2Str(emails);
        SkyDRMApp.getInstance().getDBProvider().updateMyVaultItemSharedWith(mRaw._id, shareStr);
        mRaw.isShared = true;
        mRaw.sharedWith = shareStr;
    }

    @Override
    public void setRevoked() {
        SkyDRMApp.getInstance().getDBProvider().updateMyVaultItemRevoked(mRaw._id, true);
        mRaw.isRevoked = true;
    }

    @Override
    public void setDeleted() {
        SkyDRMApp.getInstance().getDBProvider().updateMyVaultItemDeleted(mRaw._id, true);
        mRaw.isRevoked = true;
        mRaw.isDeleted = true;
    }

    @Override
    public void setOperationStatus(int status) {
        SkyDRMApp.getInstance().getDBProvider().updateMyVaultItemOperationStatus(mRaw._id, status);
        mRaw.operationStatus = status;
    }

    @Override
    public void setFavoriteStatus(boolean favorite) {
        SkyDRMApp.getInstance().getDBProvider().updateMyVaultItemFavorite(mRaw._id, favorite);
        mRaw.isFavorite = favorite;
    }

    @Override
    public void setOfflineStatus(boolean offline) {
        SkyDRMApp.getInstance().getDBProvider().updateMyVaultItemOffline(mRaw._id, offline);
        mRaw.isOffline = offline;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mRaw, flags);
    }
}
