package com.skydrm.rmc.ui.service.offline.downloader.core;

import com.skydrm.rmc.ui.service.offline.downloader.ICallback;
import com.skydrm.rmc.ui.service.offline.downloader.architecture.DownloadStatus;
import com.skydrm.rmc.ui.service.offline.downloader.architecture.IDownloadResponse;
import com.skydrm.rmc.ui.service.offline.downloader.architecture.INotifySystem;
import com.skydrm.rmc.ui.service.offline.downloader.exception.DownloadException;

public class DownloadResponse implements IDownloadResponse {
    private INotifySystem mNotifySystem;
    private DownloadStatus mDownloadStatus;

    public DownloadResponse(INotifySystem notifySystem, ICallback callback) {
        this.mNotifySystem = notifySystem;
        this.mDownloadStatus = new DownloadStatus();
        mDownloadStatus.setCallback(callback);
    }

    @Override
    public void onDownloadStart() {
        mDownloadStatus.setStatus(DownloadStatus.STATUS_STARTED);
        mNotifySystem.post(mDownloadStatus);
    }

    @Override
    public void onDownloadProgress(long done, long total, int percent) {
        mDownloadStatus.setStatus(DownloadStatus.STATUS_PROGRESS);
        mDownloadStatus.setFinished(done);
        mDownloadStatus.setLength(total);
        mDownloadStatus.setPercent(percent);
        mNotifySystem.post(mDownloadStatus);
    }

    @Override
    public void onDownloadPaused() {
        mDownloadStatus.setStatus(DownloadStatus.STATUS_PAUSED);
        mNotifySystem.post(mDownloadStatus);
    }

    @Override
    public void onDownloadCanceled() {
        mDownloadStatus.setStatus(DownloadStatus.STATUS_CANCELED);
        mNotifySystem.post(mDownloadStatus);
    }

    @Override
    public void onDownloadCompleted() {
        mDownloadStatus.setStatus(DownloadStatus.STATUS_COMPLETED);
        mNotifySystem.post(mDownloadStatus);
    }

    @Override
    public void onDownloadFailed(DownloadException e) {
        mDownloadStatus.setStatus(DownloadStatus.STATUS_FAILED);
        mDownloadStatus.setException(e);
        mNotifySystem.post(mDownloadStatus);
    }
}
