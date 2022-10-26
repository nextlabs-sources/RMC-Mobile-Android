package com.skydrm.rmc.database.table.project;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.skydrm.sdk.rms.rest.project.ListProjectItemResult;

public class ProjectMemberBean implements Parcelable {
    //ProjectMemberBean primary key.[auto increment&unique]
    public int _id = -1;
    //ProjectBean table primary key.
    public int _project_id = -1;

    public int userId;
    public String displayName;
    public String email;
    public long creationTime;
    private String reserved1;
    private String reserved2;

    private ProjectMemberBean() {

    }

    private ProjectMemberBean(Parcel in) {
        _id = in.readInt();
        _project_id = in.readInt();
        userId = in.readInt();
        displayName = in.readString();
        email = in.readString();
        creationTime = in.readLong();
    }

    public static final Creator<ProjectMemberBean> CREATOR = new Creator<ProjectMemberBean>() {
        @Override
        public ProjectMemberBean createFromParcel(Parcel in) {
            return new ProjectMemberBean(in);
        }

        @Override
        public ProjectMemberBean[] newArray(int size) {
            return new ProjectMemberBean[size];
        }
    };

    static ProjectMemberBean newByCursor(Cursor c) {
        ProjectMemberBean ret = new ProjectMemberBean();
        ret._id = c.getInt(c.getColumnIndexOrThrow("_id"));
        ret._project_id = c.getInt(c.getColumnIndexOrThrow("_project_id"));

        ret.userId = c.getInt(c.getColumnIndexOrThrow("user_id"));
        ret.displayName = c.getString(c.getColumnIndexOrThrow("display_name"));
        ret.email = c.getString(c.getColumnIndexOrThrow("email"));
        ret.creationTime = c.getLong(c.getColumnIndexOrThrow("creation_time"));
        return ret;
    }

    public static ProjectMemberBean getInsertItem(ListProjectItemResult.ResultsBean.DetailBean.ProjectMembersBean.MembersBean r) {
        ProjectMemberBean ret = new ProjectMemberBean();
        ret.userId = r.getUserId();
        ret.displayName = r.getDisplayName();
        ret.email = r.getEmail();
        ret.creationTime = r.getCreationTime();
        return ret;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(_id);
        dest.writeInt(_project_id);
        dest.writeInt(userId);
        dest.writeString(displayName);
        dest.writeString(email);
        dest.writeLong(creationTime);
    }
}
