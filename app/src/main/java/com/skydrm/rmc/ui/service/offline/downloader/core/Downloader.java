package com.skydrm.rmc.ui.service.offline.downloader.core;

import com.skydrm.rmc.ui.service.offline.downloader.architecture.DownloadStatus;
import com.skydrm.rmc.ui.service.offline.downloader.architecture.IDownloadResponse;
import com.skydrm.rmc.ui.service.offline.downloader.architecture.IDownloadTask;
import com.skydrm.rmc.ui.service.offline.downloader.architecture.IDownloader;
import com.skydrm.rmc.ui.service.offline.downloader.config.DownloadInfo;
import com.skydrm.rmc.ui.service.offline.downloader.config.ThreadInfo;
import com.skydrm.rmc.ui.service.offline.downloader.exception.DownloadException;
import com.skydrm.rmc.ui.service.offline.downloader.utils.LogUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;

public class Downloader implements IDownloader, DownloadTask.IDownloadListener {
    private DownloadRequest mRequest;
    private IDownloadResponse mResponse;
    private List<DownloadTask> mDownloadTasks;

    private int mStatus;
    private DownloadInfo mDownloadInfo;
    private IDownloaderDestroyListener mDownloaderDestroyListener;
    private Executor mExecutor;
    private String mTag;

    public Downloader(DownloadRequest request, IDownloadResponse response,
                      Executor executor, String tag, IDownloaderDestroyListener listener) {
        this.mRequest = request;
        this.mResponse = response;
        this.mExecutor = executor;
        this.mTag = tag;
        this.mDownloaderDestroyListener = listener;
        init();
    }

    private void init() {
        mDownloadInfo = new DownloadInfo(mRequest.getUrl(), mRequest.getLocalPath(),
                mRequest.getStart(), mRequest.getLength(),
                mRequest.getPathId(), mRequest.getType());
        mDownloadInfo.setTransactionId(mRequest.getTransactionId());
        mDownloadInfo.setTransactionCode(mRequest.getTransactionCode());
        mDownloadInfo.setSpaceId(mRequest.getSpaceId());
        mDownloadTasks = new LinkedList<>();
    }

    @Override
    public boolean isRunning() {
        return mStatus == DownloadStatus.STATUS_STARTED ||
                mStatus == DownloadStatus.STATUS_PROGRESS;
    }

    @Override
    public void start() {
        mStatus = DownloadStatus.STATUS_STARTED;
        download(0, false);
    }

    private void download(long length, boolean acceptRanges) {
        mStatus = DownloadStatus.STATUS_PROGRESS;
        initDownloadTasks(length, acceptRanges);
        for (IDownloadTask dt : mDownloadTasks) {
            mExecutor.execute(dt);
        }
    }

    private void initDownloadTasks(long length, boolean acceptRanges) {
        mDownloadTasks.clear();
        if (acceptRanges) {
            // TODO: 8/14/2018 if server support multiple task download.
//            List<ThreadInfo> multipleThreadInfo = getMultipleThreadInfo(length);
//            for (ThreadInfo ti : multipleThreadInfo) {
//
//            }
            ThreadInfo ti = getSingleThreadInfo();
            DownloadTask dt = new SingleDownloadTask(mDownloadInfo, ti, this);
            mDownloadTasks.add(dt);
        } else {
            ThreadInfo ti = getSingleThreadInfo();
            DownloadTask dt = new SingleDownloadTask(mDownloadInfo, ti, this);
            mDownloadTasks.add(dt);
        }
    }

