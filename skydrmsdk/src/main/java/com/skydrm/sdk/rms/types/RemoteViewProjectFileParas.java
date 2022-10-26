package com.skydrm.sdk.rms.types;

/**
 * Created by aning on 7/3/2017.
 */

public class RemoteViewProjectFileParas {
    // project id
    private int projectId;
    // file path id
    private String pathId;
    // file path display name
    private String pathDisplay;
    // the time difference between UTC time and local time, in minutes. Used for generating timestamp for watermarks.
    // if you don't fill anything, watermark will be based on server time which is UTC-0
    private int offset;
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
     *  @param projectId project id
     *  @param pathId path id
     *  @param pathDisplay path display
     *  @param offset the time difference between UTC time and local time, in minutes. Used for generating timestamp for watermarks.
     *  @param email user email
     *  @param tenantName tenant name
     *  @param lastModifiedDate last modified
     *  @param operation {@link OperationType}
     *          clients can either use operations from RMS or implement their own(pass this para 0, if don't fill this para, ideally no buttons will be displayed)
     */
    public RemoteViewProjectFileParas(int projectId,
                                   String pathId,
                                   String pathDisplay,
                                   int offset,
                                   String email,
                                   String tenantName,
                                   long lastModifiedDate,
                                   int operation) {
        this.projectId = projectId;
        this.pathId = pathId;
        this.pathDisplay = pathDisplay;
        this.offset = offset;
        this.email = email;
        this.tenantName = tenantName;
        this.lastModifiedDate = lastModifiedDate;
        this.operation = operation;
    }

    /**
     *  @param projectId project id
     *  @param pathId path id
     *  @param pathDisplay path display
     *  @param email user email
     *  @param tenantName tenant name
     *  @param lastModifiedDate last modified
     */
    public RemoteViewProjectFileParas(int projectId,
                                      String pathId,
                                      String pathDisplay,
                                      String email,
                                      String tenantName,
                                      long lastModifiedDate) {
        this.projectId = projectId;
        this.pathId = pathId;
        this.pathDisplay = pathDisplay;
        // default is 0
        this.offset = 0;
        this.email = email;
        this.tenantName = tenantName;
        this.lastModifiedDate = lastModifiedDate;
        // set 0 means there is no buttons that from rms will be displayed
        this.operation = 0;
    }

    public int getProjectId() {
        return projectId;
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


