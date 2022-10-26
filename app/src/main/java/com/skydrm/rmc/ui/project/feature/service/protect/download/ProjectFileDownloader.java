package com.skydrm.rmc.ui.project.feature.service.protect.download;

import android.content.Context;
import android.support.annotation.Nullable;

import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.engine.DownloadManager;
import com.skydrm.rmc.engine.FileOperation;
import com.skydrm.rmc.reposystem.exception.FileDownloadException;
import com.skydrm.rmc.ui.project.feature.service.protect.IFileDownloader;

public class ProjectFileDownloader implements IFileDownloader {
    private INxlFile mFile;

    public ProjectFileDownloader(INxlFile f) {
        this.mFile = f;
    }

    @Override
    public void tryGetFile(Context ctx, final ICallBack callBack) {
        if (mFile == null) {
            return;
        }
        if (callBack != null) {
            callBack.onPreDownload();
        }
        FileOperation.downloadNxlFile(ctx, 1, mFile, new DownloadManager.IDownloadCallBack() {
            @Override
            public void onDownloadFinished(boolean taskStatus, String localPath, @Nullable FileDownloadException e) {
                if (callBack == null) {
                    return;
                }
                //Download success.
                if (taskStatus) {
                    callBack.onDownloadFinished(localPath);
                } else {
                    callBack.onDownloadFailed(e);
                }
            }

            @Override
            public void onDownloadProgress(long value) {
                if (callBack == null) {
                    return;
                }
                callBack.onDownloadProgress(value);
            }
        });
    }
}
