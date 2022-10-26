package com.skydrm.rmc.database.table.myvault;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.skydrm.rmc.utils.commonUtils.StringUtils;
import com.skydrm.sdk.rms.rest.myVault.MyVaultFileListResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class MyVaultFileBean implements Parcelable {
    //MyVaultFileBean table primary key.[auto increment&unique]
    public int _id = -1;
    //User table primary key.
    public int _user_id = -1;

    public String pathId;
    public String pathDisplay;
    public String repoId;
    public long sharedOn;
    public String sharedWith;
    public String rights;
    public String name;
    public String fileType;
    public String duid;
    public boolean isRevoked;
    public boolean isDeleted;
    public boolean isShared;
    public long size;
    public CustomMetadata metadata;
    public boolean isFavorite;
    public boolean isOffline;
    public int operationStatus;
    public int modifyRightsStatus;
    public int editStatus;
    public String localNxlPath;
    public String rawMetadata;
    private String reserved1;
    private String reserved2;
    private String reserved3;
    private String reserved4;

    private MyVaultFileBean() {

    }

    public static class CustomMetadata implements Parcelable {
        public String sourceFilePathDisplay;
        public String sourcePathId;
        public String sourceRepoType;
        public String sourceRepoName;
        public String sourceRepoId;

        CustomMetadata() {

        }

        CustomMetadata(Parcel in) {
            sourceFilePathDisplay = in.readString();
            sourcePathId = in.readString();
            sourceRepoType = in.readString();
            sourceRepoName = in.readString();
            sourceRepoId = in.readString();
        }

        public static final Creator<CustomMetadata> CREATOR = new Creator<CustomMetadata>() {
            @Override
            public CustomMetadata createFromParcel(Parcel in) {
                return new CustomMetadata(in);
            }

            @Override
            public CustomMetadata[] newArray(int size) {
                return new CustomMetadata[size];
            }
        };

        static String generateRawJson(CustomMetadata cmd) {
            String result = "{}";
            try {
                JSONObject metadataObj = new JSONObject();
                metadataObj.put("source_file_path_display", cmd.sourceFilePathDisplay);
                metadataObj.put("source_path_id", cmd.sourcePathId);
                metadataObj.put("source_repo_type", cmd.sourceRepoType);
                metadataObj.put("source_repo_name", cmd.sourceRepoName);
                metadataObj.put("source_repo_id", cmd.sourceRepoId);
                result = metadataObj.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result;
        }

        private static CustomMetadata json2Obj(String rawJson) {
            CustomMetadata cmd = new CustomMetadata();
            try {
                JSONObject metadataObj = new JSONObject(rawJson);
                cmd.sourceFilePathDisplay = metadataObj.optString("source_file_path_display");
                cmd.sourcePathId = metadataObj.optString("source_path_id");
                cmd.sourceRepoType = metadataObj.optString("source_repo_type");
                cmd.sourceRepoName = metadataObj.optString("source_repo_name");
                cmd.sourceRepoId = metadataObj.optString("source_repo_id");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return cmd;
        }

        public String toRawJson() {
            String result = "{}";
            try {
                JSONObject metadataObj = new JSONObject();
                metadataObj.put("source_file_path_display", sourceFilePathDisplay);
                metadataObj.put("source_path_id", sourcePathId);
                metadataObj.put("source_repo_type", sourceRepoType);
                metadataObj.put("source_repo_name", sourceRepoName);
                metadataObj.put("source_repo_id", sourceRepoId);
                result = metadataObj.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(sourceFilePathDisplay);
            dest.writeString(sourcePathId);
            dest.writeString(sourceRepoType);
            dest.writeString(sourceRepoName);
            dest.writeString(sourceRepoId);
        }
    }

    private MyVaultFileBean(Parcel in) {
        _id = in.readInt();
        _user_id = in.readInt();
        pathId = in.readString();
        pathDisplay = in.readString();
        repoId = in.readString();
        sharedOn = in.readLong();
        sharedWith = in.readString();
        rights = in.readString();
        name = in.readString();
        fileType = in.readString();
        duid = in.readString();
        isRevoked = in.readByte() != 0;
        isDeleted = in.readByte() != 0;
        isShared = in.readByte() != 0;
        size = in.readLong();
        metadata = in.readParcelable(CustomMetadata.class.getClassLoader());
        isFavorite = in.readByte() != 0;
        isOffline = in.readByte() != 0;
        operationStatus = in.readInt();
        modifyRightsStatus = in.readInt();
        editStatus = in.readInt();
        localNxlPath = in.readString();
        rawMetadata = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(_id);
        dest.writeInt(_user_id);
        dest.writeString(pathId);
        dest.writeString(pathDisplay);
        dest.writeString(repoId);
        dest.writeLong(sharedOn);
        dest.writeString(sharedWith);
        dest.writeString(rights);
        dest.writeString(name);
        dest.writeString(fileType);
        dest.writeString(duid);
        dest.writeByte((byte) (isRevoked ? 1 : 0));
        dest.writeByte((byte) (isDeleted ? 1 : 0));
        dest.writeByte((byte) (isShared ? 1 : 0));
        dest.writeLong(size);
        dest.writeParcelable(metadata, flags);
        dest.writeByte((byte) (isFavorite ? 1 : 0));
        dest.writeByte((byte) (isOffline ? 1 : 0));
        dest.writeInt(operationStatus);
        dest.writeInt(modifyRightsStatus);
        dest.writeInt(editStatus);
        dest.writeString(localNxlPath);
        dest.writeString(rawMetadata);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MyVaultFileBean> CREATOR = new Creator<MyVaultFileBean>() {
        @Override
        public MyVaultFileBean createFromParcel(Parcel in) {
            return new MyVaultFileBean(in);
        }

        @Override
        public MyVaultFileBean[] newArray(int size) {
            return new MyVaultFileBean[size];
        }
    };

    static MyVaultFileBean newByCursor(Cursor c) {
        MyVaultFileBean mvFile = new MyVaultFileBean();
        mvFile._id = c.getInt(c.getColumnIndexOrThrow("_id"));
        mvFile._user_id = c.getInt(c.getColumnIndexOrThrow("_user_id"));
        mvFile.pathId = c.getString(c.getColumnIndexOrThrow("path_id"));
        mvFile.pathDisplay = c.getString(c.getColumnIndexOrThrow("path_display"));
        mvFile.repoId = c.getString(c.getColumnIndexOrThrow("repo_id"));
        mvFile.sharedOn = c.getLong(c.getColumnIndexOrThrow("shared_on"));
        mvFile.sharedWith = c.getString(c.getColumnIndexOrThrow("shared_with"));
        mvFile.rights = c.getString(c.getColumnIndexOrThrow("rights"));
        mvFile.name = c.getString(c.getColumnIndexOrThrow("name"));
        mvFile.fileType = c.getString(c.getColumnIndexOrThrow("file_type"));
        mvFile.duid = c.getString(c.getColumnIndexOrThrow("duid"));
        mvFile.isRevoked = c.getInt(c.getColumnIndexOrThrow("is_revoked")) == 1;
        mvFile.isDeleted = c.getInt(c.getColumnIndexOrThrow("is_deleted")) == 1;
        mvFile.isShared = c.getInt(c.getColumnIndexOrThrow("is_shared")) == 1;
        mvFile.size = c.getLong(c.getColumnIndexOrThrow("size"));
        mvFile.metadata = CustomMetadata.json2Obj(c.getString(c.getColumnIndexOrThrow("custom_meta_data_raw_json")));
        mvFile.isFavorite = c.getInt(c.getColumnIndexOrThrow("is_favorite")) == 1;
        mvFile.isOffline = c.getInt(c.getColumnIndexOrThrow("is_offline")) == 1;
        mvFile.operationStatus = c.getInt(c.getColumnIndexOrThrow("operation_status"));
        mvFile.modifyRightsStatus = c.getInt(c.getColumnIndexOrThrow("modify_rights_status"));
        mvFile.editStatus = c.getInt(c.getColumnIndexOrThrow("edit_status"));
        mvFile.localNxlPath = c.getString(c.getColumnIndexOrThrow("local_path"));
        return mvFile;
    }

    public static String generateRawMetadata(String sourceFilePathDisplay, String sourcePathId,
                                             String sourceRepoType, String sourceRepoName, String sourceRepoId) {
        CustomMetadata md = new CustomMetadata();
        md.sourceFilePathDisplay = sourceFilePathDisplay;
        md.sourcePathId = sourcePathId;
        md.sourceRepoType = sourceRepoType;
        md.sourceRepoName = sourceRepoName;
        md.sourceRepoId = sourceRepoId;
        return CustomMetadata.generateRawJson(md);
    }

    public static MyVaultFileBean getInsertItem(MyVaultFileListResult.ResultsBean.DetailBean.FilesBean r) {
        MyVaultFileBean ret = new MyVaultFileBean();
        ret.pathId = r.getPathId();
        ret.pathDisplay = r.getPathDisplay();
        ret.repoId = r.getRepoId();
        ret.sharedOn = r.getSharedOn();
        ret.sharedWith = StringUtils.list2Str(r.getSharedWith());
        ret.rights = StringUtils.list2Str(r.getRights());
        ret.name = r.getName();
        ret.fileType = r.getFileType();
        ret.duid = r.getDuid();
        ret.isRevoked = r.isRevoked();
        ret.isDeleted = r.isDeleted();
        ret.isShared = r.isShared();
        ret.size = r.getSize();

        MyVaultFileBean.CustomMetadata md = new MyVaultFileBean.CustomMetadata();
        MyVaultFileListResult.ResultsBean.DetailBean.FilesBean.CustomMetadataBean cmd = r.getCustomMetadata();
        if (cmd != null) {
            md.sourceFilePathDisplay = cmd.getSourceFilePathDisplay();
            md.sourcePathId = cmd.getSourceFilePathId();
            md.sourceRepoType = cmd.getSourceRepoType();
            md.sourceRepoName = cmd.getSourceRepoName();
            md.sourceRepoId = cmd.getSourceRepoId();

            ret.metadata = md;

            ret.rawMetadata = md.toRawJson();
        }
        return ret;
    }
}
