package com.skydrm.rmc.ui.service.offline.downloader.config;

public class DownloadInfo {
    private String url;
    private long start;
    private long length;
    private String pathId;
    private boolean forViewer;
    private long finished;
    private String localPath;
    private int type;
    private String transactionId;
    private String transactionCode;
    private String spaceId;

    public DownloadInfo(String url, String localPath, long start, long length, String pathId, int type) {
        this.url = url;
        this.localPath = localPath;
        this.start = start;
        this.length = length;
        this.pathId = pathId;
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getFinished() {
        return finished;
    }

    public void setFinished(long finished) {
        this.finished = finished;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public String getPathId() {
        return pathId;
    }

    public void setPathId(String pathId) {
        this.pathId = pathId;
    }

    public boolean isForViewer() {
        return forViewer;
    }

    public void setForViewer(boolean forViewer) {
        this.forViewer = forViewer;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }
}
