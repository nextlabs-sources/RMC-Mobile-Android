package com.skydrm.rmc.datalayer.repo.myvault;

import android.content.Context;
import android.os.Parcel;
import android.support.v4.util.Pair;

import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.RepoFactory;
import com.skydrm.rmc.datalayer.repo.base.IBaseRepo;
import com.skydrm.rmc.datalayer.repo.base.NxlDoc;
import com.skydrm.rmc.datalayer.repo.base.RepoType;
import com.skydrm.rmc.dbbridge.IDBMyVaultItem;
import com.skydrm.rmc.dbbridge.myvault.DBMyVaultItem;
import com.skydrm.rmc.engine.Render.RenderHelper;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.filemark.FavoriteMarkImpl;
import com.skydrm.rmc.filemark.IMarkItem;
import com.skydrm.rmc.reposystem.localrepo.helper.Helper;
import com.skydrm.rmc.ui.service.favorite.model.IFavoriteFile;
import com.skydrm.rmc.ui.service.fileinfo.IFileInfo;
import com.skydrm.rmc.ui.service.offline.IOfflineCallback;
import com.skydrm.rmc.ui.service.offline.OfflineManager;
import com.skydrm.rmc.ui.service.offline.architecture.IOffline;
import com.skydrm.rmc.ui.service.offline.downloader.DownloadManager;
import com.skydrm.rmc.ui.service.offline.downloader.ICallback;
import com.skydrm.rmc.ui.service.offline.downloader.core.DownloadRequest;
import com.skydrm.rmc.ui.service.offline.downloader.exception.DownloadException;
import com.skydrm.rmc.utils.sort.IBaseSortable;
import com.skydrm.sdk.IRecipients;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.policy.Rights;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.rest.myVault.MyVaultMetaDataResult;
import com.skydrm.sdk.rms.rest.myVault.UpdateRecipientsResult;
import com.skydrm.sdk.rms.user.IRmUser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class MyVaultFile extends NxlDoc implements IMyVaultFile, IBaseSortable, IFileInfo,
        IMarkItem, IOffline, IFavoriteFile {
    private String mRepoId;
    private long mSharedOn;
    private List<String> mSharedWith;
    private List<String> mRights;
    private boolean isRevoked;
    private boolean isDeleted;
    private boolean isShared;

    private String mSourceRepoName;
    private String mSourceRepoId;
    private String mSourceRepoType;
    private String mSourceFilePathId;
    private String mSourceFilePathDisplay;

    //This used to update db item.
    private IDBMyVaultItem mDbItem;

    private MyVaultFile(IDBMyVaultItem item) {
        super(item.getName(), item.getDuid(),
                item.getSize(), item.getFileType(),
                item.getPathId(), item.getPathDisplay(),
                item.getLocalPath(),
                item.isFavorite(), item.isOffline(),
                item.getModifyRightsStatus(), item.getEditStatus(), item.getOperationStatus(),
                item.getSharedOn(), item.getSharedOn());

        this.mDbItem = item;

        this.mRepoId = item.getRepoId();
        this.mSharedOn = item.getSharedOn();
        this.mSharedWith = item.getSharedWith();
        this.mRights = item.getRights();
        this.isRevoked = item.isRevoked();
        this.isDeleted = item.isDeleted();
        this.isShared = item.isShared();

        this.mSourceRepoName = item.getSourceRepoName();
        this.mSourceRepoId = item.getSourceRepoId();
        this.mSourceRepoType = item.getSourceRepoType();
        this.mSourceFilePathId = item.getSourcePathId();
        this.mSourceFilePathDisplay = item.getSourceFilePathDisplay();
    }

    private MyVaultFile(Parcel in) {
        super(in);
        this.mRepoId = in.readString();
        this.mSharedOn = in.readLong();
        this.mSharedWith = in.createStringArrayList();
        this.mRights = in.createStringArrayList();
        this.isRevoked = in.readByte() == 1;
        this.isDeleted = in.readByte() == 1;
        this.isShared = in.readByte() == 1;

        this.mSourceRepoName = in.readString();
        this.mSourceRepoId = in.readString();
        this.mSourceRepoType = in.readString();
        this.mSourceFilePathId = in.readString();
        this.mSourceFilePathDisplay = in.readString();

        this.mDbItem = in.readParcelable(DBMyVaultItem.class.getClassLoader());
    }

    public static final Creator<MyVaultFile> CREATOR = new Creator<MyVaultFile>() {
        @Override
        public MyVaultFile createFromParcel(Parcel in) {
            return new MyVaultFile(in);
        }

        @Override
        public MyVaultFile[] newArray(int size) {
            return new MyVaultFile[size];
        }
    };

    @Override
    public String getRepoId() {
        return mRepoId;
    }

    @Override
    public long getSharedOn() {
        return mSharedOn;
    }

    @Override
    public List<String> getSharedWith() {
        return mSharedWith;
    }

    @Override
    public boolean isUnMark() {
        return !isFavorite;
    }

    @Override
    public String getDisplayPath() {
        return mPathDisplay;
    }

    @Override
    public String getParentFileId() {
        return mPathId;
    }

    @Override
    public long getSize() {
        return mFileSize;
    }

    @Override
    public String getServiceName(Context ctx) {
        if (ctx == null) {
            return "";
        }
        return ctx.getString(R.string.MyVault);
    }

    @Override
    public long getLastModifiedTime() {
        return mSharedOn;
    }

    @Override
    public void downloadForOffline(final ICallback callback) {
        File myVaultDir = RenderHelper.getMyVaultMountPoint();
        if (myVaultDir == null) {
            return;
        }
        final String destPath = Helper.nxPath2AbsPath(myVaultDir, mName);
        fireOfflineDownload(destPath, new ICallback() {
            @Override
            public void onDownloadProgress(long finished, long length, int percent) {
                if (callback != null) {
                    callback.onDownloadProgress(finished, length, percent);
                }
            }

            @Override
            public void onDownloadPaused() {
                if (callback != null) {
                    callback.onDownloadPaused();
                }
            }

            @Override
            public void onDownloadCanceled() {
                if (callback != null) {
                    callback.onDownloadCanceled();
                }
            }

            @Override
            public void onDownloadComplete() {
                setLocalPath(destPath);
                if (callback != null) {
                    callback.onDownloadComplete();
                }
            }

            @Override
            public void onFailed(DownloadException e) {
                if (callback != null) {
                    callback.onFailed(e);
                }
            }
        });
    }

    @Override
    public void setRights(int rights, String obligationRaw) {
        //For ad-hoc file ignore this.
    }

    @Override
    protected int getOfflineRights() {
        return -1;
    }

    @Override
    public String getOfflineObligations() {
        return "";
    }

    @Override
    protected Pair<Integer, String> getOfflineRightsAndObligations() {
        return null;
    }

    @Override
    public void updateOfflineStatus(boolean active) {
        mDbItem.setOfflineStatus(active);
        isOffline = active;
        mDbItem.setOperationStatus(-1);
        mOperationStatus = -1;
    }

    @Override
    protected String getPartialPath() {
        File myVaultDir = RenderHelper.getMyVaultMountPoint();
        if (myVaultDir == null) {
            return "";
        }
        String partialPath = Helper.nxPath2AbsPath(myVaultDir, "partial_".concat(mName));
        File partialF = new File(partialPath);
        if (partialF.exists() && partialF.isFile()) {
            return partialPath;
        }
        return "";
    }

    @Override
    protected String createNewPartialPath() {
        File myVaultDir = RenderHelper.getMyVaultMountPoint();
        if (myVaultDir == null) {
            return "";
        }
        return Helper.nxPath2AbsPath(myVaultDir, "partial_".concat(mName));
    }

    @Override
    protected void partialDownload(File doc, DownloadListener listener)
            throws RmsRestAPIException, SessionInvalidException, InvalidRMClientException {
        innerDownload(1, doc.getAbsolutePath(), 0, 0x4000, listener);
    }

    @Override
    public List<String> getRights() {
        return mRights;
    }

    @Override
    public boolean isRevoked() {
        return isRevoked;
    }

    public void setRevoked(boolean revoked) {
        isRevoked = revoked;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    @Override
    public void setOperationStatus(int status) {
        super.setOperationStatus(status);
        mDbItem.setOperationStatus(status);
    }

    @Override
    public boolean isDeleted() {
        return isDeleted;
    }

    @Override
    public boolean isShared() {
        return isShared;
    }

    @Override
    public String getSourceRepoName() {
        return mSourceRepoName;
    }

    @Override
    public String getSourceRepoId() {
        return mSourceRepoId;
    }

    @Override
    public boolean share(List<String> rights, final List<String> emails, String comments)
            throws FileNotFoundException, RmsRestAPIException {
        Rights r = new Rights();
        if (rights != null && rights.size() != 0) {
            r.listToRights(rights);
        }
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        String s = session.getRmsClient().shareRepoFileToMyVault(mName,
                false,
                mRepoId,
                mPathId,
                mPathDisplay,
                r.toInteger(),
                new IRecipients() {
                    @Override
                    public Iterator<String> iterator() {
                        return emails.iterator();
                    }
                },
                comments, null, null);
        if (s == null || s.isEmpty()) {
            return false;
        }
        try {
            IBaseRepo repo = RepoFactory.getRepo(RepoType.TYPE_MYVAULT);
            MyVaultRepo vRepo = (MyVaultRepo) repo;
            vRepo.sync(-1);
        } catch (SessionInvalidException
                | InvalidRMClientException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public UpdateRecipientsResult updateRecipients(List<String> added, List<String> removed, String comments)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        UpdateRecipientsResult result = session.getRmsRestAPI()
                .getSharingService(session.getRmUser())
                .updateRecipients(mDuid, added, removed, comments);
        if (result == null) {
            return null;
        }
        UpdateRecipientsResult.ResultsBean results = result.getResults();
        if (results == null) {
            return null;
        }
        List<String> newRecipients = results.getNewRecipients();
        if (newRecipients != null && newRecipients.size() != 0) {
            mSharedWith.addAll(newRecipients);
        }
        List<String> removedRecipients = results.getRemovedRecipients();
        if (removedRecipients != null && removedRecipients.size() != 0) {
            mSharedWith.removeAll(removedRecipients);
        }
        mDbItem.setShared(mSharedWith);
        return result;
    }

    @Override
    public boolean revokeRights()
            throws InvalidRMClientException, SessionInvalidException, RmsRestAPIException {
        IRmUser user = SkyDRMApp.getInstance().getSession().getRmUser();
        boolean result = SkyDRMApp.getInstance()
                .getSession()
                .getRmsRestAPI()
                .getSharingService(user)
                .revokingDocument(mDuid);
        if (result) {
            mDbItem.setRevoked();
            isRevoked = true;
        }
        return result;
    }

    @Override
    public MyVaultMetaDataResult getMetadata()
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        return session.getRmsRestAPI()
                .getMyVaultService(session.getRmUser())
                .getMyVaultFileMetaData(mDuid, mPathId);
    }

    @Override
    public String getSourceRepoType() {
        return mSourceRepoType;
    }

    @Override
    public String getSourceFilePathId() {
        return mSourceFilePathId;
    }

    @Override
    public String getSourceFilePathDisplay() {
        return mSourceFilePathDisplay;
    }

    @Override
    public void delete()
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        session.getRmsRestAPI()
                .getMyVaultService(session.getRmUser())
                .deleteMyVaultFile(mDuid, mPathId);

        //If file was deleted, the clear local path if exists.
        String localPath = getLocalPath();
        if (localPath != null && !localPath.isEmpty()) {
            File f = new File(localPath);
            if (f.delete()) {
                setLocalPath("");
            }
        }
        mDbItem.setDeleted();
    }

    @Override
    public void markAsOffline(final IOfflineCallback callback) {
        OfflineManager.getInstance().markAsOffline(this, callback);
    }

    @Override
    public void unMarkAsOffline() {
        OfflineManager.getInstance().unMarkAsOffline(this);
    }

    @Override
    public void markAsFavorite() {
        mDbItem.setFavoriteStatus(true);
        isFavorite = true;
        //update item in fav lists which update remote.
        updateMarkList();
    }

    @Override
    public void unMarkAsFavorite() {
        mDbItem.setFavoriteStatus(false);
        isFavorite = false;
        //update item in fav lists which update remote.
        updateMarkList();
    }

    @Override
    public void download(int type, final DownloadListener listener) throws SessionInvalidException,
            InvalidRMClientException, RmsRestAPIException, IOException {
        File myVaultDir = RenderHelper.getMyVaultMountPoint();
        if (myVaultDir == null) {
            return;
        }
        String destPath = Helper.nxPath2AbsPath(myVaultDir, mName);

        File doc = new File(destPath);
        if (doc.exists() && doc.isFile() && doc.length() > 0x4000) {
            setLocalPath(destPath);
            if (listener != null) {
                listener.onComplete();
            }
            return;
        }

        if (doc.createNewFile()) {
        }
        innerDownload(type, destPath, -1, -1, listener);
        setLocalPath(destPath);
        if (listener != null) {
            listener.onComplete();
        }
    }

    private void innerDownload(int type, final String destPath, int start, int end, final DownloadListener listener)
            throws RmsRestAPIException, SessionInvalidException, InvalidRMClientException {
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        session.getRmsRestAPI()
                .getMyVaultService(session.getRmUser())
                .downloadMyVaultFile(mPathId, destPath, type,
                        new RestAPI.DownloadListener() {
                            @Override
                            public void current(int i) {
                                if (listener != null) {
                                    listener.onProgress(i);
                                }
                            }

                            @Override
                            public void cancel() {
                                if (listener != null) {
                                    listener.cancel();
                                }
                            }
                        }, start, end);
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
        return mSharedOn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyVaultFile that = (MyVaultFile) o;
        return mFileSize == that.mFileSize &&
                mSharedOn == that.mSharedOn &&
                isRevoked == that.isRevoked &&
                isDeleted == that.isDeleted &&
                isShared == that.isShared &&
                isFavorite == that.isFavorite &&
                Objects.equals(mName, that.mName) &&
                Objects.equals(mSharedWith, that.mSharedWith) &&
                Objects.equals(mRights, that.mRights);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mName, mFileSize, mSharedOn, mSharedWith, mRights,
                isRevoked, isDeleted, isShared, isFavorite);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeString(mRepoId);
        dest.writeLong(mSharedOn);
        dest.writeStringList(mSharedWith);
        dest.writeStringList(mRights);
        dest.writeByte((byte) (isRevoked ? 1 : 0));
        dest.writeByte((byte) (isDeleted ? 1 : 0));
        dest.writeByte((byte) (isShared ? 1 : 0));

        dest.writeString(mSourceRepoName);
        dest.writeString(mSourceRepoId);
        dest.writeString(mSourceRepoType);
        dest.writeString(mSourceFilePathId);
        dest.writeString(mSourceFilePathDisplay);

        dest.writeParcelable((DBMyVaultItem) mDbItem, 0);
    }

    /**
     * Should run on background thread.
     *
     * @param localPath local nxl file path.
     */
    @Override
    public void setLocalPath(String localPath) {
        this.mLocalPath = localPath;
        mDbItem.setLocalPath(localPath);
    }

    private void updateMarkList() {
        FavoriteMarkImpl.getInstance().addMarkFileCacheSet(mRepoId, this);
    }

    private void fireOfflineDownload(String localPath, ICallback callback) {
        DownloadRequest request = new DownloadRequest.Builder()
                .setUrl(getDownloadURL())
                .setPathId(mPathId)
                .setLocalPath(localPath)
                .setStart(-1)
                .setLength(-1)
                .setType(2)
                .build();
        DownloadManager.getInstance().start(request, mPathId, callback);
    }

    private String getDownloadURL() {
        try {
            return SkyDRMApp.getInstance()
                    .getSession()
                    .getRmsRestAPI()
                    .getConfig()
                    .getMyVaultDownloadFileURL();
        } catch (SessionInvalidException e) {
            e.printStackTrace();
        }
        return "";
    }

    static MyVaultFile newByDBItem(IDBMyVaultItem i) {
        return new MyVaultFile(i);
    }
}
