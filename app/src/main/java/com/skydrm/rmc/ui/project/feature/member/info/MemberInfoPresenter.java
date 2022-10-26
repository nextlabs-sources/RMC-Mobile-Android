package com.skydrm.rmc.ui.project.feature.member.info;

import com.skydrm.rmc.datalayer.repo.project.IMember;

public class MemberInfoPresenter implements IMemberInfoContact.IPresenter {
    private IMember mMember;
    private IMemberInfoContact.IView mView;

    private MemberInfoCallback mMemberInfoCallback;
    private MemberActionCallback mMemberActionCallback;

    MemberInfoPresenter(IMember m, IMemberInfoContact.IView v) {
        this.mMember = m;
        this.mView = v;
        this.mMemberInfoCallback = new MemberInfoCallback();
        this.mMemberActionCallback = new MemberActionCallback();
    }

    @Override
    public void getMemberDetail() {
        new MemberInfoTask(mMember, mMemberInfoCallback).run();
    }

    @Override
    public void removeMember() {
        new MemberActionTask(mMember, MemberActionTask.Type.REMOVE, mMemberActionCallback).run();
    }

    @Override
    public void resendInvitation() {
        new MemberActionTask(mMember, MemberActionTask.Type.RESEND, mMemberActionCallback).run();
    }

    @Override
    public void revokeInvitation() {
        new MemberActionTask(mMember, MemberActionTask.Type.REVOKE, mMemberActionCallback).run();
    }

    @Override
    public void onDestroy() {
        if (mView != null) {
            mView = null;
        }
        if (mMemberInfoCallback != null) {
            mMemberInfoCallback = null;
        }
        if (mMemberActionCallback != null) {
            mMemberActionCallback = null;
        }
    }

    class MemberInfoCallback implements MemberInfoTask.ITaskCallback<MemberInfoTask.Result, Exception> {

        @Override
        public void onTaskPreExecute() {

        }

        @Override
        public void onTaskExecuteSuccess(MemberInfoTask.Result results) {
            if (mView != null) {
                mView.showInvitorName(results.detail);
            }
        }

        @Override
        public void onTaskExecuteFailed(Exception e) {
            if (mView != null) {
                mView.showErrorView(e);
            }
        }
    }

    class MemberActionCallback implements MemberActionTask.ITaskCallback<MemberActionTask.Result, Exception> {

        @Override
        public void onTaskPreExecute() {
            if (mView != null) {
                mView.showLoadingIndicator(true);
            }
        }

        @Override
        public void onTaskExecuteSuccess(MemberActionTask.Result results) {
            if (mView != null) {
                mView.showLoadingIndicator(false);
            }
            if (mView != null) {
                mView.onActionSuccess(results.result, results.mType, results.mTarget);
            }
        }

        @Override
        public void onTaskExecuteFailed(Exception e) {
            if (mView != null) {
                mView.showLoadingIndicator(false);
            }
            if (mView != null) {
                mView.showErrorView(e);
            }
        }
    }
}
