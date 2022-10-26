package com.skydrm.rmc.datalayer.repo.project;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.skydrm.rmc.datalayer.heartbeat.IHeartBeatListener;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.dbbridge.IOwner;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.policy.Expiry;
import com.skydrm.sdk.policy.Obligations;
import com.skydrm.sdk.policy.Rights;
import com.skydrm.sdk.rms.rest.project.GetProjectMetadataResult;
import com.skydrm.sdk.rms.rest.project.ReclassifyResult;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IProject {
    int getId();

    String getParentTenantId();

    String getParentTenantName();

    String getTokenGroupName();

    String getName();

    String getDescription();

    String getDisplayName();

    long getCreationTime();

    long getConfigurationModified();

    int getTotalMembers();

    int getTotalFiles();

    boolean isOwnedByMe();

    IOwner getOwner();

    String getAccountType();

    long getTrialEndTime();

    String getExpiry();

    String getWatermark();

    String getClassificationRaw();

    boolean isPendingInvite();

    long getQuota();

    long getUsage();

    String inviteMember(List<String> emails, String invitationMsg)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException;

    List<IMember> listMember();

    List<IMember> syncMember()
            throws InvalidRMClientException, RmsRestAPIException, SessionInvalidException;

    List<IMember> syncPendingMember()
            throws InvalidRMClientException, RmsRestAPIException, SessionInvalidException;

    List<INxlFile> listFile(String pathId, boolean recursively);

    List<INxlFile> syncFile(String pathId, boolean recursively)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException;

    List<INxlFile> listRecentFile();

    List<INxlFile> syncRecentFile()
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException;

    List<INxlFile> listOfflineFile();

    List<INxlFile> listAllSharedFile();

    List<INxlFile> listSharedWithFile();

    List<INxlFile> syncSharedWithFile()
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException;

    boolean update(String name, String description, String invitationMsg)
            throws RmsRestAPIException, SessionInvalidException, InvalidRMClientException;

    GetProjectMetadataResult getMetadata()
            throws InvalidRMClientException, SessionInvalidException, RmsRestAPIException;

    boolean createFolder(String parentPathId, String name, boolean autoRename)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException;

    void syncClassification()
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException;

    void syncMemberShip() throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException;

    /**
     * Add file from project.(Central policy protected file supported only.)
     *
     * @param nxlPath      the nxl file local path.
     * @param tags         classifications apply to the new file.
     * @param parentPathId which place the new nxl file should be upload to.
     * @param callback     operation callbacks.
     */
    @Deprecated
    void addFile(String nxlPath, Map<String, Set<String>> tags, String parentPathId, IAddFileCallback callback);

    @Deprecated
    void addFile(String normalPath, Rights rights, Obligations obligations, Expiry expiry, String parentPathId, IAddFileCallback callback);

    @Deprecated
    void addFile(File f, Map<String, Set<String>> tags, String parentPathId, IAddFileCallback callback);

    ReclassifyResult reClassifyFile(@NonNull String fileName, @NonNull String parentPathId, @Nullable String fileTags)
            throws RmsRestAPIException, SessionInvalidException, InvalidRMClientException;

    void hitPoint();

    void syncClassificationWithInterval();

    void onHeartBeat(IHeartBeatListener l);

    long getCacheSize();

    void clearCache();

    void updatePartial();

    void updateResetAllOperationStatus();

    interface IAddFileCallback {
        void onPreAdd();

        void onSuccess();

        void onFailed(Exception e);
    }
}
