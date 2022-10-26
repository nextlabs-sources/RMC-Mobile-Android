package com.skydrm.rmc.ui.project.feature.service.protect.download;

import android.content.Context;

import com.skydrm.rmc.reposystem.exception.FileDownloadException;
import com.skydrm.rmc.ui.project.feature.service.protect.IFileDownloader;

import java.io.File;

@Deprecated
public class LibraryFileDownloader implements IFileDownloader {
    private String mFilePath;

    public LibraryFileDownloader(String filePath) {
        this.mFilePath = filePath;
    }

    @Override
    public void tryGetFile(Context ctx, ICallBack callBack) {
        if (mFilePath == null || mFilePath.isEmpty()) {
            if (callBack != null) {
                callBack.onDownloadFailed(new FileDownloadException("Unknown error."));
            }
        } else {
            File f = new File(mFilePath);
            if (!f.exists()) {
                if (callBack != null) {
                    callBack.onDownloadFailed(new FileDownloadException("Target file doesn't exists."));
                }
                return;
            }
            if (f.isDirectory()) {
                if (callBack != null) {
                    callBack.onDownloadFailed(new FileDownloadException("Target file is a dir."));
                }
                return;
            }
            if (callBack != null) {
                callBack.onDownloadFinished(mFilePath);
            }
        }
    }
}
