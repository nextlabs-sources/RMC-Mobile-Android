package com.skydrm.rmc.ui.service.offline.core;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.skydrm.rmc.ui.service.offline.IOfflineCallback;
import com.skydrm.rmc.ui.service.offline.architecture.IOfflineStatusDelivery;
import com.skydrm.rmc.ui.service.offline.architecture.OfflineStatus;

public class OfflineStatusDelivery implements IOfflineStatusDelivery {
    private final InternalHandler ih;

    public OfflineStatusDelivery(Looper looper) {
        ih = new InternalHandler(looper);
    }

    @Override
    public void post(OfflineStatus status) {
        Message msg = Message.obtain();
        msg.what = status.getStatus();
        msg.obj = status;
        ih.sendMessage(msg);
    }

    private static class InternalHandler extends Handler {
        InternalHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            OfflineStatus status = (OfflineStatus) msg.obj;
            IOfflineCallback callback = status.getCallback();
            if (callback == null) {
                return;
            }
            switch (msg.what) {
                case OfflineStatus.STATUS_STARTED:
                    callback.onStarted();
                    break;
                case OfflineStatus.STATUS_PROGRESS:
                    callback.onProgress();
                    break;
                case OfflineStatus.STATUS_MARK_SUCCESS:
                    callback.onMarkDone();
                    break;
                case OfflineStatus.STATUS_FAILED:
                    callback.onMarkFailed(status.getException());
                    break;
            }
        }
    }
}
