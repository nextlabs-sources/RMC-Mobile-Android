package com.skydrm.rmc.ui.project.service;

import android.view.View;

import com.skydrm.rmc.datalayer.repo.project.IInvitePending;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.datalayer.repo.project.Project;
import com.skydrm.rmc.ui.activity.home.view.model.GetPendingInvitationTask;
import com.skydrm.rmc.ui.activity.home.view.model.GetProjectsTask;
import com.skydrm.rmc.ui.activity.home.view.model.InvitationTask;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.rmc.utils.sort.SortContext;
import com.skydrm.rmc.utils.sort.SortType;

import java.util.ArrayList;
import java.util.List;

public class ProjectPresenter implements IProjectContact.IPresenter {
    private IProjectContact.IView mView;

    private static List<IProject> mTmpData = new ArrayList<>();
    private List<IProject> mCreatedByMeOnly = new ArrayList<>();
    private List<IProject> mInvitedByOtherOnly = new ArrayList<>();
    private List<IProject> mPendingProjects = new ArrayList<>();

    private boolean refresh;
    private boolean initialize;

    private SortType mSortType = SortType.TIME_DESCEND;

    private LoadProjectCallback mLoadProjectCallback;
    private InvitationCallback mInvitationCallback;
    private LoadingPendingCallback mLoadingPendingCallback;

    public ProjectPresenter(IProjectContact.IView v) {
        this.mView = v;
    }

    public static List<IProject> getSearchData() {
        return mTmpData;
    }

    @Override
    public void initialize() {
        initialize = true;
        if (mView != null) {
            mView.onInitialize(true);
        }
        getPendingInvitation();
        getProjectInternal(0, false, true);
    }

    @Override
    public void refresh() {
        refresh = true;
        getPendingInvitation();
        getProjectInternal(0, true, true);
    }

    @Override
    public void sort(SortType sortType) {
        mSortType = sortType;

        showSortedCreatedByMeData(mCreatedByMeOnly, false);
        showSortedInvitedByOtherData(mInvitedByOtherOnly, mPendingProjects, false);
    }

    @Override
    public void updateSortType(SortType sortType) {
        this.mSortType = sortType;
    }

    @Override
    public void getProjects(int type) {
        getProjectInternal(type, false, false);
    }

    @Override
    public void getProjectsAndResetOperationStatus(int type) {
        if (mLoadProjectCallback == null) {
            mLoadProjectCallback = new LoadProjectCallback(type);
        }
        getProjectsAndResetStatus(type, mLoadProjectCallback);
    }

    @Override
    public void getPendingInvitation() {
        if (mLoadingPendingCallback == null) {
            mLoadingPendingCallback = new LoadingPendingCallback();
        }
        GetPendingInvitationTask task = new GetPendingInvitationTask(mLoadingPendingCallback);
        task.run();
    }

    @Override
    public void acceptInvitation(final IInvitePending project, final View loadingBar) {
        if (mInvitationCallback == null) {
            mInvitationCallback = new InvitationCallback(project, loadingBar);
        }
        InvitationTask task = new InvitationTask(project, true, mInvitationCallback);
        task.run();
    }

    @Override
    public void denyInvitation(final IInvitePending project, String reason, final View loadingBar) {
        if (mInvitationCallback == null) {
            mInvitationCallback = new InvitationCallback(project, loadingBar);
        }
        InvitationTask task = new InvitationTask(project, false, mInvitationCallback);
        task.setDenyReason(reason);
        task.run();
    }

    @Override
    public void onDestroy() {
        if (mView != null) {
            mView = null;
        }
        if (mLoadProjectCallback != null) {
            mLoadProjectCallback = null;
        }
        if (mInvitationCallback != null) {
            mInvitationCallback = null;
        }
        if (mLoadingPendingCallback != null) {
            mLoadingPendingCallback = null;
        }
    }

    private void getProjectInternal(final int type, final boolean initial, boolean sync) {
        if (mLoadProjectCallback == null) {
            mLoadProjectCallback = new LoadProjectCallback(type);
        }
        getProjects(type, sync, mLoadProjectCallback);
    }

    private void getProjects(int type, boolean sync, LoadTask.ITaskCallback<GetProjectsTask.Result, Exception> callback) {
        GetProjectsTask task = new GetProjectsTask(type, false, sync, callback);
        task.run();
    }

    private void getProjectsAndResetStatus(int type, LoadTask.ITaskCallback<GetProjectsTask.Result, Exception> callback) {
        GetProjectsTask task = new GetProjectsTask(type, true, false, callback);
        task.run();
    }

    private void showSortedCreatedByMeData(List<IProject> createdByMe, boolean clear) {
        if (clear) {
            mCreatedByMeOnly.clear();
            if (createdByMe != null && createdByMe.size() != 0) {
                mCreatedByMeOnly.addAll(createdByMe);
            }
        }

        mTmpData.clear();
        mTmpData.addAll(mPendingProjects);
        if (createdByMe != null) {
            mTmpData.addAll(createdByMe);
        }
        mTmpData.addAll(mInvitedByOtherOnly);

        if (mView != null) {
            mView.showCreatedByMeProjects(sort(createdByMe));
        }
    }

