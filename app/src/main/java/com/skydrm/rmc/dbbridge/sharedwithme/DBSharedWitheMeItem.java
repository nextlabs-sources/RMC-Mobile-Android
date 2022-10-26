package com.skydrm.rmc.dbbridge.sharedwithme;

import android.os.Parcel;
import android.os.Parcelable;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.database.table.sharedwithme.SharedWithMeFileBean;
import com.skydrm.rmc.dbbridge.IDBSharedWithMeItem;
import com.skydrm.rmc.utils.commonUtils.StringUtils;

import java.util.List;

public class DBSharedWitheMeItem implements IDBSharedWithMeItem, Parcelable {
    private SharedWithMeFileBean mRaw;

    public DBSharedWitheMeItem(SharedWithMeFileBean raw) {
        this.mRaw = raw;
    }

    private DBSharedWitheMeItem(Parcel in) {
        mRaw = in.readParcelable(SharedWithMeFileBean.class.getClassLoader());
    }

    public static final Creator<DBSharedWitheMeItem> CREATOR = new Creator<DBSharedWitheMeItem>() {
        @Override
        public DBSharedWitheMeItem createFromParcel(Parcel in) {
            return new DBSharedWitheMeItem(in);
        }

        @Override
        public DBSharedWitheMeItem[] newArray(int size) {
            return new DBSharedWitheMeItem[size];
        }
    };

    @Override
    public int getSharedWithMeFileTBPK() {
        return mRaw._id;
    }

    @Override
    public int getUserTBPK() {
        return mRaw._user_id;
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
    public String getComment() {
        return mRaw.comment;
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
    public boolean isFavorite() {
        return mRaw.isFavorite;
    }

    @Override
    public boolean isOffline() {
        return mRaw.isOffline;
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
    public void setLocalPath(String localPath) {
        SkyDRMApp.getInstance()
                .getDBProvider()
                .updateSharedWitheMeItemLocalPath(mRaw._id, localPath);
        mRaw.localPath = localPath;
    }

    @Override
    public void setOfflineStatus(boolean offline) {
        SkyDRMApp.getInstance()
                .getDBProvider()
                .updateSharedWitheMeItemOfflineStatus(mRaw._id, offline);
        mRaw.isOffline = offline;
    }

    @Override
    public void setOperationStatus(int status) {
        SkyDRMApp.getInstance()
                .getDBProvider()
                .updateSharedWitheMeItemOperationStatus(mRaw._id, status);
        mRaw.operationStatus = status;
    }

    @Override
    public void setRightsAndObligation(int rights, String obligation) {
        SkyDRMApp.getInstance()
                .getDBProvider()
                .updateSharedWitheMeItemRightsAndObligations(mRaw._id, rights, obligation);
    }

    @Override
    public int getOfflineRights() {
        return SkyDRMApp.getInstance()
                .getDBProvider()
                .querySharedWithMeItemOfflineRights(mRaw._id);
    }

    @Override
    public String getOfflineObligations() {
        return SkyDRMApp.getInstance()
                .getDBProvider()
                .querySharedWithMeItemOfflineObligations(mRaw._id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mRaw, flags);
    }

    @Override
    public String getPathId() {
        return ("/" + mRaw.name).toLowerCase();
    }

    @Override
    public boolean isFolder() {
        return false;
    }
}
