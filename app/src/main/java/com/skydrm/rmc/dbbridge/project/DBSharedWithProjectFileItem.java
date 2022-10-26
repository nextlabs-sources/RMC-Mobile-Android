package com.skydrm.rmc.dbbridge.project;

import android.os.Parcel;
import android.os.Parcelable;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.database.table.project.SharedWithProjectFileBean;
import com.skydrm.rmc.dbbridge.IDBSharedWithProjectItem;
import com.skydrm.rmc.utils.commonUtils.StringUtils;

import java.util.List;

public class DBSharedWithProjectFileItem implements IDBSharedWithProjectItem, Parcelable {
    private SharedWithProjectFileBean mRaw;

    public DBSharedWithProjectFileItem(SharedWithProjectFileBean raw) {
        this.mRaw = raw;
    }

    private DBSharedWithProjectFileItem(Parcel in) {
        this.mRaw = in.readParcelable(SharedWithProjectFileBean.class.getClassLoader());
    }

    public static final Creator<DBSharedWithProjectFileItem> CREATOR = new Creator<DBSharedWithProjectFileItem>() {
        @Override
        public DBSharedWithProjectFileItem createFromParcel(Parcel in) {
            return new DBSharedWithProjectFileItem(in);
        }

        @Override
        public DBSharedWithProjectFileItem[] newArray(int size) {
            return new DBSharedWithProjectFileItem[size];
        }
    };

    @Override
    public int getSharedWithProjectFileTBPK() {
        return mRaw._id;
    }

    @Override
    public int getProjectTBPK() {
        return mRaw._project_id;
    }

    @Override
    public String getDuid() {
        return mRaw.duid;
    }

    @Override
    public String getName() {
        return mRaw.name;
    }

    @Override
    public long getSize() {
        return mRaw.size;
    }

    @Override
    public String getFileType() {
        return mRaw.fileType;
    }

    @Override
    public long getSharedDate() {
        return mRaw.sharedDate;
    }

    @Override
    public String getSharedBy() {
        return mRaw.sharedBy;
    }

    @Override
    public String getTransactionId() {
        return mRaw.transactionId;
    }

    @Override
    public String getTransactionCode() {
        return mRaw.transactionCode;
    }

    @Override
    public String getSharedLink() {
        return mRaw.sharedLink;
    }

    @Override
    public List<String> getRights() {
        return StringUtils.str2List(mRaw.rights);
    }

    @Override
    public boolean isOwner() {
        return mRaw.isOwner;
    }

    @Override
    public int getProtectionType() {
        return mRaw.protectionType;
    }

    @Override
    public String getSharedBySpace() {
        return mRaw.shareBySpace;
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
    public int getOfflineRights() {
        return SkyDRMApp.getInstance()
                .getDBProvider()
                .querySharedWithProjectFileItemOfflineRights(mRaw._id);
    }

    @Override
    public String getOfflineObligations() {
        return SkyDRMApp.getInstance()
                .getDBProvider()
                .querySharedWithProjectFileItemOfflineObligations(mRaw._id);
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
        return mRaw.localPath;
    }

    @Override
    public String getComment() {
        return mRaw.comment;
    }

    @Override
    public void setLocalPath(String path) {
        SkyDRMApp.getInstance()
                .getDBProvider()
                .updateSharedWithProjectFileItemLocalPath(mRaw._id, path);
    }

    @Override
    public void setOperationStatus(int status) {
        SkyDRMApp.getInstance()
                .getDBProvider()
                .updateSharedWithProjectFileItemOperationStatus(mRaw._id, status);
    }

    @Override
    public void updateOfflineMarker(boolean offline) {
        SkyDRMApp.getInstance()
                .getDBProvider()
                .updateSharedWithProjectFileItemOfflineStatus(mRaw._id, offline);
    }

    @Override
    public void cacheRights(int rights, String obligationRaw) {
        SkyDRMApp.getInstance()
                .getDBProvider()
                .updateSharedWithProjectFileItemRightsAndObligations(mRaw._id, rights, obligationRaw);
    }

    @Override
    public void delete() {
        SkyDRMApp.getInstance()
                .getDBProvider()
                .deleteOneSharedWithProjectFileItem(mRaw._id);
    }

    @Override
    public String getPathId() {
        return "/" + mRaw.name;
    }

    @Override
    public boolean isFolder() {
        return false;
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
