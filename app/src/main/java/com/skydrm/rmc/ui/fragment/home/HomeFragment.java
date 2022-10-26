package com.skydrm.rmc.ui.fragment.home;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.project.IInvitePending;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.engine.eventBusMsg.account.UpdateUserNameEvent;
import com.skydrm.rmc.engine.eventBusMsg.account.UserLinkedRepoChangedEvent;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.ui.activity.home.main.BandRepositoryAdapter;
import com.skydrm.rmc.ui.activity.home.main.HomeRecycleViewItemClickListener;
import com.skydrm.rmc.ui.activity.home.view.HomePresenter;
import com.skydrm.rmc.ui.activity.home.view.IHomeContact;
import com.skydrm.rmc.ui.activity.profile.ProfileActivity;
import com.skydrm.rmc.ui.activity.repository.RepoSettingActivity;
import com.skydrm.rmc.ui.adapter.RepoSpaceSizeAdapter2;
import com.skydrm.rmc.ui.adapter.RepoSpaceSizeItem;
import com.skydrm.rmc.ui.base.BaseFragment;
import com.skydrm.rmc.ui.base.NavigationType;
import com.skydrm.rmc.ui.project.ProjectActivity;
import com.skydrm.rmc.ui.project.service.IProjectContact;
import com.skydrm.rmc.ui.project.service.NewProjectSplashActivity;
import com.skydrm.rmc.ui.project.service.ProjectPresenter;
import com.skydrm.rmc.ui.project.service.adapter.ProjectCreatedByMeAdapter;
import com.skydrm.rmc.ui.project.service.adapter.ProjectInvitedByOtherAdapter;
import com.skydrm.rmc.ui.widget.popupwindow.HomeContextMenu2;
import com.skydrm.rmc.utils.ViewUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;


