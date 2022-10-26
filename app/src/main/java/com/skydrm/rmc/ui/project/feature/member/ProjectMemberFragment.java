package com.skydrm.rmc.ui.project.feature.member;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.project.IMember;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.datalayer.repo.project.Member;
import com.skydrm.rmc.dbbridge.base.Owner;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.ui.DividerItemDecoration;
import com.skydrm.rmc.ui.base.BaseFragment;
import com.skydrm.rmc.ui.project.common.ProjectContextMenu;
import com.skydrm.rmc.ui.project.common.SortMenu;
import com.skydrm.rmc.ui.project.feature.configuration.UpdateNameMsg;
import com.skydrm.rmc.ui.project.feature.member.info.MemberInfoActivity;
import com.skydrm.rmc.ui.project.service.SwitchProjectActivity;
import com.skydrm.rmc.ui.service.search.SearchActivity;
import com.skydrm.rmc.ui.widget.NxlSwipeRefreshLayout;
import com.skydrm.rmc.ui.widget.swipelayout.SwipeMenuRecyclerView;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;
import com.skydrm.rmc.utils.sort.SortType;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;

public class ProjectMemberFragment extends BaseFragment implements IMemberContact.IView {
    @BindView(R.id.project_members_toolbar3)
    Toolbar mToolbar;
    @BindView(R.id.swipeToLoadLayout)
    NxlSwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.recyclerView)
    SwipeMenuRecyclerView mRecyclerView;
    @BindView(R.id.project_members_fab)
    FloatingActionButton mFab;
    @BindView(R.id.to_switch_project_activity)
    ImageButton mIb2ProjectListActivity;

    @BindView(R.id.bt_refresh)
    Button mBtRefresh;

    private IProject mProject;
    private ProjectMemberAdapter mMemberAdapter;
    private IMemberContact.IPresenter mPresenter;
    private ProjectContextMenu mProjectCtxMenu;
    private SortType mSortType = SortType.NAME_ASCEND;


    public static ProjectMemberFragment newInstance() {
        return new ProjectMemberFragment();
    }

    public void updateContactParcel(Intent data) {
        mProjectCtxMenu.wrapEmails(data);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveInvitationMsg(InvitationMsg msg) {
        refresh();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveUpdateNameMsg(UpdateNameMsg msg) {
        mToolbar.setTitle(msg.name);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        CommonUtils.releaseResource(mPresenter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CommonUtils.releaseResource(mPresenter);
    }

    @Override
    public void onInitialize(boolean active) {
        if (active) {
            showLoading(_activity.getString(R.string.wait_load));
        } else {
            hideLoading();
        }
    }

    @Override
    public void showLoadingIndicator(boolean active) {
        mSwipeRefreshLayout.setRefreshing(active);
    }

    @Override
    public void update(List<MemberItem> data) {
        mMemberAdapter.setData(data);
    }

    @Override
    public void showEmptyView(boolean active) {
        if (active) {
            showEmpty(_activity.getString(R.string.Empty_Data));
        } else {
            hideEmpty();
        }
    }

    @Override
    public void showErrorView(Exception e) {
        ExceptionHandler.handleException(_activity, e);
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    protected void onUserFirstVisible() {
        mPresenter.initialize();
    }

    @Override
    protected void onUserVisible() {
        mPresenter.refresh();
    }

    @Override
    protected void onUserInvisible() {

    }

    @Override
    protected void initViewAndEvents() {
        resolveBundle();
        if (mProject == null) {
            return;
        }
        initToolbar();
        initRecyclerView();
        initListener();
        initProjectCtxMenu();
        mPresenter = new MemberPresenter(mProject, this);
    }

    @Override
    protected View getLoadingTargetView() {
        return mSwipeRefreshLayout;
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.fragment_projects_members3;
    }

    @Override
    protected boolean isBindEventBusHere() {
        return true;
    }

    private void resolveBundle() {
        Bundle arguments = getArguments();
        mProject = arguments.getParcelable(Constant.PROJECT_DETAIL);
    }

    private void initToolbar() {
        initToolbarNavi(mToolbar, true);
        mToolbar.setTitle(mProject.getName());
        mToolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
        mToolbar.inflateMenu(R.menu.menu_project_summary);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_settings:
                        showSortMenu();
                        break;
                    case R.id.action_search:
                        searchMember();
                        break;
                }
                return true;
            }
        });
    }

    private void initRecyclerView() {
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.main_green_light),
                getResources().getColor(R.color.color_blue));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(_activity);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(_activity, null,
                true, true));
        mRecyclerView.configAnimator();

        mMemberAdapter = new ProjectMemberAdapter(_activity);
        mRecyclerView.setAdapter(mMemberAdapter);
        mMemberAdapter.setOnItemClickListener(new ProjectMemberAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(IMember member) {
                showMemberInfo(member);
            }
        });
    }

    private void initListener() {
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProjectCtxMenu();
            }
        });
        mIb2ProjectListActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lunch2ProjectListActivity();
            }
        });
        mBtRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });
    }

    private void initProjectCtxMenu() {
        mProjectCtxMenu = ProjectContextMenu.newInstance();
        mProjectCtxMenu.setProject(mProject);
        mProjectCtxMenu.bindContext(_activity);
        mProjectCtxMenu.setHideAddFileMenuItem(true);
        mProjectCtxMenu.setHideCreateNewFolderMenuItem(true);
        mProjectCtxMenu.setHideScanDocMenuItem(true);
        mProjectCtxMenu.setHideGoToAllProjectsMenuItem(true);
    }

    private void showProjectCtxMenu() {
        //mProjectCtxMenu.show(getFragmentManager(), "ProjectContextMenu");
        mProjectCtxMenu.showInviteDialog();
    }

    private void showSortMenu() {
        SortMenu menu = new SortMenu(_activity,
                new SortMenu.OnSortByItemSelectListener() {
                    @Override
                    public void onItemSelected(SortType type) {
                        mSortType = type;
                        mPresenter.sort(type);
                    }
                });
        menu.setSortType(mSortType);
        menu.hideSizeButton();
        menu.showAtLocation(mRootView, Gravity.BOTTOM, 0, 0);
    }

    private void lunch2ProjectListActivity() {
        Intent intent = new Intent(_activity, SwitchProjectActivity.class);
        intent.putExtra(Constant.KEY, Constant.FLAG_FROM_PROJECT);
        intent.putExtra("project_name", mProject.getName());
        intent.putExtra("project_id", mProject.getId());
        startActivity(intent);
    }

    private void searchMember() {
        Intent i = new Intent(_activity, SearchActivity.class);
        i.setAction(Constant.ACTION_SEARCH_PROJECT_MEMBERS);
        i.putExtra("project_id", mProject.getId());
        i.putExtra("is_created_by_me", mProject.isOwnedByMe());
        //searchIntent.putExtra("project_files_members_data", new MessageSearchFromProjectMembers(projectPeopleAdapter3.getmListMembers(), projectPeopleAdapter3.getmPendingList()));
        i.putExtra("owner", (Owner) mProject.getOwner());
        _activity.startActivity(i);
    }

    private void refresh() {
        mPresenter.refresh();
    }

    private void showMemberInfo(IMember member) {
        Intent i = new Intent(_activity, MemberInfoActivity.class);
        i.putExtra(Constant.PROJECT_OWNER_BY_ME, mProject.isOwnedByMe());
        i.putExtra(Constant.MEMBER_DETAIL, (Member) member);
        _activity.startActivity(i);
    }
}
