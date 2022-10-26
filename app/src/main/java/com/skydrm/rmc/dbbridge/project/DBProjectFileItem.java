package com.skydrm.rmc.dbbridge.project;

import android.os.Parcel;
import android.os.Parcelable;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.database.table.project.ProjectFileBean;
import com.skydrm.rmc.database.table.project.ProjectFileExBean;
import com.skydrm.rmc.dbbridge.IDBProjectFileItem;
import com.skydrm.rmc.dbbridge.IOwner;
import com.skydrm.rmc.dbbridge.base.Owner;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class DBProjectFileItem implements IDBProjectFileItem, Parcelable {
    private ProjectFileExBean mRaw;

    private DBProjectFileItem(Parcel in) {
        mRaw = in.readParcelable(ProjectFileBean.class.getClassLoader());
    }

    public DBProjectFileItem(ProjectFileExBean raw) {
        this.mRaw = raw;
    }

    public static final Creator<DBProjectFileItem> CREATOR = new Creator<DBProjectFileItem>() {
        @Override
        public DBProjectFileItem createFromParcel(Parcel in) {
            return new DBProjectFileItem(in);
        }

        @Override
        public DBProjectFileItem[] newArray(int size) {
            return new DBProjectFileItem[size];
        }
    };

    @Override
    public int getProjectFileTBPK() {
        return mRaw._id;
    }

    @Override
    public int getProjectTBPK() {
        return mRaw._project_id;
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
    public IOwner getOwner() {
        return Owner.newByJson(mRaw.ownerRawJson);
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
    public int getOfflineRights() {
        return SkyDRMApp.getInstance()
                .getDBProvider()
                .queryProjectFileItemOfflineRights(mRaw._id);
    }

    @Override
    public String getOfflineObligations() {
        return SkyDRMApp.getInstance()
                .getDBProvider()
                .queryProjectFileItemOfflineObligations(mRaw._id);
    }

    @Override
    public boolean isShared() {
        return mRaw.isShared;
    }

    @Override
    public boolean isRevoked() {
        return mRaw.isRevoked;
    }

    @Override
    public List<Integer> getShareWithProject() {
        List<Integer> ret = new ArrayList<>();
        String raw = mRaw.shareWithProjectRawJson;
        if (raw == null || raw.isEmpty()) {
            return ret;
        }
        if (raw.equals("{}")) {
            return ret;
        }
        try {
            JSONArray arr = new JSONArray(mRaw.shareWithProjectRawJson);
            for (int i = 0; i < arr.length(); i++) {
                ret.add(arr.getInt(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public void setLocalPath(String localPath) {
        SkyDRMApp.getInstance()
                .getDBProvider()
                .updateProjectFileItemLocalPath(mRaw._id, localPath);
        mRaw.localPath = localPath;
    }

    @Override
    public void setOperationStatus(int status) {
        SkyDRMApp.getInstance()
                .getDBProvider()
                .updateProjectFileItemOperationStatus(mRaw._id, status);
        mRaw.operationStatus = status;
    }

    @Override
    public void updateOfflineMarker(boolean offline) {
        SkyDRMApp.getInstance()
                .getDBProvider()
                .updateProjectFileItemOfflineMarker(mRaw._id, offline);
        mRaw.isOffline = offline;
    }

    @Override
    public void updateLastModifiedTime(long lastModifiedTime) {
        SkyDRMApp.getInstance()
                .getDBProvider()
                .updateProjectFileItemLastModifiedTimeValue(mRaw._id, lastModifiedTime);
        mRaw.lastModified = lastModifiedTime;
    }

    @Override
    public void cacheRights(int rights, String obligationRaw) {
        SkyDRMApp.getInstance()
                .getDBProvider()
                .updateProjectFileItemRightsAndObligations(mRaw._id, rights, obligationRaw);
    }

    @Override
    public void delete() {
        SkyDRMApp.getInstance()
                .getDBProvider()
                .deleteProjectFileItem(mRaw._id);
    }

    @Override
    public void updateShareStatus(boolean isShared) {
        SkyDRMApp.getInstance()
                .getDBProvider()
                .updateProjectFileItemShareStatus(mRaw._id, isShared);
    }

    @Override
    public void updateRevokeStatus(boolean isRevoked) {
        SkyDRMApp.getInstance()
                .getDBProvider()
                .updateProjectFileItemRevokeStatus(mRaw._id, isRevoked);
    }

    @Override
    public void updateShareWithProject(List<Integer> data) {
        String raw = ProjectFileExBean.generateShareWithProjectRawJson(data);
        SkyDRMApp.getInstance()
                .getDBProvider()
                .updateProjectFileItemShareWithProject(mRaw._id, raw);
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
