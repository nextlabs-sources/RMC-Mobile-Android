package com.skydrm.rmc.database.table.project;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.skydrm.rmc.utils.commonUtils.StringUtils;
import com.skydrm.sdk.rms.rest.sharedwithspace.ListFileResult;

public class SharedWithProjectFileBean implements Parcelable {
    public int _id;
    public int _project_id;

    public String duid;
    public String name;
    public long size;
    public String fileType;
    public long sharedDate;
    public String sharedBy;
    public String transactionId;
    public String transactionCode;
    public String sharedLink;
    public String rights;
    public boolean isOwner;
    public int protectionType;
    public String shareBySpace;
    public boolean isFavorite;
    public boolean isOffline;
    public int modifyRightsStatus;
    public int editStatus;
    public int operationStatus;
    public String comment;
    public String localPath;

    public String reserved1;
    public String reserved2;
    public String reserved3;
    public String reserved4;
    public String reserved5;
    public String reserved6;
    public String reserved7;
    public String reserved8;
    public String reserved9;
    public String reserved10;
    public String reserved11;
    public String reserved12;

    private SharedWithProjectFileBean() {
        
    }

    private SharedWithProjectFileBean(Parcel in) {
        _id = in.readInt();
        _project_id = in.readInt();
        duid = in.readString();
        name = in.readString();
        size = in.readLong();
        fileType = in.readString();
        sharedDate = in.readLong();
        sharedBy = in.readString();
        transactionId = in.readString();
        transactionCode = in.readString();
        sharedLink = in.readString();
        rights = in.readString();
        isOwner = in.readByte() != 0;
        protectionType = in.readInt();
        shareBySpace = in.readString();
        isFavorite = in.readByte() != 0;
        isOffline = in.readByte() != 0;
        modifyRightsStatus = in.readInt();
        editStatus = in.readInt();
        operationStatus = in.readInt();
        comment = in.readString();
        localPath = in.readString();
    }

    public static final Creator<SharedWithProjectFileBean> CREATOR = new Creator<SharedWithProjectFileBean>() {
        @Override
        public SharedWithProjectFileBean createFromParcel(Parcel in) {
            return new SharedWithProjectFileBean(in);
        }

        @Override
        public SharedWithProjectFileBean[] newArray(int size) {
            return new SharedWithProjectFileBean[size];
        }
    };

    public static SharedWithProjectFileBean newByCursor(Cursor c) {
        SharedWithProjectFileBean ret = new SharedWithProjectFileBean();
        ret._id = c.getInt(c.getColumnIndexOrThrow("_id"));
        ret._project_id = c.getInt(c.getColumnIndexOrThrow("_project_id"));
        ret.duid = c.getString(c.getColumnIndexOrThrow("duid"));
        ret.name = c.getString(c.getColumnIndexOrThrow("name"));
        ret.size = c.getLong(c.getColumnIndexOrThrow("size"));
        ret.fileType = c.getString(c.getColumnIndexOrThrow("file_type"));
        ret.sharedDate = c.getLong(c.getColumnIndexOrThrow("shared_date"));
        ret.sharedBy = c.getString(c.getColumnIndexOrThrow("shared_by"));
        ret.transactionId = c.getString(c.getColumnIndexOrThrow("transaction_id"));
        ret.transactionCode = c.getString(c.getColumnIndexOrThrow("transaction_code"));
        ret.sharedLink = c.getString(c.getColumnIndexOrThrow("shared_link"));
        ret.rights = c.getString(c.getColumnIndexOrThrow("rights"));
        ret.isOwner = c.getInt(c.getColumnIndexOrThrow("is_owner")) == 1;
        ret.protectionType = c.getInt(c.getColumnIndexOrThrow("protection_type"));
        ret.shareBySpace = c.getString(c.getColumnIndexOrThrow("shared_by_space"));
        ret.isFavorite = c.getInt(c.getColumnIndexOrThrow("is_favorite")) == 1;
        ret.isOffline = c.getInt(c.getColumnIndexOrThrow("is_offline")) == 1;
        ret.modifyRightsStatus = c.getInt(c.getColumnIndexOrThrow("modify_rights_status"));
        ret.editStatus = c.getInt(c.getColumnIndexOrThrow("edit_status"));
        ret.operationStatus = c.getInt(c.getColumnIndexOrThrow("operation_status"));
        ret.comment = c.getString(c.getColumnIndexOrThrow("comment"));
        ret.localPath = c.getString(c.getColumnIndexOrThrow("local_path"));
        return ret;
    }

    public static SharedWithProjectFileBean getInsertItem(ListFileResult.ResultsBean.DetailBean.FilesBean r) {
        SharedWithProjectFileBean ret = new SharedWithProjectFileBean();
        if (r == null) {
            return ret;
        }
        ret.duid = r.getDuid();
        ret.name = r.getName();
        ret.size = r.getSize();
        ret.fileType = r.getFileType();
        ret.sharedDate = r.getSharedDate();
        ret.sharedBy = r.getSharedBy();
        ret.transactionId = r.getTransactionId();
        ret.transactionCode = r.getTransactionCode();
        ret.sharedLink = r.getSharedLink();
        ret.rights = StringUtils.list2Str(r.getRights());
        ret.isOwner = r.isIsOwner();
        ret.protectionType = r.getProtectionType();
        ret.shareBySpace = r.getSharedByProject();
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
        dest.writeString(duid);
        dest.writeString(name);
        dest.writeLong(size);
        dest.writeString(fileType);
        dest.writeLong(sharedDate);
        dest.writeString(sharedBy);
        dest.writeString(transactionId);
        dest.writeString(transactionCode);
        dest.writeString(sharedLink);
        dest.writeString(rights);
        dest.writeByte((byte) (isOwner ? 1 : 0));
        dest.writeInt(protectionType);
        dest.writeString(shareBySpace);
        dest.writeByte((byte) (isFavorite ? 1 : 0));
        dest.writeByte((byte) (isOffline ? 1 : 0));
        dest.writeInt(modifyRightsStatus);
        dest.writeInt(editStatus);
        dest.writeInt(operationStatus);
        dest.writeString(comment);
        dest.writeString(localPath);
    }
}
