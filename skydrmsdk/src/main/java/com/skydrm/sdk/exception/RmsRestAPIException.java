package com.skydrm.sdk.exception;


public class RmsRestAPIException extends Exception {
    private int rmsStatusCode;
    private ExceptionDomain domain;

    public RmsRestAPIException(String detailMessage) {
        super(detailMessage);
        rmsStatusCode = 0;
        domain = ExceptionDomain.Common;
    }

    public RmsRestAPIException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
        rmsStatusCode = 0;
        domain = ExceptionDomain.Common;
    }

    public RmsRestAPIException(String detailMessage, ExceptionDomain domain) {
        super(detailMessage);
        rmsStatusCode = 0;
        this.domain = domain;
    }

    public RmsRestAPIException(String detailMessage, ExceptionDomain domain, int statusCode) {
        super(detailMessage);
        this.rmsStatusCode = statusCode;
        this.domain = domain;
    }

    public RmsRestAPIException(Throwable throwable) {
        super(throwable);
        rmsStatusCode = 0;
        domain = ExceptionDomain.Common;
    }

    public ExceptionDomain getDomain() {
        return domain;
    }

    public int getRmsStatusCode() {
        return rmsStatusCode;
    }

    public enum ExceptionDomain {
        // for common
        Common,                   // ParamInvalid,IllegalOperation,
        // for Net work IO
        NetWorkIOFailed,        // wrapper Java.IOException
        // for file
        FileNotFound,
        FileIOFailed,           // Java.io.IOException

        NoSuchAlgorithm,        // Java.security.NoSuchAlgorithmException.

        ThreadInterrupted,      //ThreadInterrupted.

        // for RMS defined common
        MalformedRequest,        // 400  Malformed request
        AuthenticationFailed,   // 401  Authentication failed
        AccessDenied,            // 403  Access denied
        NotFound,                // 404
        InternalServerError,   // 500  Internal Server Error

        // for RMS each section expanded
        // rmUser
        InvalidPassword,        // 4001 Incorrect password
        TooManyAttemps,         // 4002 Too many attempts

        // myDrive
        UNVERIFIED_METADATA_FOR_DUID, // 4000 Unverified metadata for duid
        InvalidFolderName,       // 4001 Invalid Folder Name
        FileAlreadyExists,       // 4002 File already exists
        DriveStorageExceeded,    // 6001 Drive Storage Exceeded

        // share
        FileHasBeenRevoked,       // 4001, 304 File has been revoked.
        NoTransactionPerformed,  // 4002 No transaction has been performed. You need to share the document in order to update the recipients.
        CommentTooLong,           // 4007 Comment too long. (Maximum length for the comment is 250)

        // myVault
        InvalidNxlFormat,       // 5001 Invalid NXL format.
        InvalidRepoMetadata,   // 5002 Invalid repository metadata.
        InvalidFileName,       // 5003 Invalid filename.
        InvalidFileExtension,  // 5004 Invalid file extension.

        // project
        InvalidProject,                    // 400 invalid project(when the owner is removed from the project)
        InvalidProjectName,               // 4001 Project Name Too Long, 4003 Project Name containing illegal special characters.
        InvalidProjectDescription,       // 4002 Project Description Too Long.
        ProjectFileAlreadyExists,        //4006	 File already exists.

        OnlyOwnerRemoveMember,           // 5001 Only Project owner can remove a member.
        OwnerCannotBeRemoved,            // 5002 Project owner cannot be removed .
        ProjectNameAlreadyExist,         // 5005 Project name already exists.

        InvitationExpired,              // 4001 Invitation expired
        InvitationAlreadyDeclined,     // 4002 Invitation already declined
        EmailNotMatched,                // 4003 logged in email does not match with invitee email
        InvitationAlreadyAccepted,    // 4005 Invitation already accepted
        InvitationAlreadyRevoked,     // 4006 Invitation already revoked
        DeclineReasonTooLong,          // 4007 Decline reason too long
        UnknownError,
        JSONException,

        // Repository Service
        RepoAlreadyExist,                // 304  "Repository already exists"
        RepoNameNotValid,                // 400  "Repository name is not valid"
        RepoNotExist,                    // 404   "Repository does not exist"
        RepoNameCollided,                // 409  "There is already a repository with the given name"
        NameTooLong,                     // 4001 "Repository Name Too Long"
        NamingViolation,                  // 4003 "Repository Name containing illegal special characters"

        // remote viewer
        FileTypeNotSupported,          // 415   File type not supported
        InvalidNxlFile,                 // 5007  Invalid/corrupt NXL file
        MissingDependencies,            // 5008  Missing dependencies. Assembly files are not supported as of now

        //shared with me
        Missing_Request,                 //400	Missing request.
        Invalid_Transaction,
        File_Share_Deny,
        File_Revoked,

        //Get user profile
        MISSING_REQUIRED_PARAMETERS,//400 "Missing required parameters. (Missing category names or label names)"
        //4001 "Category/Label name Too Long." /4002 "Category/Label name contains illegal characters." /4003 "Duplicate Category/Label name."
        //4004 "A non multi-select category cannot have multiple default labels" 4005 "Category/Label limit exceeded."
        INVALID_LABEL,
        //{
        //	"statusCode": 4003,
        //	"message": "File is expired",
        //	"serverTime": 1531965813263
        //}
        FILE_EXPIRED,

        InvalidRequestParametersOrParametersMissing,
        UNVERIFIED_METADATA,
        MISSING_REQUIRED_PARAMS,
        NOPOLICY_TO_EVALUATE,
        UNSUPPORTED_WORKSPACE_UPLOAD_FILE
    }
}
