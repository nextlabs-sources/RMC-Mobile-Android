package com.skydrm.sdk;

import android.support.annotation.NonNull;

import java.math.BigInteger;

public final class Config {
    // timeout
    public static final int CONNECT_TIMEOUT = 60;
    public static final int READ_TIMEOUT = 60;
    public static final int WRITE_TIMEOUT = 60;
    // set timeout for remote view as 10 minutes (for excel file with big size rms may need long time to convert).
    public static final int REMOTE_VIEW_CONNECT_TIMEOUT = 60 * 10;
    public static final int REMOTE_VIEW_READ_TIMEOUT = 60 * 10;
    public static final int REMOTE_VIEW_WRITE_TIMEOUT = 60 * 10;

    // for DH Algo
    // DH- prime modulus
    final static String P = "D310125B294DBD856814DFD4BAB4DC767DF6A999C9EDFA8F8D7B12551F8D71EF6032357405C7F11EE147DB0332716FC8FD85ED027585268360D16BD761563D7D1659D4D73DAED617F3E4223F48BCEFA421860C3FC4393D27545677B22459E852F5254D3AC58C0D63DD79DE2D8D868CD940DECF5A274605DB0EEE762020C39D0F6486606580EAACCE16FB70FB7C759EA9AABAB4DCBF941891B0CE94EC4D3D5954217C6E84A9274F1AB86073BDF9DC851E563B90455B8397DAE3A1B998607BB7699CEA0805A7FF013EF44FDE7AF830F1FD051FFAEC539CE4452D8229098AE3EE2008AB9DB7B2C948312CBC0137C082D6672618E1BFE5D5006E810DC7AA7F1E6EE3";
    public final static BigInteger p = new BigInteger(P, 16);
    // DH- base generator
    final static String G = "64ACEBA5F7BC803EF29731C9C6AE009B86FC5201F81BC2B8F84890FCF71CAD51C1429FD261A2A715C8946154E0E4E28EF6B2D493CC1739F5659E9F14DD14037F5FE72B3BA4D9BCB3B95B8417BDA48F118E61C8214CF8D558DA6774F08B58D97B2CCE20F5AA2F8E9539C014E7761E4E6336CFFC35127DDD527206766AE72045C11B0FF4DA76172523713B31C9F18ABABA92612BDE105141F04DB5DA3C39CDE5C6877B7F8CD96949FCC876E2C1224FB9188D714FDD6CB80682F8967833AD4B51354A8D58598E6B2DEF4571A597AD39BD3177D54B24CA518EDA996EEDBA8A31D5876EFED8AA44023CC9F13D86DCB4DDFCF389C7A1435082EF69703603638325954E";
    public final static BigInteger g = new BigInteger(G, 16);
    private static final String SERVICE_TENANT_URL = "/router/rs/q/tenant/";
    private static final String SERVICE_LOGIN_URL = "/rms/rs/usr";
    private static final String SERVICE_MEMBERSHIP_URL = "/rms/rs/membership";
    private static final String SERVICE_TOKEN_URL = "/rms/rs/token";
    private static final String SERVICE_SHARE_URL = "/rms/rs/share";
    private static final String SERVICE_HEARTBEAT_V2 = "/rms/rs/v2/heartbeat";
    private static final String SERVICE_MYDRIVE_LIST = "/rms/rs/myDrive/list";
    private static final String SERVICE_MYDRIVE_DELETE = "/rms/rs/myDrive/delete";
    private static final String SERVICE_MYDRIVE_CREATE_FOLDER = "/rms/rs/myDrive/createFolder";
    private static final String SERVICE_MYDRIVE_STORAGE_USED = "/rms/rs/myDrive/getUsage";
    private static final String SERVICE_MYDRIVE_CREATE_PUBLIC_SHARE = "/rms/rs/myDrive/getPublicUrl";
    private static final String SERVICE_MYDRIVE_DOWNLOAD = "/rms/rs/myDrive/download";
    private static final String SERVICE_MYDRIVE_UPLOAD = "/rms/rs/myDrive/uploadFile";
    private static final String SERVICE_PROFILE_UPDATE_URL = "/rms/rs/usr/profile";
    private static final String SERVICE_PROFILE_RETRIEVE_URL = "/rms/rs/usr/v2/profile";
    // - Favorite new
    private static final String SERVICE_ALL_REPOS_FAVORITE = "/rms/rs/favorite";
    private static final String SERVICE_ALL_REPOS_FAVORITE_LIST = "/rms/rs/favorite/list";
    private static final String SERVICE_ONE_REPO_FAVORITE = "/rms/rs/favorite/{repository_id}";
    private static final String SERVICE_FAVORITE = "/rms/rs/favorite/{repository_id}";
    // Repostiory
    private static final String SERVICE_REPOSITORY_URL = "/rms/rs/repository";
    private static final String SERVICE_REPOSITORY_ACCESSTOKEN_URL_TEMPLATE = "/rms/rs/repository/{repoId}/accessToken";
    private static final String SERVICE_REPOSITORY_AUTH_URL = "/rms/rs/repository/authURL";
    // send log
    private static final String SERVICE_SEND_LOG_URL = "/rms/rs/log/v2/activity";
    //get captcha
    private static final String SERVICE_Get_CAPTCHA = "/rms/rs/usr/captcha";
    //Reset user password
    private static final String SERVICE_Reset_user_password = "/rms/rs/usr/forgotPassword";
    // new share
    private static final String SERVICE_SHARE_LOCAL_FILE_URL = "/rms/rs/share/local";
    private static final String SERVICE_SHARE_REPOSITORY_FILE_URL = "/rms/rs/share/repository";
    private static final String SERVICE_SHARE_REMOVE_RECIPIENTS = "/rms/rs/share";
    private static final String SERVICE_SHARE_REVOKING_DOCUMENT = "/rms/rs/share/{duid}/revoke";
    private static final String SERVICE_SHARE_UPDATE_RECIPIENTS = "/rms/rs/share/{duid}/update";
    // myVault
    private static final String SERVICE_MYVAULT_UPLOAD_FILE = "/rms/rs/myVault/upload";
    private static final String SERVICE_MYVAULT_LIST_FILE = "/rms/rs/myVault?";
    private static final String SERVICE_MYVAULT_DOWNLOAD_FILE = "/rms/rs/myVault/v2/download";
    private static final String SERVICE_MYVAULT_FILE_METADATA = "/rms/rs/myVault/{duid}/metadata";
    private static final String SERVICE_MYVAULT_DELETE_FILE = "/rms/rs/myVault/{duid}/delete";
    //checkUpdateApp
    private static final String SERVICE_CHECKUPDATEAPP_URL = "https://play.google.com/store/apps/details?id=";
    // fetch activity log
    private static final String SERVICE_FETCH_LOG_URL = "/rms/rs/log/v2/activity/{DUID}?";

