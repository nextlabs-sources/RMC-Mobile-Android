package com.skydrm.rmc.ui.service.offline.downloader.core;

import android.text.TextUtils;

import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.ui.service.offline.downloader.architecture.DownloadStatus;
import com.skydrm.rmc.ui.service.offline.downloader.architecture.IDownloadTask;
import com.skydrm.rmc.ui.service.offline.downloader.config.DownloadInfo;
import com.skydrm.rmc.ui.service.offline.downloader.config.ThreadInfo;
import com.skydrm.rmc.ui.service.offline.downloader.exception.DownloadException;
import com.skydrm.rmc.ui.service.offline.downloader.utils.IOUtils;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

abstract class DownloadTask implements IDownloadTask {
    private volatile int mComment;
    private int mStatus;
    private DownloadInfo mDownloadInfo;
    private ThreadInfo mThreadInfo;
    private IDownloadListener mDownloadListener;

    DownloadTask(DownloadInfo downloadInfo, ThreadInfo threadInfo, IDownloadListener listener) {
        this.mDownloadInfo = downloadInfo;
        this.mThreadInfo = threadInfo;
        this.mDownloadListener = listener;
    }

    @Override
    public void pause() {
        mComment = DownloadStatus.STATUS_PAUSED;
    }

    @Override
    public void cancel() {
        mComment = DownloadStatus.STATUS_CANCELED;
    }

    @Override
    public boolean isPaused() {
        return mStatus == DownloadStatus.STATUS_PAUSED;
    }

    @Override
    public boolean isCanceled() {
        return mStatus == DownloadStatus.STATUS_CANCELED;
    }

    @Override
    public boolean isDownloading() {
        return mStatus == DownloadStatus.STATUS_PROGRESS;
    }

    @Override
    public boolean isComplete() {
        return mStatus == DownloadStatus.STATUS_COMPLETED;
    }

    @Override
    public boolean isFailed() {
        return mStatus == DownloadStatus.STATUS_FAILED;
    }

    @Override
    public void run() {
        try {
            execDownload();
            mStatus = DownloadStatus.STATUS_COMPLETED;
            mDownloadListener.onDownloadCompleted();
        } catch (DownloadException e) {
            handleException(e);
        }
    }

    private void handleException(DownloadException e) {
        switch (e.getErrCode()) {
            case DownloadStatus.STATUS_PAUSED:
                mStatus = DownloadStatus.STATUS_PAUSED;
                mDownloadListener.onDownloadPaused();
                break;
            case DownloadStatus.STATUS_CANCELED:
                mStatus = DownloadStatus.STATUS_CANCELED;
                mDownloadListener.onDownloadCanceled();
                break;
            case DownloadStatus.STATUS_FAILED:
                mStatus = DownloadStatus.STATUS_FAILED;
                mDownloadListener.onDownloadFailed(e);
                break;
            default:
                throw new IllegalStateException(e);
        }
    }

