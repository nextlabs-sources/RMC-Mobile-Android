package com.skydrm.rmc.ui.service.offline.downloader.architecture;

import com.skydrm.rmc.ui.service.offline.downloader.exception.DownloadException;

public interface IDownloadResponse {
    void onDownloadStart();

    void onDownloadProgress(long done, long total, int percent);

    void onDownloadPaused();

    void onDownloadCanceled();

    void onDownloadCompleted();

    void onDownloadFailed(DownloadException e);
}
