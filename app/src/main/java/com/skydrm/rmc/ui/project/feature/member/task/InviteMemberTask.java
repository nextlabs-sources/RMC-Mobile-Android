package com.skydrm.rmc.ui.project.feature.member.task;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.sdk.exception.RmsRestAPIException;

import java.util.List;

public class InviteMemberTask extends LoadTask<Void, String> {
    private IProject mProject;
    private List<String> mEmails;
    private String mInvitationMsg;

    private Exception mExp;
    private ITaskCallback<Result, Exception> mCallback;

    public InviteMemberTask(IProject p, List<String> emails, String invitationMsg,
                            ITaskCallback<Result, Exception> callback) {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
        this.mProject = p;
        this.mEmails = emails;
        this.mInvitationMsg = invitationMsg;
        this.mCallback = callback;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            return mProject.inviteMember(mEmails, mInvitationMsg);
        } catch (SessionInvalidException e) {
            mExp = e;
        } catch (InvalidRMClientException e) {
            mExp = e;
        } catch (RmsRestAPIException e) {
            mExp = e;
        }
        return "";
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (mExp == null) {
            if (mCallback != null) {
                mCallback.onTaskExecuteSuccess(new Result(result));
            }
        } else {
            if (mCallback != null) {
                mCallback.onTaskExecuteFailed(mExp);
            }
        }
    }

    public class Result implements IResult {
        public String msg;

        public Result(String msg) {
            this.msg = msg;
        }
    }
}
