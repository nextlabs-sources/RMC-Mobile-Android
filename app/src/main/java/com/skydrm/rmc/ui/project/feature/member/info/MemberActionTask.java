package com.skydrm.rmc.ui.project.feature.member.info;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.datalayer.repo.project.IMember;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.sdk.exception.RmsRestAPIException;

public class MemberActionTask extends LoadTask<Void, String> {
    private IMember mMember;
    private Type mType;
    private ITaskCallback<Result, Exception> mCallback;

    private Exception mExp;

    MemberActionTask(IMember m, Type t, ITaskCallback<Result, Exception> callback) {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
        this.mMember = m;
        this.mType = t;
        this.mCallback = callback;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            if (mType == Type.REMOVE) {
                return mMember.remove();
            }
            if (mType == Type.RESEND) {
                return mMember.resendInvitation();
            }
            if (mType == Type.REVOKE) {
                return mMember.revokeInvitation();
            }
        } catch (RmsRestAPIException e) {
            mExp = e;
        } catch (SessionInvalidException e) {
            mExp = e;
        } catch (InvalidRMClientException e) {
            mExp = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (mExp == null) {
            if (mCallback != null) {
                mCallback.onTaskExecuteSuccess(new Result(s, mType, mMember));
            }
        } else {
            if (mCallback != null) {
                mCallback.onTaskExecuteFailed(mExp);
            }
        }
    }

    class Result implements IResult {
        public String result;
        public Type mType;
        public IMember mTarget;

        public Result(String result, Type type, IMember target) {
            this.result = result;
            this.mType = type;
            this.mTarget = target;
        }
    }

    public enum Type {
        RESEND,
        REVOKE,
        REMOVE
    }
}
