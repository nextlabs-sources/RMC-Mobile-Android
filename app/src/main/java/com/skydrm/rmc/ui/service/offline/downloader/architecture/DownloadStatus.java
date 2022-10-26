package com.skydrm.rmc.ui.service.offline.downloader.architecture;

import com.skydrm.rmc.ui.service.offline.downloader.ICallback;
import com.skydrm.rmc.ui.service.offline.downloader.exception.DownloadException;

public class DownloadStatus {
    public static final int STATUS_STARTED = 100;
    public static final int STATUS_PAUSED = 101;
    public static final int STATUS_CANCELED = 102;
    public static final int STATUS_PROGRESS = 103;
    public static final int STATUS_COMPLETED = 104;
    public static final int STATUS_FAILED = 105;

    private long time;
    private long length;
    private long finished;
    private int percent;
    private boolean acceptRanges;
    private volatile int status;
    private ICallback callback;
    private DownloadException exception;

    public long getLength() {
        return length;
    }

    public long getFinished() {
        return finished;
    }

    public int getPercent() {
        return percent;
    }

    public boolean isAcceptRanges() {
        return acceptRanges;
    }

    public DownloadException getException() {
        return exception;
    }

    public int getStatus() {
        return status;
    }

    public ICallback getCallback() {
        return callback;
    }

    public void setCallback(ICallback callback) {
        this.callback = callback;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public void setFinished(long finished) {
        this.finished = finished;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public void setException(DownloadException exception) {
        this.exception = exception;
    }
}
