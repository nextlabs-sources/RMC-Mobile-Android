package com.skydrm.rmc.ui.project.feature.service.share;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.ui.DividerItemDecoration;
import com.skydrm.rmc.ui.base.BaseFragment;
import com.skydrm.rmc.ui.project.service.IProjectContact;
import com.skydrm.rmc.ui.project.service.ProjectOperateActivity;
import com.skydrm.rmc.ui.project.service.ProjectPresenter;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;

import java.util.List;

import butterknife.BindView;

public class ProjectSelectFragment extends BaseFragment implements IProjectContact.IView {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.tv_cancel)
    TextView mTvCancel;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    private IProjectContact.IPresenter mPresenter;
    private ProjectSelectAdapter mAdapter;

    public static ProjectSelectFragment newInstance() {
        return new ProjectSelectFragment();
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
        mPresenter = new ProjectPresenter(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(_activity));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(_activity, null,
                false, true));
        mAdapter = new ProjectSelectAdapter();
        mAdapter.setIgnoreDisplayProjectId(projectId);
        mRecyclerView.setAdapter(mAdapter);
        initEvents();
    }

    @Override
    protected View getLoadingTargetView() {
        return mRecyclerView;
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.fragment_project_select;
    }

    @Override
    protected boolean isBindEventBusHere() {
        return false;
    }

    private void initEvents() {
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishParent();
            }
        });
        mAdapter.setOnItemClickListener(new ProjectSelectAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(IProject p, int pos) {
                p.syncClassificationWithInterval();
                if (_activity instanceof ProjectOperateActivity) {
                    ProjectOperateActivity activity = (ProjectOperateActivity) _activity;
                    activity.replaceLoadProjectAddFileFrag(p);
                }
            }
        });
        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishParent();
            }
        });
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

    }

    @Override
    public void showCreatedByMeProjects(List<IProject> createdByMe) {
        mAdapter.setOwnerByMeData(createdByMe);
    }

    @Override
    public void showInvitedByOtherProjects(List<IProject> invitedByOther) {
        mAdapter.setOwnerByOtherData(invitedByOther);
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
        CommonUtils.releaseResource(mAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.onDestroy();
        }
        CommonUtils.releaseResource(mAdapter);
    }
}
