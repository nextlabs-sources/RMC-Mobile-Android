package com.skydrm.rmc.ui.service.offline.core;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.ui.service.offline.architecture.IOffline;
import com.skydrm.rmc.ui.service.offline.architecture.IViewer;
import com.skydrm.rmc.ui.service.offline.architecture.OfflineHandler;
import com.skydrm.rmc.ui.service.offline.architecture.OfflineRequest;
import com.skydrm.rmc.ui.service.offline.architecture.OfflineStatus;
import com.skydrm.rmc.ui.service.offline.exception.OfflineException;

public class OfflineViewer implements IViewer, OfflineExpiryHandler.IExpiryCallback,
        OfflinePolicyHandler.IPolicyCallback, OfflineTokenHandler.ITokenHandlerCallback {
    private static final int MSG_EXCEPTION = 0x001;
    private static final int MSG_TOKEN_EXPIRED = 0x002;
    private Context mContext;
    private IOffline mOfflineFile;
    private final InternalHandler mIH;
    private ICallback mCallback;

    public OfflineViewer(Context context, IOffline offlineFile, Looper looper, ICallback callback) {
        this.mContext = context;
        this.mOfflineFile = offlineFile;
        this.mIH = new InternalHandler(looper);
        this.mCallback = callback;
    }

    @Override
    public void view() {
        OfflineHandler expiryHandler = new OfflineExpiryHandler(this);
        OfflineHandler viewHandler = new OfflineViewHandler(mContext);
        expiryHandler.setSuccessor(viewHandler);
        OfflineHandler tokenHandler = new OfflineTokenHandler(null);
        viewHandler.setSuccessor(tokenHandler);
        OfflineRequest request = new OfflineRequest.Builder()
                .setOffline(mOfflineFile)
                .setTokenAction(OfflineRequest.TOKEN_INJECT)
                .build();
        try {
            expiryHandler.handleRequest(request);
        } catch (OfflineException e) {
            sendMessage(MSG_EXCEPTION, e);
        }
    }

    @Override
    public void onTokenExpired() {
        if (isNetworkConnected()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                    .setTitle(R.string.app_name)
                    .setMessage("This file has been expired,do you want to refresh it now?")
                    .setPositiveButton(R.string.common_ok_uppercase, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            checkPolicy();
                        }
                    })
                    .setNegativeButton(R.string.common_cancel_initcap, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            mCallback.onTokenCancelRecovery();
                            clearCaches();
                        }
                    });
            builder.setCancelable(false);
            builder.show();
        } else {
            //hint user with token expired msg.
            sendMessage(MSG_TOKEN_EXPIRED, new OfflineException(OfflineStatus.STATUS_TOKEN_EXPIRED, "EncryptToken has been expired already,waiting network to refresh it."));
        }
    }

    private void clearCaches() {
        try {
            OfflineRequest request = new OfflineRequest.Builder()
                    .setOffline(mOfflineFile)
                    .build();
            OfflineHandler clearHandler = new OfflineClearHandler(null);
            clearHandler.handleRequest(request);
        } catch (OfflineException e) {
            sendMessage(MSG_EXCEPTION, e);
        }
    }

    private void checkPolicy() {
        try {
            OfflineHandler policyHandler = new OfflinePolicyHandler(this);
            OfflineHandler offlineDownloadHandler = new OfflineDownloadHandler(null);
            policyHandler.setSuccessor(offlineDownloadHandler);
            OfflineHandler tokenHandler = new OfflineTokenHandler(this);
            offlineDownloadHandler.setSuccessor(tokenHandler);
            OfflineHandler viewHandler = new OfflineViewHandler(mContext);
            tokenHandler.setSuccessor(viewHandler);
            OfflineRequest request = new OfflineRequest.Builder()
                    .setOffline(mOfflineFile)
                    .setTokenAction(OfflineRequest.TOKEN_RETRIEVE)
                    .build();
            policyHandler.handleRequest(request);
        } catch (OfflineException e) {
            sendMessage(MSG_EXCEPTION, e);
        }
    }

    @Override
    public void onCheckPolicy(int type) {

    }

    @Override
    public void onCacheRights() {

    }

    @Override
    public void onCheckError(OfflineException e) {
        sendMessage(MSG_EXCEPTION, e);
    }

    @Override
    public void onTokenProcessed() {

    }

    @Override
    public void onTokenError(OfflineException e) {
        sendMessage(MSG_EXCEPTION, e);
    }

    private void sendMessage(int what, OfflineException e) {
        Message message = Message.obtain();
        message.what = what;
        message.obj = new ViewBundle(mCallback, e);
        mIH.sendMessage(message);
    }

    private boolean isNetworkConnected() {
        return SkyDRMApp.getInstance().isNetworkAvailable();
    }

    private static class InternalHandler extends Handler {
        InternalHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_EXCEPTION:
                    ViewBundle b1 = (ViewBundle) msg.obj;
                    b1.callback.onError(b1.e);
                    break;
                case MSG_TOKEN_EXPIRED:
                    ViewBundle b2 = (ViewBundle) msg.obj;
                    b2.callback.onError(b2.e);
                    break;
            }
        }
    }

    static class ViewBundle {
        private ICallback callback;
        private OfflineException e;

        ViewBundle(ICallback callback, OfflineException e) {
            this.callback = callback;
            this.e = e;
        }
    }
}
