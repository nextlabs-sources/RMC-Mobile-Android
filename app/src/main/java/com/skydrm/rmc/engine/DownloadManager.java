package com.skydrm.rmc.engine;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.reposystem.IRemoteRepo;
import com.skydrm.rmc.reposystem.exception.FileDownloadException;
import com.skydrm.rmc.reposystem.remoterepo.ICancelable;
import com.skydrm.rmc.reposystem.types.INxFile;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by aning on 12/8/2016.
 */
@Deprecated
public class DownloadManager {
    private static final DevLog log = new DevLog(DownloadManager.class.getSimpleName());
    private static DownloadManager ourInstance = new DownloadManager();
    // String --- localPath, ConcurrentHashMap -- is thread security.
    private ConcurrentHashMap<String, Downloader> mDownloadMap = new ConcurrentHashMap<>();
    // used to hold different view download callback when multiple view(progress bar) use share one downloader
    // vector ---  is thread security.
    private Vector<IDownloadCallBack> mVector = new Vector<>();

    private DownloadManager() {
    }

    public static DownloadManager getInstance() {
        return ourInstance;
    }

    public ConcurrentHashMap<String, Downloader> getDownloadMap() {
        return mDownloadMap;
    }

    /**
     * @param clickItemFile  -- INxFile
     * @param progressBar
     * @param textView       -- display the percentage of progress, will use it in view page
     * @param bIsViewPage    -- flag in view page
     * @param downloadFinish
     */
    @Deprecated
    public void downloadFile(Context context, INxFile clickItemFile, ProgressBar progressBar, TextView textView, boolean bIsViewPage, IDownloadCallBack downloadFinish) {
        // look at if the given file is loading
        Downloader downloader = tryGetDownloader(clickItemFile);
        if (downloader == null) {
            downloader = new Downloader(progressBar, textView, bIsViewPage, downloadFinish);
            mDownloadMap.put(clickItemFile.getLocalPath(), downloader);
        } else {
            downloader.mProgressBar.get().setVisibility(View.VISIBLE);
            downloader.mProgressBar.get().setProgress(downloader.mProgressValue);
        }

        try {
            SkyDRMApp.getInstance().getRepoSystem().getFile(clickItemFile, downloader);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "get file exception!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * used to get file in the case that don't know the file if in local cache.
     * note: to judge by the function isCached() of INxlFile will error sometimes, so can use this function to judge(look at the return value if is null)
     */
    public File tryGetFile(Context context, INxFile clickItemFile, ProgressBar progressBar, TextView textView, boolean bIsViewPage, IDownloadCallBack downloadCallBack) {
        WeakReference<Context> mContext = new WeakReference<>(context);
        File document = null;
        // look at if the given file is loading
        Downloader downloader = tryGetDownloader(clickItemFile);
        log.e("tryGetFile: =========");
        if (downloader == null) {
            downloader = new Downloader(progressBar, textView, bIsViewPage, downloadCallBack);
            mDownloadMap.put(clickItemFile.getLocalPath(), downloader);
        } else { // is downloading
            if (downloader.mProgressBar.get() != null) {
                downloader.mProgressBar.get().setVisibility(View.VISIBLE);
                downloader.mProgressBar.get().setProgress(downloader.mProgressValue);
                downloader.bIsDownloading = true;
            }
            mVector.add(downloadCallBack);
            downloader.mTextView = new WeakReference<>(textView);
            return null;
        }

        try {
            document = SkyDRMApp.getInstance().getRepoSystem().getFile(clickItemFile, downloader);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext.get(),mContext.get().getString(R.string.hint_msg_download_file_failed), Toast.LENGTH_SHORT).show();
        }

        // have cached in local, so not need to download
        if (document != null) {
            removeDownloader(clickItemFile);
        }

        return document;
    }

    /**
     * will directly get the downloader if the file is downloading.
     */
    public Downloader tryGetDownloader(INxFile clickItemFile) {
        Iterator iterator = mDownloadMap.keySet().iterator();
        while (iterator.hasNext()) {
            String localPath = (String) iterator.next();
            if (!TextUtils.isEmpty(localPath) && clickItemFile.getLocalPath().equals(localPath)) {
                return mDownloadMap.get(localPath);
            }
        }
        return null;
    }

    /**
     * we should delete the corresponding downloader after finished download or the file in local cache.
     */
    public void removeDownloader(INxFile clickItemFile) {
        Iterator iterator = mDownloadMap.keySet().iterator();
        String localPath = null;
        while (iterator.hasNext()) {
            String tmpPath = (String) iterator.next();
            if (!TextUtils.isEmpty(tmpPath) && clickItemFile.getLocalPath().equals(tmpPath)) {
                localPath = tmpPath;
                break;
            }
        }

        if (localPath != null) {
            iterator.remove();
            mDownloadMap.remove(localPath);
        }
    }

    public interface IDownloadCallBack {
        void onDownloadFinished(boolean taskStatus, String localPath, @Nullable FileDownloadException e);

        void onDownloadProgress(long value);
    }

    public class Downloader implements IRemoteRepo.IDownLoadCallback {
        private WeakReference<ProgressBar> mProgressBar;
        private WeakReference<TextView> mTextView;
        private boolean bIsViewPage = false;
        private boolean bIsDownloading = false;
        private int mProgressValue;

        private ICancelable mDownLoadCancelHandler;
        private IDownloadCallBack mDownloadCallback;

        private Downloader(ProgressBar progressBar, TextView textView, boolean bIsViewPage, IDownloadCallBack downloadFinish) {
            mProgressBar = new WeakReference<>(progressBar);
            mTextView = new WeakReference<>(textView);
            this.bIsViewPage = bIsViewPage;
            mDownloadCallback = downloadFinish;

            mProgressBar.get().setProgress(0);
        }

        public ICancelable getDownLoadCancelHandler() {
            return mDownLoadCancelHandler;
        }

        public ProgressBar getProgressBar() {
            return mProgressBar.get();
        }

        public boolean isbIsDownloading() {
            return bIsDownloading;
        }

        @Override
        public void cancelHandler(ICancelable handler) {
            mDownLoadCancelHandler = handler;
        }

        @Override
        public void progressing(long newValue) {
            try {
                if (bIsViewPage && mTextView.get() != null) {
                    String text = String.format(Locale.getDefault(), "%d", newValue) + "%";
                    mTextView.get().setText(text);
                }

                mProgressValue = (int) newValue;
                if (mProgressBar.get() != null) {
                    mProgressBar.get().setProgress(mProgressValue);
                }
                log.e("progressing: " + newValue);

                mDownloadCallback.onDownloadProgress(newValue); // now is useless

                // used can display download progress bar in multiple different view page for one file which is loading
                if (bIsDownloading) {
                    for (IDownloadCallBack one : mVector) {
                        one.onDownloadProgress(newValue);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFinishedDownload(boolean taskStatus, String localPath, @Nullable FileDownloadException e) {
            mProgressValue = 0;
            if (mProgressBar.get() != null) {
                mProgressBar.get().setProgress(mProgressValue);
            }

            if (mDownloadCallback != null) {
                mDownloadCallback.onDownloadFinished(taskStatus, localPath, e);
            }

            if (bIsDownloading) {
                for (IDownloadCallBack one : mVector) {
                    one.onDownloadFinished(taskStatus, localPath, e);
                }
                // clear
                mVector.clear();
                // reset
                bIsDownloading = false;
            }
        }
    }

}
