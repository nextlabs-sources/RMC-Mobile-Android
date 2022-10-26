package com.skydrm.rmc.ui.project.feature.service.share.core;

import android.support.annotation.IntDef;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.ui.service.offline.architecture.IMarker;
import com.skydrm.rmc.ui.project.feature.service.IMarkerResponse;
import com.skydrm.rmc.ui.project.feature.service.core.MarkBaseHandler;
import com.skydrm.rmc.ui.project.feature.service.core.MarkException;
import com.skydrm.rmc.ui.project.feature.service.core.MarkerStatus;
import com.skydrm.rmc.ui.project.feature.service.share.IShare;

@Deprecated
public class ShareMarker implements IMarker, Runnable, DispatchHandler.ICallback {
    public static final int ADD_TO_PROJECT = 0;
    public static final int SHARE_TO_PERSON = 1;

    private String mTag;
    private IShare mFile;
    private IMarker.OnDestroyListener mDestroyListener;
    private IMarkerResponse mResponse;

    private int mStatus;
    private int mType;

    public ShareMarker(String tag, IShare file, @ShareType int type,
                       IMarkerResponse response, OnDestroyListener listener) {
        this.mTag = tag;
        this.mFile = file;
        this.mType = type;
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
        if (mDestroyListener == null) {
            return;
        }
        mDestroyListener.onDestroy(mTag, this);
    }

    @Override
    public void run() {
        // 1. Check policy type& rights first.
        MarkBaseHandler checkHandler = getCheckHandler();
        if (checkHandler == null) {
            mStatus = MarkerStatus.STATUS_MARK_FAILED;
            mResponse.onMarkFailed(new MarkException(MarkerStatus.STATUS_FAILED_COMMON,
                    "Invalid share type performed."));
            return;
        }
        // 2. Select target project.
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

    private MarkBaseHandler getCheckHandler() {
        if (mType == ADD_TO_PROJECT) {
            return new AddToProjectCheckHandler();
        } else if (mType == SHARE_TO_PERSON) {
            return new ShareToPersonCheckHandler(mFile.getProjectId());
        }
        return null;
    }

    @Override
    public void onMarkAllow() {
        mStatus = MarkerStatus.STATUS_MARK_ALLOW;
        mResponse.onMarkAllow();

        destroy();
    }

    @IntDef({ADD_TO_PROJECT, SHARE_TO_PERSON})
    @interface ShareType {

    }
}