    // Project
    private static final String SERVICE_PROJECT_LIST_ALL_PROJECTS = "/rms/rs/project/allProjects";
    private static final String SERVICE_LIST_PROJECTS = "/rms/rs/project";
    private static final String SERVICE_GET_PROJECT_METADATA = "/rms/rs/project/{projectId}";
    private static final String SERVICE_CREATE_PROJECT = "/rms/rs/project";
    private static final String SERVICE_UPDATE_PROJECT = "/rms/rs/project/{projectId}";
    private static final String SERVICE_PROJECT_UPLOAD_FILE = "/rms/rs/project/{projectId}/upload";
    private static final String SERVICE_PROJECT_GET_TICKET = "/rms/rs/project/{projectId}/uploadTicket";
    private static final String SERVICE_PROJECT_UPLOAD_LARGE_FILE = "/rms/rs/project/{projectId}/uploadLargeFile/{routing_key}";
    private static final String SERVICE_PROJECT_FILE_LISTING = "/rms/rs/project/{projectId}/files?";
    private static final String SERVICE_PROJECT_CREATE_FOLDER = "/rms/rs/project/{projectId}/createFolder";
    private static final String SERVICE_PROJECT_DELETE_FILE_OR_FOLDER = "/rms/rs/project/{projectId}/delete";
    private static final String SERVICE_PROJECT_FILE_METADATA = "/rms/rs/project/{projectId}/file/metadata";
    private static final String SERVICE_PROJECT_DOWNLOAD_FILE = "/rms/rs/project/{projectId}/v2/download";
    private static final String SERVICE_PROJECT_INVITATION = "/rms/rs/project/{projectId}/invite";
    private static final String SERVICE_PROJECT_SEND_INVITATION_REMINDER = "/rms/rs/project/sendReminder";
    private static final String SERVICE_PROJECT_REVOKE_INVITATION = "/rms/rs/project/revokeInvite";
    private static final String SERVICE_PROJECT_ACCEPT_INVITATION = "/rms/rs/project/accept?id={invitation_id}&code={code}";
    private static final String SERVICE_PROJECT_DECLINE_INVITATION = "/rms/rs/project/decline";
    private static final String SERVICE_PROJECT_LIST_MEMBERS = "/rms/rs/project/{projectId}/members?";
    private static final String SERVICE_PROJECT_REMOVE_MEMBER = "/rms/rs/project/{projectId}/members/remove";
    private static final String SERVICE_PROJECT_GET_MEMBERSHIP = "/rms/rs/project/{projectId}/membership";
    private static final String SERVICE_PROJECT_MEMBER_DETAILS = "/rms/rs/project/{projectId}/member/{memberId}";
    private static final String SERVICE_PROJECT_LIST_PENDING_INVITATIONS_FOR_PROJECT = "/rms/rs/project/{projectId}/invitation/pending?";
    private static final String SERVICE_PROJECT_LIST_PENDING_INVITATIONS_FOR_USER = "/rms/rs/project/user/invitation/pending";
    private static final String SERVICE_PROJECT_FOLDER_METADATA = "/rms/rs/project/{projectId}/folderMetadata?";
    private static final String SERVICE_PROJECT_RECLASSIFY_FILE = "/rms/rs/project/{projectId}/file/classification";
    //end project

