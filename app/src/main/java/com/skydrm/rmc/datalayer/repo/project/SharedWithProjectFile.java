package com.skydrm.rmc.datalayer.repo.project;

import android.content.Context;
import android.os.Parcel;
import android.support.v4.util.Pair;
import android.text.TextUtils;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.repo.base.SharedWithBase;
import com.skydrm.rmc.dbbridge.IDBProjectItem;
import com.skydrm.rmc.dbbridge.IDBSharedWithProjectItem;
import com.skydrm.rmc.dbbridge.project.DBSharedWithProjectFileItem;
import com.skydrm.rmc.engine.Render.RenderHelper;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.reposystem.localrepo.helper.Helper;
import com.skydrm.rmc.ui.project.feature.centralpolicy.PolicyEvaluation;
import com.skydrm.rmc.ui.project.feature.service.IMarkCallback;
import com.skydrm.rmc.ui.service.offline.IOfflineCallback;
import com.skydrm.rmc.ui.service.offline.OfflineManager;
import com.skydrm.rmc.ui.service.offline.downloader.DownloadManager;
import com.skydrm.rmc.ui.service.offline.downloader.ICallback;
import com.skydrm.rmc.ui.service.offline.downloader.core.DownloadRequest;
import com.skydrm.rmc.ui.service.offline.downloader.exception.DownloadException;
import com.skydrm.rmc.ui.service.share.ISharingFile;
import com.skydrm.rmc.ui.service.share.ShareManager;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.INxlRights;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.exception.TokenAccessDenyException;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.rest.project.ReShareResult;
import com.skydrm.sdk.rms.user.IRmUser;
import com.skydrm.sdk.rms.user.membership.IMemberShip;
import com.skydrm.sdk.rms.user.membership.ProjectMemberShip;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SharedWithProjectFile extends SharedWithBase implements ISharingFile {
    private String mShareByProjectId;
    private String mSharedByProjectName;
    private IDBSharedWithProjectItem mDBItem;

    SharedWithProjectFile(IDBSharedWithProjectItem item) {
        super(item.getName(), item.getDuid(), item.getSize(),
                item.getFileType(), item.getPathId(), "/" + item.getName(),
                item.getLocalPath(), item.isFavorite(), item.isOffline(),
                item.getModifyRightsStatus(), item.getEditStatus(), item.getOperationStatus(),
                item.getSharedDate(), item.getSharedDate(),
                item.getSharedDate(), item.getSharedBy(),
                item.getTransactionId(), item.getTransactionCode(),
                item.getSharedLink(), item.getRights(),
                item.getComment(), item.isOwner(), item.getProtectionType());

        this.mShareByProjectId = item.getSharedBySpace() == null ? "-1" : item.getSharedBySpace();
        this.mDBItem = item;
    }

    private SharedWithProjectFile(Parcel in) {
        super(in);
        this.mShareByProjectId = in.readString();
        this.mDBItem = in.readParcelable(DBSharedWithProjectFileItem.class.getClassLoader());
    }

    public static final Creator<SharedWithProjectFile> CREATOR = new Creator<SharedWithProjectFile>() {
        @Override
        public SharedWithProjectFile createFromParcel(Parcel in) {
            return new SharedWithProjectFile(in);
        }

        @Override
        public SharedWithProjectFile[] newArray(int size) {
            return new SharedWithProjectFile[size];
        }
    };

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
    public void setRights(int rights, String obligationRaw) {
        if (mDBItem != null) {
            mDBItem.cacheRights(rights, obligationRaw);
        }
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

    public String getShareByProjectId() {
        return mShareByProjectId;
    }

    public String getShareByProjectName() {
        if (mSharedByProjectName == null || mSharedByProjectName.isEmpty()) {
            IDBProjectItem item = SkyDRMApp.getInstance()
                    .getDBProvider()
                    .queryProjectItemByProjectID(Integer.valueOf(mShareByProjectId));
            if (item == null) {
                return "";
            }
            mSharedByProjectName = item.getName();
        }
        return mSharedByProjectName;
    }

    @Override
    protected String getPartialPath() {
        if (mDBItem == null) {
            return "";
        }
        IDBProjectItem item = SkyDRMApp.getInstance()
                .getDBProvider()
                .queryProjectItem(mDBItem.getProjectTBPK());
        if (item == null) {
            return "";
        }
        String partialPath = Helper.nxPath2AbsPath(getSharedWithMountPoint(item.getName()),
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
        IDBProjectItem item = SkyDRMApp.getInstance()
                .getDBProvider()
                .queryProjectItem(mDBItem.getProjectTBPK());
        if (item == null) {
            return "";
        }
        return Helper.nxPath2AbsPath(getSharedWithMountPoint(item.getName()),
                ("partial_").concat(mName));
    }

    @Override
    protected void setLocalPath(String path) {
        if (mDBItem == null) {
            return;
        }
        this.mLocalPath = path;
        mDBItem.setLocalPath(path);
        if (path == null || path.isEmpty()) {
            resetPartialPath();
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
    protected void partialDownload(File doc, DownloadListener listener)
            throws RmsRestAPIException, SessionInvalidException, InvalidRMClientException {
        if (mDBItem == null) {
            return;
        }
        IDBProjectItem item = SkyDRMApp.getInstance()
                .getDBProvider()
                .queryProjectItem(mDBItem.getProjectTBPK());
        innerDownload(doc, item.getId(), 1, 0, 0x4000, listener);
    }

    @Override
    public void download(int type, DownloadListener listener)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException, IOException {
        if (mDBItem == null) {
            return;
        }
        IDBProjectItem item = SkyDRMApp.getInstance()
                .getDBProvider()
                .queryProjectItem(mDBItem.getProjectTBPK());

        String absPath = Helper.nxPath2AbsPath(getSharedWithMountPoint(item.getName()), mName);
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
        innerDownload(doc, item.getId(), type, -1, -1, listener);

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
        IDBProjectItem item = SkyDRMApp.getInstance()
                .getDBProvider()
                .queryProjectItem(mDBItem.getProjectTBPK());

        final String absPath = Helper.nxPath2AbsPath(getSharedWithMountPoint(item.getName()), mName);
        File doc = new File(absPath);
        if (doc.exists() && doc.length() > 0x4000) {
            setLocalPath(absPath);
            if (callback != null) {
                callback.onDownloadComplete();
            }
            return;
        }
        fireOfflineDownload(String.valueOf(item.getId()), absPath, new ICallback() {
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
    public void markAsOffline(IOfflineCallback callback) {
        OfflineManager.getInstance().markAsOffline(this, callback);
    }

    @Override
    public void unMarkAsOffline() {
        OfflineManager.getInstance().unMarkAsOffline(this);
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
        return mRights;
    }

    @Override
    public void doPolicyEvaluation(INxlFileFingerPrint fp, final IPolicyCallback callback) {
        final int rights = INxlRights.VIEW + INxlRights.EDIT + INxlRights.PRINT
                + INxlRights.SHARE + INxlRights.DOWNLOAD
                + INxlRights.WATERMARK + INxlRights.DECRYPT;

        if (isOffline & !SkyDRMApp.getInstance().isNetworkAvailable()) {
            Pair<Integer, String> result = getOfflineRightsAndObligations();
            if (result == null) {
                if (callback != null) {
                    callback.onFailed(new Exception("Operation failed."));
                }
            } else {
                if (callback != null) {
                    int first = result.first == null ? -1 : result.first;
                    callback.onSuccess(integer2Rights(first), result.second);
                }
            }
            return;
        }

        String membershipId = fp.getOwnerID();
        int userId = -1;
        try {
            IRmUser rmUser = SkyDRMApp.getInstance().getSession().getRmUser();
            userId = rmUser.getUserId();
            membershipId = getMembershipId();
        } catch (InvalidRMClientException e) {
            e.printStackTrace();
        }

        Map<String, Set<String>> tags = fp.getAll() == null ? new HashMap<String, Set<String>>() : fp.getAll();
        PolicyEvaluation.EvaluationBean evalBean = PolicyEvaluation.buildEvalBean(membershipId,
                userId, 0, rights,
                mName, mDuid, tags);
        PolicyEvaluation.evaluate(evalBean, new PolicyEvaluation.IEvaluationCallback() {
            @Override
            public void onEvaluated(String result) {
                if (!TextUtils.isEmpty(result)) {
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
                                    callback.onFailed(new Exception("Unknown result."));
                                }
                            }
                        }
                    } catch (JSONException e) {
                        if (callback != null) {
                            callback.onFailed(e);
                        }
                    }
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

    public boolean reShare(List<Integer> recipients, String comments, int spaceId)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        ReShareResult result = session
                .getRmsRestAPI()
                .getProjectService(session.getRmUser())
                .reShare(mTransactionId, mTransactionCode, spaceId, recipients, comments);
        return result != null;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(mShareByProjectId);
        dest.writeParcelable((DBSharedWithProjectFileItem) mDBItem, flags);
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
    public boolean isRevokeable()
            throws RmsRestAPIException, InvalidRMClientException, SessionInvalidException {
        return true;
    }

    @Override
    public boolean isSharable()
            throws InvalidRMClientException, SessionInvalidException, IOException, TokenAccessDenyException, RmsRestAPIException {
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

    public int getId() {
        IDBProjectItem item = SkyDRMApp.getInstance()
                .getDBProvider()
                .queryProjectItem(mDBItem.getProjectTBPK());
        if (item == null) {
            return -1;
        }
        return item.getId();
    }

    public String getMembershipId() throws InvalidRMClientException {
        IDBProjectItem item = SkyDRMApp.getInstance()
                .getDBProvider()
                .queryProjectItem(mDBItem.getProjectTBPK());
        if (item == null) {
            return "";
        }
        IRmUser rmUser = SkyDRMApp.getInstance()
                .getSession()
                .getRmUser();
        if (rmUser == null) {
            return "";
        }
        List<IMemberShip> memberships = rmUser.getMemberships();
        if (memberships == null || memberships.isEmpty()) {
            return "";
        }
        for (IMemberShip m : memberships) {
            if (m instanceof ProjectMemberShip) {
                ProjectMemberShip pms = (ProjectMemberShip) m;
                if (pms.getProjectId() == item.getId()) {
                    return pms.getId();
                }
            }
        }
        return "";
    }

    private void innerDownload(File doc, int projectId, int type,
                               int start, int end, final DownloadListener listener)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        try {
            SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
            session.getRmsRestAPI()
                    .getSharedWithSpaceService(session.getRmUser())
                    .downloadFile(doc.getPath(), String.valueOf(projectId), type == 1,
                            mTransactionId, mTransactionCode, new RestAPI.DownloadListener() {
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

    private void fireOfflineDownload(String spaceId, String localPath, ICallback callback) {
        DownloadRequest request = new DownloadRequest.Builder()
                .setUrl(getDownloadURL())
                .setLocalPath(localPath)
                .setStart(-1)
                .setLength(-1)
                .setType(1)
                .setTransactionId(mTransactionId)
                .setTransactionCode(mTransactionCode)
                .setSpaceId(spaceId)
                .build();
        DownloadManager.getInstance().start(request, mPathId, callback);
    }

    private File getSharedWithMountPoint(String name) {
        File rootDir = RenderHelper.getProjectsMountPoint(name);
        if (rootDir == null) {
            return null;
        }
        // if fileDirectory is empty, means the file is in Root directory, like: /daily expenses.xls.nxl
        File sharedWithDir = new File(rootDir, "sharedwith");
        if (!sharedWithDir.exists()) {
            sharedWithDir.mkdirs();
        }
        return sharedWithDir;
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
