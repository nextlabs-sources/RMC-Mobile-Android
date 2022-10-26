package com.skydrm.sdk.policy;

import com.skydrm.sdk.nxl.FileInfo;

/**
 * Policy can contain two components, AdHoc & Central policy
 * but currently we only support one each time, either AdHoc or Central
 */
public class Policy {
    private AdhocPolicy adhocPolicy = null;
    private CentralPolicy centralPolicy = null;
    private FileInfo fileInfo = null;

    public Policy(AdhocPolicy adhocPolicy) {
        this.adhocPolicy = adhocPolicy;
    }

    public Policy(CentralPolicy centralPolicy) {
        this.centralPolicy = centralPolicy;
    }

    public boolean hasAdhocPolicy() {
        return adhocPolicy != null;
    }

    public boolean hasCentralPolicy() {
        return this.centralPolicy != null;
    }

    public boolean hasFileInfo() {
        return this.fileInfo != null;
    }

    public AdhocPolicy getAdhocPolicy() {
        return adhocPolicy;
    }

    public void setAdhocPolicy(AdhocPolicy adhocPolicy) {
        this.adhocPolicy = adhocPolicy;
    }

    public CentralPolicy getCentralPolicy() {
        return centralPolicy;
    }

    public void setCentralPolicy(CentralPolicy centralPolicy) {
        this.centralPolicy = centralPolicy;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }
}
