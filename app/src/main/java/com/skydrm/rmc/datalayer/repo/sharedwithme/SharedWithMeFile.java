package com.skydrm.rmc.datalayer.repo.sharedwithme;

import android.content.Context;
import android.os.Parcel;
import android.support.v4.util.Pair;

import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.repo.base.SharedWithBase;
import com.skydrm.rmc.dbbridge.IDBSharedWithMeItem;
import com.skydrm.rmc.dbbridge.sharedwithme.DBSharedWitheMeItem;
import com.skydrm.rmc.engine.Render.RenderHelper;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.reposystem.localrepo.helper.Helper;
import com.skydrm.rmc.ui.service.offline.downloader.DownloadManager;
import com.skydrm.rmc.ui.service.offline.downloader.ICallback;
import com.skydrm.rmc.ui.service.offline.downloader.core.DownloadRequest;
import com.skydrm.rmc.ui.service.offline.downloader.exception.DownloadException;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.rest.ISharedWithMeService;
import com.skydrm.sdk.rms.rest.sharewithme.SharedWithMeReshareResult;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SharedWithMeFile extends SharedWithBase implements ISharedWithMeFile {
    private IDBSharedWithMeItem mDBItem;

    private SharedWithMeFile(IDBSharedWithMeItem item) {
        super(item.getName(), item.getDuid(), item.getSize(), item.getFileType(),
                item.getPathId(), "/" + item.getName(),
                item.getLocalPath(), item.isFavorite(), item.isOffline(),
                item.getModifyRightsStatus(), item.getEditStatus(), item.getOperationStatus(),
                item.getSharedDate(), item.getSharedDate(),
                item.getSharedDate(), item.getSharedBy(),
                item.getTransactionId(), item.getTransactionCode(),
                item.getSharedLink(), item.getRights(),
                item.getComment(), item.isOwner(), item.getProtectionType());

        this.mDBItem = item;
    }

    protected SharedWithMeFile(Parcel in) {
        super(in);
        this.mDBItem = in.readParcelable(DBSharedWitheMeItem.class.getClassLoader());
    }

    public static final Creator<SharedWithMeFile> CREATOR = new Creator<SharedWithMeFile>() {
        @Override
        public SharedWithMeFile createFromParcel(Parcel in) {
            return new SharedWithMeFile(in);
        }

        @Override
        public SharedWithMeFile[] newArray(int size) {
            return new SharedWithMeFile[size];
        }
    };

    @Override
    public int getProtectionType() {
        return mProtectionType;
    }

    @Override
    public SharedWithMeReshareResult reShare(List<String> members, String comments) throws
            SessionInvalidException, InvalidRMClientException, RmsRestAPIException {

        ISharedWithMeService sharedWithMeService = SkyDRMApp.getInstance().getSession().
                getRmsRestAPI().getSharedWithMeService(SkyDRMApp.getInstance().getSession().getRmUser());

        return sharedWithMeService.
                reShareFile(mTransactionId, mTransactionCode, members, comments);
    }

    @Override
    public void download(int type, final DownloadListener listener) throws SessionInvalidException,
            InvalidRMClientException, RmsRestAPIException, IOException {
        File sharedWithMeMountPoint = RenderHelper.getShareLinkFileMountPoint();
        if (sharedWithMeMountPoint == null) {
            return;
        }
        String destPath = Helper.nxPath2AbsPath(sharedWithMeMountPoint, mName);
        File doc = new File(destPath);
        if (doc.exists() && doc.length() > 0x4000) {
            setLocalPath(destPath);
            return;
        } else {
            if (doc.createNewFile()) {
            }
            innerDownload(type, -1, -1, doc, listener);
        }

        setLocalPath(destPath);
        if (listener != null) {
            listener.onComplete();
        }
    }

    @Override
    public void setOperationStatus(int status) {
        super.setOperationStatus(status);
        if (mDBItem != null) {
            mDBItem.setOperationStatus(status);
        }
    }

    @Override
    public void updateOfflineStatus(boolean active) {
        mDBItem.setOfflineStatus(active);
        isOffline = active;
        mDBItem.setOperationStatus(-1);
        mOperationStatus = -1;
    }

    @Override
    public void setRights(int rights, String obligationRaw) {
        mDBItem.setRightsAndObligation(rights, obligationRaw);
    }

    @Override
    protected int getOfflineRights() {
        if (mDBItem == null) {
            return -1;
        }
        return mDBItem.getOfflineRights();
    }

    @Override
    public String getOfflineObligations() {
        if (mDBItem == null) {
            return "";
        }
        return mDBItem.getOfflineObligations();
    }

    @Override
    protected Pair<Integer, String> getOfflineRightsAndObligations() {
        if (mDBItem == null) {
            return null;
        }
        int rights = mDBItem.getOfflineRights();
        String obligations = mDBItem.getOfflineObligations();
        return new Pair<>(rights, obligations);
    }

    @Override
    public void setLocalPath(String localPath) {
        this.mLocalPath = localPath;
        if (mDBItem != null) {
            mDBItem.setLocalPath(localPath);
        }
        if (localPath == null || localPath.isEmpty()) {
            resetPartialPath();
        }
    }

    @Override
    protected String getPartialPath() {
        File sharedWithMeMountPoint = RenderHelper.getShareLinkFileMountPoint();
        if (sharedWithMeMountPoint == null) {
            return "";
        }
        String partialPath = Helper.nxPath2AbsPath(sharedWithMeMountPoint, ("partial_").concat(mName));

        File partialF = new File(partialPath);
        if (partialF.exists() && partialF.isFile()) {
            return partialPath;
        }
        return "";
    }

    @Override
    protected String createNewPartialPath() {
        File sharedWithMeMountPoint = RenderHelper.getShareLinkFileMountPoint();
        if (sharedWithMeMountPoint == null) {
            return "";
        }
        return Helper.nxPath2AbsPath(sharedWithMeMountPoint, ("partial_").concat(mName));
    }

    @Override
    protected void partialDownload(File doc, DownloadListener listener)
            throws RmsRestAPIException, SessionInvalidException, InvalidRMClientException {
        innerDownload(1, 0, 0x4000, doc, listener);
    }

    private void innerDownload(int type, int start, int length,
                               File doc, final DownloadListener listener)
            throws RmsRestAPIException, SessionInvalidException, InvalidRMClientException {
        SkyDRMApp.getInstance().getSession()
                .getRmsRestAPI()
                .getSharedWithMeService(SkyDRMApp.getInstance().getSession().getRmUser())
                .download(mTransactionId, mTransactionCode, type == 1,
                        doc, start, length,
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
                        });
    }

    @Override
    public void downloadForOffline(final ICallback callback) {
        File sharedWithMeMountPoint = RenderHelper.getShareLinkFileMountPoint();
        if (sharedWithMeMountPoint == null) {
            return;
        }
        final String destPath = Helper.nxPath2AbsPath(sharedWithMeMountPoint, mName);
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

    private void fireOfflineDownload(String localPath, ICallback callback) {
        DownloadRequest request = new DownloadRequest.Builder()
                .setUrl(getDownloadURL())
                .setPathId(mPathId)
                .setLocalPath(localPath)
                .setStart(-1)
                .setLength(-1)
                .setType(1)
                .setTransactionId(mTransactionId)
                .setTransactionCode(mTransactionCode)
                .build();
        DownloadManager.getInstance().start(request, mPathId, callback);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable((DBSharedWitheMeItem) mDBItem, flags);
    }

    static SharedWithMeFile newByDBItem(IDBSharedWithMeItem i) {
        return new SharedWithMeFile(i);
    }

    @Override
    public String getServiceName(Context ctx) {
        if (ctx == null) {
            return "";
        }
        return ctx.getString(R.string.shared_with_me);
    }
}
