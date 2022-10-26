package com.skydrm.rmc.datalayer.repo.project;

import android.content.Context;
import android.os.Parcel;
import android.support.v4.util.Pair;
import android.text.TextUtils;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.RepoFactory;
import com.skydrm.rmc.datalayer.repo.base.NxlDoc;
import com.skydrm.rmc.datalayer.repo.base.RepoType;
import com.skydrm.rmc.datalayer.repo.base.Utils;
import com.skydrm.rmc.datalayer.user.UserService;
import com.skydrm.rmc.dbbridge.IDBProjectFileItem;
import com.skydrm.rmc.dbbridge.IDBProjectItem;
import com.skydrm.rmc.dbbridge.IOwner;
import com.skydrm.rmc.dbbridge.base.Owner;
import com.skydrm.rmc.dbbridge.project.DBProjectFileItem;
import com.skydrm.rmc.engine.Render.RenderHelper;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.reposystem.localrepo.helper.Helper;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.rmc.ui.project.feature.centralpolicy.PolicyEvaluation;
import com.skydrm.rmc.ui.project.feature.service.IMarkCallback;
import com.skydrm.rmc.ui.project.feature.service.MarkerManager;
import com.skydrm.rmc.ui.project.feature.service.core.MarkException;
import com.skydrm.rmc.ui.project.feature.service.core.MarkerStatus;
import com.skydrm.rmc.ui.project.feature.service.share.IShare;
import com.skydrm.rmc.ui.service.fileinfo.IFileInfo;
import com.skydrm.rmc.ui.service.modifyrights.IModifyRightsFile;
import com.skydrm.rmc.ui.service.modifyrights.task.RemoteModifiedCheckTask;
import com.skydrm.rmc.ui.service.offline.IOfflineCallback;
import com.skydrm.rmc.ui.service.offline.OfflineManager;
import com.skydrm.rmc.ui.service.offline.downloader.DownloadManager;
import com.skydrm.rmc.ui.service.offline.downloader.ICallback;
import com.skydrm.rmc.ui.service.offline.downloader.core.DownloadRequest;
import com.skydrm.rmc.ui.service.offline.downloader.exception.DownloadException;
import com.skydrm.rmc.ui.service.share.ISharingFile;
import com.skydrm.rmc.ui.service.share.ShareManager;
import com.skydrm.rmc.utils.sort.IBaseSortable;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.INxlRights;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.exception.TokenAccessDenyException;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.rest.project.AllProjectsResult;
import com.skydrm.sdk.rms.rest.project.FileMetadata;
import com.skydrm.sdk.rms.user.IRmUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProjectFile extends NxlDoc implements IBaseSortable,
        IShare, IModifyRightsFile, ISharingFile {
    private String mId;
    private IOwner mOwner;
    private IOwner mLastModifiedUser;
    private boolean isShared;
    private boolean isRevoked;

    private IDBProjectFileItem mDBItem;

    private ProjectFile(Parcel in) {
        super(in);
        mId = in.readString();
        isShared = in.readByte() != 0;
        isRevoked = in.readByte() != 0;
        mDBItem = in.readParcelable(DBProjectFileItem.class.getClassLoader());
        mOwner = in.readParcelable(Owner.class.getClassLoader());
        mLastModifiedUser = in.readParcelable(Owner.class.getClassLoader());
    }

    private ProjectFile(IDBProjectFileItem item) {
        super(item.getName(), item.getDuid(), item.getSize(), item.getFileType(),
                item.getPathId(), item.getPathDisplay(), item.getLocalPath(),
                item.isFavorite(), item.isOffline(),
                item.getModifyRightsStatus(), item.getEditStatus(), item.getOperationStatus(),
                item.getLastModified(), item.getCreationTime());
        this.mDBItem = item;

        this.mId = item.getId();
        this.mOwner = item.getOwner();
        this.mLastModifiedUser = item.getLastModifiedUser();
        this.isShared = item.isShared();
        this.isRevoked = item.isRevoked();
    }

    public ProjectFile(String fileName, String duid, long fileSize, String fileType,
                       String filePathId, String filePathDisplay,
                       int modifyRightsStatus, int editStatus, int operationsStatus,
                       long lastModifiedTime, long creationTime,
                       boolean isShared, boolean isRevoked,
                       List<Integer> shareWithProjects) {
        super(fileName, duid, fileSize, fileType,
                filePathId, filePathDisplay,
                "", false, false,
                modifyRightsStatus, editStatus, operationsStatus,
                lastModifiedTime, creationTime);
        this.isShared = isShared;
        this.isRevoked = isRevoked;
    }

    @Override
    protected String getPartialPath() {
        if (mDBItem == null) {
            return "";
        }
        IDBProjectItem idbProjectItem = SkyDRMApp.getInstance()
                .getDBProvider()
                .queryProjectItem(mDBItem.getProjectTBPK());
        String partialPath = Helper.nxPath2AbsPath(getProjectMountPoint(idbProjectItem.getName()),
                ("partial_").concat(mName));
        File partialF = new File(partialPath);
        if (partialF.exists() && partialF.isFile()) {
            return partialPath;
        }
        return "";
    }

    @Override
    protected String createNewPartialPath() {
        if (mDBItem == null) {
            return "";
        }
        IDBProjectItem idbProjectItem = SkyDRMApp.getInstance()
                .getDBProvider()
                .queryProjectItem(mDBItem.getProjectTBPK());
        return Helper.nxPath2AbsPath(getProjectMountPoint(idbProjectItem.getName()),
                ("partial_").concat(mName));
    }

    @Override
    protected void partialDownload(File doc, DownloadListener listener) throws
            RmsRestAPIException, SessionInvalidException, InvalidRMClientException {
        if (mDBItem == null) {
            return;
        }
        IDBProjectItem idbProjectItem = SkyDRMApp.getInstance()
                .getDBProvider()
                .queryProjectItem(mDBItem.getProjectTBPK());
        innerDownload(idbProjectItem.getId(), mPathId, doc, 1, -1, -1, listener);
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

    public String getId() {
        return mId;
    }

    public IOwner getOwner() {
        return mOwner;
    }

    public IOwner getLastModifiedUser() {
        return mLastModifiedUser;
    }

    @Override
    public String getServiceName(Context ctx) {
        if (mDBItem == null) {
            return "";
        }
        IDBProjectItem item = SkyDRMApp.getInstance()
                .getDBProvider()
                .queryProjectItem(mDBItem.getProjectTBPK());
        if (item == null) {
            return "";
        }
        return item.getName();
    }

    @Override
    public List<String> getRights() {
        return null;
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
    public String getLocalPath() {
        File doc = new File(mLocalPath);
        if (doc.exists() && doc.isFile() && doc.length() != 0) {
            return mLocalPath;
        }
        return "";
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
    public void markAsFavorite() {

    }

    @Override
    public void unMarkAsFavorite() {

    }

    @Override
    public void setOperationStatus(int status) {
        super.setOperationStatus(status);
        if (mDBItem != null) {
            mDBItem.setOperationStatus(status);
        }
    }

    public void setSharedStatus(boolean shared) {
        isShared = shared;
        if (mDBItem != null) {
            mDBItem.updateShareStatus(shared);
        }
    }

    public void setRevokeStatus(boolean revoked) {
        isRevoked = revoked;
        if (mDBItem != null) {
            mDBItem.updateRevokeStatus(revoked);
        }
    }

    @Override
    public void setRights(int rights, String obligationRaw) {
        if (mDBItem != null) {
            mDBItem.cacheRights(rights, obligationRaw);
        }
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
            mDBItem.cacheRights(-1, "");
            mDBItem.updateOfflineMarker(false);
            mDBItem.setOperationStatus(-1);
            mOperationStatus = -1;
            isOffline = false;
        }
    }

    @Override
    public void download(int type, DownloadListener listener)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException, IOException {
        if (mDBItem == null) {
            return;
        }
        String projectName = "";
        int projectId = -1;
        IDBProjectItem idbProjectItem = SkyDRMApp.getInstance()
                .getDBProvider()
                .queryProjectItem(mDBItem.getProjectTBPK());

        projectName = idbProjectItem.getName();
        projectId = idbProjectItem.getId();
        // file path ---- current directory + fileName, like:  /test/for_test-2017-02-09-07-44-48.xlsx.nxl"
        String absPath = Helper.nxPath2AbsPath(getProjectMountPoint(projectName), mName);
        File doc = new File(absPath);
        if (doc.exists() && doc.isFile() && doc.length() > 0x4000) {
            setLocalPath(absPath);
            if (listener != null) {
                listener.onComplete();
            }
            return;
        }

        if (doc.createNewFile()) {
        }
        innerDownload(projectId, mPathId, doc, type, -1, -1, listener);

        setLocalPath(absPath);
        if (listener != null) {
            listener.onComplete();
        }
    }

    @Override
    public void downloadForOffline(final ICallback callback) {
        if (mDBItem == null) {
            return;
        }
        String projectName = "";
        int projectId = -1;
        IDBProjectItem idbProjectItem = SkyDRMApp.getInstance()
                .getDBProvider()
                .queryProjectItem(mDBItem.getProjectTBPK());

        projectName = idbProjectItem.getName();
        projectId = idbProjectItem.getId();
        // file path ---- current directory + fileName, like:  /test/for_test-2017-02-09-07-44-48.xlsx.nxl"
        final String absPath = Helper.nxPath2AbsPath(getProjectMountPoint(projectName), mName);
        File doc = new File(absPath);
        if (doc.exists() && doc.length() > 0x4000) {
            setLocalPath(absPath);
            if (callback != null) {
                callback.onDownloadComplete();
            }
            return;
        }
        fireOfflineDownload(projectId, absPath, new ICallback() {
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
    public void delete() throws SessionInvalidException,
            InvalidRMClientException, RmsRestAPIException {
        if (mDBItem == null) {
            return;
        }

        IDBProjectItem idbProjectItem = SkyDRMApp.getInstance()
                .getDBProvider()
                .queryProjectItem(mDBItem.getProjectTBPK());

        try {
            SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
            boolean delete = session.getRmsRestAPI()
                    .getProjectService(session.getRmUser())
                    .deleteFile(idbProjectItem.getId(), mPathId);

            if (delete) {
                mDBItem.delete();
                mDBItem = null;
            }
        } catch (RmsRestAPIException e) {
            removeLocalIfNecessary(e);
            throw e;
        }
    }

    private void fireOfflineDownload(int projectId, String localPath, ICallback callback) {
        DownloadRequest request = new DownloadRequest.Builder()
                .setUrl(getDownloadURL(projectId))
                .setPathId(mPathId)
                .setLocalPath(localPath)
                .setStart(-1)
                .setLength(-1)
                .setType(isOffline ? 1 : 2)
                .build();
        DownloadManager.getInstance().start(request, mPathId, callback);
    }

    private void innerDownload(int projectId, String pathId, File doc, int type,
                               int start, int end, final DownloadListener listener)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        try {
            SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
            session.getRmsRestAPI()
                    .getProjectService(session.getRmUser())
                    .downloadFile(projectId, pathId, doc, type, new RestAPI.DownloadListener() {
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

    @Override
    public void setLocalPath(String localPath) {
        if (mDBItem == null) {
            return;
        }
        this.mLocalPath = localPath;
        mDBItem.setLocalPath(localPath);
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
        return mDBItem.getOfflineObligations();
    }

    static ProjectFile newByDBItem(IDBProjectFileItem item) {
        return new ProjectFile(item);
    }

    public static final Creator<ProjectFile> CREATOR = new Creator<ProjectFile>() {
        @Override
        public ProjectFile createFromParcel(Parcel in) {
            return new ProjectFile(in);
        }

        @Override
        public ProjectFile[] newArray(int size) {
            return new ProjectFile[size];
        }
    };

    @Override
    public int describeContents() {
        return super.describeContents();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(mId);
        dest.writeByte((byte) (isShared ? 1 : 0));
        dest.writeByte((byte) (isRevoked ? 1 : 0));
        dest.writeParcelable((DBProjectFileItem) mDBItem, flags);
        dest.writeParcelable((Owner) mOwner, flags);
        dest.writeParcelable((Owner) mLastModifiedUser, flags);
    }

    @Deprecated
    public void shareToProject(IMarkCallback callback) {
        MarkerManager.getInstance().shareToProject(this, callback);
    }

    @Deprecated
    public void shareToPerson(IMarkCallback callback) {
        MarkerManager.getInstance().shareToPerson(this, callback);
    }

    @Override
    public Map<String, String> getShareWith() {
        Map<String, String> ret = new HashMap<>();
        List<Integer> shareWithProject = mDBItem.getShareWithProject();
        if (shareWithProject == null || shareWithProject.isEmpty()) {
            return ret;
        }
        ProjectRepo repo = (ProjectRepo) RepoFactory.getRepo(RepoType.TYPE_PROJECT);
        List<AllProjectsResult.ResultsBean.DetailBean> allProjectTmpData = repo.getAllProjectTmpData();
        if (allProjectTmpData == null || allProjectTmpData.isEmpty()) {
            return ret;
        }
        for (AllProjectsResult.ResultsBean.DetailBean detailBean : allProjectTmpData) {
            if (detailBean == null) {
                continue;
            }
            if (shareWithProject.contains(detailBean.getId())) {
                ret.put(detailBean.getName(), String.valueOf(detailBean.getId()));
            }
        }
//        List<IProject> projects = repo.listProject(0);
//        if (projects == null || projects.isEmpty()) {
//            return ret;
//        }
//        for (IProject p : projects) {
//            if (p == null) {
//                continue;
//            }
//            if (mShareWithProjects.contains(p.getId())) {
//                ret.put(p.getName(), String.valueOf(p.getId()));
//            }
//        }
        return ret;
    }

    public void share(IMarkCallback callback) {
        ShareManager.getInstance().share(this, callback);
    }

    @Override
    public boolean isRevokeable()
            throws RmsRestAPIException, InvalidRMClientException, SessionInvalidException {
        // Sync project-ad lists from rms first.
        //UserService.syncProjectAdminAttr();
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        IRmUser usr = session.getRmUser();
        if (usr == null) {
            return false;
        }
        // Only project admin have rights to revoke file.
        return usr.isProjectAdmin();
    }

    @Override
    public void modifyRights(IMarkCallback callback) {
        MarkerManager.getInstance().modifyRights(this, callback);
    }

    @Override
    public void doPolicyEvaluation(String membershipId, Map<String, Set<String>> tags,
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
                    | IOException
                    | TokenAccessDenyException
                    | InvalidRMClientException
                    | SessionInvalidException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isSharable() throws InvalidRMClientException, SessionInvalidException,
            IOException, TokenAccessDenyException, RmsRestAPIException {
        // Sync project-ad lists from rms first.
        UserService.syncProjectAdminAttr();
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        IRmUser usr = session.getRmUser();
        if (usr == null) {
            return false;
        }
        // If current usr is project admin,all rights granted.
        // If not check file re-share rights.
        if (usr.isProjectAdmin()) {
            return true;
        }
        INxlFileFingerPrint fp = getFingerPrint();
        if (fp.isExpired()) {
            throw new RmsRestAPIException("The file you are sharing is expired.",
                    RmsRestAPIException.ExceptionDomain.Common);
        }
        return hasReShareRights(fp);
    }

    @Override
    public boolean isShared() {
        return isShared;
    }

    @Override
    public boolean isRevoked() {
        return isRevoked;
    }

    @Override
    public INxlFileFingerPrint getFingerPrint() throws RmsRestAPIException,
            IOException, TokenAccessDenyException, InvalidRMClientException, SessionInvalidException {
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
    public void update(List<String> newRecipients, List<String> removedRecipients) {
        Set<Integer> all = new HashSet<>(Utils.translateToIntegerList(newRecipients));
        List<Integer> shareWithProjects = mDBItem.getShareWithProject();
        if (shareWithProjects != null) {
            all.addAll(shareWithProjects);
        }
        if (removedRecipients != null && !removedRecipients.isEmpty()) {
            Iterator<Integer> it = all.iterator();
            while (it.hasNext()) {
                if (removedRecipients.contains(String.valueOf(it.next()))) {
                    it.remove();
                }
            }
        }
        //process all shared lists.
        mDBItem.updateShareWithProject(new ArrayList<>(all));
        if (!isShared) {
            setSharedStatus(true);
        }
    }

    @Override
    public void update(boolean revoked) {
        setRevokeStatus(revoked);
    }

    @Override
    public int getProjectId() {
        if (mDBItem == null) {
            return -1;
        }
        IDBProjectItem idbProjectItem = SkyDRMApp.getInstance()
                .getDBProvider()
                .queryProjectItem(mDBItem.getProjectTBPK());
        return idbProjectItem.getId();
    }

    public void checkRemoteRightsModifiedAsync(LoadTask.ITaskCallback<RemoteModifiedCheckTask.Result, Exception> callback) {
        RemoteModifiedCheckTask task = new RemoteModifiedCheckTask(this, callback);
        task.run();
    }

    @Override
    public boolean checkRemoteRightsModifiedThenUpdate() throws InvalidRMClientException,
            TokenAccessDenyException, SessionInvalidException, RmsRestAPIException, IOException {
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

    private boolean isRemoteRightsModified() throws InvalidRMClientException, IOException,
            SessionInvalidException, TokenAccessDenyException, RmsRestAPIException {
        INxlFileFingerPrint fp = super.getFingerPrint();
        return isRemoteRightsModified(fp.getLastModifiedTime());
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

    public FileMetadata getMetadata()
            throws InvalidRMClientException, SessionInvalidException, RmsRestAPIException {
        if (mDBItem == null) {
            return null;
        }
        SkyDRMApp app = SkyDRMApp.getInstance();
        IDBProjectItem projectDbItem = app.getDBProvider()
                .queryProjectItem(mDBItem.getProjectTBPK());
        if (projectDbItem == null) {
            return null;
        }
        int projectId = projectDbItem.getId();
        if (projectId == -1) {
            return null;
        }
        SkyDRMApp.Session2 session = app.getSession();
        if (session == null) {
            return null;
        }
        try {
            return session.getRmsRestAPI()
                    .getProjectService(session.getRmUser())
                    .getFileMetadata(projectId, mPathId);
        } catch (RmsRestAPIException e) {
            removeLocalIfNecessary(e);
            throw e;
        }
    }

    private File getProjectMountPoint(String name) {
        File projectMountPoint = RenderHelper.getProjectsMountPoint(name);
        if (projectMountPoint == null) {
            return null;
        }
        // if fileDirectory is empty, means the file is in Root directory, like: /daily expenses.xls.nxl
        String fileDirectory = mPathId.substring(0, mPathId.lastIndexOf("/"));
        // means this file is not in Root directory, and in subFolder, like: /test/aily expenses.xls.nxl
        if (fileDirectory.length() > 0 && !fileDirectory.equals("/")) {
            projectMountPoint = new File(projectMountPoint.getAbsolutePath() + fileDirectory);
            if (!projectMountPoint.exists()) {
                projectMountPoint.mkdirs();
            }
        }
        return projectMountPoint;
    }

    private String getDownloadURL(int projectId) {
        try {
            return SkyDRMApp.getInstance()
                    .getSession()
                    .getRmsRestAPI()
                    .getConfig()
                    .getProjectDownloadFileURL().replace("{projectId}",
                            Integer.toString(projectId));
        } catch (SessionInvalidException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void removeLocalIfNecessary(RmsRestAPIException e) {
        if (e == null) {
            return;
        }
        if (e.getDomain() == RmsRestAPIException.ExceptionDomain.FileNotFound) {
            mDBItem.delete();
            mDBItem = null;
        }
    }

}
