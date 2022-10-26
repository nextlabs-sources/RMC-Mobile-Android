package com.skydrm.rmc.datalayer.repo.library;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.support.v4.util.Pair;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.base.NxlDoc;
import com.skydrm.rmc.datalayer.repo.base.Utils;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.reposystem.exception.FileDownloadException;
import com.skydrm.rmc.ui.project.feature.service.IMarkCallback;
import com.skydrm.rmc.ui.service.offline.IOfflineCallback;
import com.skydrm.rmc.ui.service.offline.downloader.ICallback;
import com.skydrm.rmc.ui.service.protect.IProtectFile;
import com.skydrm.rmc.ui.service.share.ISharingFile;
import com.skydrm.rmc.ui.service.share.ShareManager;
import com.skydrm.rmc.utils.sort.IBaseSortable;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.policy.Expiry;
import com.skydrm.sdk.policy.Obligations;
import com.skydrm.sdk.policy.Rights;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class LibraryFile extends NxlDoc implements IBaseSortable, ISharingFile, IProtectFile {
    private Rights mRights;
    private Obligations mObligations;
    private Expiry mExpiry;

    private Uri uri;
    private File mTmpDest;

    private LibraryFile(Parcel in) {
        super(in);
    }

    public static final Creator<LibraryFile> CREATOR = new Creator<LibraryFile>() {
        @Override
        public LibraryFile createFromParcel(Parcel in) {
            return new LibraryFile(in);
        }

        @Override
        public LibraryFile[] newArray(int size) {
            return new LibraryFile[size];
        }
    };

    public LibraryFile(String fileName, String duid, long fileSize,
                       String fileType, String filePathId, String filePathDisplay,
                       String localPath, boolean isFavorite, boolean isOffline,
                       int modifyRightsStatus, int editStatus, int operationStatus,
                       long lastModifiedTime, long creationTime) {
        super(fileName, duid, fileSize,
                fileType, filePathId, filePathDisplay,
                localPath, isFavorite, isOffline,
                modifyRightsStatus, editStatus, operationStatus,
                lastModifiedTime, creationTime);
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public Uri getUri() {
        return uri;
    }

    @Override
    public String getOfflineObligations() {
        return null;
    }

    public void encapsulateRights(Rights r, Obligations o, Expiry e) {
        this.mRights = r;
        this.mObligations = o;
        this.mExpiry = e;
    }

    Rights getEncryptionRights() {
        return mRights;
    }

    Obligations getEncryptionObligations() {
        return mObligations;
    }

    Expiry getEncryptionExpiry() {
        return mExpiry;
    }

    @Override
    protected String getPartialPath() {
        return null;
    }

    @Override
    protected String createNewPartialPath() {
        return null;
    }

    @Override
    protected void setLocalPath(String path) {

    }

    @Override
    protected void partialDownload(File doc, DownloadListener listener)
            throws RmsRestAPIException, SessionInvalidException, InvalidRMClientException {

    }

    @Override
    protected Pair<Integer, String> getOfflineRightsAndObligations() {
        return null;
    }

    @Override
    public void download(int type, DownloadListener listener)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException, IOException {

    }

    @Override
    public void delete()
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {

    }

    @Override
    public void markAsOffline(IOfflineCallback callback) {

    }

    @Override
    public void unMarkAsOffline() {

    }

    @Override
    public String getServiceName(Context ctx) {
        if (ctx == null) {
            return "";
        }
        return ctx.getString(R.string.Library);
    }

    @Override
    public List<String> getRights() {
        return null;
    }

    @Override
    public void downloadForOffline(ICallback callback) {

    }

    @Override
    public void setRights(int rights, String obligationRaw) {

    }

    @Override
    public void updateOfflineStatus(boolean active) {

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
        return mLastModifiedTime;
    }

    @Override
    public Map<String, String> getShareWith() {
        return null;
    }

    @Override
    public void share(IMarkCallback callback) {
        ShareManager.getInstance().share(this, callback);
    }

    @Override
    public boolean isRevokeable() {
        return false;
    }

    @Override
    public boolean isSharable() {
        return true;
    }

    @Override
    public boolean isShared() {
        return false;
    }

    @Override
    public boolean isRevoked() {
        return false;
    }

    @Override
    public void update(List<String> newRecipients, List<String> removedRecipients) {

    }

    @Override
    public void update(boolean revoked) {

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    @Override
    public long getCacheSize() {
        return 0;
    }

    @Override
    protected int getOfflineRights() {
        return 0;
    }

    @Override
    public void clearCache() {
        // do nothing.
    }

    @Override
    public void tryGetFile(Context ctx, ICallBack callBack) {
        if (ctx == null) {
            if (callBack != null) {
                callBack.onDownloadFailed(new FileDownloadException("Parameter ctx is null."));
            }
            return;
        }
        if (uri == null) {
            if (callBack != null) {
                callBack.onDownloadFailed(new FileDownloadException("File uri is required to perform download action."));
            }
            return;
        }

        try {
            InputStream is = ctx.getContentResolver().openInputStream(uri);
            mTmpDest = new File(getDownloadMountPoint(), mName);

            DownloadTask task = new DownloadTask(is, mTmpDest, callBack);
            task.run();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            if (callBack != null) {
                callBack.onDownloadFailed(new FileDownloadException(e.getMessage(),
                        FileDownloadException.ExceptionCode.Common));
            }
        }
    }

    @Override
    public void release() {
        if (mTmpDest != null) {
            mTmpDest.delete();
        }
    }

    private File getDownloadMountPoint() {
        try {
            return Utils.getTmpMountPoint();
        } catch (SessionInvalidException e) {
            e.printStackTrace();
        }
        return null;
    }

}
