package com.skydrm.rmc.ui.service.share;

import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.ui.base.BaseFragment;

import butterknife.BindView;

public class SharedWithFragment extends BaseFragment {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.tv_cancel)
    TextView mTvCancel;
    @BindView(R.id.tv_project_site)
    TextView mTvProjectSite;
    @BindView(R.id.tv_user_site)
    TextView mTvUserSite;
    @BindView(R.id.tv_workspace_site)
    TextView mTvWorkSpaceSite;
    @BindView(R.id.bt_next)
    Button mBtNext;

    public static SharedWithFragment newInstance() {
        return new SharedWithFragment();
    }

    @Override
    protected void onUserFirstVisible() {

    }

    @Override
    protected void onUserVisible() {

    }

    @Override
    protected void onUserInvisible() {

    }

    @Override
    protected void initViewAndEvents() {

    }

    @Override
    protected View getLoadingTargetView() {
        return null;
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.fragment_shared_with;
    }

    @Override
    protected boolean isBindEventBusHere() {
        return false;
    }

}
