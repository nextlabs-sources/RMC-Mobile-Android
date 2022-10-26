package com.skydrm.rmc.ui.activity.home.view.model;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.datalayer.repo.project.IInvitePending;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.sdk.exception.RmsRestAPIException;

public class InvitationTask extends LoadTask<Void, Boolean> {
    private IInvitePending mPendingProject;
    private boolean accept;
    private Exception mExp;
    private ITaskCallback<Result, Exception> mCallback;

    private String mReason;

    public InvitationTask(IInvitePending project, boolean accept, ITaskCallback<Result, Exception> callback) {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
        this.mPendingProject = project;
        this.accept = accept;
        this.mCallback = callback;
    }

    public void setDenyReason(String reason) {
        this.mReason = reason;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mCallback != null) {
            mCallback.onTaskPreExecute();
        }
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        if (mPendingProject == null) {
            return false;
        }
        try {
            return accept ? mPendingProject.acceptInvitation()
                    : mPendingProject.denyInvitation(mReason);
        } catch (RmsRestAPIException
                | SessionInvalidException
                | InvalidRMClientException e) {
            mExp = e;
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (result) {
            if (mCallback != null) {
                mCallback.onTaskExecuteSuccess(new Result());
            }
        } else {
            if (mCallback != null) {
                mCallback.onTaskExecuteFailed(mExp == null ? new Exception("Operation failed.") : mExp);
            }
        }
    }

    public class Result implements IResult {

    }
}
