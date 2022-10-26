package com.skydrm.rmc.ui.project.feature.summary;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.base.NxlDoc;
import com.skydrm.rmc.datalayer.repo.project.IMember;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.ui.DividerItemDecoration;
import com.skydrm.rmc.ui.base.BaseFragment;
import com.skydrm.rmc.ui.common.NxlAdapter;
import com.skydrm.rmc.ui.common.NxlFileItem;
import com.skydrm.rmc.ui.common.NxlItemHelper;
import com.skydrm.rmc.ui.common.RightMenuItemClickListener;
import com.skydrm.rmc.ui.service.offline.IOfflineCallback;
import com.skydrm.rmc.ui.service.offline.exception.OfflineException;
import com.skydrm.rmc.ui.project.common.FileContextMenu;
import com.skydrm.rmc.ui.project.common.MsgFileNotFound;
import com.skydrm.rmc.ui.project.common.ProjectContextMenu;
import com.skydrm.rmc.ui.project.feature.configuration.UpdateNameMsg;
import com.skydrm.rmc.ui.project.service.ProjectOperateActivity;
import com.skydrm.rmc.ui.project.service.SwitchProjectActivity;
import com.skydrm.rmc.ui.service.share.ShareActivity;
import com.skydrm.rmc.ui.widget.NxlSwipeRefreshLayout;
import com.skydrm.rmc.ui.widget.ProjectInflateIconHelper;
import com.skydrm.rmc.ui.widget.customcontrol.FlowLayout;
import com.skydrm.rmc.ui.widget.swipelayout.SwipeMenuRecyclerView;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;

