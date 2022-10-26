package com.skydrm.rmc.ui.service.offline.core;

import com.skydrm.rmc.ui.service.offline.architecture.IOffline;
import com.skydrm.rmc.ui.service.offline.architecture.OfflineHandler;
import com.skydrm.rmc.ui.service.offline.architecture.OfflineRequest;
import com.skydrm.rmc.ui.service.offline.architecture.OfflineStatus;
import com.skydrm.rmc.ui.service.offline.downloader.ICallback;
import com.skydrm.rmc.ui.service.offline.downloader.exception.DownloadException;
import com.skydrm.rmc.ui.service.offline.exception.OfflineException;

import java.io.File;

public class OfflineDownloadHandler extends OfflineHandler implements ICallback {
    private OfflineRequest mRequest;
    private IDownloadHandlerCallback mCallback;

    OfflineDownloadHandler(IDownloadHandlerCallback callback) {
        this.mCallback = callback;
    }

    @Override
    public void handleRequest(final OfflineRequest request) throws OfflineException {
        if (request == null) {
            throw new OfflineException(OfflineStatus.STATUS_FAILED, "OfflineDownloadHandler request must not be null.");
        }
        this.mRequest = request;
        IOffline offline = request.getOffline();
        if (offline == null) {
            throw new OfflineException(OfflineStatus.STATUS_FAILED, "OfflineDownloadHandler offlineFile must not be null.");
        }

        String localPath = offline.getLocalPath();
        File file = new File(localPath);
        if (file.exists() && file.isFile() && file.length() > 0x4000) {
            dispatchRequest();
        } else {
            if (mCallback != null) {
                mCallback.onDownloadStart();
            }
            //download file directly.
            offline.downloadForOffline(this);
        }
    }

    @Override
    public void onDownloadProgress(long finished, long length, int percent) {
        if (mCallback != null) {
            mCallback.onDownloadProgress(finished, length, percent);
        }
    }

    @Override
    public void onDownloadPaused() {

    }

    @Override
    public void onDownloadCanceled() {
        setSuccessor(null);
        if (mCallback != null) {
            mCallback.onDownloadCancel();
        }
    }

    @Override
    public void onDownloadComplete() {
        dispatchRequest();
    }

    @Override
    public void onFailed(DownloadException e) {
        //handle exception
        if (e.getStatusCode() == 403) {
            handleException(new OfflineException(OfflineStatus.STATUS_UNAUTHORIZED, e.getMessage(), e));
        } else if (e.getStatusCode() == 404) {
            handleException(new OfflineException(OfflineStatus.STATUS_FILE_NOT_FOUND, e.getMessage(), e));
        } else {
            handleException(new OfflineException(OfflineStatus.STATUS_FAILED, e.getMessage(), e));
        }
    }

    private void dispatchRequest() {
        try {
            if (mCallback != null) {
                mCallback.onDownloaded();
            }
            if (successor != null) {
                successor.handleRequest(mRequest);
            }
        } catch (OfflineException e) {
            handleException(e);
        }
    }

    private void handleException(OfflineException e) {
        if (mCallback != null) {
            mCallback.onDownloadError(e);
        }
    }

    public interface IDownloadHandlerCallback {
        void onDownloadStart();

        void onDownloadProgress(long finished, long length, int percent);

        void onDownloadCancel();

        void onDownloaded();

        void onDownloadError(OfflineException e);
    }
}
