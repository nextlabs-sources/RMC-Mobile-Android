package com.skydrm.sdk.rms.types;

/**
 * Created by aning on 7/3/2017.
 */

public class RemoteViewRepoFileParas {
    // repository id
    private String repoId;
    // file path id
    private String pathId;
    // file path display name
    private String pathDisplay;
    // the time difference between UTC time and local time, in minutes. Used for generating timestamp for watermarks.
    // if you don't fill anything, watermark will be based on server time which is UTC-0
    private int offset;
    // repository name
    private String repoName;
    // repository type
    private String repoType;
    // user email
    private String email;
    // tenant name
    private String tenantName;
    // last modified date
    private long lastModifiedDate;
    // supported operations: View File Info 1; Print 2; Protect 4; Share 8; Download 16.
    // clients can either use operations from RMS or implement their own(pass this para 0, if don't fill this para, ideally no buttons will be displayed)
    private int operation;

    /**
     *  @param repoId repo id
     *  @param pathId path id
     *  @param pathDisplay path display
     *  @param offset the time difference between UTC time and local time, in minutes. Used for generating timestamp for watermarks.
     *  @param repoName repo name
     *  @param repoType repo type
     *  @param email user email
     *  @param tenantName tenant name
     *  @param lastModifiedDate last modified
     *  @param operation {@link OperationType}
     *          clients can either use operations from RMS or implement their own(pass this para 0, if don't fill this para, ideally no buttons will be displayed)
     */
    public RemoteViewRepoFileParas(String repoId,
                                   String pathId,
                                   String pathDisplay,
                                   int offset,
                                   String repoName,
                                   String repoType,
                                   String email,
                                   String tenantName,
                                   long lastModifiedDate,
                                   int operation) {
        this.repoId = repoId;
        this.pathId = pathId;
        this.pathDisplay = pathDisplay;
        this.offset = offset;
        this.repoName = repoName;
        this.repoType = repoType;
        this.email = email;
        this.tenantName = tenantName;
        this.lastModifiedDate = lastModifiedDate;
        this.operation = operation;
    }

    /**
     *  @param repoId repo id
     *  @param pathId path id
     *  @param pathDisplay path display
     *  @param repoName repo name
     *  @param repoType repo type
     *  @param email user email
     *  @param tenantName tenant name
     *  @param lastModifiedDate last modified
     */
    public RemoteViewRepoFileParas(String repoId,
                                   String pathId,
                                   String pathDisplay,
                                   String repoName,
                                   String repoType,
                                   String email,
                                   String tenantName,
                                   long lastModifiedDate) {
        this.repoId = repoId;
        this.pathId = pathId;
        this.pathDisplay = pathDisplay;
        // default is 0
        this.offset = 0;
        this.repoName = repoName;
        this.repoType = repoType;
        this.email = email;
        this.tenantName = tenantName;
        this.lastModifiedDate = lastModifiedDate;
        // set 0 means there is no buttons that from rms will be displayed
        this.operation = 0;
    }

    public String getRepoId() {
        return repoId;
    }

    public String getPathId() {
        return pathId;
    }

    public String getPathDisplay() {
        return pathDisplay;
    }

    public int getOffset() {
        return offset;
    }

    public String getRepoName() {
        return repoName;
    }

    public String getRepoType() {
        return repoType;
    }

    public String getEmail() {
        return email;
    }

    public String getTenantName() {
        return tenantName;
    }

    public long getLastModifiedDate() {
        return lastModifiedDate;
    }

    public int getOperation() {
        return operation;
    }

    public static class OperationType {
        public static final int OPERATION_VIEW_FILE_INFO = 1;
        public static final int OPERATION_PRINT = 2;
        public static final int OPERATION_PROTECT = 4;
        public static final int OPERATION_SHARE = 8;
        public static final int OPERATION_DOWNLOAD = 16;
    }
}