    // convert CAD
    private static final String SERVICE_CONVERT_CAD = "/rms/rs/convert/v2/file?fileName={filename}&toFormat=hsf";
    // remote view local
    private static final String SERVICE_REMOTE_VIEWER_LOCAL = "/rms/rs/remoteView/local";
    // remote view repository
    private static final String SERVICE_REMOTE_VIEWER_REPO = "/rms/rs/remoteView/repository";
    // remote view project
    private static final String SERVICE_REMOTE_VIEWER_PROJECT = "/rms/rs/remoteView/project";
    // change password
    private static final String SERVICE_CHANGE_PASSWORD = "/rms/rs/usr/changePassword";
    // SharedWithMe
    private static final String SERVICE_SHAREDWITHME_LIST = "/rms/rs/sharedWithMe/list?";
    private static final String SERVICE_SHAREDWITHME_RESHARE = "/rms/rs/sharedWithMe/reshare";
    private static final String SERVICE_SHAREDWITHME_DOWNLOAD = "/rms/rs/sharedWithMe/download";
    private static final String SERVICE_SHAREDWITHME_METADATA = "/rms/rs/sharedWithMe/metadata/{transactionId}/{transactionCode}";
    private static final String SERVICE_SHAREDWITHME_DECRYPT = "/rms/rs/sharedWithMe/decrypt";

    private static final String UPDATE_USER_PREFERENCE = "/rms/rs/usr/preference";
    private static final String GET_CLASSIFICATION_PROFILE = "/rms/rs/classification/{tokenGroupName}";
    private static final String PERFORM_POLICY_EVALUATION = "/rms/rs/policyEval";
    // tenant service
    private static final String GET_PROJECT_ADMIN = "/rms/rs/tenant/{tenant_id}/projectAdmin";
    private static final String GET_TENANT_PREFERENCES = "/rms/rs/tenant/v2/{tenant_id}";

    // WorkSpace service
    private static final String SERVICE_WORKSPACE_LIST_FILE = "/rms/rs/enterprisews/files?";
    private static final String SERVICE_WORKSPACE_UPLOAD_FILE = "/rms/rs/enterprisews/file";
    private static final String SERVICE_WORKSPACE_CREATE_FOLDER = "/rms/rs/enterprisews/createFolder";
    private static final String SERVICE_WORKSPACE_DELETE_ITEM = "/rms/rs/enterprisews/delete";
    private static final String SERVICE_WORKSPACE_GET_FILE_METADATA = "/rms/rs/enterprisews/file/metadata";
    private static final String SERVICE_WORKSPACE_GET_FOLDER_METADATA = "/rms/rs/enterprisews/folderMetadata?";
    private static final String SERVICE_WORKSPACE_DOWNLOAD_FILE = "/rms/rs/enterprisews/v2/download";
    private static final String SERVICE_WORKSPACE_RECLASSIFY_FILE = "/rms/rs/enterprisews/file/classification";

