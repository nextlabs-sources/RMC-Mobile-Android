package com.skydrm.rmc.database.table.project;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.skydrm.rmc.dbbridge.base.Owner;
import com.skydrm.sdk.rms.rest.project.file.ListFileResult;

public class ProjectFileBean implements Parcelable {
    //ProjectFileBean table primary key.[auto increment&unique]
    public int _id = -1;
    //ProjectBean table primary key.
    public int _project_id = -1;

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
    public String ownerRawJson;
    public String lastModifiedUserRawJson;
    public boolean isFavorite;
    public boolean isOffline;
    public int modifyRightsStatus;
    public int editStatus;
    public int operationStatus;
    public String localPath;
    //Used for offline rights.
    private String reserved1;
    //Used for offline obligations.
    private String reserved2;
    private String reserved3;
    private String reserved4;

    protected ProjectFileBean() {

    }

    protected ProjectFileBean(Parcel in) {
        _id = in.readInt();
        _project_id = in.readInt();
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
        ownerRawJson = in.readString();
        lastModifiedUserRawJson = in.readString();
        isFavorite = in.readByte() != 0;
        isOffline = in.readByte() != 0;
        modifyRightsStatus = in.readInt();
        editStatus = in.readInt();
        operationStatus = in.readInt();
        localPath = in.readString();
    }

    public static final Creator<ProjectFileBean> CREATOR = new Creator<ProjectFileBean>() {
        @Override
        public ProjectFileBean createFromParcel(Parcel in) {
            return new ProjectFileBean(in);
        }

        @Override
        public ProjectFileBean[] newArray(int size) {
            return new ProjectFileBean[size];
        }
    };

    public static ProjectFileBean getInsertBean(ListFileResult.ResultsBean.DetailBean.FilesBean r) {
        ProjectFileBean ret = new ProjectFileBean();
        ret.id = r.getId();
        ret.duid = r.getDuid();
        ret.pathDisplay = r.getPathDisplay();
        ret.pathId = r.getPathId();
        ret.name = r.getName();
        ret.fileType = r.getFileType();
        ret.lastModified = r.getLastModified();
        ret.creationTime = r.getCreationTime();
        ret.size = r.getSize();
        ret.isFolder = r.isFolder();
        ret.ownerRawJson = Owner.generateRawJson(r.getOwner().getUserId(), r.getOwner().getDisplayName(),
                r.getOwner().getEmail());
        ret.lastModifiedUserRawJson = getModifiedUserRawJson(r.getLastModifiedUser());
        return ret;
    }

    private static String getModifiedUserRawJson(ListFileResult.ResultsBean.DetailBean.FilesBean.LastModifiedUserBean r) {
        String raw = "{}";
        if (r == null) {
            return raw;
        }
        return Owner.generateRawJson(r.getUserId(),
                r.getDisplayName(),
                r.getEmail());
    }

    public static ProjectFileBean getInsertBean(String id, String duid, String pathDisplay,
                                                String pathId, String name, String fileType,
                                                long lastModified, long creationTime, long size,
                                                boolean isFolder, String ownerRawJson,
                                                String lastModifiedUserRawJson) {
        ProjectFileBean ret = new ProjectFileBean();
        ret.id = id;
        ret.duid = duid;
        ret.pathDisplay = pathDisplay;
        ret.pathId = pathId;
        ret.name = name;
        ret.fileType = fileType;
        ret.lastModified = lastModified;
        ret.creationTime = creationTime;
        ret.size = size;
        ret.isFolder = isFolder;
        ret.ownerRawJson = ownerRawJson;
        ret.lastModifiedUserRawJson = lastModifiedUserRawJson;
        return ret;
    }

    public static ProjectFileBean getUpdateBean(int _id, String id, String duid, String pathDisplay,
                                                String pathId, String name, String fileType,
                                                long lastModified, long creationTime, long size,
                                                boolean isFolder, String ownerRawJson,
                                                String lastModifiedUserRawJson) {
        ProjectFileBean ret = new ProjectFileBean();
        ret._id = _id;
        ret.id = id;
        ret.duid = duid;
        ret.pathDisplay = pathDisplay;
        ret.pathId = pathId;
        ret.name = name;
        ret.fileType = fileType;
        ret.lastModified = lastModified;
        ret.creationTime = creationTime;
        ret.size = size;
        ret.isFolder = isFolder;
        ret.ownerRawJson = ownerRawJson;
        ret.lastModifiedUserRawJson = lastModifiedUserRawJson;
        return ret;
    }

    public static ProjectFileBean newByCursor(Cursor c) {
        ProjectFileBean ret = new ProjectFileBean();
        ret._id = c.getInt(c.getColumnIndexOrThrow("_id"));
        ret._project_id = c.getInt(c.getColumnIndexOrThrow("_project_id"));
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
        ret.ownerRawJson = c.getString(c.getColumnIndexOrThrow("owner_raw_json"));
        ret.lastModifiedUserRawJson = c.getString(c.getColumnIndexOrThrow("last_modified_user_raw_json"));
        ret.isFavorite = c.getInt(c.getColumnIndexOrThrow("is_favorite")) == 1;
        ret.isOffline = c.getInt(c.getColumnIndexOrThrow("is_offline")) == 1;
        ret.modifyRightsStatus = c.getInt(c.getColumnIndexOrThrow("modify_rights_status"));
        ret.editStatus = c.getInt(c.getColumnIndexOrThrow("edit_status"));
        ret.operationStatus = c.getInt(c.getColumnIndexOrThrow("operation_status"));
        ret.localPath = c.getString(c.getColumnIndexOrThrow("local_path"));
        return ret;
    }

    @Override
    public String toString() {
        return "ProjectFileBean{" +
                "id='" + id + '\'' +
                ", duid='" + duid + '\'' +
                ", pathDisplay='" + pathDisplay + '\'' +
                ", pathId='" + pathId + '\'' +
                ", name='" + name + '\'' +
                ", fileType='" + fileType + '\'' +
                ", lastModified=" + lastModified +
                ", creationTime=" + creationTime +
                ", size=" + size +
                ", isFolder=" + isFolder +
                ", ownerRawJson='" + ownerRawJson + '\'' +
                ", lastModifiedUserRawJson='" + lastModifiedUserRawJson + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(_id);
        dest.writeInt(_project_id);
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
        dest.writeString(ownerRawJson);
        dest.writeString(lastModifiedUserRawJson);
        dest.writeByte((byte) (isFavorite ? 1 : 0));
        dest.writeByte((byte) (isOffline ? 1 : 0));
        dest.writeInt(modifyRightsStatus);
        dest.writeInt(editStatus);
        dest.writeInt(operationStatus);
        dest.writeString(localPath);
    }
}
