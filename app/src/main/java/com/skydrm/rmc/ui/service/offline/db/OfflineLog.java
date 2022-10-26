package com.skydrm.rmc.ui.service.offline.db;

import com.skydrm.rmc.database.table.log.ActivityLogBean;

public class OfflineLog {
    private String duid;
    private int operationId;
    private int deviceType;
    private String fileName;
    private String filePath;
    private int accessResult;
    private long accessTime;
    private String activityData;//""

    public String getDuid() {
        return duid;
    }

    public void setDuid(String duid) {
        this.duid = duid;
    }

    public int getOperationId() {
        return operationId;
    }

    public void setOperationId(int operationId) {
        this.operationId = operationId;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getAccessResult() {
        return accessResult;
    }

    public void setAccessResult(int accessResult) {
        this.accessResult = accessResult;
    }

    public long getAccessTime() {
        return accessTime;
    }

    public void setAccessTime(long accessTime) {
        this.accessTime = accessTime;
    }

    public String getActivityData() {
        return activityData;
    }

    public void setActivityData(String activityData) {
        this.activityData = activityData;
    }

    public static OfflineLog newByDBItem(ActivityLogBean i) {
        OfflineLog ret = new OfflineLog();
        if (i == null) {
            return ret;
        }
        ret.duid = i.duid;
        ret.operationId = i.operationId;
        ret.deviceType = i.deviceType;
        ret.fileName = i.fileName;
        ret.filePath = i.filePath;
        ret.accessResult = i.accessResult;
        ret.accessTime = i.accessTime;
        ret.activityData = i.activityData;
        return ret;
    }
}
