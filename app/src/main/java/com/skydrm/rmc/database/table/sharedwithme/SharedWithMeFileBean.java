package com.skydrm.rmc.database.table.sharedwithme;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.skydrm.rmc.utils.commonUtils.StringUtils;
import com.skydrm.sdk.rms.rest.sharewithme.SharedWithMeListFileResult;

public class SharedWithMeFileBean implements Parcelable {
    //SharedWithMeRepo file table primary key.[auto increment&unique]
    public int _id = -1;
    //User table primary key.
    public int _user_id = -1;

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
    public String comment;
    public boolean isOwner;
    public int protectionType;
    public boolean isFavorite;
    public boolean isOffline;
    public int modifyRightsStatus;
    public int editStatus;
    public int operationStatus;
    public String localPath;
    private String reserved1;
    private String reserved2;
    private String reserved3;
    private String reserved4;

    public SharedWithMeFileBean() {
    }

    protected SharedWithMeFileBean(Parcel in) {
        _id = in.readInt();
        _user_id = in.readInt();
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
        comment = in.readString();
        isOwner = in.readByte() != 0;
        protectionType = in.readInt();
        isFavorite = in.readByte() != 0;
        isOffline = in.readByte() != 0;
        modifyRightsStatus = in.readInt();
        editStatus = in.readInt();
        operationStatus = in.readInt();
        localPath = in.readString();
        reserved1 = in.readString();
        reserved2 = in.readString();
        reserved3 = in.readString();
        reserved4 = in.readString();
    }

    public static final Creator<SharedWithMeFileBean> CREATOR = new Creator<SharedWithMeFileBean>() {
        @Override
        public SharedWithMeFileBean createFromParcel(Parcel in) {
            return new SharedWithMeFileBean(in);
        }

        @Override
        public SharedWithMeFileBean[] newArray(int size) {
            return new SharedWithMeFileBean[size];
        }
    };

    public static SharedWithMeFileBean newByCursor(Cursor c) {
        SharedWithMeFileBean ret = new SharedWithMeFileBean();
        ret._id = c.getInt(c.getColumnIndexOrThrow("_id"));
        ret._user_id = c.getInt(c.getColumnIndexOrThrow("_user_id"));
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
        ret.comment = c.getString(c.getColumnIndexOrThrow("comment"));
        ret.isOwner = c.getInt(c.getColumnIndexOrThrow("is_owner")) == 1;
        ret.protectionType = c.getInt(c.getColumnIndexOrThrow("protection_type"));
        ret.isFavorite = c.getInt(c.getColumnIndexOrThrow("is_favorite")) == 1;
        ret.isOffline = c.getInt(c.getColumnIndexOrThrow("is_offline")) == 1;
        ret.modifyRightsStatus = c.getInt(c.getColumnIndexOrThrow("modify_rights_status"));
        ret.editStatus = c.getInt(c.getColumnIndexOrThrow("edit_status"));
        ret.operationStatus = c.getInt(c.getColumnIndexOrThrow("operation_status"));
        ret.localPath = c.getString(c.getColumnIndexOrThrow("local_path"));
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
        dest.writeString(comment);
        dest.writeByte((byte) (isOwner ? 1 : 0));
        dest.writeInt(protectionType);
        dest.writeByte((byte) (isFavorite ? 1 : 0));
        dest.writeByte((byte) (isOffline ? 1 : 0));
        dest.writeInt(modifyRightsStatus);
        dest.writeInt(editStatus);
        dest.writeInt(operationStatus);
        dest.writeString(localPath);
        dest.writeString(reserved1);
        dest.writeString(reserved2);
        dest.writeString(reserved3);
        dest.writeString(reserved4);
    }

    public static SharedWithMeFileBean getInsertItem(SharedWithMeListFileResult.ResultsBean.DetailBean.FilesBean r) {
        SharedWithMeFileBean ret = new SharedWithMeFileBean();
        ret.duid = r.getDuid();
        ret.name = r.getName();
        ret.size = r.getSize();
        ret.sharedDate = r.getSharedDate();
        ret.sharedBy = r.getSharedBy();
        ret.transactionId = r.getTransactionId();
        ret.transactionCode = r.getTransactionCode();
        ret.sharedLink = r.getSharedLink();
        ret.rights = StringUtils.list2Str(r.getRights());
        ret.comment = r.getComment();
        ret.isOwner = r.isIsOwner();
        ret.protectionType = r.getProtectionType();
        return ret;
    }
}
