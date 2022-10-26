package com.skydrm.rmc.ui.service.share.core;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.ui.service.offline.architecture.IMarker;
import com.skydrm.rmc.ui.project.feature.service.IMarkerResponse;
import com.skydrm.rmc.ui.project.feature.service.core.MarkBaseHandler;
import com.skydrm.rmc.ui.project.feature.service.core.MarkException;
import com.skydrm.rmc.ui.project.feature.service.core.MarkerStatus;
import com.skydrm.rmc.ui.service.share.ISharingFile;

public class ShareMarker implements IMarker, DispatchHandler.ICallback, Runnable {
    private String mTag;
    private ISharingFile mFile;
    private IMarkerResponse mResponse;
    private IMarker.OnDestroyListener mListener;

    private int mStatus;

    public ShareMarker(String tag, ISharingFile file,
                       IMarkerResponse response, OnDestroyListener listener) {
        this.mTag = tag;
        this.mFile = file;
        this.mResponse = response;
        this.mListener = listener;
    }

    @Override
    public void start() {
        mStatus = MarkerStatus.STATUS_MARK_START;
        mResponse.onMarkStart();
        ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI).execute(this);
    }

    @Override
    public void run() {
        // 1. Check re-share rights first.
        MarkBaseHandler checkHandler = new ShareCheckHandler();
        // 2. Select target to share.
        MarkBaseHandler selectHandler = new DispatchHandler(this);

        checkHandler.setSuccessor(selectHandler);
        try {
            checkHandler.handleRequest(new ShareRequest(mFile));
        } catch (MarkException e) {
            mStatus = MarkerStatus.STATUS_MARK_FAILED;
            mResponse.onMarkFailed(e);

            destroy();
        }
    }

    @Override
    public void cancel() {
        mStatus = MarkerStatus.STATUS_MARK_CANCEL;
        mResponse.onMarkCanceled();
        destroy();
    }

    @Override
    public boolean isRunning() {
        return mStatus == MarkerStatus.STATUS_MARK_START ||
                mStatus == MarkerStatus.STATUS_MARK_ALLOW;
    }

    @Override
    public void destroy() {
        if (mListener == null) {
            return;
        }
        mListener.onDestroy(mTag, this);
    }

    @Override
    public void onMarkAllow() {
        mStatus = MarkerStatus.STATUS_MARK_ALLOW;
        mResponse.onMarkAllow();

        destroy();
    }
}