    private String rmsURL;
    private String tenantURL;
    private String loginURL;
    private String membershipURL;
    private String tokenURL;
    private String shareURL; // deprecated
    private String heartbeatV2URL;
    private String myDriveListURL;
    private String myDriveDeleteURL;
    private String myDriveCreateFolderURL;
    private String myDriveStorageUsedURL;
    private String myDriveCreatePublicShareURL;
    private String myDriveDownloadURL;
    private String myDriveUploadURL;
    private String myProfileUpdateURL;
    private String myProfileRetrieveURL;
    private String syncFavoriteURL;
    private String getAllReposFavoriteURL;
    private String getAllReposFavoriteListURL;
    private String getOneRepoFavoriteURL;
    private String repositoryURL;
    private String repositoryAuthURL;
    private String repositoryAccessTokenTemplateURL;
    private String sendLogURL;
    private String getCaptcha;
    private String sendCaptcha;
    private String shareLocalFileURL;
    private String shareRepoFileURL;

    // myVault
    private String myVaultUploadFileURL;
    private String myVaultListFileURL;
    private String myVaultDownloadFileURL;
    private String myVaultDeleteFileURL;
    private String myVaultFileMetaDataURL;

    private String removeRecipientsURL;
    private String revokingDocumentURL;
    private String updateRecipientURL;

    private String myCheckUpDateAppUrl;

    private String fetchLogUrl;

    // projects
    private String mListAllProjectsURL;
    private String mListProjectsURL;
    private String mGetProjectMetaDataURL;
    private String mCreateProjectURL;
    private String mUpdateProjectURL;
    private String mProjectUploadFileURL;
    private String mGetProjectTicketRUL;
    private String mProjectUploadLargeFileURL;
    private String mProjectFileListingURL;
    private String mProjectCreateFolderURL;
    private String mProjectDeleteFileOrFolderURL;
    private String mProjectGetFileMetaDataURL;
    private String mProjectDownloadFileURL;
    private String mProjectInvitationURL;
    private String mProjectSendInvitationReminder;
    private String mProjectRevokeInvitationURL;
    private String mProjectAcceptInvitationURL;
    private String mProjectDeclineInvitationURL;
    private String mProjectListMembersURL;
    private String mProjectRemoveMemberURL;
    private String mProjectGetMembershipURL;
    private String mProjectMemberDetailsURL;
    private String mListPendingInvitationsForAProject;
    private String mListPendingInvitationsForAUser;
    private String mProjectFolderMetaData;
    private String mProjectReClassifyFile;
    // end project

    private String convertCADURL;
    private String remoteViewLocalURL;
    private String remoteViewRepoURL;
    private String remoteViewProjectURL;
    private String changepasswordURL;
    private String updateUserPreferenceURL;

    // SharedWithMe
    private String sharedWithMeList;
    private String sharedWithMeReshare;
    private String sharedWithMeDownload;
    private String sharedWithMeMetadata;
    private String sharedWithMeDecrypt;

    //Get classification profile.
    private String classificationProfileURL;
    private String performPolicyEvaluation;

    private String getProjectAdmin;
    private String getTenantPreferences;

    private String mWorkSpaceListFileURL;
    private String mWorkSpaceUploadFileURL;
    private String mWorkSpaceCreateFolderURL;
    private String mWorkSpaceDeleteItemURL;
    private String mWorkSpaceFileMetadataURL;
    private String mWorkSpaceFolderMetadataURL;
    private String mWorkSpaceDownloadFileURL;
    private String mWorkSpaceReClassifyFileURL;

