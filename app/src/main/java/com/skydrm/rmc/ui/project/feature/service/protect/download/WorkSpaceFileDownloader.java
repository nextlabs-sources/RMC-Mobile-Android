package com.skydrm.rmc.ui.project.feature.service.protect.download;

import android.content.Context;
import android.support.annotation.Nullable;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.skydrm.rmc.engine.DownloadManager;
import com.skydrm.rmc.reposystem.exception.FileDownloadException;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.ui.project.feature.service.protect.IFileDownloader;

import java.io.File;

@Deprecated
public class WorkSpaceFileDownloader implements IFileDownloader {
    private INxFile mClickFileItem;
    private ProgressBar mProgressBar;
    private TextView mProgressValue;

    public WorkSpaceFileDownloader(INxFile file, ProgressBar progressBar, TextView progressValue) {
        this.mClickFileItem = file;
        this.mProgressBar = progressBar;
        this.mProgressValue = progressValue;
    }

    @Override
    public void tryGetFile(Context ctx, final ICallBack callBack) {
        if (mClickFileItem == null || mProgressBar == null || mProgressValue == null) {
            return;
        }
        if (callBack != null) {
            callBack.onPreDownload();
        }
        File doc = DownloadManager.getInstance().tryGetFile(ctx, mClickFileItem,
                mProgressBar, mProgressValue, true, new DownloadManager.IDownloadCallBack() {
                    @Override
                    public void onDownloadFinished(boolean taskStatus, String localPath,
                                                   @Nullable FileDownloadException e) {
                        // remove the downloader
                        DownloadManager.getInstance().removeDownloader(mClickFileItem);
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
                        if (DownloadManager.getInstance().tryGetDownloader(mClickFileItem) != null
                                && DownloadManager.getInstance().tryGetDownloader(mClickFileItem).isbIsDownloading()) {
                            callBack.onDownloadProgress(value);
                        }
                    }
                });// parameter: true  --- need opitimized
        if (doc == null) {
            return;
        }
        if (callBack == null) {
            return;
        }
        callBack.onDownloadFinished(doc.getPath());
    }
}
