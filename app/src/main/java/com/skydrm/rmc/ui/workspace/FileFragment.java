package com.skydrm.rmc.ui.workspace;

import android.text.TextUtils;

import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.ui.common.NxlFileType;
import com.skydrm.rmc.ui.common.NxlItemHelper;
import com.skydrm.rmc.ui.project.common.MsgFileNotFound;
import com.skydrm.rmc.utils.FileUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class FileFragment extends WorkSpaceBaseFragment {
    private String mRootPathId = "/";

    public static FileFragment newInstance() {
        return new FileFragment();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveNavigateFolderMsg(MsgNavigate2WorkSpaceFolder msg) {
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

        //Avoid dead recursively load.
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
    protected boolean showToolbar() {
        return false;
    }

    @Override
    protected void onToolbarNavigationClick() {

    }

    @Override
    protected int getFileType() {
        return NxlFileType.ALL.getValue();
    }

    @Override
    protected void onFileItemClick(INxlFile f, int pos) {
        NxlItemHelper.viewFile(_activity, f);
    }

    @Override
    protected void onFolderClick(INxlFile f, int pos) {
        mTvPathDisplay.setText(f.getPathDisplay());
        displayPathSite(true);
        listCurrent();
    }

    @Override
    protected String getRootPathId() {
        return mRootPathId;
    }

    @Override
    protected boolean isBindEventBusHere() {
        return true;
    }

    @Override
    protected boolean isOfflineDisplay() {
        return false;
    }
}