    /**
     * This method is used to divide a file into multiple sections(which can be dynamic set)
     * the number of section depends on thread num apply for it.in our download setting which will always be 3
     * which means a remote file can be downloaded by 3 threads at one time most.
     *
     * @param length Total length of target file.
     * @return
     */
    private List<ThreadInfo> getMultipleThreadInfo(long length) {
        //if we support breakpoint download which should be loaded from db or serializable objects.
        List<ThreadInfo> threadInfos = new ArrayList<>();
        int threadNum = 3;
        for (int i = 0; i < threadNum; i++) {
            //1.divide the file length into {@link threadNum(which is 3 here)}parts. calculate average firstly.
            final long average = length / threadNum;
            //2.calculate the accurate section belongs to each thread(which represented by ThreadInfo)
            final long start = i * average;
            final long end;
            //if is the last section which end is just the [start,length);
            if (i == threadNum - 1) {
                end = length;
            } else {
                end = start + average - 1;
            }
            ThreadInfo ti = new ThreadInfo(i, mTag, mRequest.getUrl(), start, end, 0);
            threadInfos.add(ti);
        }
        return threadInfos;
    }

    private ThreadInfo getSingleThreadInfo() {
        return new ThreadInfo(0, mTag, mRequest.getUrl(), 0);
    }

    @Override
    public void pause() {
        for (IDownloadTask dt : mDownloadTasks) {
            dt.pause();
        }
        if (mStatus != DownloadStatus.STATUS_PROGRESS) {
            onDownloadPaused();
        }
    }

    @Override
    public void cancel() {
        for (IDownloadTask dt : mDownloadTasks) {
            dt.cancel();
        }
        if (mStatus != DownloadStatus.STATUS_PROGRESS) {
            onDownloadCanceled();
        }
    }

    @Override
    public void destroy() {
        mDownloaderDestroyListener.onDestroyed(mTag, this);
    }

    @Override
    public void onDownloadProgress(long done, long total) {
        mStatus = DownloadStatus.STATUS_PROGRESS;
        int percent = (int) ((done * 1.0 / total) * 100);
        mResponse.onDownloadProgress(done, total, percent);
        LogUtil.d("percent:" + percent);
    }

    @Override
    public void onDownloadPaused() {
        if (isAllPaused()) {
            mStatus = DownloadStatus.STATUS_PAUSED;
            mResponse.onDownloadPaused();
            destroy();
        }
    }

    @Override
    public void onDownloadCanceled() {
        if (isAllCanceled()) {
            deleteFile();
            mStatus = DownloadStatus.STATUS_CANCELED;
            mResponse.onDownloadCanceled();
            destroy();
        }
    }

    @Override
    public void onDownloadCompleted() {
        if (isAllComplete()) {
            mStatus = DownloadStatus.STATUS_COMPLETED;
            mResponse.onDownloadCompleted();
            destroy();
        }
    }

    @Override
    public void onDownloadFailed(DownloadException e) {
        LogUtil.e("Downloader-LOG", "onDownloadFailed" + e);
        if (isAllFailed()) {
            mStatus = DownloadStatus.STATUS_FAILED;
            mResponse.onDownloadFailed(e);
            destroy();
        }
    }

    private boolean isAllComplete() {
        boolean allComplete = true;
        for (IDownloadTask dt : mDownloadTasks) {
            if (!dt.isComplete()) {
                allComplete = false;
                break;
            }
        }
        return allComplete;
    }

    private boolean isAllPaused() {
        boolean allPaused = true;
        for (IDownloadTask dt : mDownloadTasks) {
            if (!dt.isPaused()) {
                allPaused = false;
                break;
            }
        }
        return allPaused;
    }

    private boolean isAllCanceled() {
        boolean allCanceled = true;
        for (IDownloadTask dt : mDownloadTasks) {
            if (!dt.isCanceled()) {
                allCanceled = false;
                break;
            }
        }
        return allCanceled;
    }

    private boolean isAllFailed() {
        boolean allFailed = true;
        for (IDownloadTask dt : mDownloadTasks) {
            if (!dt.isFailed()) {
                allFailed = false;
                break;
            }
        }
        return allFailed;
    }

    private void deleteFile() {
        File file = new File(mDownloadInfo.getLocalPath());
        if (file.exists() && file.isFile()) {
            file.delete();
        }
    }
}
