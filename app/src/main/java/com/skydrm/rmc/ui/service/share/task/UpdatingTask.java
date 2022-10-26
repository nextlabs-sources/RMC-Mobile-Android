package com.skydrm.rmc.ui.service.share.task;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.rmc.ui.service.share.ISharingFile;
import com.skydrm.rmc.ui.service.share.ISharingService;
import com.skydrm.sdk.exception.RmsRestAPIException;

import java.util.List;

public class UpdatingTask extends LoadTask<Void, Boolean> {
    private ISharingService mService;
    private ISharingFile mFile;
    private List<String> mNewRecipients;
    private List<String> mRemovedRecipients;
    private String mComments;

    private ITaskCallback<Result, Exception> mCallback;
    private Exception mExp;

    public UpdatingTask(ISharingService service,
                        ISharingFile file,
                        List<String> newRecipients,
                        List<String> removedRecipients,
                        String comments,
                        ITaskCallback<Result, Exception> callback) {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
        this.mService = service;
        this.mFile = file;
        this.mNewRecipients = newRecipients;
        this.mRemovedRecipients = removedRecipients;
        this.mComments = comments;
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
            if (mFile.isRevoked()) {
                mExp = new RmsRestAPIException("The rights-protected file has already been revoked and cannot be shared.",
                        RmsRestAPIException.ExceptionDomain.Common);
                return false;
            }
            return mService.updateRecipients(mFile, mNewRecipients, mRemovedRecipients, mComments);
        } catch (SessionInvalidException | InvalidRMClientException | RmsRestAPIException e) {
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
