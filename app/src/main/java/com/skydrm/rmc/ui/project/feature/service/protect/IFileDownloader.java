package com.skydrm.rmc.ui.project.feature.service.protect;

import android.content.Context;

import com.skydrm.rmc.reposystem.exception.FileDownloadException;

@Deprecated
public interface IFileDownloader {
    void tryGetFile(Context ctx, ICallBack callBack);

    interface ICallBack {
        void onPreDownload();

        void onDownloadFinished(String localPath);

        void onDownloadProgress(long value);

        void onDownloadFailed(FileDownloadException e);
    }
}
