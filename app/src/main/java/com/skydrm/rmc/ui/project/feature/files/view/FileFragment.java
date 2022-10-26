package com.skydrm.rmc.ui.project.feature.files.view;

import android.text.TextUtils;
import android.view.View;

import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.engine.eventBusMsg.ProjectAddCompleteNotifyEvent;
import com.skydrm.rmc.ui.common.NxlFileItem;
import com.skydrm.rmc.ui.common.NxlFileType;
import com.skydrm.rmc.ui.project.common.MsgFileNotFound;
import com.skydrm.rmc.ui.project.service.message.MsgNavigate2TargetFolder;
import com.skydrm.rmc.utils.FileUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Stack;

public class FileFragment extends ProjectFileBaseFragment {
    private String mRootPathId = "/";

    private Stack<Integer> mFolderPosition = new Stack<>();
    private Stack<Integer> mFolderOffset = new Stack<>();

    private int mCurrentScrollToFolderPos = -1;
    private int mCurrentScrollOffset = -1;


    public static FileFragment newInstance() {
        return new FileFragment();
    }

    @Override
    public void handleFolderBack() {
        super.handleFolderBack();
        String parentPathId = FileUtils.getParent(mAdapter.getPathId());
        mCurrentScrollToFolderPos = getScrollToFolderPos();
        mCurrentScrollOffset = getScrollToFolderOffset();
        if (parentPathId.equals(mRootPathId)) {
            mCurrentScrollToFolderPos = -1;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveFileUploadMsg(ProjectAddCompleteNotifyEvent event) {
        String parentPathId = event.getParentPathId();
        mPresenter.refresh(getFileType(), parentPathId);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveNavigateFolderMsg(MsgNavigate2TargetFolder msg) {
        String pathId = msg.mPathId;
        String pathDisplay = msg.mPathDisplay;
        mAdapter.setPathId(pathId);
        mAdapter.setPathDisplay(pathDisplay);

        displayPathSite(!pathId.equals(mRootPathId));
        mTvPathDisplay.setText(pathDisplay);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveFileNotFoundMsg(MsgFileNotFound msg) {
        INxlFile f = mAdapter.getWorkingFile();
        if (f == null) {
            return;
        }
        String parentPathId = FileUtils.getParent(f.getPathId());
        String parentPathDisplay = FileUtils.getParent(f.getPathDisplay());

        //Avoid loading recursively.
        if (TextUtils.equals(parentPathId, mAdapter.getPathId())) {
            listCurrent();
            return;
        }

        mAdapter.setPathId(parentPathId);
        mAdapter.setPathDisplay(parentPathDisplay);

        mTvPathDisplay.setText(parentPathDisplay);

        displayPathSite(!parentPathId.equals(mRootPathId));

        listCurrent();
    }

    @Override
    public void update(List<NxlFileItem> data) {
        super.update(data);
        scrollAfterDataRefresh(mCurrentScrollToFolderPos);
    }

    @Override
    protected int getFileType() {
        return NxlFileType.ALL.getValue();
    }

    @Override
    protected void onFolderClick(INxlFile f, int pos) {
        super.onFolderClick(f, pos);
        View view = mLayoutManager.findViewByPosition(pos);
        if (view != null) {
            mFolderOffset.push(view.getTop());
        }
        mFolderPosition.push(pos);
    }

    @Override
    protected boolean isBindEventBusHere() {
        return true;
    }

    private int getScrollToFolderPos() {
        if (mFolderPosition.isEmpty()) {
            return -1;
        }
        return mFolderPosition.pop();
    }

    private int getScrollToFolderOffset() {
        if (mFolderOffset.isEmpty()) {
            return -1;
        }
        return mFolderOffset.pop();
    }

    private void scrollAfterDataRefresh(int pos) {
        if (pos == -1) {
            return;
        }
        mLayoutManager.scrollToPositionWithOffset(pos, mCurrentScrollOffset);
    }

}
