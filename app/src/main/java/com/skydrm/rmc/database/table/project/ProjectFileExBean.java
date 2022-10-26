package com.skydrm.rmc.database.table.project;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.skydrm.sdk.rms.rest.project.file.ListFileResult;

import org.json.JSONArray;

import java.util.List;

public class ProjectFileExBean extends ProjectFileBean implements Parcelable {
    // ProjectFile table primary key.
    public int _project_file_id = -1;

    public boolean isShared;
    public boolean isRevoked;
    public String shareWithProjectRawJson;
    public String shareWithPersonRawJson;

    // Reserved for future use.
    private String reserved1;
    private String reserved2;
    private String reserved3;
    private String reserved4;
    private String reserved5;
    private String reserved6;
    private String reserved7;
    private String reserved8;
    private String reserved9;
    private String reserved10;
    private String reserved11;
    private String reserved12;

    private ProjectFileExBean() {

    }

    private ProjectFileExBean(Parcel in) {
        super(in);
        _project_file_id = in.readInt();
        isShared = in.readByte() != 0;
        isRevoked = in.readByte() != 0;
        shareWithProjectRawJson = in.readString();
        shareWithPersonRawJson = in.readString();
    }

    public static final Creator<ProjectFileExBean> CREATOR = new Creator<ProjectFileExBean>() {
        @Override
        public ProjectFileExBean createFromParcel(Parcel in) {
            return new ProjectFileExBean(in);
        }

        @Override
        public ProjectFileExBean[] newArray(int size) {
            return new ProjectFileExBean[size];
        }
    };


    public static ProjectFileExBean newByCursor(Cursor c) {
        ProjectFileExBean ret = new ProjectFileExBean();
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

        ret._project_file_id = c.getInt(c.getColumnIndexOrThrow("_project_file_id"));
        ret.isShared = c.getInt(c.getColumnIndexOrThrow("is_shared")) == 1;
        ret.isRevoked = c.getInt(c.getColumnIndexOrThrow("is_revoked")) == 1;
        ret.shareWithProjectRawJson = c.getString(c.getColumnIndexOrThrow("share_with_project_raw_json"));
        ret.shareWithPersonRawJson = c.getString(c.getColumnIndexOrThrow("share_with_person_raw_json"));

        return ret;
    }

    public static ProjectFileExBean getInsertBean(ListFileResult.ResultsBean.DetailBean.FilesBean r) {
        ProjectFileExBean ret = new ProjectFileExBean();
        ret.id = r.getId();
        ret.isShared = r.isIsShared();
        ret.isRevoked = r.isRevoked();
        ret.shareWithProjectRawJson = generateShareWithProjectRawJson(r.getShareWithProject());
        ret.shareWithPersonRawJson = "{}";
        return ret;
    }

    public static String generateShareWithProjectRawJson(List<Integer> shareWithProjects) {
        JSONArray array = new JSONArray();
        if (shareWithProjects == null || shareWithProjects.isEmpty()) {
            return "{}";
        }
        for (Integer id : shareWithProjects) {
            array.put(id);
        }
        return array.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(_project_file_id);
        dest.writeByte((byte) (isShared ? 1 : 0));
        dest.writeByte((byte) (isRevoked ? 1 : 0));
        dest.writeString(shareWithProjectRawJson);
        dest.writeString(shareWithPersonRawJson);
    }

}
