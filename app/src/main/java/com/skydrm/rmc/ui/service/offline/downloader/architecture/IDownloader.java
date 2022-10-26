package com.skydrm.rmc.ui.service.offline.downloader.architecture;

public interface IDownloader {
    interface IDownloaderDestroyListener {
        void onDestroyed(String tag, IDownloader downloader);
    }

    boolean isRunning();

    void start();

    void pause();

    void cancel();

    void destroy();
}
