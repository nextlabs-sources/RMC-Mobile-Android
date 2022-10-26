package com.skydrm.rmc.ui.project.feature.member;

import com.skydrm.rmc.datalayer.repo.project.IMember;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.ui.project.feature.member.task.GetMemberTask;
import com.skydrm.rmc.utils.sort.SortContext;
import com.skydrm.rmc.utils.sort.SortType;

import java.util.ArrayList;
import java.util.List;

public class MemberPresenter implements IMemberContact.IPresenter {
    private static List<MemberItem> mTmpItem = new ArrayList<>();
    private IProject mProject;
    private IMemberContact.IView mView;
    private GetMemberCallback mGetMemberCallback;

    private boolean initialize;
    private boolean refresh;
    private boolean showEmptyView;

    private static SortType mSortType = SortType.NAME_ASCEND;
    private List<IMember> mMembers = new ArrayList<>();

    MemberPresenter(IProject p, IMemberContact.IView v) {
        this.mProject = p;
        this.mView = v;
        this.mGetMemberCallback = new GetMemberCallback();
    }

    public static List<MemberItem> getSearchItem() {
        return mTmpItem;
    }

    @Override
    public void initialize() {
        initialize = true;
        if (mView != null) {
            if (mView.isActive()) {
                mView.onInitialize(true);
            }
        }
        getMember(false, true);
    }

    @Override
    public void refresh() {
        if (initialize) {
            return;
        }
        refresh = true;
        getMember(true, true);
    }

    @Override
    public void sort(SortType type) {
        mSortType = type;
        update(mMembers, true);
    }

    private void getMember(boolean sync, boolean pending) {
        GetMemberTask task = new GetMemberTask(mProject, sync, mGetMemberCallback);
        task.setPending(pending);
        task.run();
    }

    @Override
    public void onReleaseResource() {
        if (mView != null) {
            mView = null;
        }
        if (mGetMemberCallback != null) {
            mGetMemberCallback = null;
        }
    }

    class GetMemberCallback implements GetMemberTask.ITaskCallback<GetMemberTask.Result, Exception> {

        @Override
        public void onTaskPreExecute() {

        }

        @Override
        public void onTaskExecuteSuccess(GetMemberTask.Result results) {
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
            update(results.mMembers, false);
        }

        @Override
        public void onTaskExecuteFailed(Exception e) {
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
            if (mView != null) {
                mView.showErrorView(e);
            }
        }
    }

    private void update(List<IMember> members, boolean sort) {
        if (members == null || members.size() == 0) {
            if (mView != null) {
                mView.showEmptyView(true);
                showEmptyView = true;
            }
            return;
        }
        if (showEmptyView) {
            showEmptyView = false;
            if (mView != null) {
                mView.showEmptyView(false);
            }
        }
        if (!sort) {
            mMembers.clear();
            mMembers.addAll(members);
        }
        List<MemberItem> memberItems = SortContext.sortMember(members, mSortType);
        mTmpItem.clear();
        mTmpItem.addAll(memberItems);
        if (mView != null) {
            mView.update(memberItems);
        }
    }
}
