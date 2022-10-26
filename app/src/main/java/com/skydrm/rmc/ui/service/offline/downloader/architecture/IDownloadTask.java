package com.skydrm.rmc.ui.service.offline.downloader.architecture;

import com.skydrm.rmc.ui.service.offline.downloader.exception.DownloadException;

public interface IDownloadTask extends Runnable {
    interface IDownloadListener {
        void onDownloadProgress(long done, long total);

        void onDownloadPaused();

        void onDownloadCanceled();

        void onDownloadCompleted();

        void onDownloadFailed(DownloadException e);
    }

    void pause();

    void cancel();

    boolean isPaused();

    boolean isCanceled();

    boolean isDownloading();

    boolean isComplete();

    boolean isFailed();

    @Override
    void run();
}
