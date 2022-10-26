package com.skydrm.rmc.ui.service.share.task;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.rmc.ui.service.share.ISharingFile;
import com.skydrm.rmc.ui.service.share.ISharingService;
import com.skydrm.sdk.exception.RmsRestAPIException;

public class RevokingTask extends LoadTask<Void, Boolean> {
    private ISharingService mService;
    private ISharingFile mFile;

    private ITaskCallback<Result, Exception> mCallback;
    private Exception mExp;

    public RevokingTask(ISharingService service,
                        ISharingFile file,
                        ITaskCallback<Result, Exception> callback) {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
        this.mService = service;
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
        if (mService == null || mFile == null) {
            return false;
        }
        try {
            if (!mFile.isRevokeable()) {
                mExp = new Exception("You are not authorized to perform this action.");
                return false;
            }
            return mService.revokeAllRights(mFile);
        } catch (SessionInvalidException
                | InvalidRMClientException
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
                mCallback.onTaskExecuteFailed(mExp == null ? new Exception("Necessary params are " +
                        "required to perform sharing transaction.") : mExp);
            }
        }
    }

    public class Result implements IResult {

    }
}
