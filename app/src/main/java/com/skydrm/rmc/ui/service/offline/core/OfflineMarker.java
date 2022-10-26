package com.skydrm.rmc.ui.service.offline.core;

import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.ui.service.offline.architecture.IMarker;
import com.skydrm.rmc.ui.service.offline.architecture.IOffline;
import com.skydrm.rmc.ui.service.offline.architecture.IOfflineFilter;
import com.skydrm.rmc.ui.service.offline.architecture.IOfflineResponse;
import com.skydrm.rmc.ui.service.offline.architecture.OfflineHandler;
import com.skydrm.rmc.ui.service.offline.architecture.OfflineRequest;
import com.skydrm.rmc.ui.service.offline.architecture.OfflineStatus;
import com.skydrm.rmc.ui.service.offline.exception.OfflineException;
import com.skydrm.rmc.ui.service.offline.filter.OfflineFileFilter;

public class OfflineMarker implements IMarker, IOfflineFilter.ICallback, OfflinePolicyHandler.IPolicyCallback,
        OfflineDownloadHandler.IDownloadHandlerCallback, OfflineTokenHandler.ITokenHandlerCallback {
    private static final DevLog log = new DevLog(OfflineMarker.class.getSimpleName());
    private OfflineFileFilter mFileFilter;
    private IOffline mOffline;
    private String mTag;
    private IMarker.OnDestroyListener mOnDestroyListener;
    private IOfflineResponse mResponse;
    private int mStatus;

    public OfflineMarker(OfflineFileFilter filter, IOffline file, String tag,
                         IOfflineResponse response, OnDestroyListener listener) {
        this.mFileFilter = filter;
        this.mOffline = file;
        this.mTag = tag;
        this.mOnDestroyListener = listener;
        this.mResponse = response;
    }

    @Override
    public void start() {
        //filter handler.
        OfflineHandler filterHandler = new OfflineFilterHandler(mFileFilter, this);
        //policy handler.
        OfflineHandler policyHandler = new OfflinePolicyHandler(this);
        //make policy handler is the successor of filter handler.
        filterHandler.setSuccessor(policyHandler);
        //download handler.
        OfflineHandler downloadHandler = new OfflineDownloadHandler(this);
        //make download handler is the successor of policy handler.
        policyHandler.setSuccessor(downloadHandler);
        //token handler
        OfflineHandler tokenHandler = new OfflineTokenHandler(this);
        //make token handler is the successor of download handler.
        downloadHandler.setSuccessor(tokenHandler);
        OfflineRequest request = new OfflineRequest.Builder()
                .setOffline(mOffline)
                .setTokenAction(OfflineRequest.TOKEN_RETRIEVE)
                .build();
        try {
            filterHandler.handleRequest(request);
        } catch (OfflineException e) {
            mResponse.onFailed(e);
            destroy();
        }
    }

    @Override
    public void cancel() {
        try {
            if (mOffline != null) {
                mOffline.cancel();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        destroy();
    }

    @Override
    public boolean isRunning() {
        return mStatus == OfflineStatus.STATUS_STARTED ||
                mStatus == OfflineStatus.STATUS_ACCEPTED ||
                mStatus == OfflineStatus.STATUS_CHECK_POLICY ||
                mStatus == OfflineStatus.STATUS_CACHE_RIGHTS ||
                mStatus == OfflineStatus.STATUS_DOWNLOAD_START ||
                mStatus == OfflineStatus.STATUS_DOWNLOAD_PROGRESS ||
                mStatus == OfflineStatus.STATUS_DOWNLOAD_COMPLETE ||
                mStatus == OfflineStatus.STATUS_TOKEN_PROCESSED;
    }

    @Override
    public void destroy() {
        mOnDestroyListener.onDestroy(mTag, this);
    }

    @Override
    public void onAccepted() {
        mStatus = OfflineStatus.STATUS_STARTED;
        mResponse.onStarted(System.currentTimeMillis());
//        mStatus = OfflineStatus.STATUS_ACCEPTED;
//        mResponse.onAccepted();
    }

    @Override
    public void onCheckPolicy(int type) {
        mStatus = OfflineStatus.STATUS_CHECK_POLICY;
        mResponse.onCheckPolicy(type);
    }

    @Override
    public void onCacheRights() {
        mStatus = OfflineStatus.STATUS_CACHE_RIGHTS;
        mResponse.onCacheRights();
    }

    @Override
    public void onCheckError(OfflineException e) {
        mResponse.onFailed(e);
        destroy();
    }

    @Override
    public void onDownloadStart() {
        mStatus = OfflineStatus.STATUS_DOWNLOAD_START;
        mResponse.onDownloadStarted();
    }

    @Override
    public void onDownloadProgress(long finished, long length, int percent) {
        log.d("finished:" + finished + ",length:" + length + ",percent:" + percent);
        mStatus = OfflineStatus.STATUS_DOWNLOAD_PROGRESS;
        mResponse.onDownloadProgress();
    }

    @Override
    public void onDownloadCancel() {
        mStatus = OfflineStatus.STATUS_DOWNLOAD_CANCEL;
        destroy();
    }

    @Override
    public void onDownloaded() {
        mStatus = OfflineStatus.STATUS_DOWNLOAD_COMPLETE;
        mResponse.onDownloadComplete();
    }

    @Override
    public void onDownloadError(OfflineException e) {
        mStatus = OfflineStatus.STATUS_DOWNLOAD_FAILED;
        mResponse.onFailed(e);
        destroy();
    }

    @Override
    public void onTokenProcessed() {
        mStatus = OfflineStatus.STATUS_TOKEN_PROCESSED;
        mResponse.onTokenCached();
        //if token cached successfully then means mark done.
        mResponse.onMarkDone(System.currentTimeMillis());
        destroy();
    }

    @Override
    public void onTokenError(OfflineException e) {
        mStatus = OfflineStatus.STATUS_TOKEN_PROCESS_FAILED;
        mResponse.onFailed(e);
        destroy();
    }
}