public class ProjectSummaryFragment extends BaseFragment implements ISummaryContact.IView {
    @BindView(R.id.project_files_toolbar3)
    Toolbar mToolbar;
    @BindView(R.id.project_members)
    FlowLayout mFlMember;
    @BindView(R.id.project_summary_description)
    TextView mTvDesc;
    @BindView(R.id.to_switch_project_activity)
    ImageButton mIb2ProjectActivity;
    @BindView(R.id.swipeToLoadLayout)
    NxlSwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.recyclerView)
    SwipeMenuRecyclerView mSwipeMenuRecyclerView;
    @BindView(R.id.summary_fab)
    FloatingActionButton mFab;

    @BindView(R.id.pb_project_usage)
    ProgressBar mPbProjectUsage;
    @BindView(R.id.project_used)
    TextView mTvProjectUsed;
    @BindView(R.id.project_free)
    TextView mTvProjectFree;


    private ProjectInflateIconHelper mIconHelper;
    private NxlAdapter mFilesAdapter;

    public long tempQuota = 0;
    public long tempUsage = 0;


    private IProject mProject;

    private ISummaryContact.IPresenter mPresenter;
    private ProjectContextMenu mProjectCtxMenu;
    private FileContextMenu mFileCtxMenu;

    public static ProjectSummaryFragment newInstance() {
        return new ProjectSummaryFragment();
    }

    @Override
    protected void onUserFirstVisible() {
        mPresenter.initialize();
    }

    @Override
    protected void onUserVisible() {
        mPresenter.listMemberAndFile();
    }

    @Override
    protected void onUserInvisible() {
    }

    @Override
    protected void initViewAndEvents() {
        Bundle arguments = getArguments();
        if (arguments == null) {
            return;
        }
        mProject = arguments.getParcelable(Constant.PROJECT_DETAIL);
        if (mProject == null) {
            return;
        }
        initToolbarNavi(mToolbar, true);
        mToolbar.setTitle(mProject.getName());
        mToolbar.setTitleTextColor(getResources().getColor(android.R.color.black));

        mTvDesc.setText(mProject.getDescription().trim());
        initRecycleView();
        initMenu();
        initListener();
        mPresenter = new SummaryPresenter(mProject, this);
        mIconHelper = new ProjectInflateIconHelper(_activity);
    }

    @Override
    protected View getLoadingTargetView() {
        return mSwipeRefreshLayout;
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.fragment_projects_summary3;
    }

    @Override
    protected boolean isBindEventBusHere() {
        return true;
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
    public void showLoadingIndicator(boolean show) {
        mSwipeRefreshLayout.setRefreshing(show);
    }

    @Override
    public void showProjectSpace(long quota, long usage) {
        if (tempQuota != quota || tempUsage != usage) {
            tempQuota = quota;
            tempUsage = usage;
            mPbProjectUsage.setMax(100);
            int progress = (int) Math.ceil(usage * 100.0 / quota);
            mPbProjectUsage.setProgress(progress);
            mTvProjectUsed.setText(Formatter.formatFileSize(_activity, usage).concat(" used"));
            mTvProjectFree.setText(Formatter.formatFileSize(_activity, quota - usage).concat(" free"));
        }
    }

    @Override
    public void displayMember(List<IMember> members) {
        if (members != null && members.size() != 0) {
            mIconHelper.inflateInitial(mFlMember, members);
        }
    }

    @Override
    public void update(List<NxlFileItem> recent) {
        mFilesAdapter.setData(recent);
    }

    @Override
    public void showEmpty(boolean active) {
        if (active) {
            showEmpty("");
        } else {
            hideEmpty();
        }
    }

    @Override
    public void showErrorView(Exception e) {
        ExceptionHandler.handleException(_activity, e);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveUpdateNameMsg(UpdateNameMsg msg) {
        mToolbar.setTitle(msg.name);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveFileNotFoundMsg(MsgFileNotFound msg) {
        if (mPresenter != null) {
            mPresenter.refresh();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        releaseRsr();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseRsr();
    }

    private void initRecycleView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(_activity);
        mSwipeMenuRecyclerView.setLayoutManager(layoutManager);
        mSwipeMenuRecyclerView.configAnimator();
        mSwipeMenuRecyclerView.addItemDecoration(new DividerItemDecoration(_activity, null,
                true, true));

        mFilesAdapter = new NxlAdapter(_activity);
        mFilesAdapter.setCreatedByMe(mProject.isOwnedByMe());
        mSwipeMenuRecyclerView.setAdapter(mFilesAdapter);
        mFilesAdapter.setOnItemClickListener(new NxlAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(INxlFile f, int pos) {
                //Recent file should never contains folder.
                if (f.isFolder()) {
                    return;
                }
                NxlItemHelper.viewFile(_activity, mProject.getId(), mProject.getName(), f);
            }
        });
        mFilesAdapter.setOnMenuToggleListener(new NxlAdapter.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(INxlFile f, int pos) {
                mFileCtxMenu.setFile(f);
                mFileCtxMenu.show(getFragmentManager(), FileContextMenu.class.getSimpleName());
            }
        });
        mFilesAdapter.setOnLeftMenuItemClickListener(new NxlAdapter.OnLeftMenuItemClickListener() {
            @Override
            public void onButton01Click(INxlFile f, final int pos, int type) {
                NxlItemHelper.showDeleteDialog(_activity, f,
                        new NxlItemHelper.OnDeleteButtonClickListener() {
                            @Override
                            public void onClick(INxlFile f) {
                                mPresenter.delete(f);
                            }
                        });
            }
        });
        mFilesAdapter.setOnRightMenuItemClickListener(new RightMenuItemClickListener(_activity,
                mProject.getId(), mProject.getName()));
    }

    private void initMenu() {
        initProjectCtxMenu();
        initFileCtxMenu();
    }

    private void initProjectCtxMenu() {
        mProjectCtxMenu = ProjectContextMenu.newInstance();
        mProjectCtxMenu.setProject(mProject);
        mProjectCtxMenu.setCurrentPathId("/");
    }

    private void initFileCtxMenu() {
        mFileCtxMenu = FileContextMenu.newInstance();
        mFileCtxMenu.setProjectId(mProject.getId());
        mFileCtxMenu.setProjectName(mProject.getName());
        mFileCtxMenu.setOwnerByMe(mProject.isOwnedByMe());
        mFileCtxMenu.setOnMarkOfflineClickListener(new FileContextMenu.OnMarkOfflineClickListener() {
            @Override
            public void onClick(INxlFile f, boolean offline, int pos) {
                if (offline) {
                    unMarkAsOffline(f);
                } else {
                    markAsOffline(f);
                }
            }
        });
        mFileCtxMenu.setOnShareItemClickListener(new FileContextMenu.OnShareItemClickListener() {
            @Override
            public void onAddToProjectItemClick(INxlFile f, int pos) {
                addToProject(f, pos);
            }

            @Override
            public void onShareToPersonItemClick(INxlFile f, int pos) {
                share(f, pos);
            }
        });
        mFileCtxMenu.setOnModifyRightsClickListener(new FileContextMenu.OnModifyRightsClickListener() {
            @Override
            public void onModifyRights(INxlFile f, int pos) {
                modifyRights(f, pos);
            }
        });
    }

    private void initListener() {
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.main_green_light),
                getResources().getColor(R.color.color_blue));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        mIb2ProjectActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoProjectPreviewPage();
            }
        });
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProjectCtxMenu();
            }
        });
        mTvDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDescDialog();
            }
        });
        mFileCtxMenu.setOnDeleteButtonClickListener(new NxlItemHelper.OnDeleteButtonClickListener() {
            @Override
            public void onClick(INxlFile f) {
                mPresenter.delete(f);
            }
        });
    }

    private void refresh() {
        mPresenter.refresh();
    }

    private void gotoProjectPreviewPage() {
        Intent intent = new Intent(_activity, SwitchProjectActivity.class);
        intent.putExtra(Constant.KEY, Constant.FLAG_FROM_PROJECT);
        intent.putExtra("project_name", mProject.getName());
        intent.putExtra("project_id", mProject.getId());
        startActivity(intent);
    }

    private void showProjectCtxMenu() {
        mProjectCtxMenu.show(getFragmentManager(), ProjectContextMenu.class.getSimpleName());
    }

    private void markAsOffline(final INxlFile f) {
        f.markAsOffline(new IOfflineCallback() {
            @Override
            public void onStarted() {
                mFilesAdapter.setOperationStatus(f, INxlFile.PROCESS);
            }

            @Override
            public void onProgress() {

            }

            @Override
            public void onMarkDone() {
                mFilesAdapter.setOfflineStatus(f, true);
            }

            @Override
            public void onMarkFailed(OfflineException e) {
                ExceptionHandler.handleException(_activity, e);
                mFilesAdapter.setOperationStatus(f, INxlFile.MARK_ERROR);
            }
        });
    }

    private void unMarkAsOffline(INxlFile f) {
        f.unMarkAsOffline();
        mFilesAdapter.setOfflineStatus(f, false);
    }

    private void showDescDialog() {
        new AlertDialog.Builder(_activity)
                .setTitle(R.string.app_name)
                .setMessage(getString(R.string.project_Name) + mProject.getName() + System.lineSeparator() + System.lineSeparator() +
                        getString(R.string.summary_Description) + mProject.getDescription())
                .setNegativeButton(R.string.common_ok_uppercase, null)
                .show();
    }

    @Deprecated
    private void addToProject(INxlFile f, int pos) {
        Intent i = new Intent(_activity, ProjectOperateActivity.class);
        i.setAction(Constant.ACTION_LUNCH_PROJECT_SELECT_FRAGMENT);
        i.putExtra(Constant.PROJECT_FILE_ENTRY, (NxlDoc) f);
        i.putExtra(Constant.PROJECT_DETAIL, (Parcelable) mProject);
        _activity.startActivity(i);
    }

    @Deprecated
    private void shareToPerson(INxlFile f, int pos) {
        Intent i = new Intent(_activity, ProjectOperateActivity.class);
        i.setAction(Constant.ACTION_CHECK_THEN_SHARE_TO_PERSON);
        i.putExtra(Constant.PROJECT_FILE_ENTRY, (NxlDoc) f);
        i.putExtra(Constant.PROJECT_DETAIL, (Parcelable) mProject);
        _activity.startActivity(i);
    }

    private void share(INxlFile f, int pos) {
        Intent i = new Intent(_activity, ShareActivity.class);
        i.putExtra(Constant.SHARING_SERVICE, (Parcelable) mProject);
        i.putExtra(Constant.SHARING_ENTRY, (Parcelable) f);
        _activity.startActivity(i);
    }

    private void modifyRights(INxlFile f, int pos) {
        Intent i = new Intent(_activity, ProjectOperateActivity.class);
        i.setAction(Constant.ACTION_CHECK_THEN_MODIFY_RIGHTS);
        i.putExtra(Constant.MODIFY_RIGHTS_ENTRY, (NxlDoc) f);
        i.putExtra(Constant.MODIFY_RIGHTS_SERVICE, (Parcelable) mProject);
        _activity.startActivity(i);
    }

    private void releaseRsr() {
        CommonUtils.releaseResource(mFilesAdapter);
        CommonUtils.releaseResource(mPresenter);
    }
}
