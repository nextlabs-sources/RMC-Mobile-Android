package com.skydrm.rmc.ui.workspace;

import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.ui.common.NxlFileType;

public class OfflineFragment extends WorkSpaceBaseFragment {

    public static OfflineFragment newInstance() {
        return new OfflineFragment();
    }

    @Override
    protected boolean showToolbar() {
        return false;
    }

    @Override
    protected int getFileType() {
        return NxlFileType.OFFLINE.getValue();
    }

    @Override
    protected String getRootPathId() {
        return "/";
    }

    @Override
    public String getCurrentPathId() {
        return "/";
    }

    @Override
    protected void onToolbarNavigationClick() {

    }

    @Override
    public boolean needInterceptBackPress() {
        return false;
    }

    @Override
    protected void initViewAndEvents() {
        super.initViewAndEvents();
        mAdapter.setDisableRightSwipeMenu(true);
    }

    @Override
    public void notifyItemDelete(int pos) {
        mAdapter.removeItem(pos);
        if (mAdapter.getItemCount() == 0) {
            setEmptyView(true);
        }
    }

    @Override
    protected void unMarkAsOffline(INxlFile f, int pos) {
        f.unMarkAsOffline();
        listCurrent();
    }

    @Override
    protected boolean isOfflineDisplay() {
        return true;
    }

}
