package com.skydrm.rmc.dbbridge.workspace;

import android.os.Parcel;
import android.os.Parcelable;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.database.table.workspace.WorkSpaceFileBean;
import com.skydrm.rmc.dbbridge.IDBWorkSpaceFileItem;
import com.skydrm.rmc.dbbridge.IOwner;
import com.skydrm.rmc.dbbridge.base.Owner;

public class DBWorkSpaceFileItem implements Parcelable, IDBWorkSpaceFileItem {
    private WorkSpaceFileBean mRaw;

    private DBWorkSpaceFileItem(Parcel in) {
        mRaw = in.readParcelable(WorkSpaceFileBean.class.getClassLoader());
    }

    public DBWorkSpaceFileItem(WorkSpaceFileBean raw) {
        this.mRaw = raw;
    }

    @Override
    public int getWorkSpaceFileTBPK() {
        return mRaw._id;
    }

    @Override
    public int getUserTBPK() {
        return mRaw._user_id;
    }

    @Override
    public String getId() {
        return mRaw.id;
    }

    @Override
    public String getDuid() {
        return mRaw.duid;
    }

    @Override
    public String getPathDisplay() {
        return mRaw.pathDisplay;
    }

    @Override
    public String getPathId() {
        return mRaw.pathId;
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
    public long getLastModified() {
        return mRaw.lastModified;
    }

    @Override
    public long getCreationTime() {
        return mRaw.creationTime;
    }

    @Override
    public long getSize() {
        return mRaw.size;
    }

    @Override
    public boolean isFolder() {
        return mRaw.isFolder;
    }

    @Override
    public IOwner getUploader() {
        return Owner.newByJson(mRaw.uploaderRawJson);
    }

    @Override
    public IOwner getLastModifiedUser() {
        return Owner.newByJson(mRaw.lastModifiedUserRawJson);
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
    public void delete() {
        SkyDRMApp.getInstance()
                .getDBProvider()
                .deleteWorkSpaceFileItem(mRaw._id);
    }

    @Override
    public void setLocalPath(String path) {
        SkyDRMApp.getInstance()
                .getDBProvider()
                .updateWorkSpaceFileItemLocalPath(mRaw._id, path);
    }

    @Override
    public void cacheRights(int rights, String obligationRaw) {
        SkyDRMApp.getInstance()
                .getDBProvider()
                .updateWorkSpaceFileItemRightsAndObligations(mRaw._id, rights, obligationRaw);
    }

    @Override
    public void updateOfflineMarker(boolean offline) {
        SkyDRMApp.getInstance()
                .getDBProvider()
                .updateWorkSpaceFileItemOfflineMarker(mRaw._id, offline);
    }

    @Override
    public void setOperationStatus(int status) {
        SkyDRMApp.getInstance()
                .getDBProvider()
                .updateWorkSpaceFileItemOperationStatus(mRaw._id, status);
    }

    @Override
    public int getOfflineRights() {
        return SkyDRMApp.getInstance()
                .getDBProvider()
                .queryWorkSpaceFileItemOfflineRights(mRaw._id);
    }

    @Override
    public String getOfflineObligations() {
        return SkyDRMApp.getInstance()
                .getDBProvider()
                .queryWorkSpaceFileItemOfflineObligations(mRaw._id);
    }

    @Override
    public void updateLastModifiedTime(long lastModifiedTime) {
        SkyDRMApp.getInstance()
                .getDBProvider()
                .updateWorkSpaceFileItemLastModifiedTimeValue(mRaw._id, lastModifiedTime);
    }

    public static final Creator<DBWorkSpaceFileItem> CREATOR = new Creator<DBWorkSpaceFileItem>() {
        @Override
        public DBWorkSpaceFileItem createFromParcel(Parcel in) {
            return new DBWorkSpaceFileItem(in);
        }

        @Override
        public DBWorkSpaceFileItem[] newArray(int size) {
            return new DBWorkSpaceFileItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mRaw, flags);
    }
}
