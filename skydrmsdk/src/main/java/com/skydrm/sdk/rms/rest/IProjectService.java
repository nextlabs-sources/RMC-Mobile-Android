package com.skydrm.sdk.rms.rest;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.rest.project.AllProjectsResult;
import com.skydrm.sdk.rms.rest.project.CreateProjectResult;
import com.skydrm.sdk.rms.rest.project.FileMetadata;
import com.skydrm.sdk.rms.rest.project.GetProjectMetadataResult;
import com.skydrm.sdk.rms.rest.project.ListPendingInvitationResult;
import com.skydrm.sdk.rms.rest.project.ListProjectItemResult;
import com.skydrm.sdk.rms.rest.project.ReShareResult;
import com.skydrm.sdk.rms.rest.project.ReclassifyResult;
import com.skydrm.sdk.rms.rest.project.ShareFileResult;
import com.skydrm.sdk.rms.rest.project.UpdateProjectResult;
import com.skydrm.sdk.rms.rest.project.file.ListFileParam;
import com.skydrm.sdk.rms.rest.project.file.ListFileResult;
import com.skydrm.sdk.rms.rest.project.file.ProjectDownloadHeader;
import com.skydrm.sdk.rms.rest.project.file.UploadFileResult;
import com.skydrm.sdk.rms.rest.project.file.UploadForNXLFileParam;
import com.skydrm.sdk.rms.rest.project.member.ListMemberParam;
import com.skydrm.sdk.rms.rest.project.member.ListMemberResult;
import com.skydrm.sdk.rms.rest.project.member.MemberDetailResult;
import com.skydrm.sdk.rms.rest.project.member.PendingInvitationResult;
import com.skydrm.sdk.rms.rest.project.member.ProjectInvitationResult;
import com.skydrm.sdk.rms.rest.project.member.ProjectPendingInvitationsParas;
import com.skydrm.sdk.ui.uploadprogress.ProgressRequestListener;

import java.io.File;
import java.util.List;

public interface IProjectService {
    ListProjectItemResult listProject(int page, int size, String orderBy, boolean ownedByMe) throws RmsRestAPIException;

    CreateProjectResult createProject(String name, String description,
                                      List<String> emails, String invitationMsg) throws RmsRestAPIException;

    UpdateProjectResult updateProject(int projectId, String projectName, String projectDescription, String invitationMsg) throws RmsRestAPIException;

    GetProjectMetadataResult getProjectMetadata(int projectId) throws RmsRestAPIException;

    String getProjectMembershipId(int projectId) throws RmsRestAPIException;

    UploadFileResult uploadNXLFile(int projectId, UploadForNXLFileParam param, File file, ProgressRequestListener progressRequestListener) throws RmsRestAPIException;

    ListFileResult listFile(int projectId, ListFileParam paras) throws RmsRestAPIException;

    ListMemberResult listMember(int projectId, ListMemberParam requestParas) throws RmsRestAPIException;

    String removeMember(int projectId, int memberId) throws RmsRestAPIException;

    MemberDetailResult getMemberDetail(int projectId, int memberId) throws RmsRestAPIException;

    ListPendingInvitationResult listPendingInvitationForUser() throws RmsRestAPIException;

    String acceptInvitation(int invitationId, String code) throws RmsRestAPIException;

    boolean denyInvitation(int invitationId, String code, @Nullable String reason) throws RmsRestAPIException;

    ProjectDownloadHeader downloadFile(int projectId, String pathId, File doc, int type,
                                       RestAPI.DownloadListener listener, int... args) throws RmsRestAPIException;

    boolean deleteFile(int projectId, String pathId) throws RmsRestAPIException;

    ProjectInvitationResult inviteMember(int projectId, List<String> emails,
                                         String invitationMsg) throws RmsRestAPIException;

    String createFolder(int projectId, String parentPathId, String name, boolean bIsAutoRename)
            throws RmsRestAPIException;

    PendingInvitationResult listPendingInvitations(int projectId, ProjectPendingInvitationsParas requestParas) throws RmsRestAPIException;

    String resendInvitation(int invitationId) throws RmsRestAPIException;

    String revokeInvitation(int invitationId) throws RmsRestAPIException;

    FileMetadata getFileMetadata(int projectId, String filePathId) throws RmsRestAPIException;

    ReclassifyResult reClassify(int projectId, @NonNull String fileName, @NonNull String parentPathId, @Nullable String fileTags) throws RmsRestAPIException;

    ShareFileResult shareFile(int projectId, String membershipId,
                              String fileName, String filePathId, String filePath,
                              List<Integer> recipients, String comments) throws RmsRestAPIException;

    ReShareResult reShare(String transactionId, String transactionCode, int spaceId,
                          List<Integer> recipients, String comments) throws RmsRestAPIException;

    AllProjectsResult listAllProjects() throws RmsRestAPIException;
}
