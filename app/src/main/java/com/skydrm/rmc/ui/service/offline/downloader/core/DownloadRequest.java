package com.skydrm.rmc.ui.service.offline.downloader.core;

public class DownloadRequest {
    private String url;
    private String localPath;
    private long start;
    private long length;
    private String pathId;
    private int type;
    private String transactionId;
    private String transactionCode;
    private String spaceId;

    private DownloadRequest(Builder builder) {
        this.url = builder.url;
        this.localPath = builder.localPath;
        this.start = builder.start;
        this.length = builder.length;
        this.pathId = builder.pathId;
        this.type = builder.type;
        this.transactionId = builder.transactionId;
        this.transactionCode = builder.transactionCode;
        this.spaceId = builder.spaceId;
    }

    public String getUrl() {
        return url;
    }

    public long getStart() {
        return start;
    }

    public long getLength() {
        return length;
    }

    public String getPathId() {
        return pathId;
    }

    public String getLocalPath() {
        return localPath;
    }

    public int getType() {
        return type;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public static class Builder {
        private String url;
        private long start;
        private long length;
        private String pathId;
        private int type;
        private String localPath;
        private String transactionId;
        private String transactionCode;
        private String spaceId;

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setStart(long start) {
            this.start = start;
            return this;
        }

        public Builder setLength(long length) {
            this.length = length;
            return this;
        }

        public Builder setPathId(String pathId) {
            this.pathId = pathId;
            return this;
        }

        public Builder setType(int type) {
            this.type = type;
            return this;
        }

        public Builder setLocalPath(String localPath) {
            this.localPath = localPath;
            return this;
        }

        public Builder setTransactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public Builder setTransactionCode(String transactionCode) {
            this.transactionCode = transactionCode;
            return this;
        }

        public Builder setSpaceId(String spaceId) {
            this.spaceId = spaceId;
            return this;
        }

        public DownloadRequest build() {
            return new DownloadRequest(this);
        }
    }
}
