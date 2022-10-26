package com.skydrm.rmc.datalayer.repo.workspace;

import android.content.Context;
import android.os.Parcel;
import android.support.v4.util.Pair;
import android.text.TextUtils;

import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.base.NxlDoc;
import com.skydrm.rmc.datalayer.repo.base.Utils;
import com.skydrm.rmc.dbbridge.IDBWorkSpaceFileItem;
import com.skydrm.rmc.dbbridge.IOwner;
import com.skydrm.rmc.dbbridge.base.Owner;
import com.skydrm.rmc.dbbridge.project.DBProjectFileItem;
import com.skydrm.rmc.dbbridge.workspace.DBWorkSpaceFileItem;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.reposystem.localrepo.helper.Helper;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.rmc.ui.service.fileinfo.IFileInfo;
import com.skydrm.rmc.ui.service.offline.IOfflineCallback;
import com.skydrm.rmc.ui.service.offline.OfflineManager;
import com.skydrm.rmc.ui.service.offline.downloader.DownloadManager;
import com.skydrm.rmc.ui.service.offline.downloader.ICallback;
import com.skydrm.rmc.ui.service.offline.downloader.core.DownloadRequest;
import com.skydrm.rmc.ui.service.offline.downloader.exception.DownloadException;
import com.skydrm.rmc.ui.project.feature.centralpolicy.PolicyEvaluation;
import com.skydrm.rmc.ui.project.feature.service.IMarkCallback;
import com.skydrm.rmc.ui.project.feature.service.MarkerManager;
import com.skydrm.rmc.ui.project.feature.service.core.MarkException;
import com.skydrm.rmc.ui.project.feature.service.core.MarkerStatus;
import com.skydrm.rmc.ui.project.feature.service.share.IShare;
import com.skydrm.rmc.ui.service.modifyrights.IModifyRightsFile;
import com.skydrm.rmc.ui.service.modifyrights.task.RemoteModifiedCheckTask;
import com.skydrm.rmc.utils.sort.IBaseSortable;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.INxlRights;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.exception.TokenAccessDenyException;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.rest.workspace.DeleteItemResult;
import com.skydrm.sdk.rms.rest.workspace.FileMetadata;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WorkSpaceFile extends NxlDoc implements IBaseSortable, IModifyRightsFile {
    private IOwner mUploader;
    private IOwner mLastModifiedUser;
    private IDBWorkSpaceFileItem mDBItem;

    private WorkSpaceFile(Parcel in) {
        super(in);
        mUploader = in.readParcelable(Owner.class.getClassLoader());
        mLastModifiedUser = in.readParcelable(Owner.class.getClassLoader());
        mDBItem = in.readParcelable(DBProjectFileItem.class.getClassLoader());
    }

    private WorkSpaceFile(IDBWorkSpaceFileItem item) {
        super(item.getName(), item.getDuid(),
                item.getSize(), item.getFileType(),
                item.getPathId(), item.getPathDisplay(), item.getLocalPath(),
                item.isFavorite(), item.isOffline(),
                item.getModifyRightsStatus(), item.getEditStatus(), item.getOperationStatus(),
                item.getLastModified(), item.getCreationTime());

        this.mUploader = item.getUploader();
        this.mLastModifiedUser = item.getLastModifiedUser();
        this.mDBItem = item;
    }

    @Override
    protected String getPartialPath() {
        String partialPath = Helper.nxPath2AbsPath(getDownloadMountPoint(),
                ("partial_").concat(mName));
        File partialF = new File(partialPath);
        if (partialF.exists() && partialF.isFile()) {
            return partialPath;
        }
        return "";
    }

    @Override
    protected String createNewPartialPath() {
        return Helper.nxPath2AbsPath(getDownloadMountPoint(),
                ("partial_").concat(mName));
    }

    @Override
    protected void setLocalPath(String path) {
        if (mDBItem == null) {
            return;
        }
        this.mLocalPath = path;
        mDBItem.setLocalPath(path);
    }

    @Override
    protected Pair<Integer, String> getOfflineRightsAndObligations() {
        if (mDBItem == null) {
            return null;
        }
        int offlineRights = mDBItem.getOfflineRights();
        String offlineObligations = mDBItem.getOfflineObligations();
        return new Pair<>(offlineRights, offlineObligations);
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
    protected void partialDownload(File doc, DownloadListener listener)
            throws RmsRestAPIException, SessionInvalidException, InvalidRMClientException {
        innerDownload(doc, mPathId, 1, 0, 0x4000, listener);
    }

    @Override
    public void download(int type, DownloadListener listener)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException, IOException {
        String absPath = Helper.nxPath2AbsPath(getDownloadMountPoint(), mName);
        File doc = new File(absPath);
        if (doc.exists() && doc.isFile() && doc.length() > 0x4000) {
            setLocalPath(absPath);
            if (listener != null) {
                listener.onComplete();
            }
            return;
        }

        doc.createNewFile();
        innerDownload(doc, mPathId, type, -1, -1, listener);

        setLocalPath(absPath);
        if (listener != null) {
            listener.onComplete();
        }

    }

    @Override
    public void delete()
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        try {
            DeleteItemResult result = session.getRmsRestAPI()
                    .getWorkSpaceService(session.getRmUser())
                    .deleteItem(mPathId);

            if (result != null) {
                deleteLocal();
            }

        } catch (RmsRestAPIException e) {
            removeLocalIfNecessary(e);
            throw e;
        }
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
    public String getServiceName(Context ctx) {
        if (ctx == null) {
            return "";
        }
        return ctx.getString(R.string.name_workspace);
    }

    @Override
    public List<String> getRights() {
        return null;
    }

    @Override
    public void downloadForOffline(final ICallback callback) {
        final String absPath = Helper.nxPath2AbsPath(getDownloadMountPoint(), mName);
        File doc = new File(absPath);
        if (doc.exists() && doc.length() > 0x4000) {
            setLocalPath(absPath);
            if (callback != null) {
                callback.onDownloadComplete();
            }
            return;
        }
        fireOfflineDownload(absPath, new ICallback() {
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
                setLocalPath(absPath);
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
        if (mDBItem == null) {
            return;
        }
        mDBItem.cacheRights(rights, obligationRaw);
    }

    @Override
    public void updateOfflineStatus(boolean active) {
        if (mDBItem == null) {
            return;
        }
        if (active) {
            mDBItem.updateOfflineMarker(true);
            mDBItem.setOperationStatus(-1);
            mOperationStatus = -1;
            isOffline = true;
        } else {
            setRights(-1, "");
            mDBItem.updateOfflineMarker(false);
            mDBItem.setOperationStatus(-1);
            mOperationStatus = -1;
            isOffline = false;
        }
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
    public void modifyRights(IMarkCallback callback) {
        MarkerManager.getInstance().modifyRights(this, callback);
    }

    @Override
    public void doPolicyEvaluation(String membershipId,
                                   Map<String, Set<String>> tags,
                                   final IShare.IPolicyCallback callback) {
        int userId = -1;
        try {
            userId = SkyDRMApp.getInstance().getSession().getRmUser().getUserId();
        } catch (InvalidRMClientException e) {
            e.printStackTrace();
        }

        if (userId == -1) {
            return;
        }

        int evalType = 0;//defined by rms api.
        int rights = INxlRights.VIEW + INxlRights.EDIT + INxlRights.PRINT
                + INxlRights.SHARE + INxlRights.DOWNLOAD
                + INxlRights.WATERMARK + INxlRights.DECRYPT;

        PolicyEvaluation.evaluate(PolicyEvaluation.buildEvalBean(membershipId, userId,
                evalType, rights, mName, mDuid, tags),
                new PolicyEvaluation.IEvaluationCallback() {
                    @Override
                    public void onEvaluated(String result) {
                        if (TextUtils.isEmpty(result)) {
                            return;
                        }
                        try {
                            JSONObject responseObj = new JSONObject(result);
                            if (responseObj.has("results")) {
                                JSONObject resultsObj = responseObj.optJSONObject("results");
                                if (resultsObj != null) {
                                    int rights = resultsObj.optInt("rights");
                                    JSONArray obligations = resultsObj.optJSONArray("obligations");
                                    List<String> rightsArray = integer2Rights(rights);
                                    if (obligations != null && obligations.length() != 0) {
                                        if (rightsArray != null) {
                                            rightsArray.add("WATERMARK");
                                        }
                                    }
                                    if (callback != null) {
                                        callback.onSuccess(rightsArray, obligations == null ?
                                                "" : obligations.toString());
                                    }
                                } else {
                                    if (callback != null) {
                                        callback.onFailed(new MarkException(MarkerStatus.STATUS_FAILED_COMMON,
                                                "Unknown result."));
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            if (callback != null) {
                                callback.onFailed(new MarkException(MarkerStatus.STATUS_FAILED_COMMON,
                                        e.getMessage(), e));
                            }
                        }
                    }

                    @Override
                    public void onFailed(Exception e) {
                        if (callback != null) {
                            callback.onFailed(new MarkException(MarkerStatus.STATUS_FAILED_COMMON,
                                    e.getMessage(), e));
                        }
                    }
                });
    }

    @Override
    public INxlFileFingerPrint getFingerPrint() throws RmsRestAPIException, IOException,
            TokenAccessDenyException, InvalidRMClientException, SessionInvalidException {
        if (mDBItem == null) {
            return null;
        }
        String localPath = getLocalPath();
        File f = new File(localPath);
        if (f.exists() && f.isFile() && f.length() > 0x4000) {
            //Hit a point may need to clear partial path.
            resetPartialPath();
            // Also need to check lastModifiedTime in file header.
            INxlFileFingerPrint fp = SkyDRMApp.getInstance()
                    .getSession()
                    .getRmsClient()
                    .extractFingerPrint(localPath);

            if (SkyDRMApp.getInstance().isNetworkAvailable()
                    && isRemoteRightsModified(fp.getLastModifiedTime())) {
                // If file update success, then update lastModifiedTime flag.
                mDBItem.updateLastModifiedTime(mLastModifiedTime);
                return getRightsModifiedTarget();
            }

            return fp;
        } else {
            String partialPath = getPartialPath();
            File pf = new File(partialPath);
            if (pf.exists() && pf.isFile() && pf.length() != 0) {
                localPath = partialPath;
                // Check whether there is need to refresh file header[re-download it.]
                INxlFileFingerPrint fp = SkyDRMApp.getInstance()
                        .getSession()
                        .getRmsClient()
                        .extractFingerPrint(localPath);

                if (SkyDRMApp.getInstance().isNetworkAvailable()
                        && isRemoteRightsModified(fp.getLastModifiedTime())) {
                    // If file update success, then update lastModifiedTime flag.
                    mDBItem.updateLastModifiedTime(mLastModifiedTime);
                    return getRightsModifiedTarget();
                }
                return fp;
            } else {
                String newPartialPath = createNewPartialPath();
                File nDoc = new File(newPartialPath);
                nDoc.createNewFile();
                partialDownload(nDoc, null);
                localPath = newPartialPath;

                return SkyDRMApp.getInstance()
                        .getSession()
                        .getRmsClient()
                        .extractFingerPrint(localPath);
            }
        }
    }

    @Override
    public void handleModifyFileRights() {
        deleteLocalFile();
        deleteLocalPartialFile();

        if (isOffline) {
            try {
                download(1, null);
            } catch (SessionInvalidException
                    | InvalidRMClientException
                    | RmsRestAPIException
                    | IOException e) {
                e.printStackTrace();
            }

            try {
                doPolicyEvaluation(super.getFingerPrint(), null);
            } catch (RmsRestAPIException
                    | SessionInvalidException
                    | InvalidRMClientException
                    | TokenAccessDenyException
                    | IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void doPolicyEvaluation(final INxlFileFingerPrint fp, final IFileInfo.IPolicyCallback callback) {
        super.doPolicyEvaluation(fp, new IFileInfo.IPolicyCallback() {

            @Override
            public void onSuccess(List<String> rights, String obligations) {
                if (isOffline) {
                    setRights(rights2Integer(rights), obligations);
                }
                if (callback != null) {
                    callback.onSuccess(rights, obligations);
                }
            }

            @Override
            public void onFailed(Exception e) {
                if (callback != null) {
                    callback.onFailed(e);
                }
            }
        });
    }

    public static INxlFile newByDBItem(IDBWorkSpaceFileItem i) {
        return new WorkSpaceFile(i);
    }

    public static final Creator<WorkSpaceFile> CREATOR = new Creator<WorkSpaceFile>() {
        @Override
        public WorkSpaceFile createFromParcel(Parcel in) {
            return new WorkSpaceFile(in);
        }

        @Override
        public WorkSpaceFile[] newArray(int size) {
            return new WorkSpaceFile[size];
        }
    };

    @Override
    public int describeContents() {
        return super.describeContents();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable((Owner) mUploader, flags);
        dest.writeParcelable((Owner) mLastModifiedUser, flags);
        dest.writeParcelable((DBWorkSpaceFileItem) mDBItem, flags);
    }

    public void checkRemoteRightsModifiedAsync(LoadTask.ITaskCallback<RemoteModifiedCheckTask.Result, Exception> callback) {
        RemoteModifiedCheckTask task = new RemoteModifiedCheckTask(this, callback);
        task.run();
    }

    @Override
    public boolean checkRemoteRightsModifiedThenUpdate()
            throws SessionInvalidException, IOException, InvalidRMClientException,
            RmsRestAPIException, TokenAccessDenyException {
        boolean remoteRightsModified = isRemoteRightsModified();
        if (remoteRightsModified) {
            deleteLocalFile();
            deleteLocalPartialFile();
            // get new remote updated file.
            download(1, null);

            if (isOffline) {
                // should cache the new obligations and rights.
                doPolicyEvaluation(super.getFingerPrint(), null);
            }

            return true;
        }

        return false;
    }

    private boolean isRemoteRightsModified()
            throws InvalidRMClientException, SessionInvalidException, RmsRestAPIException,
            IOException, TokenAccessDenyException {
        INxlFileFingerPrint fp = super.getFingerPrint();
        return isRemoteRightsModified(fp.getLastModifiedTime());
    }

    private boolean isRemoteRightsModified(long localLastModifiedTime)
            throws SessionInvalidException, RmsRestAPIException, InvalidRMClientException {
        long remoteLastModifiedTime = getRemoteLastModifiedTime();
        boolean modified = localLastModifiedTime != remoteLastModifiedTime;
        if (modified) {
            mLastModifiedTime = remoteLastModifiedTime;
        }
        return modified;
    }

    private long getRemoteLastModifiedTime()
            throws RmsRestAPIException, InvalidRMClientException, SessionInvalidException {
        long lstModifiedTime = -1;
        FileMetadata metadata = getMetadata();
        if (metadata == null) {
            return lstModifiedTime;
        }
        FileMetadata.ResultsBean results = metadata.getResults();
        if (results == null) {
            return lstModifiedTime;
        }
        FileMetadata.ResultsBean.FileInfoBean fileInfo = results.getFileInfo();
        if (fileInfo == null) {
            return lstModifiedTime;
        }
        return fileInfo.getLastModified();
    }

    private FileMetadata getMetadata()
            throws InvalidRMClientException, SessionInvalidException, RmsRestAPIException {
        SkyDRMApp app = SkyDRMApp.getInstance();
        SkyDRMApp.Session2 session = app.getSession();
        try {
            return session.getRmsRestAPI()
                    .getWorkSpaceService(session.getRmUser())
                    .getFileMetadata(mPathId);
        } catch (RmsRestAPIException e) {
            removeLocalIfNecessary(e);
            throw e;
        }
    }

    private INxlFileFingerPrint getRightsModifiedTarget() throws IOException,
            InvalidRMClientException, RmsRestAPIException, SessionInvalidException, TokenAccessDenyException {
        // Need update file.
        // Delete local file and  local partial_file if exists.
        deleteLocalFile();
        deleteLocalPartialFile();

        String fingerPrintPath;
        if (isOffline) {
            download(1, null);

            fingerPrintPath = getLocalPath();
        } else {
            String newPartialPath = createNewPartialPath();
            File nDoc = new File(newPartialPath);
            nDoc.createNewFile();
            partialDownload(nDoc, null);

            fingerPrintPath = newPartialPath;
        }

        return SkyDRMApp.getInstance()
                .getSession()
                .getRmsClient()
                .extractFingerPrint(fingerPrintPath);
    }

    private void innerDownload(File doc, String pathId, int type,
                               int start, int end, final DownloadListener listener)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        if (doc == null) {
            return;
        }
        try {
            SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
            session.getRmsRestAPI()
                    .getWorkSpaceService(session.getRmUser())
                    .downloadFile(doc.getPath(), pathId, type, new RestAPI.DownloadListener() {
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
        } catch (RmsRestAPIException e) {
            removeLocalIfNecessary(e);
            throw e;
        }
    }

    private void fireOfflineDownload(String localPath, ICallback callback) {
        DownloadRequest request = new DownloadRequest.Builder()
                .setUrl(getDownloadURL())
                .setPathId(mPathId)
                .setLocalPath(localPath)
                .setStart(-1)
                .setLength(-1)
                .setType(isOffline ? 1 : 2)
                .build();
        DownloadManager.getInstance().start(request, mPathId, callback);
    }

    private String getDownloadURL() {
        try {
            return SkyDRMApp.getInstance()
                    .getSession()
                    .getRmsRestAPI()
                    .getConfig()
                    .getWorkSpaceDownloadFileURL();
        } catch (SessionInvalidException e) {
            e.printStackTrace();
        }
        return "";
    }

    private File getDownloadMountPoint() {
        try {
            File workSpaceMountDir = Utils.getWorkSpaceMountPoint();
            if (workSpaceMountDir == null) {
                return null;
            }
            if (!workSpaceMountDir.exists() || !workSpaceMountDir.isDirectory()) {
                return null;
            }
            String fileDirectory = mPathId.substring(0, mPathId.lastIndexOf("/"));
            // means this file is not in Root directory, and in subFolder, like: /test/aily expenses.xls.nxl
            if (fileDirectory.length() > 0 && !fileDirectory.equals("/")) {
                workSpaceMountDir = new File(workSpaceMountDir.getAbsolutePath() + fileDirectory);
                if (!workSpaceMountDir.exists()) {
                    workSpaceMountDir.mkdirs();
                }
            }
            return workSpaceMountDir;
        } catch (SessionInvalidException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void removeLocalIfNecessary(RmsRestAPIException e) {
        if (e == null) {
            return;
        }
        if (e.getDomain() == RmsRestAPIException.ExceptionDomain.FileNotFound) {
            deleteLocal();
        }
    }

    private void deleteLocal() {
        if (mDBItem != null) {
            mDBItem.delete();
            mDBItem = null;
        }
    }
}