    public Config(String rmServer) {
        setRmsURL(rmServer);
        tenantURL = SERVICE_TENANT_URL;
        loginURL = SERVICE_LOGIN_URL;
        membershipURL = SERVICE_MEMBERSHIP_URL;
        tokenURL = SERVICE_TOKEN_URL;
        shareURL = SERVICE_SHARE_URL;
        // for heartbeat

        heartbeatV2URL = SERVICE_HEARTBEAT_V2;
        // for myDrive
        myDriveListURL = SERVICE_MYDRIVE_LIST;
        myDriveDeleteURL = SERVICE_MYDRIVE_DELETE;
        myDriveCreateFolderURL = SERVICE_MYDRIVE_CREATE_FOLDER;
        myDriveStorageUsedURL = SERVICE_MYDRIVE_STORAGE_USED;
        myDriveCreatePublicShareURL = SERVICE_MYDRIVE_CREATE_PUBLIC_SHARE;
        myDriveDownloadURL = SERVICE_MYDRIVE_DOWNLOAD;
        myDriveUploadURL = SERVICE_MYDRIVE_UPLOAD;
        //update user's profile
        myProfileUpdateURL = SERVICE_PROFILE_UPDATE_URL;
        //Retrieve user's profile
        myProfileRetrieveURL = SERVICE_PROFILE_RETRIEVE_URL;
        // fav new
        getAllReposFavoriteURL = SERVICE_ALL_REPOS_FAVORITE;
        getAllReposFavoriteListURL = SERVICE_ALL_REPOS_FAVORITE_LIST;
        getOneRepoFavoriteURL = SERVICE_ONE_REPO_FAVORITE;
        syncFavoriteURL = SERVICE_FAVORITE;

        // repository
        repositoryURL = SERVICE_REPOSITORY_URL;
        repositoryAccessTokenTemplateURL = SERVICE_REPOSITORY_ACCESSTOKEN_URL_TEMPLATE;
        repositoryAuthURL = SERVICE_REPOSITORY_AUTH_URL;
        // send log
        sendLogURL = SERVICE_SEND_LOG_URL;
        //get captcha
        getCaptcha = SERVICE_Get_CAPTCHA;
        sendCaptcha = SERVICE_Reset_user_password;
        // share local file
        shareLocalFileURL = SERVICE_SHARE_LOCAL_FILE_URL;
        // share repository file
        shareRepoFileURL = SERVICE_SHARE_REPOSITORY_FILE_URL;
        removeRecipientsURL = SERVICE_SHARE_REMOVE_RECIPIENTS;
        revokingDocumentURL = SERVICE_SHARE_REVOKING_DOCUMENT;
        updateRecipientURL = SERVICE_SHARE_UPDATE_RECIPIENTS;
        // myVault
        myVaultListFileURL = SERVICE_MYVAULT_LIST_FILE;
        myVaultUploadFileURL = SERVICE_MYVAULT_UPLOAD_FILE;
        myVaultDownloadFileURL = SERVICE_MYVAULT_DOWNLOAD_FILE;
        myVaultDeleteFileURL = SERVICE_MYVAULT_DELETE_FILE;
        myVaultFileMetaDataURL = SERVICE_MYVAULT_FILE_METADATA;

        myCheckUpDateAppUrl = SERVICE_CHECKUPDATEAPP_URL;
        fetchLogUrl = SERVICE_FETCH_LOG_URL;

        // project
        mListAllProjectsURL = SERVICE_PROJECT_LIST_ALL_PROJECTS;
        mListProjectsURL = SERVICE_LIST_PROJECTS;
        mGetProjectMetaDataURL = SERVICE_GET_PROJECT_METADATA;
        mCreateProjectURL = SERVICE_CREATE_PROJECT;
        mUpdateProjectURL = SERVICE_UPDATE_PROJECT;
        mProjectUploadFileURL = SERVICE_PROJECT_UPLOAD_FILE;
        mGetProjectTicketRUL = SERVICE_PROJECT_GET_TICKET;
        mProjectUploadLargeFileURL = SERVICE_PROJECT_UPLOAD_LARGE_FILE;
        mProjectFileListingURL = SERVICE_PROJECT_FILE_LISTING;
        mProjectCreateFolderURL = SERVICE_PROJECT_CREATE_FOLDER;
        mProjectDeleteFileOrFolderURL = SERVICE_PROJECT_DELETE_FILE_OR_FOLDER;
        mProjectGetFileMetaDataURL = SERVICE_PROJECT_FILE_METADATA;
        mProjectDownloadFileURL = SERVICE_PROJECT_DOWNLOAD_FILE;
        mProjectInvitationURL = SERVICE_PROJECT_INVITATION;
        mProjectSendInvitationReminder = SERVICE_PROJECT_SEND_INVITATION_REMINDER;
        mProjectRevokeInvitationURL = SERVICE_PROJECT_REVOKE_INVITATION;
        mProjectAcceptInvitationURL = SERVICE_PROJECT_ACCEPT_INVITATION;
        mProjectDeclineInvitationURL = SERVICE_PROJECT_DECLINE_INVITATION;
        mProjectListMembersURL = SERVICE_PROJECT_LIST_MEMBERS;
        mProjectRemoveMemberURL = SERVICE_PROJECT_REMOVE_MEMBER;
        mProjectGetMembershipURL = SERVICE_PROJECT_GET_MEMBERSHIP;
        mProjectMemberDetailsURL = SERVICE_PROJECT_MEMBER_DETAILS;
        mListPendingInvitationsForAProject = SERVICE_PROJECT_LIST_PENDING_INVITATIONS_FOR_PROJECT;
        mListPendingInvitationsForAUser = SERVICE_PROJECT_LIST_PENDING_INVITATIONS_FOR_USER;
        mProjectFolderMetaData = SERVICE_PROJECT_FOLDER_METADATA;
        mProjectReClassifyFile = SERVICE_PROJECT_RECLASSIFY_FILE;
        // end project

        convertCADURL = SERVICE_CONVERT_CAD;
        remoteViewLocalURL = SERVICE_REMOTE_VIEWER_LOCAL;
        remoteViewRepoURL = SERVICE_REMOTE_VIEWER_REPO;
        remoteViewProjectURL = SERVICE_REMOTE_VIEWER_PROJECT;
        changepasswordURL = SERVICE_CHANGE_PASSWORD;

        //SharedWithMe
        sharedWithMeList = SERVICE_SHAREDWITHME_LIST;
        sharedWithMeReshare = SERVICE_SHAREDWITHME_RESHARE;
        sharedWithMeDownload = SERVICE_SHAREDWITHME_DOWNLOAD;
        sharedWithMeMetadata = SERVICE_SHAREDWITHME_METADATA;
        sharedWithMeDecrypt = SERVICE_SHAREDWITHME_DECRYPT;

        updateUserPreferenceURL = UPDATE_USER_PREFERENCE;
        //Get classification profile
        classificationProfileURL = GET_CLASSIFICATION_PROFILE;
        performPolicyEvaluation = PERFORM_POLICY_EVALUATION;
        getProjectAdmin = GET_PROJECT_ADMIN;
        getTenantPreferences = GET_TENANT_PREFERENCES;

        mWorkSpaceListFileURL = SERVICE_WORKSPACE_LIST_FILE;
        mWorkSpaceUploadFileURL = SERVICE_WORKSPACE_UPLOAD_FILE;
        mWorkSpaceCreateFolderURL = SERVICE_WORKSPACE_CREATE_FOLDER;
        mWorkSpaceDeleteItemURL = SERVICE_WORKSPACE_DELETE_ITEM;
        mWorkSpaceFileMetadataURL = SERVICE_WORKSPACE_GET_FILE_METADATA;
        mWorkSpaceFolderMetadataURL = SERVICE_WORKSPACE_GET_FOLDER_METADATA;
        mWorkSpaceDownloadFileURL = SERVICE_WORKSPACE_DOWNLOAD_FILE;
        mWorkSpaceReClassifyFileURL = SERVICE_WORKSPACE_RECLASSIFY_FILE;
    }

