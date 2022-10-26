package com.skydrm.rmc.ui.project.feature.member.info;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.datalayer.repo.project.IMember;
import com.skydrm.rmc.datalayer.repo.project.IMemberDetail;
import com.skydrm.rmc.datalayer.repo.project.Member;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.sdk.exception.RmsRestAPIException;

public class MemberInfoTask extends LoadTask<Void, IMemberDetail> {
    private IMember mMember;
    private ITaskCallback<Result, Exception> mCallback;

    private Exception mExp;

    MemberInfoTask(IMember m, ITaskCallback<Result, Exception> callback) {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
        this.mMember = m;
        this.mCallback = callback;
    }

    @Override
    protected IMemberDetail doInBackground(Void... voids) {
        try {
            mMember.getDetail();
            return (Member) mMember;
        } catch (SessionInvalidException e) {
            this.mExp = e;
        } catch (InvalidRMClientException e) {
            this.mExp = e;
        } catch (RmsRestAPIException e) {
            this.mExp = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(IMemberDetail result) {
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
        public IMemberDetail detail;

        public Result(IMemberDetail result) {
            detail = result;
        }
    }
}
