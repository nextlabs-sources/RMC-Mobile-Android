package com.skydrm.rmc.ui.service.modifyrights.core;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.ui.service.offline.architecture.IMarker;
import com.skydrm.rmc.ui.project.feature.service.IMarkerResponse;
import com.skydrm.rmc.ui.project.feature.service.core.MarkBaseHandler;
import com.skydrm.rmc.ui.project.feature.service.core.MarkException;
import com.skydrm.rmc.ui.project.feature.service.core.MarkerStatus;
import com.skydrm.rmc.ui.project.feature.service.share.core.DispatchHandler;
import com.skydrm.rmc.ui.service.modifyrights.IModifyRightsFile;

public class ModifyRightsMarker implements IMarker, Runnable, DispatchHandler.ICallback {
    private String mTag;
    private IModifyRightsFile mFile;
    private IMarkerResponse mResponse;
    private OnDestroyListener mDestroyListener;

    private int mStatus;

    public ModifyRightsMarker(String tag, IModifyRightsFile file,
                              IMarkerResponse response,
                              OnDestroyListener listener) {
        this.mTag = tag;
        this.mFile = file;
        this.mResponse = response;
        this.mDestroyListener = listener;
    }

    @Override
    public void start() {
        mStatus = MarkerStatus.STATUS_MARK_START;
        mResponse.onMarkStart();
        ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI).execute(this);
    }

    @Override
    public boolean isRunning() {
        return mStatus == MarkerStatus.STATUS_MARK_START ||
                mStatus == MarkerStatus.STATUS_MARK_ALLOW;
    }

    @Override
    public void run() {
        MarkBaseHandler first = new ModifyRightsCheckHandler();

        MarkBaseHandler second = new DispatchHandler(this);
        first.setSuccessor(second);
        try {
            first.handleRequest(new ModifyRightsRequest(mFile));
        } catch (MarkException e) {
            mStatus = MarkerStatus.STATUS_MARK_FAILED;
            mResponse.onMarkFailed(e);

            destroy();
        }
    }

    @Override
    public void onMarkAllow() {
        mStatus = MarkerStatus.STATUS_MARK_ALLOW;
        mResponse.onMarkAllow();

        destroy();
    }

    @Override
    public void cancel() {
        mStatus = MarkerStatus.STATUS_MARK_CANCEL;
        mResponse.onMarkCanceled();

        destroy();
    }

    @Override
    public void destroy() {
        if (mDestroyListener != null) {
            mDestroyListener.onDestroy(mTag, this);
        }
    }
}