    public void setRmsURL(@NonNull String rmsURL) {
        if (rmsURL.isEmpty()) {
            throw new RuntimeException("do not accept empty size of rms url");
        }
//        if (!rmsURL.startsWith("https://")) {
//            throw new RuntimeException("only https can be accepted");
//        }
        // rectify
        if (rmsURL.endsWith("/rms")) {
            rmsURL = rmsURL.substring(0, rmsURL.length() - 4);
        }
        this.rmsURL = rmsURL;
    }

    public String getRMSURL() {
        return this.rmsURL;
    }

    public String getMyCheckUpDateAppUrl() {
        return myCheckUpDateAppUrl;
    }

    public String getMyDriveListURL() {
        return rmsURL + myDriveListURL;
    }

    public String getMyDriveDeleteURL() {
        return rmsURL + myDriveDeleteURL;
    }

    public String getMyDriveCreateFolderURL() {
        return rmsURL + myDriveCreateFolderURL;
    }

    public String getMyDriveCreatePublicShareURL() {
        return rmsURL + myDriveCreatePublicShareURL;
    }

    public String getMyDriveDownloadURL() {
        return rmsURL + myDriveDownloadURL;
    }

    public String getMyDriveUploadURL() {
        return rmsURL + myDriveUploadURL;
    }

    public String getMyDriveStorageUsedURL() {
        return rmsURL + myDriveStorageUsedURL;
    }

    public String getTenantURL() {
        return rmsURL + tenantURL;
    }

    public String getLoginURL() {
        return rmsURL + loginURL;
    }

    public String getMembershipURL() {
        return rmsURL + membershipURL;
    }

    public String getTokenURL() {
        return rmsURL + tokenURL;
    }

    public String getShareURL() {
        return rmsURL + shareURL;
    }

    public String getHeartbeatV2URL() {
        return rmsURL + heartbeatV2URL;
    }

    public String getProfileUpdateURL() {
        return rmsURL + myProfileUpdateURL;
    }