    private void showSortedInvitedByOtherData(List<IProject> invitedByOther,
                                              List<IProject> pendingProject,
                                              boolean clear) {
        if (clear) {
            mInvitedByOtherOnly.clear();
            if (invitedByOther != null && invitedByOther.size() != 0) {
                mInvitedByOtherOnly.addAll(invitedByOther);
            }
        }

        mTmpData.clear();
        mTmpData.addAll(pendingProject);
        mTmpData.addAll(mCreatedByMeOnly);
        if (invitedByOther != null) {
            mTmpData.addAll(invitedByOther);
        }

        showInviteByOtherData(invitedByOther, pendingProject);
    }

    private void showInviteByOtherData(List<IProject> inviteByOtherOnly,
                                       List<IProject> pendingProject) {
        List<IProject> invitedByOtherData = new ArrayList<>();
        if (pendingProject != null) {
            invitedByOtherData.addAll(pendingProject);
        }
        if (inviteByOtherOnly != null) {
            invitedByOtherData.addAll(sort(inviteByOtherOnly));
        }
        if (mView != null) {
            mView.showInvitedByOtherProjects(invitedByOtherData);
        }
    }

    private List<IProject> sort(List<IProject> data) {
        return SortContext.sortProject(data, mSortType);
    }

    class LoadProjectCallback implements LoadTask.ITaskCallback<GetProjectsTask.Result, Exception> {
        private int type;

        LoadProjectCallback(int type) {
            this.type = type;
        }

        @Override
        public void onTaskPreExecute() {

        }

        @Override
        public void onTaskExecuteSuccess(GetProjectsTask.Result result) {
            if (initialize) {
                if (mView != null) {
                    mView.onInitialize(false);
                }
                initialize = false;
            }
            if (refresh) {
                if (mView != null) {
                    mView.showLoadingIndicator(false);
                }
                refresh = false;
            }
            if (type == 0) {
                //show all projects
                showSortedCreatedByMeData(result.getCreatedByMe(), true);
                showSortedInvitedByOtherData(result.getInvitedByOther(), mPendingProjects, true);
            } else if (type == 1) {
                //show created by me only.
                showSortedCreatedByMeData(result.getCreatedByMe(), true);
            } else if (type == 2) {
                //show invited by other.
                showSortedInvitedByOtherData(result.getInvitedByOther(), mPendingProjects, true);
            }
        }

        @Override
        public void onTaskExecuteFailed(Exception e) {
            if (refresh) {
                if (mView != null) {
                    mView.showLoadingIndicator(false);
                }
                refresh = false;
            }
            if (initialize) {
                if (mView != null) {
                    mView.onInitialize(false);
                }
                initialize = false;
            }
            if (mView != null) {
                mView.showErrorView(e);
            }
        }
    }

    class LoadingPendingCallback implements LoadTask.ITaskCallback<GetPendingInvitationTask.Result, Exception> {

        @Override
        public void onTaskPreExecute() {

        }

        @Override
        public void onTaskExecuteSuccess(GetPendingInvitationTask.Result results) {
            if (refresh) {
                if (mView != null) {
                    mView.showLoadingIndicator(false);
                }
                refresh = false;
            }
            List<IInvitePending> pendingList = results.mPendingResult;
            mPendingProjects.clear();
            if (pendingList != null && pendingList.size() != 0) {
                for (IInvitePending p : pendingList) {
                    if (p == null) {
                        continue;
                    }
                    mPendingProjects.add((Project) p);
                }
            }
            showSortedInvitedByOtherData(mInvitedByOtherOnly, mPendingProjects, false);
        }

        @Override
        public void onTaskExecuteFailed(Exception e) {
            if (refresh) {
                if (mView != null) {
                    mView.showLoadingIndicator(false);
                }
                refresh = false;
            }
            if (mView != null) {
                mView.showErrorView(e);
            }
        }
    }

    class InvitationCallback implements LoadTask.ITaskCallback<InvitationTask.Result, Exception> {
        IInvitePending mPending;
        View mLoadingBar;

        InvitationCallback(IInvitePending pending, View loadingBar) {
            this.mPending = pending;
            this.mLoadingBar = loadingBar;
        }

        @Override
        public void onTaskPreExecute() {
            if (mLoadingBar != null) {
                mLoadingBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onTaskExecuteSuccess(InvitationTask.Result results) {
            if (mLoadingBar != null) {
                mLoadingBar.setVisibility(View.GONE);
            }
            if (removePending()) {
                getProjectInternal(2, false, true);
            } else {
                getPendingInvitation();
            }
        }

        @Override
        public void onTaskExecuteFailed(Exception e) {
            if (mLoadingBar != null) {
                mLoadingBar.setVisibility(View.GONE);
            }
            if (removePending()) {
                showSortedInvitedByOtherData(mInvitedByOtherOnly, mPendingProjects, false);
            } else {
                getPendingInvitation();
            }

            if (mView != null) {
                mView.showErrorView(e);
            }
        }

        private boolean removePending() {
            if (mPending == null) {
                return false;
            }
            IProject toBeRemoved = null;
            for (IProject i : mPendingProjects) {
                Project p = (Project) i;
                if (p.getInvitationId() == mPending.getInvitationId() &&
                        p.getInviteCode().equals(mPending.getInviteCode())) {
                    toBeRemoved = i;
                    break;
                }
            }
            if (toBeRemoved == null) {
                return false;
            }
            return mPendingProjects.remove(toBeRemoved);
        }
    }
}
