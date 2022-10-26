package com.skydrm.rmc.ui.myspace.myvault.view.task;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.datalayer.repo.myvault.IMyVaultFile;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.sdk.exception.RmsRestAPIException;

public class RevokeTask extends LoadTask<Void, Boolean> {
    private IMyVaultFile mFile;
    private ITaskCallback<Result, Exception> mCallback;

    private Exception mExp;

    public RevokeTask(IMyVaultFile file, ITaskCallback<Result, Exception> callback) {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
        this.mFile = file;
        this.mCallback = callback;
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
        if (mFile == null) {
            return false;
        }
        try {
            return mFile.revokeRights();
        } catch (InvalidRMClientException
                | SessionInvalidException
                | RmsRestAPIException e) {
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
