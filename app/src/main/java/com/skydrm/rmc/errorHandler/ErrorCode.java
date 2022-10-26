package com.skydrm.rmc.errorHandler;

public class ErrorCode {


    public final static int REPO_ONE_DRIVE_INIT_FAILED = 26;



    public static int SHARE_POINT_UPLOAD_REQUEST_ERROR = -1;
    public static int SHARE_POINT_ONLINE_UPLOAD_REQUEST_ERROR = -1;


    // Error for Runtime
    public static final String E_RT_PARAM_INVALID = "Error, invalid param.";
    public static final String E_RT_PARAM_CALLBACK_INVALID = "Error, invalid callback param.";
    public static final String E_RT_PARAM_DOC_INVALID = "Error, invalid document param.";
    public static final String E_RT_PARAM_SERVICE_INVALID = "Error, invalid service param.";

    public static final String E_RT_SHOULD_NEVER_REACH_HERE = "Error, should never reach here.";

    // Error for file system
    public static final String E_FS_INSTALL_REPO = "Error, can not install repo.";
    public static final String E_FS_MOUNTPOINT_INVALID = "Error,invalid mount point.";
    public static final String E_FS_PARTIAL_DOWNLOAD_INVALID_PARAS = "Error, invalid parameters";

    // Error for Nxl format
    public static final String E_NXLF_PARAM_FOLDER_REQUIRED = "Error, param required a folder.";

    // Error for Repository
    public static final String E_REPO_NO_REPOS = "Error, no repositories.";
    public static final String E_REPO_NULL_LINKED_SERVICE = "Error, null linked service.";
    public static final String E_REPO_CANNOT_FIND_LOCAL_REPO = "Error, can not find the local repo.";
    // Error for UI
    // Error for IO
    // Error for Network
    public static final String E_IO_NO_NETWORK = "Error, no network.";

}
