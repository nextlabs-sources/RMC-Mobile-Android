package com.skydrm.rmc.database.table.workspace;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.skydrm.rmc.datalayer.repo.base.Utils;
import com.skydrm.rmc.dbbridge.base.Owner;
import com.skydrm.sdk.rms.rest.workspace.ListFileResult;

public class WorkSpaceFileBean implements Parcelable {
    public int _id;
    public int _user_id;
    public String id;
    public String duid;
    public String pathDisplay;
    public String pathId;
    public String name;
    public String fileType;
    public long lastModified;
    public long creationTime;
    public long size;
    public boolean isFolder;
    public String uploaderRawJson;
    public String lastModifiedUserRawJson;
    public boolean isFavorite;
    public boolean isOffline;
    public int modifyRightsStatus;
    public int editStatus;
    public int operationStatus;
    public String localPath;
    public String reserved1;
    public String reserved2;
    public String reserved3;
    public String reserved4;
    public String reserved5;
    public String reserved6;
    public String reserved7;
    public String reserved8;

    private WorkSpaceFileBean() {

    }

    private WorkSpaceFileBean(Parcel in) {
        _id = in.readInt();
        _user_id = in.readInt();
        id = in.readString();
        duid = in.readString();
        pathDisplay = in.readString();
        pathId = in.readString();
        name = in.readString();
        fileType = in.readString();
        lastModified = in.readLong();
        creationTime = in.readLong();
        size = in.readLong();
        isFolder = in.readByte() != 0;
        uploaderRawJson = in.readString();
        lastModifiedUserRawJson = in.readString();
        isFavorite = in.readByte() != 0;
        isOffline = in.readByte() != 0;
        modifyRightsStatus = in.readInt();
        editStatus = in.readInt();
        operationStatus = in.readInt();
        localPath = in.readString();
    }

    public static final Creator<WorkSpaceFileBean> CREATOR = new Creator<WorkSpaceFileBean>() {
        @Override
        public WorkSpaceFileBean createFromParcel(Parcel in) {
            return new WorkSpaceFileBean(in);
        }

        @Override
        public WorkSpaceFileBean[] newArray(int size) {
            return new WorkSpaceFileBean[size];
        }
    };

    public static WorkSpaceFileBean newByCursor(Cursor c) {
        WorkSpaceFileBean ret = new WorkSpaceFileBean();
        if (c == null) {
            return ret;
        }
        ret._id = c.getInt(c.getColumnIndexOrThrow("_id"));
        ret._user_id = c.getInt(c.getColumnIndexOrThrow("_user_id"));
        ret.id = c.getString(c.getColumnIndexOrThrow("id"));
        ret.duid = c.getString(c.getColumnIndexOrThrow("duid"));
        ret.pathDisplay = c.getString(c.getColumnIndexOrThrow("path_display"));
        ret.pathId = c.getString(c.getColumnIndexOrThrow("path_id"));
        ret.name = c.getString(c.getColumnIndexOrThrow("name"));
        ret.fileType = c.getString(c.getColumnIndexOrThrow("file_type"));
        ret.lastModified = c.getLong(c.getColumnIndexOrThrow("last_modified"));
        ret.creationTime = c.getLong(c.getColumnIndexOrThrow("creation_time"));
        ret.size = c.getLong(c.getColumnIndexOrThrow("size"));
        ret.isFolder = c.getInt(c.getColumnIndexOrThrow("is_folder")) == 1;
        ret.uploaderRawJson = c.getString(c.getColumnIndexOrThrow("uploader_raw_json"));
        ret.lastModifiedUserRawJson = c.getString(c.getColumnIndexOrThrow("last_modified_user_raw_json"));
        ret.isFavorite = c.getInt(c.getColumnIndexOrThrow("is_favorite")) == 1;
        ret.isOffline = c.getInt(c.getColumnIndexOrThrow("is_offline")) == 1;
        ret.modifyRightsStatus = c.getInt(c.getColumnIndexOrThrow("modify_rights_status"));
        ret.editStatus = c.getInt(c.getColumnIndexOrThrow("edit_status"));
        ret.operationStatus = c.getInt(c.getColumnIndexOrThrow("operation_status"));
        ret.localPath = c.getString(c.getColumnIndexOrThrow("local_path"));
        return ret;
    }


    public static WorkSpaceFileBean getInsertItem(ListFileResult.ResultsBean.DetailBean.FilesBean f) {
        WorkSpaceFileBean ret = new WorkSpaceFileBean();
        if (f == null) {
            return ret;
        }
        ret.id = f.getId();
        String duid = f.getDuid();
        ret.duid = duid == null ? "" : duid;
        ret.pathDisplay = f.getPathDisplay();
        ret.pathId = f.getPathId();
        ret.name = f.getName();
        String fileType = f.getFileType();
        ret.fileType = fileType == null ? "" : fileType;
        ret.lastModified = f.getLastModified();
        ret.creationTime = f.getCreationTime();
        ret.size = f.getSize();
        ret.isFolder = f.isFolder();
        ret.uploaderRawJson = Utils.generateUploaderRawJson(f.getUploader());
        ret.lastModifiedUserRawJson = Utils.generateLastModifiedUserRawJson(f.getLastModifiedUser());
        return ret;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(_id);
        dest.writeInt(_user_id);
        dest.writeString(id);
        dest.writeString(duid);
        dest.writeString(pathDisplay);
        dest.writeString(pathId);
        dest.writeString(name);
        dest.writeString(fileType);
        dest.writeLong(lastModified);
        dest.writeLong(creationTime);
        dest.writeLong(size);
        dest.writeByte((byte) (isFolder ? 1 : 0));
        dest.writeString(uploaderRawJson);
        dest.writeString(lastModifiedUserRawJson);
        dest.writeByte((byte) (isFavorite ? 1 : 0));
        dest.writeByte((byte) (isOffline ? 1 : 0));
        dest.writeInt(modifyRightsStatus);
        dest.writeInt(editStatus);
        dest.writeInt(operationStatus);
        dest.writeString(localPath);
    }
}
