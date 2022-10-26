package com.skydrm.rmc.datalayer.repo.project;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.RepoFactory;
import com.skydrm.rmc.datalayer.heartbeat.HeartbeatPolicyGenerator;
import com.skydrm.rmc.datalayer.heartbeat.IHeartBeatListener;
import com.skydrm.rmc.datalayer.heartbeat.IHeartBeatPolicy;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.base.RepoType;
import com.skydrm.rmc.datalayer.repo.base.Utils;
import com.skydrm.rmc.dbbridge.IDBProjectItem;
import com.skydrm.rmc.dbbridge.IOwner;
import com.skydrm.rmc.dbbridge.base.Owner;
import com.skydrm.rmc.engine.Render.RenderHelper;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.base.IDataService;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.rmc.ui.common.NxlFileType;
import com.skydrm.rmc.ui.project.feature.service.core.MarkException;
import com.skydrm.rmc.ui.project.feature.service.core.MarkerStatus;
import com.skydrm.rmc.ui.project.feature.service.protect.task.AddFileWithADHocRightsTask;
import com.skydrm.rmc.ui.project.feature.service.protect.task.AddFileWithClassificationTask;
import com.skydrm.rmc.ui.project.feature.service.share.core.task.AddFileToProjectTask;
import com.skydrm.rmc.ui.service.createfolder.ICreateFolderService;
import com.skydrm.rmc.ui.service.modifyrights.IModifyRightsService;
import com.skydrm.rmc.ui.service.protect.IProtectService;
import com.skydrm.rmc.ui.service.protect.task.ProtectWithADHocTask;
import com.skydrm.rmc.ui.service.protect.task.ProtectWithCentralPolicyTask;
import com.skydrm.rmc.ui.service.share.ISharingFile;
import com.skydrm.rmc.ui.service.share.ISharingService;
import com.skydrm.rmc.utils.sort.IBaseSortable;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.policy.Expiry;
import com.skydrm.sdk.policy.Obligations;
import com.skydrm.sdk.policy.Rights;
import com.skydrm.sdk.rms.rest.project.GetProjectMetadataResult;
import com.skydrm.sdk.rms.rest.project.ReclassifyResult;
import com.skydrm.sdk.rms.rest.project.ShareFileResult;
import com.skydrm.sdk.rms.rest.project.UpdateProjectResult;
import com.skydrm.sdk.rms.rest.user.User;
import com.skydrm.sdk.rms.types.UpdateProjectRecipientsResult;
import com.skydrm.sdk.rms.user.IRmUser;
import com.skydrm.sdk.rms.user.membership.IMemberShip;
import com.skydrm.sdk.rms.user.membership.ProjectMemberShip;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class Project implements IProject, IDataService,
        IInvitePending, IBaseSortable, IModifyRightsService,
        ICreateFolderService, ISharingService, IProtectService, Parcelable {
    private int mId;
    private String mParentTenantId;
    private String mParentTenantName;
    private String mTokenGroupName;
    private String mName;
    private String mDescription;
    private String mDisplayName;
    private long mCreationTime;
    private long mConfigurationModified;
    private int mTotalMembers;
    private int mTotalFiles;
    private boolean isOwnedByMe;
    private IOwner mOwner;
    private String mAccountType;
    private long mTrialEndTime;

    private String mExpiry;
    private String mWatermark;
    private String mClassification;

    //Invite pending
    private int mInvitationId;
    private String mInviteeEmail;
    private String mInviterDisplayName;
    private String mInviterEmail;
    private long mInviteTime;
    private String mInvitationCode;
    private String mInvitationMsg;
    private boolean mPendingInvite;
    private long mLastAccessTime;

    private IDBProjectItem mDBItem;
    private FileService mFS;
    private MemberService mMS;
    private InvitationService mIS;
    private SharedWithProjectFileService mSWFS;

    public Project() {

    }

    private Project(int invitationId, String inviteeEmail, String inviterDisplayName,
                    String inviterEmail, long inviteTime, String code,
                    String invitationMsg, int id, String name,
                    String description, String displayName, long creationTime,
                    int ownerUserId, String ownerName, String ownerEmail) {
        this.mIS = new InvitationService();
        this.mInvitationId = invitationId;
        this.mInviteeEmail = inviteeEmail;
        this.mInviterDisplayName = inviterDisplayName;
        this.mInviterEmail = inviterEmail;
        this.mInviteTime = inviteTime;
        this.mInvitationMsg = invitationMsg;
        this.mInvitationCode = code;

        this.mId = id;
        this.mName = name;
        this.mDescription = description;
        this.mDisplayName = displayName;
        this.mCreationTime = creationTime;
        this.mOwner = Owner.createOwner(ownerUserId, ownerName, ownerEmail);

        isOwnedByMe = false;
        mPendingInvite = true;
    }

    private Project(IDBProjectItem item) {
        this.mDBItem = item;
        this.mFS = new FileService(mDBItem.getId(), mDBItem.getProjectTBPK());
        this.mMS = new MemberService(mDBItem.getId(), mDBItem.getProjectTBPK(), mDBItem.getOwner());
        this.mSWFS = new SharedWithProjectFileService(mDBItem.getId(), mDBItem.getProjectTBPK());

        this.mId = item.getId();
        this.mParentTenantId = item.getParentTenantId();
        this.mParentTenantName = item.getParentTenantName();
        this.mTokenGroupName = item.getTokenGroupName();
        this.mName = item.getName();
        this.mDescription = item.getDescription();
        this.mDisplayName = item.getDisplayName();
        this.mCreationTime = item.getCreationTime();
        this.mConfigurationModified = item.getConfigurationModified();
        this.mTotalMembers = item.getTotalMembers();
        this.mTotalFiles = item.getTotalFiles();
        this.isOwnedByMe = item.isOwnedByMe();
        this.mOwner = item.getOwner();
        this.mAccountType = item.getAccountType();
        this.mTrialEndTime = item.getTrialEndTime();

        this.mExpiry = item.getExpiry();
        this.mWatermark = item.getWatermark();
        this.mClassification = item.getClassificationRaw();
        this.mLastAccessTime = item.getLastAccessTime();
    }

    protected Project(Parcel in) {
        mId = in.readInt();
        mParentTenantId = in.readString();
        mParentTenantName = in.readString();
        mTokenGroupName = in.readString();
        mName = in.readString();
        mDescription = in.readString();
        mDisplayName = in.readString();
        mCreationTime = in.readLong();
        mConfigurationModified = in.readLong();
        mTotalMembers = in.readInt();
        mTotalFiles = in.readInt();
        isOwnedByMe = in.readByte() != 0;
        mAccountType = in.readString();
        mTrialEndTime = in.readLong();
        mExpiry = in.readString();
        mWatermark = in.readString();
        mClassification = in.readString();
        mLastAccessTime = in.readLong();

        mDBItem = in.readParcelable(IDBProjectItem.class.getClassLoader());
        mFS = in.readParcelable(FileService.class.getClassLoader());
        mMS = in.readParcelable(MemberService.class.getClassLoader());
        mIS = in.readParcelable(InvitationService.class.getClassLoader());
        mSWFS = in.readParcelable(SharedWithProjectFileService.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(mParentTenantId);
        dest.writeString(mParentTenantName);
        dest.writeString(mTokenGroupName);
        dest.writeString(mName);
        dest.writeString(mDescription);
        dest.writeString(mDisplayName);
        dest.writeLong(mCreationTime);
        dest.writeLong(mConfigurationModified);
        dest.writeInt(mTotalMembers);
        dest.writeInt(mTotalFiles);
        dest.writeByte((byte) (isOwnedByMe ? 1 : 0));
        dest.writeString(mAccountType);
        dest.writeLong(mTrialEndTime);
        dest.writeString(mExpiry);
        dest.writeString(mWatermark);
        dest.writeString(mClassification);
        dest.writeLong(mLastAccessTime);

        dest.writeParcelable(mDBItem, flags);
        dest.writeParcelable(mFS, flags);
        dest.writeParcelable(mMS, flags);
        dest.writeParcelable(mIS, flags);
        dest.writeParcelable(mSWFS, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Project> CREATOR = new Creator<Project>() {
        @Override
        public Project createFromParcel(Parcel in) {
            return new Project(in);
        }

        @Override
        public Project[] newArray(int size) {
            return new Project[size];
        }
    };

    @Override
    public int getId() {
        return mId;
    }

    @Override
    public String getParentTenantId() {
        return mParentTenantId;
    }

    @Override
    public String getParentTenantName() {
        return mParentTenantName;
    }

    @Override
    public String getTokenGroupName() {
        return mTokenGroupName;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getDescription() {
        return mDescription;
    }

    @Override
    public String getDisplayName() {
        return mDisplayName;
    }

    @Override
    public long getCreationTime() {
        return mCreationTime;
    }

    @Override
    public long getConfigurationModified() {
        return mConfigurationModified;
    }

    @Override
    public int getTotalMembers() {
        return mTotalMembers;
    }

    @Override
    public int getTotalFiles() {
        return mTotalFiles;
    }

    @Override
    public boolean isOwnedByMe() {
        return isOwnedByMe;
    }

    @Override
    public IOwner getOwner() {
        return mOwner;
    }

    @Override
    public String getAccountType() {
        return mAccountType;
    }

    @Override
    public long getTrialEndTime() {
        return mTrialEndTime;
    }

    @Override
    public String getExpiry() {
        return mExpiry;
    }

    @Override
    public String getWatermark() {
        return mWatermark;
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
    public void protect(String normalPath,
                        Map<String, Set<String>> tags,
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

    @Override
    public String getClassificationRaw() {
        return mClassification;
    }

    @Override
    public User.IExpiry getIExpiry() {
        return getExpiry(mExpiry);
    }

    @Override
    public boolean modifyRights(@NonNull String fileName,
                                @NonNull String parentPathId,
                                @Nullable String fileTags)
            throws RmsRestAPIException, SessionInvalidException, InvalidRMClientException {
        ReclassifyResult result = reClassifyFile(fileName, parentPathId, fileTags);
        return result != null;
    }

    @Override
    public boolean isPendingInvite() {
        return mPendingInvite;
    }

    @Override
    public long getQuota() {
        if (mDBItem == null) {
            return 0;
        }
        return mDBItem.getQuota();
    }

    @Override
    public long getUsage() {
        if (mDBItem == null) {
            return 0;
        }
        return mDBItem.getUsage();
    }

    @Override
    public String inviteMember(List<String> emails, String invitationMsg)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        try {
            return mMS.invite(emails, invitationMsg);
        } catch (RmsRestAPIException e) {
            removeSelfIfNecessary(e);
            throw e;
        }
    }

    @Override
    public List<IMember> listMember() {
        return mMS.listMember();
    }

    @Override
    public List<IMember> syncMember()
            throws InvalidRMClientException, RmsRestAPIException, SessionInvalidException {
        return mMS.syncMember();
    }

    @Override
    public List<IMember> syncPendingMember()
            throws InvalidRMClientException, RmsRestAPIException, SessionInvalidException {
        return mMS.syncPendingMember();
    }

    @Override
    public List<INxlFile> list(int type, String pathId, boolean recursively) {
        if (type == NxlFileType.ALL.getValue()) {
            return listFile(pathId, recursively);
        } else if (type == NxlFileType.OFFLINE.getValue()) {
            return listOfflineFile();
        } else if (type == NxlFileType.SHARED_BY_ME.getValue()) {
            return listAllSharedFile();
        } else if (type == NxlFileType.SHARED_WITH_ME.getValue()) {
            return listSharedWithFile();
        } else if (type == NxlFileType.RECENT.getValue()) {
            return listRecentFile();
        } else {
            return listFile(pathId, recursively);
        }
    }

    @Override
    public List<INxlFile> sync(int type, String pathId, boolean recursively)
            throws SessionInvalidException, RmsRestAPIException, InvalidRMClientException {
        if (type == NxlFileType.ALL.getValue()) {
            return syncFile(pathId, recursively);
        } else if (type == NxlFileType.OFFLINE.getValue()) {
            return listOfflineFile();
        } else if (type == NxlFileType.SHARED_BY_ME.getValue()) {
            return listAllSharedFile();
        } else if (type == NxlFileType.SHARED_WITH_ME.getValue()) {
            return syncSharedWithFile();
        } else if (type == NxlFileType.RECENT.getValue()) {
            return syncRecentFile();
        } else {
            return syncFile(pathId, recursively);
        }
    }

    @Override
    public List<INxlFile> listFile(String pathId, boolean recursively) {
        return mFS.listFile(pathId, recursively);
    }

    @Override
    public List<INxlFile> syncFile(String pathId, boolean recursively)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        try {
            return mFS.syncFile(pathId, recursively);
        } catch (RmsRestAPIException e) {
            removeSelfIfNecessary(e);
            throw e;
        }
    }

    @Override
    public List<INxlFile> listRecentFile() {
        return mFS.listRecentFile();
    }

    @Override
    public List<INxlFile> syncRecentFile()
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        try {
            return mFS.syncRecentFile();
        } catch (RmsRestAPIException e) {
            removeSelfIfNecessary(e);
            throw e;
        }
    }

    @Override
    public List<INxlFile> listOfflineFile() {
        List<INxlFile> ret = new ArrayList<>();

        List<INxlFile> result1 = mFS.listOfflineFile();
        if (result1 != null) {
            ret.addAll(result1);
        }
        List<INxlFile> result2 = mSWFS.listOfflineFile();
        if (result2 != null) {
            ret.addAll(result2);
        }

        return ret;
    }

    @Override
    public List<INxlFile> listAllSharedFile() {
        return mFS.listAllSharedFile();
    }

    @Override
    public List<INxlFile> listSharedWithFile() {
        return mSWFS.listTree("/");
    }

    @Override
    public List<INxlFile> syncSharedWithFile()
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        return mSWFS.syncTree("/");
    }

    @Override
    public boolean update(String name, String description, String invitationMsg)
            throws RmsRestAPIException, SessionInvalidException, InvalidRMClientException {
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        try {

            UpdateProjectResult result = session.getRmsRestAPI()
                    .getProjectService(session.getRmUser())
                    .updateProject(mId, name, description, invitationMsg);
            if (result == null) {
                return false;
            }
            UpdateProjectResult.ResultsBean results = result.getResults();
            if (results == null) {
                return false;
            }
            UpdateProjectResult.ResultsBean.DetailBean detail = results.getDetail();
            if (detail == null) {
                return false;
            }
            this.mName = detail.getName();
            this.mDisplayName = detail.getDisplayName();
            this.mDescription = detail.getDescription();
            return SkyDRMApp.getInstance()
                    .getDBProvider()
                    .updateProjectItem(mDBItem.getProjectTBPK(), mId, mName, mDescription,
                            mDisplayName, detail.getCreationTime(), detail.getTotalMembers(),
                            detail.getTotalFiles(), detail.isOwnedByMe(),
                            detail.getAccountType(), detail.getTrialEndTime());

        } catch (RmsRestAPIException e) {
            removeSelfIfNecessary(e);
            throw e;
        }
    }

    @Override
    public GetProjectMetadataResult getMetadata()
            throws InvalidRMClientException, SessionInvalidException, RmsRestAPIException {
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        try {
            return session.getRmsRestAPI()
                    .getProjectService(session.getRmUser())
                    .getProjectMetadata(mId);
        } catch (RmsRestAPIException e) {
            removeSelfIfNecessary(e);
            throw e;
        }
    }

    @Override
    public boolean createFolder(String parentPathId, String name, boolean autoRename)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        try {
            return mFS.createFolder(parentPathId, name, autoRename);
        } catch (RmsRestAPIException e) {
            removeSelfIfNecessary(e);
            throw e;
        }
    }

    @Override
    public void syncClassification()
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();

        String classificationRaw = session.getRmsRestAPI()
                .getClassificationProfileService(session.getRmUser())
                .getClassificationProfile(mTokenGroupName);

        if (mDBItem == null) {
            return;
        }
        mDBItem.setClassification(classificationRaw);
        this.mClassification = classificationRaw;
    }

    @Override
    public void syncMemberShip()
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        String response = session.getRmsRestAPI()
                .getProjectService(session.getRmUser())
                .getProjectMembershipId(mId);

        try {
            JSONObject responseObj = new JSONObject(response);
            JSONObject resultsObj = responseObj.optJSONObject("results");
            if (resultsObj == null) {
                return;
            }
            JSONObject membershipObj = resultsObj.optJSONObject("membership");
            if (membershipObj == null) {
                return;
            }
            String id = membershipObj.optString("id");
            int type = membershipObj.optInt("type");
            String tokenGroupName = membershipObj.optString("tokenGroupName");
            int projectId = membershipObj.optInt("projectId");

            session.getRmUser().updateOrInsertMembershipItem(new ProjectMemberShip(id, type,
                    tokenGroupName, projectId));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addFile(String nxlPath, Map<String, Set<String>> tags, String parentPathId,
                        final IAddFileCallback callback) {
        String membershipId = "";
        try {
            membershipId = getMemberShipId();
        } catch (InvalidRMClientException e) {
            e.printStackTrace();
            if (callback != null) {
                callback.onFailed(new MarkException(MarkerStatus.STATUS_FAILED_INVALID_RMC_CLIENT,
                        e.getMessage(), e));
            }
        }
        if (membershipId == null || membershipId.isEmpty()) {
            if (callback != null) {
                callback.onFailed(new MarkException(MarkerStatus.STATUS_FAILED_COMMON,
                        "Membership id is required to perform this action."));
            }
            return;
        }
        AddFileToProjectTask task = new AddFileToProjectTask(nxlPath,
                membershipId, tags,
                mId, parentPathId,
                new LoadTask.ITaskCallback<AddFileToProjectTask.Result, MarkException>() {
                    @Override
                    public void onTaskPreExecute() {
                        if (callback != null) {
                            callback.onPreAdd();
                        }
                    }

                    @Override
                    public void onTaskExecuteSuccess(AddFileToProjectTask.Result results) {
                        increaseTotalCount();
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }

                    @Override
                    public void onTaskExecuteFailed(MarkException e) {
                        if (callback != null) {
                            callback.onFailed(e);
                        }
                    }
                });
        task.run();
    }

    @Override
    public void addFile(String normalPath,
                        Rights rights, Obligations obligations, Expiry expiry,
                        String parentPathId,
                        final IAddFileCallback callback) {
        if (normalPath == null || normalPath.isEmpty()) {
            return;
        }
        if (rights == null || obligations == null || expiry == null) {
            return;
        }
        String memberShipId = "";
        try {
            memberShipId = getMemberShipId();
        } catch (InvalidRMClientException e) {
            e.printStackTrace();
        }
        if (memberShipId == null || memberShipId.isEmpty()) {
            return;
        }
        File f = new File(normalPath);
        if (!f.exists() || !f.isFile()) {
            return;
        }
        AddFileWithADHocRightsTask task = new AddFileWithADHocRightsTask(this, memberShipId, f,
                rights, obligations, expiry, mId, parentPathId,
                new LoadTask.ITaskCallback<AddFileWithADHocRightsTask.Result, Exception>() {
                    @Override
                    public void onTaskPreExecute() {
                        if (callback != null) {
                            callback.onPreAdd();
                        }
                    }

                    @Override
                    public void onTaskExecuteSuccess(AddFileWithADHocRightsTask.Result results) {
                        increaseTotalCount();
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }

                    @Override
                    public void onTaskExecuteFailed(Exception e) {
                        if (e instanceof RmsRestAPIException) {
                            RmsRestAPIException e1 = (RmsRestAPIException) e;
                            if (e1.getDomain() == RmsRestAPIException.ExceptionDomain.NotFound) {
                                e = new RmsRestAPIException("Invalid project", e1.getDomain());
                                removeSelfIfNecessary(e1);
                            }
                        }
                        if (callback != null) {
                            callback.onFailed(e);
                        }
                    }
                });
        task.run();
    }

    @Override
    public void addFile(File f,
                        Map<String, Set<String>> tags,
                        String parentPathId, final IAddFileCallback callback) {
        if (f == null || tags == null) {
            return;
        }
        if (parentPathId == null || parentPathId.isEmpty()) {
            return;
        }
        if (!f.exists() || !f.isFile()) {
            return;
        }
        String membershipId = "";

        try {
            membershipId = getMemberShipId();
        } catch (InvalidRMClientException e) {
            e.printStackTrace();
        }

        if (membershipId == null || membershipId.isEmpty()) {
            return;
        }

        AddFileWithClassificationTask task = new AddFileWithClassificationTask(this, f,
                membershipId, tags,
                mId, parentPathId,
                new LoadTask.ITaskCallback<AddFileWithClassificationTask.Result, Exception>() {
                    @Override
                    public void onTaskPreExecute() {
                        if (callback != null) {
                            callback.onPreAdd();
                        }
                    }

                    @Override
                    public void onTaskExecuteSuccess(AddFileWithClassificationTask.Result results) {
                        increaseTotalCount();
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }

                    @Override
                    public void onTaskExecuteFailed(Exception e) {
                        if (e instanceof RmsRestAPIException) {
                            RmsRestAPIException e1 = (RmsRestAPIException) e;
                            if (e1.getDomain() == RmsRestAPIException.ExceptionDomain.NotFound) {
                                e = new RmsRestAPIException("Invalid project", e1.getDomain());
                                removeSelfIfNecessary(e1);
                            }
                        }
                        if (callback != null) {
                            callback.onFailed(e);
                        }
                    }
                });
        task.run();
    }

    @Override
    public ReclassifyResult reClassifyFile(@NonNull String fileName,
                                           @NonNull String parentPathId,
                                           @Nullable String fileTags)
            throws RmsRestAPIException, SessionInvalidException, InvalidRMClientException {

        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        IRmUser rmUser = session.getRmUser();

        try {
            return session.getRmsRestAPI()
                    .getProjectService(rmUser)
                    .reClassify(mId, fileName, parentPathId, fileTags);
        } catch (RmsRestAPIException e) {
            removeLocalFileIfNecessary(e, parentPathId);
            throw e;
        }
    }

    @Override
    public void hitPoint() {
        if (mDBItem == null) {
            return;
        }
        mDBItem.updateAccessCount();
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        long now = calendar.getTimeInMillis();
        mDBItem.updateLastAccessTime(now);

        mLastAccessTime = now;

        joinTheTaskQueueIfNecessary(true);
    }

    @Override
    public void syncClassificationWithInterval() {
        joinTheTaskQueueIfNecessary(false);
    }

    static Project newByDBItem(IDBProjectItem item) {
        return new Project(item);
    }

    @Override
    public String getSortableName() {
        return mName;
    }

    @Override
    public long getSortableSize() {
        return mId;
    }

    @Override
    public long getSortableTime() {
        if (mLastAccessTime != 0) {
            return mLastAccessTime;
        }
        return mCreationTime;
    }

    @Override
    public boolean isFolder() {
        return false;
    }

    @Override
    public int getInvitationId() {
        return mInvitationId;
    }

    @Override
    public String getInviteeEmail() {
        return mInviteeEmail;
    }

    @Override
    public String getInviterDisplayName() {
        return mInviterDisplayName;
    }

    @Override
    public String getInviterEmail() {
        return mInviterEmail;
    }

    @Override
    public long getInviteTime() {
        return mInviteTime;
    }

    @Override
    public String getInviteCode() {
        return mInvitationCode;
    }

    @Override
    public String getInviteMsg() {
        return mInvitationMsg;
    }

    @Override
    public boolean acceptInvitation()
            throws RmsRestAPIException, SessionInvalidException, InvalidRMClientException {
        return mIS.accept(mInvitationId, mInvitationCode);
    }

    @Override
    public boolean denyInvitation(String reason)
            throws RmsRestAPIException, SessionInvalidException, InvalidRMClientException {
        return mIS.deny(mInvitationId, mInvitationCode, reason);
    }

    @Override
    public void onHeartBeat(IHeartBeatListener l) {
        Log.d("HeartBeat", "onHeartBeat() called target name = [" + mName + "]");
        if (l != null) {
            l.onTaskBegin();
        }
        try {
            syncClassification();
        } catch (SessionInvalidException e) {
            handleException(l, e);
        } catch (InvalidRMClientException e) {
            handleException(l, e);
        } catch (RmsRestAPIException e) {
            handleException(l, e);
        } catch (Exception e) {
            handleException(l, e);
        }
        try {
            syncMemberShip();
        } catch (SessionInvalidException e) {
            handleException(l, e);
        } catch (InvalidRMClientException e) {
            handleException(l, e);
        } catch (RmsRestAPIException e) {
            handleException(l, e);
        } catch (Exception e) {
            handleException(l, e);
        }

        mFS.onHearBeat();

        mSWFS.onHeartBeat();
        if (l != null) {
            l.onTaskFinish();
        }
        // recording refresh time-millis.
        if (mDBItem != null) {
            mDBItem.updateLastRefreshTime();
        }
    }

    @Override
    public long getCacheSize() {
        // 1. If we delete all (include all offline files) using condition 1. else then 2.
        //return FileUtils.getSize(getProjectMountPoint());
        // 2. Build tree and visit each node and leaf node (except offline file.)
        //Build tree first.
        long ret = 0;
        List<INxlFile> tree = listFile("/", true);
        //Empty tree.
        if (tree == null || tree.size() == 0) {
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
    public void clearCache() {
        //Reference to getCacheSize is the same logic.
//        //Update reset project file tb field local_path.
//        SkyDRMApp.getInstance()
//                .getDBProvider()
//                .updateProjectFileItemResetLocalPath(mDBItem.getProjectTBPK());
//        //Clear local downloaded file recursively.
//        FileUtils.deleteRecursively(getProjectMountPoint());

        List<INxlFile> tree = listFile("/", true);
        if (tree == null || tree.size() == 0) {
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
    public void updatePartial() {
        try {
            syncClassification();
        } catch (SessionInvalidException e) {
            e.printStackTrace();
        } catch (InvalidRMClientException e) {
            e.printStackTrace();
        } catch (RmsRestAPIException e) {
            e.printStackTrace();
        }

        try {
            syncMemberShip();
        } catch (SessionInvalidException e) {
            e.printStackTrace();
        } catch (InvalidRMClientException e) {
            e.printStackTrace();
        } catch (RmsRestAPIException e) {
            e.printStackTrace();
        }

        if (mDBItem != null) {
            mDBItem.updateLastRefreshTime();
        }
    }

    @Override
    public void updateResetAllOperationStatus() {
        if (mDBItem == null) {
            return;
        }
        SkyDRMApp.getInstance()
                .getDBProvider()
                .updateProjectFileItemResetOperationStatus(mDBItem.getProjectTBPK());
    }

    private String getMemberShipId() throws InvalidRMClientException {
        IRmUser rmUser = SkyDRMApp.getInstance()
                .getSession()
                .getRmUser();

        List<IMemberShip> memberships = rmUser.getMemberships();
        if (memberships != null && memberships.size() != 0) {
            for (IMemberShip m : memberships) {
                if (m instanceof ProjectMemberShip) {
                    ProjectMemberShip pms = (ProjectMemberShip) m;
                    if (pms.getProjectId() == mId) {
                        return pms.getId();
                    }
                }
            }
        }

        return "";
    }

    private void increaseTotalCount() {
        if (mDBItem == null) {
            return;
        }
        mTotalFiles++;
        mDBItem.increaseTotalCount();
    }

    private File getProjectMountPoint() {
        return RenderHelper.getProjectsMountPoint(mName);
    }

    private void handleException(IHeartBeatListener l, Exception e) {
        e.printStackTrace();
        if (l != null) {
            l.onTaskFailed(e);
        }
    }

    private void joinTheTaskQueueIfNecessary(boolean all) {
        try {
            if (all) {
                if (isAllowedToRefresh()) {
                    ProjectRepo repo = (ProjectRepo) RepoFactory.getRepo(RepoType.TYPE_PROJECT);
                    if (repo == null) {
                        return;
                    }
                    repo.fireTaskToCrawlTarget(this);
                }
            } else {
                if (isAllowedToSyncClassification()) {
                    ProjectRepo repo = (ProjectRepo) RepoFactory.getRepo(RepoType.TYPE_PROJECT);
                    if (repo == null) {
                        return;
                    }
                    repo.fireTaskToSyncClassification(this);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isAllowedToRefresh() {
        if (mDBItem == null) {
            return false;
        }
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        long now = calendar.getTimeInMillis();
        long lstRefreshMillis = mDBItem.getLastRefreshMillis();

        IHeartBeatPolicy one = HeartbeatPolicyGenerator.getOne(HeartbeatPolicyGenerator.TYPE_PROJECT);
        long interval = one.getInterval() * 1000;
        return now - lstRefreshMillis > interval;
    }

    private boolean isAllowedToSyncClassification() {
        if (mDBItem == null) {
            return false;
        }
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        long now = calendar.getTimeInMillis();
        long lstRefreshMillis = mDBItem.getLastRefreshMillis();

        long interval = 20 * 1000;
        return now - lstRefreshMillis > interval;
    }

    static Project newPendingProject(int invitationId, String inviteeEmail, String inviterDisplayName,
                                     String inviterEmail, long inviteTime, String code,
                                     String invitationMsg, int id, String name,
                                     String description, String displayName, long creationTime,
                                     int ownerUserId, String ownerName, String ownerEmail) {
        return new Project(invitationId, inviteeEmail, inviterDisplayName, inviterEmail, inviteTime,
                code, invitationMsg, id, name, description, displayName, creationTime,
                ownerUserId, ownerName, ownerEmail);
    }

    static Project newByInvitation(int id, String name, String parentTenantId, String parentTenantName,
                                   String tokenGroupName, String description, String displayName,
                                   long creationTime, String invitationMsg, int totalMembers,
                                   int totalFiles, boolean ownerByMe, String accountType,
                                   long trialEndTime, int ownerUserId, String ownerName,
                                   String ownerEmail) {
        Project p = new Project();
        p.mDBItem = null;
        p.mId = id;
        p.mName = name;
        p.mParentTenantId = parentTenantId;
        p.mParentTenantName = parentTenantName;
        p.mTokenGroupName = tokenGroupName;
        p.mDescription = description;
        p.mDisplayName = displayName;
        p.mCreationTime = creationTime;
        p.mInvitationMsg = invitationMsg;
        p.mTotalMembers = totalMembers;
        p.mTotalFiles = totalFiles;
        p.isOwnedByMe = ownerByMe;
        p.mAccountType = accountType;
        p.mTrialEndTime = trialEndTime;
        p.mOwner = Owner.createOwner(ownerUserId, ownerName, ownerEmail);
        return p;
    }

    private void removeLocalFileIfNecessary(RmsRestAPIException e, String pathId) {
        if (e == null || pathId == null || pathId.isEmpty()) {
            return;
        }
        if (e.getDomain() == RmsRestAPIException.ExceptionDomain.FileNotFound) {
            mFS.deleteFile(pathId, pathId.endsWith("/"));
        }
        if (e.getDomain() == RmsRestAPIException.ExceptionDomain.NotFound) {
            if (mDBItem != null) {
                mDBItem.delete();
                mDBItem = null;
            }
        }
    }

    private void removeSelfIfNecessary(RmsRestAPIException e) {
        if (e == null) {
            return;
        }
        if (e.getDomain() == RmsRestAPIException.ExceptionDomain.NotFound) {
            if (mDBItem != null) {
                mDBItem.delete();
                mDBItem = null;
            }
        }
    }

    @Override
    public String getServiceName(@NonNull Context ctx) {
        return ctx.getString(R.string.project);
    }

    @Override
    public boolean shareToProject(@NonNull ISharingFile file,
                                  @NonNull List<Integer> recipients,
                                  String comments)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        if (file instanceof SharedWithProjectFile) {
            SharedWithProjectFile swpf = (SharedWithProjectFile) file;
            return swpf.reShare(recipients, comments, mId);
        }
        SkyDRMApp app = SkyDRMApp.getInstance();

        String membershipId = "";
        SkyDRMApp.Session2 session = app.getSession();

        IRmUser rmUser = session.getRmUser();
        List<IMemberShip> memberships = rmUser.getMemberships();
        for (IMemberShip membership : memberships) {
            if (membership instanceof ProjectMemberShip) {
                ProjectMemberShip pms = (ProjectMemberShip) membership;
                if (pms.getProjectId() == mId) {
                    membershipId = pms.getId();
                }
            }
        }

        ShareFileResult result = session.getRmsRestAPI()
                .getProjectService(rmUser)
                .shareFile(mId, membershipId,
                        file.getName(), file.getPathId(), file.getPathId(),
                        recipients, comments);

        // If share success, then need to sync target file status.
        if (result != null) {
            ShareFileResult.ResultsBean results = result.getResults();
            if (results != null) {
                file.update(Utils.translateToStringList(results.getNewSharedList()), null);
            }
            return true;
        }

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
        List<Integer> newCandidates = new ArrayList<>();
        if (newRecipients != null && !newRecipients.isEmpty()) {
            for (String s : newRecipients) {
                newCandidates.add(Integer.valueOf(s));
            }
        }
        List<Integer> removedCandidates = new ArrayList<>();
        if (removedRecipients != null && !removedRecipients.isEmpty()) {
            for (String s : removedRecipients) {
                removedCandidates.add(Integer.valueOf(s));
            }
        }
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        UpdateProjectRecipientsResult result = session.getRmsRestAPI()
                .getSharingService(session.getRmUser())
                .updateProjectRecipients(file.getDuid(), newCandidates, removedCandidates, comments);

        if (result != null) {
            file.update(newRecipients, removedRecipients);
            return true;
        }
        return false;
    }

    @Override
    public boolean revokeAllRights(@NonNull ISharingFile file)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        boolean result = session.getRmsRestAPI()
                .getSharingService(session.getRmUser())
                .revokingDocument(file.getDuid());
        if (result) {
            file.update(true);
            return true;
        }
        return false;
    }

    private User.IExpiry getExpiry(String expiryRaw) {
        if (expiryRaw == null || expiryRaw.isEmpty()) {
            return null;
        }
        try {
            JSONObject expiry;
            expiry = new JSONObject(expiryRaw);
            if (expiry.has("option")) {
                int option = expiry.getInt("option");
                switch (option) {
                    case 0:
                        return new User.IExpiry() {
                            @Override
                            public int getOption() {
                                return 0;
                            }
                        };
                    case 1:
                        final JSONObject relativeDay = expiry.getJSONObject("relativeDay");
                        final int years = relativeDay.optInt("year");
                        final int months = relativeDay.optInt("month");
                        final int weeks = relativeDay.optInt("week");
                        final int days = relativeDay.optInt("day");
                        return new User.IRelative() {
                            @Override
                            public int getYear() {
                                return years;
                            }

                            @Override
                            public int getMonth() {
                                return months;
                            }

                            @Override
                            public int getWeek() {
                                return weeks;
                            }

                            @Override
                            public int getDay() {
                                return days;
                            }

                            @Override
                            public int getOption() {
                                return 1;
                            }
                        };
                    case 2:
                        final long absoluteEndDate = expiry.getLong("endDate");
                        return new User.IAbsolute() {
                            @Override
                            public long endDate() {
                                return absoluteEndDate;
                            }

                            @Override
                            public int getOption() {
                                return 2;
                            }
                        };
                    case 3:
                        final long rangeStartDate = expiry.getLong("startDate");
                        final long rangeEndDate = expiry.getLong("endDate");
                        return new User.IRange() {
                            @Override
                            public long startDate() {
                                return rangeStartDate;
                            }

                            @Override
                            public long endDate() {
                                return rangeEndDate;
                            }

                            @Override
                            public int getOption() {
                                return 3;
                            }
                        };
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
