package com.skydrm.rmc.ui.myspace.myvault.view.task;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.datalayer.repo.myvault.IMyVaultFile;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.rest.myVault.UpdateRecipientsResult;

import java.util.List;

public class UpdateRecipientsTask extends LoadTask<Void, UpdateRecipientsResult> {
    private IMyVaultFile mFile;
    private List<String> mAdded;
    private List<String> mRemoved;
    private String mComments;

    private ITaskCallback<Result, Exception> mCallback;
    private Exception mExp;

    public UpdateRecipientsTask(IMyVaultFile f, List<String> added, List<String> removed,
                                ITaskCallback<Result, Exception> callback) {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
        this.mFile = f;
        this.mAdded = added;
        this.mRemoved = removed;

        this.mCallback = callback;
    }

    public void setComments(String cmt) {
        this.mComments = cmt;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mCallback != null) {
            mCallback.onTaskPreExecute();
        }
    }

    @Override
    protected UpdateRecipientsResult doInBackground(Void... voids) {
        try {
            return mFile.updateRecipients(mAdded, mRemoved, mComments);
        } catch (SessionInvalidException
                | InvalidRMClientException
                | RmsRestAPIException e) {
            mExp = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(UpdateRecipientsResult result) {
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
        public UpdateRecipientsResult mResult;

        public Result(UpdateRecipientsResult result) {
            mResult = result;
        }
    }
}
