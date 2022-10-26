package com.skydrm.rmc.ui.service.offline.downloader;

import com.skydrm.rmc.ui.service.offline.downloader.exception.DownloadException;

public interface ICallback {
    /**
     * The callback  publish a file download progress
     *
     * @param finished current downloaded file size
     * @param length   file size
     * @param percent  finished/length which can be used in progress bar.
     */
    void onDownloadProgress(long finished, long length, int percent);

    /**
     * The callback download task has been paused by user.
     */
    void onDownloadPaused();

    /**
     * The callback download task has been canceled by user.
     */
    void onDownloadCanceled();

    /**
     * The callback represents file has been downloaded.
     */
    void onDownloadComplete();

    /**
     * The callback of exception happens during download task has been lunched.
     *
     * @param e exception
     */
    void onFailed(DownloadException e);
}
