package com.skydrm.rmc.datalayer.repo.base;

import android.os.Parcel;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.service.offline.IOfflineCallback;
import com.skydrm.rmc.ui.service.offline.OfflineManager;
import com.skydrm.rmc.utils.sort.ISharedWithMeSortable;
import com.skydrm.sdk.exception.RmsRestAPIException;

import java.util.List;

public abstract class SharedWithBase extends NxlDoc implements ISharedWithMeSortable {
    protected long mSharedDate;
    protected String mSharedBy;
    protected String mTransactionId;
    protected String mTransactionCode;
    protected String mSharedLink;
    protected List<String> mRights;
    protected String mComment;
    protected boolean isOwner;
    protected int mProtectionType;

    public SharedWithBase(Parcel in) {
        super(in);
        this.mSharedDate = in.readLong();
        this.mSharedBy = in.readString();
        this.mTransactionId = in.readString();
        this.mTransactionCode = in.readString();
        this.mSharedLink = in.readString();
        this.mRights = in.createStringArrayList();
        this.mComment = in.readString();
        this.isOwner = in.readByte() != 0;
        this.mProtectionType = in.readInt();
    }

    public SharedWithBase(String fileName, String duid, long fileSize,
                          String fileType, String filePathId, String filePathDisplay,
                          String localPath, boolean isFavorite, boolean isOffline,
                          int modifyRightsStatus, int editStatus, int operationStatus,
                          long lastModifiedTime, long creationTime,
                          long sharedDate, String sharedBy,
                          String transactionId, String transactionCode,
                          String sharedLink, List<String> rights,
                          String comment, boolean isOwner, int protectionType) {
        super(fileName, duid, fileSize,
                fileType, filePathId, filePathDisplay,
                localPath, isFavorite, isOffline,
                modifyRightsStatus, editStatus, operationStatus,
                lastModifiedTime, creationTime);

        this.mSharedDate = sharedDate;
        this.mSharedBy = sharedBy;
        this.mTransactionId = transactionId;
        this.mTransactionCode = transactionCode;
        this.mSharedLink = sharedLink;
        this.mRights = rights;
        this.mComment = comment;
        this.isOwner = isOwner;
        this.mProtectionType = protectionType;
    }

    public boolean hasShareRights() {
        return mRights.contains("SHARE");
    }

    public long getSharedDate() {
        return mSharedDate;
    }

    public String getSharedBy() {
        return mSharedBy;
    }

    public String getTransactionId() {
        return mTransactionId;
    }

    public String getTransactionCode() {
        return mTransactionCode;
    }

    public String getSharedLink() {
        return mSharedLink;
    }

    public List<String> getRights() {
        return mRights;
    }

    public String getComment() {
        return mComment;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public int getProtectionType() {
        return mProtectionType;
    }

    protected String getDownloadURL() {
        try {
            return SkyDRMApp.getInstance()
                    .getSession()
                    .getRmsRestAPI()
                    .getConfig()
                    .getSharedWithMeDownloadURL();
        } catch (SessionInvalidException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getSortableShareBy() {
        return mSharedBy;
    }

    @Override
    public String getSortableName() {
        return mName;
    }

    @Override
    public long getSortableSize() {
        return mFileSize;
    }

    @Override
    public long getSortableTime() {
        return mSharedDate;
    }

    @Override
    public void markAsOffline(IOfflineCallback callback) {
        OfflineManager.getInstance().markAsOffline(this, callback);
    }

    @Override
    public void unMarkAsOffline() {
        OfflineManager.getInstance().unMarkAsOffline(this);
    }

    @Override
    public void delete() throws SessionInvalidException,
            InvalidRMClientException, RmsRestAPIException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeLong(mSharedDate);
        dest.writeString(mSharedBy);
        dest.writeString(mTransactionId);
        dest.writeString(mTransactionCode);
        dest.writeString(mSharedLink);
        dest.writeStringList(mRights);
        dest.writeString(mComment);
        dest.writeByte((byte) (isOwner ? 1 : 0));
        dest.writeInt(mProtectionType);
    }
}