    private void execDownload() throws DownloadException {
        OkHttpClient client = buildHttpClient();
        if (client == null) {
            throw new IllegalArgumentException("OkHttpClient must not be null");
        }
        Request request;
        Map<String, String> requestBodyMap;
        try {
            requestBodyMap = getRequestBody(mDownloadInfo);
        } catch (JSONException e) {
            throw new DownloadException(DownloadStatus.STATUS_FAILED, e.getMessage(), e);
        }
        Map<String, String> headerMaps;
        try {
            headerMaps = getHttpHeaders(mThreadInfo);
        } catch (InvalidRMClientException e) {
            throw new DownloadException(DownloadStatus.STATUS_FAILED, e.getMessage(), e);
        }
        if (requestBodyMap != null) {
            if (requestBodyMap.size() != 0) {
                request = buildRequestWithBody(requestBodyMap, headerMaps);
            } else {
                request = buildRequestWithoutBody(headerMaps);
            }
        } else {
            request = buildRequestWithoutBody(headerMaps);
        }
        Response response;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            throw new DownloadException(DownloadStatus.STATUS_FAILED, e.getMessage(), e);
        }
        int code = response.code();
        String message = response.message();
        if (!response.isSuccessful()) {
            throw new DownloadException(DownloadStatus.STATUS_FAILED, code, TextUtils.isEmpty(message) ?
                    "Unsupported status code " + code :
                    "Unsupported status code " + code + ",error message " + message);
        }
        prepareTransferData(response.body());
    }

    private void prepareTransferData(ResponseBody body) throws DownloadException {
        long length = body.contentLength();
        mDownloadInfo.setLength(length);
        InputStream inputStream = null;
        RandomAccessFile raf = null;
        try {
            inputStream = body.byteStream();
            long offset = mThreadInfo.getStart() + mThreadInfo.getFinished();
            try {
                raf = getFile(mDownloadInfo.getLocalPath(), offset);
            } catch (IOException e) {
                throw new DownloadException(DownloadStatus.STATUS_FAILED, e.getMessage(), e);
            }
            copyData(inputStream, raf);
        } finally {
            IOUtils.closeSilently(inputStream);
            IOUtils.closeSilently(raf);
            IOUtils.closeSilently(body);
        }
    }

    private void copyData(InputStream is, RandomAccessFile raf) throws DownloadException {
        final byte[] buffer = new byte[8 * 1024];
        int len;
        while (true) {
            try {
                checkPausedOrCanceled();
                len = is.read(buffer);
                if (len == -1) {
                    break;
                }
                raf.write(buffer, 0, len);
                mThreadInfo.setFinished(mThreadInfo.getFinished() + len);
                mDownloadInfo.setFinished(mDownloadInfo.getFinished() + len);
                mDownloadListener.onDownloadProgress(mDownloadInfo.getFinished(), mDownloadInfo.getLength());
            } catch (IOException e) {
                throw new DownloadException(DownloadStatus.STATUS_FAILED, e.getMessage(), e);
            }
        }
    }

    private void checkPausedOrCanceled() throws DownloadException {
        if (mComment == DownloadStatus.STATUS_PAUSED) {
            throw new DownloadException(DownloadStatus.STATUS_PAUSED, "Download task paused.");
        } else if (mComment == DownloadStatus.STATUS_CANCELED) {
            throw new DownloadException(DownloadStatus.STATUS_CANCELED, "Download task canceled.");
        }
    }

    private Request buildRequestWithBody(Map<String, String> requestBodyMap, Map<String, String> headerMaps) {
        Request request;
        RequestBody requestBody = null;
        for (String contentType : requestBodyMap.keySet()) {
            String content = requestBodyMap.get(contentType);
            requestBody = RequestBody.create(MediaType.parse(contentType), content);
            //support only one pair request body parameters.
            break;
        }
        if (headerMaps != null && headerMaps.size() != 0) {
            request = new Request.Builder()
                    .url(mDownloadInfo.getUrl())
                    .headers(Headers.of(headerMaps))
                    .post(requestBody)
                    .build();
        } else {
            request = new Request.Builder()
                    .url(mDownloadInfo.getUrl())
                    .post(requestBody)
                    .build();
        }
        return request;
    }

    private Request buildRequestWithoutBody(Map<String, String> headerMaps) {
        Request request;
        if (headerMaps != null && headerMaps.size() != 0) {
            request = new Request.Builder()
                    .url(mDownloadInfo.getUrl())
                    .headers(Headers.of(headerMaps))
                    .get()
                    .build();
        } else {
            request = new Request.Builder()
                    .url(mDownloadInfo.getUrl())
                    .get()
                    .build();
        }
        return request;
    }
//    protected abstract int getResponseCode();

    protected abstract OkHttpClient buildHttpClient();

    protected abstract Map<String, String> getRequestBody(DownloadInfo info) throws JSONException;

    protected abstract Map<String, String> getHttpHeaders(ThreadInfo info) throws InvalidRMClientException;

    protected abstract RandomAccessFile getFile(String localPath, long offset) throws IOException;
}
