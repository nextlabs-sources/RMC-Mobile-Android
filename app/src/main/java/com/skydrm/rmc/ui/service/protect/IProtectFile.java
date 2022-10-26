package com.skydrm.rmc.ui.service.protect;

import android.content.Context;

import com.skydrm.rmc.reposystem.exception.FileDownloadException;

public interface IProtectFile {
    String getName();

    void tryGetFile(Context ctx, ICallBack callBack);

    void release();

    interface ICallBack {
        void onPreDownload();

        void onDownloadFinished(String localPath);

        void onDownloadProgress(long value);

        void onDownloadFailed(FileDownloadException e);
    }

}
