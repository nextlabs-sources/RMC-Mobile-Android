package com.skydrm.rmc.datalayer.repo.workspace;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.heartbeat.IHeartBeatListener;
import com.skydrm.rmc.datalayer.repo.NxlRepo;
import com.skydrm.rmc.datalayer.repo.base.IFileService;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.base.IDataService;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.rmc.ui.common.NxlFileType;
import com.skydrm.rmc.ui.service.createfolder.ICreateFolderService;
import com.skydrm.rmc.ui.service.modifyrights.IModifyRightsService;
import com.skydrm.rmc.ui.service.protect.IProtectService;
import com.skydrm.rmc.ui.service.protect.task.ProtectWithADHocTask;
import com.skydrm.rmc.ui.service.protect.task.ProtectWithCentralPolicyTask;
import com.skydrm.rmc.ui.service.share.ISharingFile;
import com.skydrm.rmc.ui.service.share.ISharingService;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.policy.Expiry;
import com.skydrm.sdk.policy.Obligations;
import com.skydrm.sdk.policy.Rights;
import com.skydrm.sdk.rms.rest.user.User;
import com.skydrm.sdk.rms.rest.workspace.ReClassifyResult;
import com.skydrm.sdk.rms.user.IRmUser;
import com.skydrm.sdk.rms.user.membership.IMemberShip;
import com.skydrm.sdk.rms.user.membership.SystemBucketMemberShip;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WorkSpaceRepo extends NxlRepo implements IDataService,
        IModifyRightsService, ICreateFolderService,
        IProtectService, ISharingService, Parcelable {
    private final IFileService<INxlFile> mFS;
    private volatile String mClassificationRaw = "{}";

    public WorkSpaceRepo() {
        this.mFS = new WorkSpaceFileService();
    }

    private WorkSpaceRepo(Parcel in) {
        mClassificationRaw = in.readString();
        mFS = in.readParcelable(WorkSpaceFileService.class.getClassLoader());
    }

    public WorkSpaceInfo getWorkSpaceInfo()
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        return ((WorkSpaceFileService) mFS).getWorkSpaceInfo();
    }

    @Override
    public List<INxlFile> list(int type) {
        if (type == NxlFileType.OFFLINE.getValue()) {
            return ((WorkSpaceFileService) mFS).listOffline();
        }
        return mFS.listFile("/", false);
    }

    @Override
    public List<INxlFile> list(int type, String pathId, boolean recursively) {
        if (type == NxlFileType.OFFLINE.getValue()) {
            return ((WorkSpaceFileService) mFS).listOffline();
        }
        return mFS.listFile(pathId, recursively);
    }

    @Override
    public List<INxlFile> sync(int type)
            throws SessionInvalidException, RmsRestAPIException, InvalidRMClientException {
        if (type == NxlFileType.OFFLINE.getValue()) {
            return ((WorkSpaceFileService) mFS).listOffline();
        }
        return mFS.syncFile("/", false);
    }

    @Override
    public List<INxlFile> sync(int type, String pathId, boolean recursively)
            throws SessionInvalidException, RmsRestAPIException, InvalidRMClientException {
        if (type == NxlFileType.OFFLINE.getValue()) {
            return ((WorkSpaceFileService) mFS).listOffline();
        }
        return mFS.syncFile(pathId, recursively);
    }

    @Override
    public void clearCache() {
        List<INxlFile> tree = mFS.listFile("/", true);
        if (tree == null || tree.isEmpty()) {
            return;
        }
        for (INxlFile f : tree) {
            if (f == null) {
                continue;
            }
            f.clearCache();
        }
    }

    @Override
    public long getCacheSize() {
        long ret = 0;
        List<INxlFile> tree = mFS.listFile("/", true);
        if (tree == null || tree.isEmpty()) {
            return ret;
        }
        for (INxlFile f : tree) {
            if (f == null) {
                continue;
            }
            ret += f.getCacheSize();
        }
        return ret;
    }

    @Override
    public void updateResetAllOperationStatus() {
        SkyDRMApp.getInstance()
                .getDBProvider()
                .updateWorkSpaceItemResetOperationStatus();
    }

    @Override
    public void onHeatBeat(IHeartBeatListener l) {
        try {
            syncClassification();
        } catch (SessionInvalidException
                | InvalidRMClientException
                | RmsRestAPIException e) {
            e.printStackTrace();
        }

        ((WorkSpaceFileService) mFS).onHeartBeat();
    }

    @Override
    public int getId() {
        return -1;
    }

    @Override
    public String getServiceName(@NonNull Context ctx) {
        return ctx.getString(R.string.name_workspace);
    }

    @Override
    public String getClassificationRaw() {
        return mClassificationRaw;
    }

    @Override
    public User.IExpiry getIExpiry() {
        return SkyDRMApp.getInstance()
                .getSession()
                .getUserPreference()
                .getExpiry();
    }

    @Override
    public String getWatermark() {
        return SkyDRMApp.getInstance()
                .getSession()
                .getUserPreference()
                .getWatermarkValue();
    }

    @Override
    public boolean modifyRights(@NonNull String fileName,
                                @NonNull String parentPathId,
                                @Nullable String fileTags)
            throws RmsRestAPIException, SessionInvalidException, InvalidRMClientException {

        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        IRmUser rmUser = session.getRmUser();

        try {
            ReClassifyResult result = session.getRmsRestAPI().getWorkSpaceService(rmUser)
                    .reClassifyFile(fileName, parentPathId, fileTags);

            return result != null;

        } catch (RmsRestAPIException e) {
            removeLocalIfNecessary(e, parentPathId);
            throw e;
        }

    }

    public void syncClassification() throws SessionInvalidException, InvalidRMClientException,
            RmsRestAPIException {
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        IRmUser rmUser = session.getRmUser();

        mClassificationRaw = session.getRmsRestAPI()
                .getClassificationProfileService(rmUser)
                .getClassificationProfile(rmUser.getTokenGroupName());

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mClassificationRaw);
        dest.writeParcelable((WorkSpaceFileService) mFS, flags);
    }

    public static final Creator<WorkSpaceRepo> CREATOR = new Creator<WorkSpaceRepo>() {
        @Override
        public WorkSpaceRepo createFromParcel(Parcel in) {
            return new WorkSpaceRepo(in);
        }

        @Override
        public WorkSpaceRepo[] newArray(int size) {
            return new WorkSpaceRepo[size];
        }
    };

    @Override
    public boolean createFolder(String parentPathId, String name, boolean autoRename)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        return mFS.createFolder(parentPathId, name, autoRename);
    }

    @Override
    public void protect(String normalPath,
                        Rights rights, Obligations obligations, Expiry expiry,
                        String parentPathId,
                        final IProtectCallback callback) {
        String memberShipId = "";
        try {
            memberShipId = getMemberShipId();
        } catch (InvalidRMClientException e) {
            if (callback != null) {
                callback.onProtectFailed(new Exception(e.getMessage()));
            }
        }
        if (memberShipId == null || memberShipId.isEmpty()) {
            if (callback != null) {
                callback.onProtectFailed(new Exception("Internal error,Membership id is null."));
            }
            return;
        }
        File f = new File(normalPath);
        ProtectWithADHocTask task = new ProtectWithADHocTask(this, memberShipId, f,
                rights, obligations, expiry,
                parentPathId, new LoadTask.ITaskCallback<ProtectWithADHocTask.Result, Exception>() {
            @Override
            public void onTaskPreExecute() {
                if (callback != null) {
                    callback.onPreProtect();
                }
            }

            @Override
            public void onTaskExecuteSuccess(ProtectWithADHocTask.Result results) {
                if (callback != null) {
                    callback.onProtectSuccess();
                }
            }

            @Override
            public void onTaskExecuteFailed(Exception e) {
                if (callback != null) {
                    callback.onProtectFailed(e);
                }
            }
        });
        task.run();
    }

    @Override
    public void protect(String normalPath, Map<String, Set<String>> tags,
                        String parentPathId,
                        final IProtectCallback callback) {
        String membershipId = "";
        try {
            membershipId = getMemberShipId();
        } catch (InvalidRMClientException e) {
            e.printStackTrace();
        }

        if (membershipId == null || membershipId.isEmpty()) {
            if (callback != null) {
                callback.onProtectFailed(new Exception("Internal error,Membership id is null."));
            }
            return;
        }
        File f = new File(normalPath);
        ProtectWithCentralPolicyTask task = new ProtectWithCentralPolicyTask(this, f,
                membershipId, tags, parentPathId,
                new LoadTask.ITaskCallback<ProtectWithCentralPolicyTask.Result, Exception>() {
                    @Override
                    public void onTaskPreExecute() {
                        if (callback != null) {
                            callback.onPreProtect();
                        }
                    }

                    @Override
                    public void onTaskExecuteSuccess(ProtectWithCentralPolicyTask.Result results) {
                        if (callback != null) {
                            callback.onProtectSuccess();
                        }
                    }

                    @Override
                    public void onTaskExecuteFailed(Exception e) {
                        if (callback != null) {
                            callback.onProtectFailed(e);
                        }
                    }
                });
        task.run();
    }

    @Override
    public boolean upload(File nxlFile, String parentPathId)
            throws InvalidRMClientException, SessionInvalidException, RmsRestAPIException {
        return mFS.uploadFile(parentPathId, nxlFile);
    }

    private String getMemberShipId() throws InvalidRMClientException {
        IRmUser rmUser = SkyDRMApp.getInstance()
                .getSession()
                .getRmUser();

        List<IMemberShip> memberships = rmUser.getMemberships();
        if (memberships != null && memberships.size() != 0) {
            for (IMemberShip m : memberships) {
                if (m instanceof SystemBucketMemberShip) {
                    SystemBucketMemberShip sbm = (SystemBucketMemberShip) m;
                    return sbm.getId();
                }
            }
        }
        return "";
    }

    private void removeLocalIfNecessary(RmsRestAPIException e, String pathId) {
        if (e == null || pathId == null || pathId.isEmpty()) {
            return;
        }
        if (e.getDomain() == RmsRestAPIException.ExceptionDomain.FileNotFound) {
            mFS.deleteFile(pathId, pathId.endsWith("/"));
        }
    }

    @Override
    public boolean shareToProject(@NonNull ISharingFile file,
                                  @NonNull List<Integer> recipients,
                                  String comments)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        return false;
    }

    @Override
    public boolean shareToPerson(@NonNull ISharingFile file,
                                 @NonNull List<String> recipients,
                                 String comments)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        return false;
    }

    @Override
    public boolean updateRecipients(@NonNull ISharingFile file,
                                    List<String> newRecipients,
                                    List<String> removedRecipients,
                                    String comments)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        return false;
    }

    @Override
    public boolean revokeAllRights(@NonNull ISharingFile file)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        return false;
    }

}
