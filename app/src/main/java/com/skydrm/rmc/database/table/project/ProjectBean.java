package com.skydrm.rmc.database.table.project;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class ProjectBean implements Parcelable {
    //ProjectBean table primary key.[auto increment&unique]
    public int _id = -1;
    //User table primary key.
    public int _user_id = -1;

    public int id;
    public String parentTenantId;
    public String parentTenantName;
    public String tokenGroupName;
    public String name;
    public String description;
    public String displayName;
    public long creationTime;
    public long configurationModified;
    public int totalMembers;
    public int totalFiles;
    public boolean isOwnedByMe;
    public String ownerRawJson;
    public String accountType;
    public long trialEndTime;
    public List<ProjectMemberBean> members;
    public String expiry;
    public String watermark;
    public String classification;

    //private long userAccessCount;
    //private long lastRefreshMillis;
    public long lastAccessTime;

    private String reserved1;
    private String reserved2;
    private String reserved3;
    private String reserved4;
    private String reserved5;
    private String reserved6;
    private String reserved7;
    private String reserved8;

    private ProjectBean() {

    }

    private ProjectBean(Parcel in) {
        _id = in.readInt();
        _user_id = in.readInt();
        id = in.readInt();
        parentTenantId = in.readString();
        parentTenantName = in.readString();
        tokenGroupName = in.readString();
        name = in.readString();
        description = in.readString();
        displayName = in.readString();
        creationTime = in.readLong();
        configurationModified = in.readLong();
        totalMembers = in.readInt();
        totalFiles = in.readInt();
        isOwnedByMe = in.readByte() != 0;
        ownerRawJson = in.readString();
        accountType = in.readString();
        trialEndTime = in.readLong();
        members = in.createTypedArrayList(ProjectMemberBean.CREATOR);
        expiry = in.readString();
        watermark = in.readString();
        classification = in.readString();
        lastAccessTime = in.readLong();
    }

    public static final Creator<ProjectBean> CREATOR = new Creator<ProjectBean>() {
        @Override
        public ProjectBean createFromParcel(Parcel in) {
            return new ProjectBean(in);
        }

        @Override
        public ProjectBean[] newArray(int size) {
            return new ProjectBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(_id);
        dest.writeInt(_user_id);
        dest.writeInt(id);
        dest.writeString(parentTenantId);
        dest.writeString(parentTenantName);
        dest.writeString(tokenGroupName);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(displayName);
        dest.writeLong(creationTime);
        dest.writeLong(configurationModified);
        dest.writeInt(totalMembers);
        dest.writeInt(totalFiles);
        dest.writeByte((byte) (isOwnedByMe ? 1 : 0));
        dest.writeString(ownerRawJson);
        dest.writeString(accountType);
        dest.writeLong(trialEndTime);
        dest.writeTypedList(members);
        dest.writeString(expiry);
        dest.writeString(watermark);
        dest.writeString(classification);
        dest.writeLong(lastAccessTime);
    }

    static ProjectBean newByCursor(Cursor c) {
        ProjectBean ret = new ProjectBean();
        ret._id = c.getInt(c.getColumnIndexOrThrow("_id"));
        ret._user_id = c.getInt(c.getColumnIndexOrThrow("_user_id"));
        ret.id = c.getInt(c.getColumnIndexOrThrow("id"));
        ret.parentTenantId = c.getString(c.getColumnIndexOrThrow("parent_tenant_id"));
        ret.parentTenantName = c.getString(c.getColumnIndexOrThrow("parent_tenant_name"));
        ret.tokenGroupName = c.getString(c.getColumnIndexOrThrow("token_group_name"));
        ret.name = c.getString(c.getColumnIndexOrThrow("name"));
        ret.description = c.getString(c.getColumnIndexOrThrow("description"));
        ret.displayName = c.getString(c.getColumnIndexOrThrow("display_name"));
        ret.creationTime = c.getLong(c.getColumnIndexOrThrow("creation_time"));
        ret.configurationModified = c.getLong(c.getColumnIndexOrThrow("configuration_modified"));
        ret.totalMembers = c.getInt(c.getColumnIndexOrThrow("total_members"));
        ret.totalFiles = c.getInt(c.getColumnIndexOrThrow("total_files"));
        ret.isOwnedByMe = c.getInt(c.getColumnIndexOrThrow("is_owned_by_me")) == 1;
        ret.ownerRawJson = c.getString(c.getColumnIndexOrThrow("owner_raw_json"));
        ret.accountType = c.getString(c.getColumnIndexOrThrow("account_type"));
        ret.trialEndTime = c.getLong(c.getColumnIndexOrThrow("trial_end_time"));
        ret.expiry = c.getString(c.getColumnIndexOrThrow("expiry"));
        ret.watermark = c.getString(c.getColumnIndexOrThrow("watermark"));
        ret.classification = c.getString(c.getColumnIndexOrThrow("classification_raw_json"));
        ret.lastAccessTime = c.getLong(c.getColumnIndexOrThrow("last_access_time"));
        return ret;
    }
}