    public String getProfileRetrieveUrl() {
        return rmsURL + myProfileRetrieveURL;
    }

    public String getSyncFavoriteURL(String rmsID) {
        if (rmsID == null || rmsID.isEmpty()) {
            return "";
        }
        String rt = rmsURL + syncFavoriteURL;
        rt = rt.replace("{repository_id}", rmsID);
        return rt;
    }

    public String getAllFavoriteOfflineFiles() {
        return rmsURL + getAllReposFavoriteURL;
    }

    public String getAllFavoriteFileList() {
        return rmsURL + getAllReposFavoriteListURL;
    }

    public String getOneRepoFavoriteURL() {
        return rmsURL + getOneRepoFavoriteURL;
    }

    public String getRepositoryURL() {
        return rmsURL + repositoryURL;
    }

    public String getRepositoryAuthUrl() {
        return rmsURL + repositoryAuthURL;
    }

    public String getRepositoryAccessTokenUrl(String repoID) {
        if (repoID == null || repoID.isEmpty()) {
            return "";
        }
        return rmsURL + repositoryAccessTokenTemplateURL.replace("{repoId}", repoID);
    }

    public String getSendLogURL() {
        return rmsURL + sendLogURL;
    }

    public String getCaptchaURL() {
        return rmsURL + getCaptcha;
    }

    public String getSendCaptchaURl() {
        return rmsURL + sendCaptcha;
    }

    public String getShareLocalFileURL() {
        return rmsURL + shareLocalFileURL;
    }

    public String getShareRepoFileURL() {
        return rmsURL + shareRepoFileURL;
    }

    public String getMyVaultDownloadFileURL() {
        return rmsURL + myVaultDownloadFileURL;
    }

    public String getMyVaultUploadFileURL() {
        return rmsURL + myVaultUploadFileURL;
    }

    public String getMyVaultListFileURL() {
        return rmsURL + myVaultListFileURL;
    }

    public String getMyVaultFileMetaDataURL() {
        return rmsURL + myVaultFileMetaDataURL;
    }

    public String getMyVaultDeleteFileURL() {
        return rmsURL + myVaultDeleteFileURL;
    }

    public String getRemoveRecipientsURL() {
        return rmsURL + removeRecipientsURL;
    }

    public String getUpdateRecipientURL() {
        return rmsURL + updateRecipientURL;
    }

    public String getRevokingDocumentURL() {
        return rmsURL + revokingDocumentURL;
    }

    public String getFetchLogURL() {
        return rmsURL + fetchLogUrl;
    }

    // project
    public String getListProjectsURL() {
        return rmsURL + mListProjectsURL;
    }

    public String getListAllProjectsURL() {
        return rmsURL + mListAllProjectsURL;
    }

    public String getProjectMetaDataRUL() {
        return rmsURL + mGetProjectMetaDataURL;
    }

    public String getCreateProjectURL() {
        return rmsURL + mCreateProjectURL;
    }

    public String getUpdateProjectURL() {
        return rmsURL + mUpdateProjectURL;
    }

    public String getProjectUploadNXLFileURL() {
        return rmsURL + mProjectUploadFileURL;
    }

    public String getProjectUploadNativeFileURL() {
        return rmsURL + mProjectUploadFileURL;
    }

    public String getProjectTicketRUL() {
        return rmsURL + mGetProjectTicketRUL;
    }

    public String getProjectUploadLargeNXLFileURL() {
        return rmsURL + mProjectUploadLargeFileURL;
    }

    public String getProjectUploadLargeNativeFileURL() {
        return rmsURL + mProjectUploadLargeFileURL;
    }

    public String getProjectFileListingURL() {
        return rmsURL + mProjectFileListingURL;
    }

    public String getProjectCreateFolderURL() {
        return rmsURL + mProjectCreateFolderURL;
    }

    public String getProjectDeleteFileOrFolderURL() {
        return rmsURL + mProjectDeleteFileOrFolderURL;
    }

    public String getProjectGetFileMetaDataURL(int id) {
        return rmsURL + mProjectGetFileMetaDataURL.replace("{projectId}", String.valueOf(id));
    }

    public String getProjectDownloadFileURL() {
        return rmsURL + mProjectDownloadFileURL;
    }

    public String getProjectInvitationURL() {
        return rmsURL + mProjectInvitationURL;
    }

    public String getProjectSendInvitationReminder() {
        return rmsURL + mProjectSendInvitationReminder;
    }

    public String getProjectRevokeInvitationURL() {
        return rmsURL + mProjectRevokeInvitationURL;
    }

