package com.skydrm.rmc.ui.project.feature.service.protect;

import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.ui.base.BaseFragment;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;

public class ProjectMoreOptionsFragment extends BaseFragment {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.tv_cancel)
    TextView mTvCancel;
    @BindView(R.id.toggle_extract)
    SwitchCompat mToggleExtract;

    private boolean mExtractChecked;

    public static ProjectMoreOptionsFragment newInstance() {
        return new ProjectMoreOptionsFragment();
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
        initExtractData();
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishParent();
            }
        });
        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new MsgExtractSwitchStatus(false));
                finishParent();
            }
        });
        mToggleExtract.setChecked(mExtractChecked);
        mToggleExtract.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                EventBus.getDefault().post(new MsgExtractSwitchStatus(isChecked));
            }
        });
    }

    @Override
    protected View getLoadingTargetView() {
        return null;
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.fragment_project_more_options;
    }

    @Override
    protected boolean isBindEventBusHere() {
        return false;
    }

    private void initExtractData() {
        Bundle arguments = getArguments();
        if (arguments == null) {
            return;
        }
        mExtractChecked = arguments.getBoolean(Constant.STATE_EXTRACT_SWITCH);
    }
}