public class HomeFragment extends BaseFragment implements IHomeContact.IView, IProjectContact.IView {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.fab)
    FloatingActionButton mFAB;
    @BindView(R.id.tv_user_name)
    TextView mUserName;

    @BindView(R.id.rv_repo_size)
    RecyclerView mRvRepoSpace;

    @BindView(R.id.ll_repo_desc_container)
    LinearLayout mLlRepoDescContainer;
    @BindView(R.id.tv_repo_num)
    TextView mActiveRepoNum;
    @BindView(R.id.rv_banded_repo)
    RecyclerView mRvActiveRepo;

    @BindView(R.id.bt_active_project)
    Button mBtActivateProject;

    @BindView(R.id.tv_created_by_me_num)
    TextView mTvCreatedByMeCount;
    @BindView(R.id.project_owner_by_me)
    RecyclerView mRvProjectCreateByMe;

    @BindView(R.id.tv_invited_by_other_num)
    TextView mTvInvitedByOtherCount;
    @BindView(R.id.project_owner_by_other)
    RecyclerView mRvProjectInvitedByOther;

    @BindView(R.id.bt_update_owner_by_me)
    TextView mTvUpdateOwnerByMe;

    @BindView(R.id.bt_update_owner_by_other)
    TextView mTvUpdateOwnerByOther;

    @BindView(R.id.view_all_projects)
    Button mBtViewAllProjects;

    @BindView(R.id.project_container)
    LinearLayout mLlProjectContainer;
    @BindView(R.id.project_activate_container)
    LinearLayout mLlProjectActivateContainer;
    @BindView(R.id.project_created_by_me_container)
    LinearLayout mLlProjectCreatedByMeContainer;
    @BindView(R.id.project_invited_by_other_container)
    LinearLayout mLlProjectInvitedByOtherContainer;

    private RepoSpaceSizeAdapter2 mRepoSpaceAdapter;
    private BandRepositoryAdapter mBandedRepoAdapter;

    private ProjectCreatedByMeAdapter mCreatedByMeAdapter;
    private ProjectInvitedByOtherAdapter mInviteByOtherAdapter;

    //    private ScaleViewPage invitedByOthers;
    private int spacingInPixels = 40;
    private int projectSpacingInPixels = 40;

    private boolean isCreatedByMeEmpty;
    private boolean isInvitedByOtherEmpty;

    private IHomeContact.IPresenter mHomePresenter;
    private IProjectContact.IPresenter mProjectPresenter;

    /**
     * For fragment create use
     *
     * @param index Use index can change the data displayed.
     * @return {@link HomeFragment}
     */
    public static HomeFragment newInstance(int index) {
        Bundle args = new Bundle();
        args.putInt(Constant.FRAGMENT_INDEX, index);
        HomeFragment homeFragment = new HomeFragment();
        homeFragment.setArguments(args);
        return homeFragment;
    }

    /**
     * The content view the fragment displayed.
     *
     * @return Resource id of the current fragment
     */
    @Override
    protected int getContentViewLayoutID() {
        return R.layout.fragment_home;
    }

    /**
     * initialize view and bind events
     */
    @Override
    protected void initViewAndEvents() {
        initToolbarNavi(mToolbar, false);
        mBtViewAllProjects.setVisibility(View.GONE);
        mLlProjectCreatedByMeContainer.setVisibility(View.GONE);
        mLlProjectInvitedByOtherContainer.setVisibility(View.GONE);
        mLlProjectActivateContainer.setVisibility(View.GONE);

        initRepoSpaceAdapter();
        initActiveRepoAdapter();

        initProjectCreatedByMeAdapter();
        initProjectInviteByOther();

        initListener();

        mHomePresenter = new HomePresenter(this);
        mProjectPresenter = new ProjectPresenter(this);
    }

    @Override
    protected void onPremiseLogin() {
        super.onPremiseLogin();
        mLlRepoDescContainer.setVisibility(View.GONE);
        mRvActiveRepo.setVisibility(View.GONE);
        mLlProjectActivateContainer.setVisibility(View.GONE);
    }

    /**
     * initialize data here (Data will be loaded only one time.)
     */
    @Override
    protected void onUserFirstVisible() {
        mHomePresenter.initialize();
        mProjectPresenter.getProjectsAndResetOperationStatus(0);
        mProjectPresenter.getPendingInvitation();
    }

    /**
     * On fragment resume.
     */
    @Override
    protected void onUserVisible() {
        mHomePresenter.getUserInfo();
        if (!isOnPremise()) {
            mHomePresenter.getBandedRepo();
        }
        //async
        mHomePresenter.getStorageUsed(isOnPremise());
        mProjectPresenter.getProjects(0);
        mProjectPresenter.getPendingInvitation();
    }

    /**
     * On fragment pause.
     */
    @Override
    protected void onUserInvisible() {

    }

    /**
     * For view to display different loading states
     *
     * @return loading view
     */
    @Override
    protected View getLoadingTargetView() {
        return mLlProjectContainer;
    }

    /**
     * - UpdateUserName
     * - SyncUserRepoWithRMS
     */
    @Override
    protected boolean isBindEventBusHere() {
        return true;
    }

    /* pushed from background, when user name has been changed*/
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateNameEventHandler(UpdateUserNameEvent event) {
        mHomePresenter.getUserInfo();
    }

    @Override
    public void showUserName(String name) {
        mUserName.setText(name);
    }

    @Override
    public void showStorageUsed(long totalLong, long totalUsageSize,
                                long myDriveUsageSize, long myVaultUsageSize) {
        mRepoSpaceAdapter.setMyDriveData(RepoSpaceSizeItem.createDriveItems(_activity, myDriveUsageSize, myVaultUsageSize));
    }

    @Override
    public void showWorkSpaceInfo(long usage, long quota, int totalFiles) {
        mRepoSpaceAdapter.setWorkSpaceData(RepoSpaceSizeItem.createWorkSpaceItem(_activity, usage, totalFiles));
    }

    /* pushed from background, when sync user linked account with RMS has been changed*/
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSyncUserRepoWithRMSHandler(UserLinkedRepoChangedEvent event) {
        mHomePresenter.getBandedRepo();
    }

    @Override
    public void showBandedRepo(List<BoundService> services) {
        mBandedRepoAdapter.setData(services);
        mBandedRepoAdapter.notifyDataSetChanged();
        mActiveRepoNum.setText(String.valueOf(services.size()));
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
        if (createdByMe != null && createdByMe.size() != 0) {
            isCreatedByMeEmpty = false;
            if (mBtViewAllProjects.getVisibility() == View.GONE) {
                mBtViewAllProjects.setVisibility(View.VISIBLE);
            }
            if (mLlProjectCreatedByMeContainer.getVisibility() == View.GONE) {
                mLlProjectCreatedByMeContainer.setVisibility(View.VISIBLE);
            }
            // For pro we just hide the activate site.
//            if (mLlProjectActivateContainer.getVisibility() == View.VISIBLE) {
//                mLlProjectActivateContainer.setVisibility(View.GONE);
//            }
            if (mTvUpdateOwnerByMe.getVisibility() == View.VISIBLE) {
                mTvUpdateOwnerByMe.setVisibility(View.INVISIBLE);
            }
            mTvCreatedByMeCount.setText(String.valueOf(createdByMe.size()));
            mCreatedByMeAdapter.setData(createdByMe);
        } else {
            isCreatedByMeEmpty = true;
            if (mLlProjectCreatedByMeContainer.getVisibility() != View.GONE) {
                mLlProjectCreatedByMeContainer.setVisibility(View.GONE);
            }
            if (isInvitedByOtherEmpty) {
                if (ViewUtils.isVisible(mBtViewAllProjects)) {
                    mBtViewAllProjects.setVisibility(View.GONE);
                }
            }
            // For pro we just hide the activate site.
//            if ((mLlProjectActivateContainer.getVisibility() != View.VISIBLE)) {
//                mLlProjectActivateContainer.setVisibility(View.VISIBLE);
//            }
        }
    }

    @Override
    public void showInvitedByOtherProjects(List<IProject> invitedByOther) {
        if (invitedByOther != null && invitedByOther.size() != 0) {
            isInvitedByOtherEmpty = false;
            if (mBtViewAllProjects.getVisibility() == View.GONE) {
                mBtViewAllProjects.setVisibility(View.VISIBLE);
            }
            if (mLlProjectInvitedByOtherContainer.getVisibility() == View.GONE) {
                mLlProjectInvitedByOtherContainer.setVisibility(View.VISIBLE);
            }
            if (mTvUpdateOwnerByOther.getVisibility() == View.VISIBLE) {
                mTvUpdateOwnerByOther.setVisibility(View.INVISIBLE);
            }
            mTvInvitedByOtherCount.setText(String.valueOf(invitedByOther.size()));
            mInviteByOtherAdapter.setData(invitedByOther);
        } else {
            isInvitedByOtherEmpty = true;
            if (mLlProjectInvitedByOtherContainer.getVisibility() != View.GONE) {
                mLlProjectInvitedByOtherContainer.setVisibility(View.GONE);
            }
            if (isCreatedByMeEmpty) {
                if (ViewUtils.isVisible(mBtViewAllProjects)) {
                    mBtViewAllProjects.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public void showErrorView(Exception e) {
        ExceptionHandler.handleException(_activity, e);
    }

    @Override
    public void onError(Exception e) {
        ExceptionHandler.handleException(_activity, e);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mHomePresenter != null) {
            mHomePresenter.onDestroy();
            mHomePresenter = null;
        }
        if (mProjectPresenter != null) {
            mProjectPresenter.onDestroy();
            mProjectPresenter = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHomePresenter != null) {
            mHomePresenter.onDestroy();
            mHomePresenter = null;
        }
        if (mProjectPresenter != null) {
            mProjectPresenter.onDestroy();
            mProjectPresenter = null;
        }
    }

    private void initRepoSpaceAdapter() {
        LinearLayoutManager SpaceSizeLinearLayoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        mRvRepoSpace.setLayoutManager(SpaceSizeLinearLayoutManager);
        mRepoSpaceAdapter = new RepoSpaceSizeAdapter2();
        mRepoSpaceAdapter.setOnItemClickListener(new RepoSpaceSizeAdapter2.OnItemClickListener() {
            @Override
            public void onItemClick(NavigationType type) {
                beginNavigation(type);
            }
        });
        mRvRepoSpace.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
        mRvRepoSpace.setAdapter(mRepoSpaceAdapter);
    }

    private void initActiveRepoAdapter() {
        LinearLayoutManager bandRepositoryLinearLayoutManager = new LinearLayoutManager(_activity,
                LinearLayoutManager.HORIZONTAL, false);
        mRvActiveRepo.setLayoutManager(bandRepositoryLinearLayoutManager);
        mBandedRepoAdapter = new BandRepositoryAdapter(_activity);
        mBandedRepoAdapter.setItemClickListener(new OnHomeCardsSelect());
        mRvActiveRepo.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
        mRvActiveRepo.setAdapter(mBandedRepoAdapter);
    }

    private void initProjectCreatedByMeAdapter() {
        LinearLayoutManager ownerByMeLayoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        mRvProjectCreateByMe.setLayoutManager(ownerByMeLayoutManager);
        mRvProjectCreateByMe.setNestedScrollingEnabled(false);
        mRvProjectCreateByMe.addItemDecoration(new SpacesItemDecoration(projectSpacingInPixels));
        mCreatedByMeAdapter = new ProjectCreatedByMeAdapter(_activity);
        mCreatedByMeAdapter.setMaxLimit(5);
        mRvProjectCreateByMe.setAdapter(mCreatedByMeAdapter);

        mCreatedByMeAdapter.setOnItemClickListener(new ProjectCreatedByMeAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(IProject p) {
                handlerProjectCardClick(p);
            }
        });
        mTvUpdateOwnerByMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProjectPresenter.getProjects(1);
                if (mTvUpdateOwnerByMe.getVisibility() == View.VISIBLE) {
                    mTvUpdateOwnerByMe.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void initProjectInviteByOther() {
        LinearLayoutManager ownerByOtherLayoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        mRvProjectInvitedByOther.setLayoutManager(ownerByOtherLayoutManager);
        mRvProjectInvitedByOther.addItemDecoration(new SpacesItemDecoration(projectSpacingInPixels));
        mInviteByOtherAdapter = new ProjectInvitedByOtherAdapter(_activity);
        mInviteByOtherAdapter.setMaxLimit(5);
        mRvProjectInvitedByOther.setAdapter(mInviteByOtherAdapter);

        mTvUpdateOwnerByOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProjectPresenter.getProjects(2);
                if (mTvUpdateOwnerByOther.getVisibility() == View.VISIBLE) {
                    mTvUpdateOwnerByOther.setVisibility(View.INVISIBLE);
                }
            }
        });
        mInviteByOtherAdapter.setOnInvitationItemClickListener(new ProjectInvitedByOtherAdapter.
                OnInvitationItemClickListener() {
            @Override
            public void onAccept(IInvitePending pending, View loadingBar, int pos) {
                mProjectPresenter.acceptInvitation(pending, loadingBar);
            }

            @Override
            public void onDeny(IInvitePending pending, View loadingBar, int pos) {
                showDenyDialog(pending, loadingBar);
            }
        });
        mInviteByOtherAdapter.setOnItemClickListener(new ProjectInvitedByOtherAdapter.
                OnItemClickListener() {
            @Override
            public void onItemClick(IProject p, int position) {
                handlerProjectCardClick(p);
            }
        });
    }

    private void showDenyDialog(final IInvitePending pending, final View loadingBar) {
        AlertDialog.Builder builder = new AlertDialog.Builder(_activity);
        final View ignoreView = LayoutInflater.from(_activity).inflate(R.layout.layout_ignore_invite_dialog, null);
        final EditText et_ignore = ignoreView.findViewById(R.id.ev_ignoreReason);
        builder.setCancelable(false);
        builder.setView(ignoreView);
        builder.setPositiveButton(_activity.getResources().getString(R.string.Decline), null);
        builder.setNegativeButton(_activity.getResources().getString(R.string.common_cancel_initcap), null);
        builder.setTitle(R.string.app_name);
        builder.setMessage(_activity.getResources().getString(R.string.hint_msg_ask_ignore_reason));
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reason = et_ignore.getText().toString();
                dialog.dismiss();
                mProjectPresenter.denyInvitation(pending, reason, loadingBar);
            }
        });
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void initListener() {
        mUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(_activity, ProfileActivity.class);
                startActivity(intent);
            }
        });
        mBtActivateProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(_activity, NewProjectSplashActivity.class);
                startActivity(intent);
            }
        });
        mBtViewAllProjects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beginNavigation(NavigationType.TYPE_VIEW_ALL_PROJECTS);
            }
        });
        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCtxMenu();
            }
        });
    }

    private void showCtxMenu() {
        FragmentManager fm = getFragmentManager();
        if (fm == null) {
            return;
        }
        HomeContextMenu2 menu = HomeContextMenu2.newInstance();
        menu.setCreateProjectSiteVisibility(View.VISIBLE);
        menu.setSubContextVisibility(View.GONE);
        menu.show(fm, HomeContextMenu2.class.getSimpleName());
    }

    private void handlerProjectCardClick(IProject p) {
        Intent intentProjectActivity = new Intent(_activity, ProjectActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constant.PROJECT_DETAIL, (Parcelable) p);
        bundle.putInt(Constant.PROJECT_INDEX, 0);
        intentProjectActivity.putExtras(bundle);
        startActivity(intentProjectActivity);
    }

    public static class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {

            outRect.right = space;
            outRect.bottom = space;
            outRect.top = space;

            // Add top margin only for the first item to avoid double space between items
            if (parent.getChildLayoutPosition(view) == 0)
                outRect.left = space;
        }
    }

    private class OnHomeCardsSelect implements HomeRecycleViewItemClickListener {

        @Override
        public void onSelectMySpace() {
            beginNavigation(NavigationType.TYPE_TO_MYVAULT);
        }

        @Override
        public void onSelectMyDrive() {
            beginNavigation(NavigationType.TYPE_TO_MYDRIVE);
        }

        @Override
        public void onSelectMyVault() {
            beginNavigation(NavigationType.TYPE_TO_MYVAULT);
        }

        @Override
        public void onSelectWorkSpace() {
            beginNavigation(NavigationType.TYPE_TO_WORKSPACE);
        }

        @Override
        public void onSelectRepository(BoundService boundService) {
            beginNavigationToRepo(boundService);
        }

        @Override
        public void onSelectConnectRepository() {
            Intent intent = new Intent();
            intent.setClass(_activity, RepoSettingActivity.class);
            _activity.startActivity(intent);
        }

    }
}
