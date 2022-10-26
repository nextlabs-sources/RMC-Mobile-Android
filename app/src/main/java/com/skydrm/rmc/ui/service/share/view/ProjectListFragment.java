package com.skydrm.rmc.ui.service.share.view;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.ui.base.BaseFragment;
import com.skydrm.rmc.ui.project.service.IProjectContact;
import com.skydrm.rmc.ui.project.service.ProjectPresenter;
import com.skydrm.rmc.ui.service.share.MsgProjectRecipients;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;

public class ProjectListFragment extends BaseFragment implements IProjectContact.IView {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.tv_cancel)
    TextView mTvCancel;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mRefreshLayout;
    @BindView(R.id.expandable_list_view)
    ExpandableListView mExpandLv;
    @BindView(R.id.bt_share)
    Button mBtShare;

    private IProjectContact.IPresenter mPresenter;
    private ProjectListAdapter mAdapter;

    public static ProjectListFragment newInstance() {
        return new ProjectListFragment();
    }

    @Override
    protected void onUserFirstVisible() {
        mPresenter.getProjects(0);
    }

    @Override
    protected void onUserVisible() {

    }

    @Override
    protected void onUserInvisible() {

    }

    @Override
    protected void initViewAndEvents() {
        Bundle args = getArguments();
        int projectId = -1;
        if (args != null) {
            projectId = args.getInt(Constant.PROJECT_ID);
        }
        mRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.main_green_light),
                getResources().getColor(R.color.color_blue));
        mPresenter = new ProjectPresenter(this);
        mAdapter = new ProjectListAdapter();
        mAdapter.setIgnoreDisplayProjectId(projectId);
        mAdapter.setSelectedRecipients((List<String>) args.getSerializable(Constant.RECIPIENTS));
        mExpandLv.setAdapter(mAdapter);
        initEvents();
    }

    @Override
    protected View getLoadingTargetView() {
        return mExpandLv;
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.fragment_project_list;
    }

    @Override
    protected boolean isBindEventBusHere() {
        return false;
    }

    @Override
    public void onInitialize(boolean active) {
        if (active) {
            showLoading("");
        } else {
            hideLoading();
        }
    }

    @Override
    public void showLoadingIndicator(boolean active) {
        mRefreshLayout.setRefreshing(active);
    }

    @Override
    public void showCreatedByMeProjects(List<IProject> createdByMe) {
        mAdapter.setOwnerByMeData(_activity, createdByMe);
        if (createdByMe != null && !createdByMe.isEmpty()) {
            mExpandLv.expandGroup(0);
        }
    }

    @Override
    public void showInvitedByOtherProjects(List<IProject> invitedByOther) {
        mAdapter.setOwnerByOtherData(_activity, invitedByOther);
        if (invitedByOther != null && !invitedByOther.isEmpty()) {
            mExpandLv.expandGroup(mAdapter.isCreateByMeEmpty() ? 0 : 1);
        }
    }

    @Override
    public void showErrorView(Exception e) {
        ExceptionHandler.handleException(_activity, e);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mPresenter != null) {
            mPresenter.onDestroy();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.onDestroy();
        }
    }

    private void initEvents() {
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _activity.popupFragment();
            }
        });
        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishParent();
            }
        });
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.refresh();
            }
        });
        mExpandLv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                ProjectNormalItem child = mAdapter.getData().get(groupPosition).getChildren().get(childPosition);
                child.setSelected(!child.isSelected());
                mAdapter.notifyDataSetChanged();
                return true;
            }
        });
        mBtShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new MsgProjectRecipients(getAllDisplayRecipients(),
                        getSelectedRecipients()));
                _activity.popupFragment();
            }
        });
    }

    private List<Integer> getAllDisplayRecipients() {
        List<Integer> ret = new ArrayList<>();
        List<IProject> all = mAdapter.getAll();
        if (all == null || all.isEmpty()) {
            return ret;
        }
        for (IProject p : all) {
            if (p == null) {
                continue;
            }
            ret.add(p.getId());
        }
        return ret;
    }

    private Map<String, String> getSelectedRecipients() {
        Map<String, String> ret = new HashMap<>();
        List<IProject> selected = mAdapter.getSelectData();
        if (selected == null || selected.isEmpty()) {
            return ret;
        }
        for (IProject p : selected) {
            if (p == null) {
                continue;
            }
            ret.put(p.getName(), String.valueOf(p.getId()));
        }
        return ret;
    }
}
