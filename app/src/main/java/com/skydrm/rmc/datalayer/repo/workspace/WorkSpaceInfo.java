package com.skydrm.rmc.datalayer.repo.workspace;

public class WorkSpaceInfo {
    private long mUsage;
    private long mQuota;
    private int mTotalFiles;

    public WorkSpaceInfo(long usage, long quota, int totalFiles) {
        this.mUsage = usage;
        this.mQuota = quota;
        this.mTotalFiles = totalFiles;
    }

    public long getUsage() {
        return mUsage;
    }

    public long getQuota() {
        return mQuota;
    }

    public int getTotalFiles() {
        return mTotalFiles;
    }
}
