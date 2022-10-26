package com.skydrm.rmc.ui.project.feature.service.core;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.skydrm.rmc.ui.project.feature.service.IMarkCallback;
import com.skydrm.rmc.ui.project.feature.service.IMarkerStatusDelivery;

public class MarkerStatusDelivery implements IMarkerStatusDelivery {
    private H mH;

    public MarkerStatusDelivery(Looper looper) {
        mH = new H(looper);
    }

    @Override
    public void post(MarkerStatus status) {
        Message message = Message.obtain();
        message.what = status.getStatus();
        message.obj = status;
        mH.sendMessage(message);
    }

    private static class H extends Handler {
        H(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            MarkerStatus status = (MarkerStatus) msg.obj;
            if (status == null) {
                return;
            }
            IMarkCallback callback = status.getCallback();
            switch (msg.what) {
                case MarkerStatus.STATUS_MARK_START:
                    postStarting(callback);
                    break;
                case MarkerStatus.STATUS_MARK_ALLOW:
                    postAllow(callback);
                    break;
                case MarkerStatus.STATUS_MARK_FAILED:
                    postFailure(status, callback);
                    break;
                case MarkerStatus.STATUS_MARK_CANCEL:
                    break;
            }
        }

        private void postStarting(IMarkCallback callback) {
            if (callback == null) {
                return;
            }
            callback.onMarkStart();
        }

        private void postAllow(IMarkCallback callback) {
            if (callback == null) {
                return;
            }
            callback.onMarkAllow();
        }

        private void postFailure(MarkerStatus status, IMarkCallback callback) {
            if (callback == null) {
                return;
            }
            MarkException exception = status.getException();
            callback.onMarkFailed(exception == null ?
                    new MarkException(MarkerStatus.STATUS_FAILED_COMMON, "Unknown error") :
                    exception);
        }
    }
}
