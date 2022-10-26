package com.skydrm.sdk.rms.rest.myVault;

import java.io.File;

/**
 * Created by hhu on 4/27/2018.
 */

public class MyVaultUploadFileParams {
    File nxlFile;
    String srcPathId;
    String srcPathDisplay;
    String srcRepoId;
    String srcRepoName;
    String srcRepoType;


    private MyVaultUploadFileParams(Builder builder) {
        this.nxlFile = builder.nxlFile;
        this.srcPathId = builder.srcPathId;
        this.srcPathDisplay = builder.srcPathDisplay;
        this.srcRepoId = builder.srcRepoId;
        this.srcRepoName = builder.srcRepoName;
        this.srcRepoType = builder.srcRepoType;
    }

    public File getNxlFile() {
        return nxlFile;
    }

    public String getSrcPathId() {
        return srcPathId;
    }

    public String getSrcPathDisplay() {
        return srcPathDisplay;
    }

    public String getSrcRepoId() {
        return srcRepoId;
    }

    public String getSrcRepoName() {
        return srcRepoName;
    }

    public String getSrcRepoType() {
        return srcRepoType;
    }

    @Override
    public String toString() {
        return "MyVaultUploadFileParams\n" +
                "{nxlFile=" + nxlFile.getPath()
                + ",srcPathId=" + srcPathId
                + ",srcPathDisplay=" + srcPathDisplay
                + ",srcRepoId=" + srcRepoId
                + ",srcRepoName=" + srcRepoName
                + ",srcRepoType=" + srcRepoType + "}";
    }

    public static class Builder {
        private File nxlFile;
        private String srcPathId;
        private String srcPathDisplay;
        private String srcRepoId;
        private String srcRepoName;
        private String srcRepoType;

        public Builder setNxlFile(File nxlFile) {
            this.nxlFile = nxlFile;
            return this;
        }

        public Builder setSrcPathId(String srcPathId) {
            this.srcPathId = srcPathId;
            return this;
        }

        public Builder setSrcPathDisplay(String srcPathDisplay) {
            this.srcPathDisplay = srcPathDisplay;
            return this;
        }

        public Builder setSrcRepoId(String srcRepoId) {
            this.srcRepoId = srcRepoId;
            return this;
        }

        public Builder setSrcRepoName(String srcRepoName) {
            this.srcRepoName = srcRepoName;
            return this;
        }

        public Builder setSrcRepoType(String srcRepoType) {
            this.srcRepoType = srcRepoType;
            return this;
        }

        public MyVaultUploadFileParams build() {
            return new MyVaultUploadFileParams(this);
        }
    }
}
