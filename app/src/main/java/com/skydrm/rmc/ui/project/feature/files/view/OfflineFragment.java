package com.skydrm.rmc.ui.project.feature.files.view;

import android.view.View;
import android.widget.LinearLayout;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.ui.common.NxlFileType;
import com.skydrm.rmc.utils.ViewUtils;

import butterknife.BindView;

public class OfflineFragment extends ProjectFileBaseFragment {
    @BindView(R.id.ll_offline_header)
    LinearLayout mLlOfflineHeader;

    public static OfflineFragment newInstance() {
        return new OfflineFragment();
    }

    @Override
    protected void initViewAndEvents() {
        super.initViewAndEvents();
        mAdapter.setDisableLeftSwipeMenu(true);
        mAdapter.setDisableRightSwipeMenu(true);
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.fragment_project_offline_files;
    }

    @Override
    protected void networkConnected(String extraInfo) {
        super.networkConnected(extraInfo);
        if (ViewUtils.isVisible(mLlOfflineHeader)) {
            mLlOfflineHeader.setVisibility(View.GONE);
        }
    }

    @Override
    protected void networkDisconnected() {
        super.networkDisconnected();
        if (ViewUtils.isGone(mLlOfflineHeader)) {
            mLlOfflineHeader.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected int getFileType() {
        return NxlFileType.OFFLINE.getValue();
    }

    @Override
    protected void unMarkAsOffline(INxlFile f, int pos) {
        f.unMarkAsOffline();
        listCurrent();
    }

}