    public String getProjectAcceptInvitationURL() {
        return rmsURL + mProjectAcceptInvitationURL;
    }

    public String getProjectDeclineInvitationURL() {
        return rmsURL + mProjectDeclineInvitationURL;
    }

    public String getProjectListMembersURL() {
        return rmsURL + mProjectListMembersURL;
    }

    public String getProjectRemoveMemberURL() {
        return rmsURL + mProjectRemoveMemberURL;
    }

    public String getProjectGetMembershipURL() {
        return rmsURL + mProjectGetMembershipURL;
    }

    public String getProjectMemberDetailsURL() {
        return rmsURL + mProjectMemberDetailsURL;
    }

    public String getListPendingInvitationsForAProject() {
        return rmsURL + mListPendingInvitationsForAProject;
    }

    public String getListPendingInvitationsForAUser() {
        return rmsURL + mListPendingInvitationsForAUser;
    }

    public String getProjectFolderMetaData() {
        return rmsURL + mProjectFolderMetaData;
    }

    public String getProjectReClassifyFileURL(int projectId) {
        return rmsURL + mProjectReClassifyFile.replace("{projectId}", String.valueOf(projectId));
    }
    //end project

    // convert CAD
    public String getConvertCADURL() {
        return rmsURL + convertCADURL;
    }

    // view remote local
    public String getRemoteViewLocalURL() {
        return rmsURL + remoteViewLocalURL;
    }

    // view remote repo
    public String getRemoteViewRepoURL() {
        return rmsURL + remoteViewRepoURL;
    }

    // view remote project
    public String getRemoteViewProjectURL() {
        return rmsURL + remoteViewProjectURL;
    }

    public String getChangePasswordURL() {
        return rmsURL + changepasswordURL;
    }

    // ShareWithMe service
    public String getSharedwithmeListURL() {
        return rmsURL + sharedWithMeList;
    }

    public String getSharedWithMeReshareURL() {
        return rmsURL + sharedWithMeReshare;
    }

    public String getSharedWithMeDownloadURL() {
        return rmsURL + sharedWithMeDownload;
    }

    public String getSharedWithMeMetadataURL(String transactionId, String transactionCode) {
        return rmsURL + sharedWithMeMetadata.replace("{transactionId}", transactionId)
                .replace("{transactionCode}", transactionCode);
    }

    public String getSharedWithMeDecryptURL() {
        return rmsURL + sharedWithMeDecrypt;
    }

    public String getUpdateUserPreferenceURL() {
        return rmsURL + updateUserPreferenceURL;
    }

    public String getClassificationProfileURL(String tenantId) {
        if (tenantId == null || tenantId.isEmpty()) {
            return "";
        }
        return rmsURL + classificationProfileURL.replace("{tokenGroupName}", tenantId);
    }

    public String getPerformPolicyEvaluationURL() {
        return rmsURL + performPolicyEvaluation;
    }

    public String getGetProjectAdminURL(String tenantId) {
        if (tenantId == null || tenantId.isEmpty()) {
            return "";
        }
        return rmsURL + getProjectAdmin.replace("{tenant_id}", tenantId);
    }

    public String getTenantPreferencesURL(String tenantId) {
        if (tenantId == null || tenantId.isEmpty()) {
            return "";
        }
        return rmsURL + getTenantPreferences.replace("{tenant_id}", tenantId);
    }

    //region WorkSpace section.
    public String getWorkSpaceListFileURL() {
        return rmsURL + mWorkSpaceListFileURL;
    }

    public String getWorkSpaceUploadFileURL() {
        return rmsURL + mWorkSpaceUploadFileURL;
    }

    public String getWorkSpaceCreateFolderURL() {
        return rmsURL + mWorkSpaceCreateFolderURL;
    }

    public String getWorkSpaceDeleteItemURL() {
        return rmsURL + mWorkSpaceDeleteItemURL;
    }

    public String getWorkSpaceFileMetadataURL() {
        return rmsURL + mWorkSpaceFileMetadataURL;
    }

    public String getWorkSpaceFolderMetadataURL() {
        return rmsURL + mWorkSpaceFolderMetadataURL;
    }

    public String getWorkSpaceDownloadFileURL() {
        return rmsURL + mWorkSpaceDownloadFileURL;
    }

    public String getWorkSpaceReClassifyFileURL() {
        return rmsURL + mWorkSpaceReClassifyFileURL;
    }
    //endregion
}
