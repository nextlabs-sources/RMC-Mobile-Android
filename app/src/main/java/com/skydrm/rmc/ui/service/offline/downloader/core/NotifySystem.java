package com.skydrm.rmc.ui.service.offline.downloader.core;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.skydrm.rmc.ui.service.offline.downloader.ICallback;
import com.skydrm.rmc.ui.service.offline.downloader.architecture.DownloadStatus;
import com.skydrm.rmc.ui.service.offline.downloader.architecture.INotifySystem;


public class NotifySystem implements INotifySystem {
    private final InternalHandler ih;

    public NotifySystem(Looper looper) {
        ih = new InternalHandler(looper);
    }

    @Override
    public void post(DownloadStatus status) {
        Message message = Message.obtain();
        message.what = status.getStatus();
        message.obj = status;
        ih.sendMessage(message);
    }

    private static class InternalHandler extends Handler {
        private DownloadStatus mStatus;
        private ICallback mCallback;

        InternalHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mStatus = (DownloadStatus) msg.obj;
            mCallback = mStatus.getCallback();
            switch (msg.what) {
                case DownloadStatus.STATUS_PROGRESS:
                    mCallback.onDownloadProgress(mStatus.getFinished(), mStatus.getLength(), mStatus.getPercent());
                    break;
                case DownloadStatus.STATUS_COMPLETED:
                    mCallback.onDownloadComplete();
                    break;
                case DownloadStatus.STATUS_PAUSED:
                    mCallback.onDownloadPaused();
                    break;
                case DownloadStatus.STATUS_CANCELED:
                    mCallback.onDownloadCanceled();
                    break;
                case DownloadStatus.STATUS_FAILED:
                    mCallback.onFailed(mStatus.getException());
                    break;
            }
        }
    }
}
