package com.skydrm.rmc.ui.service.modifyrights.task;

import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.rmc.ui.service.modifyrights.IModifyRightsFile;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.exception.TokenAccessDenyException;

import java.io.IOException;

public class RemoteModifiedCheckTask extends LoadTask<Void, Boolean> {
    private IModifyRightsFile mFile;
    private ITaskCallback<Result, Exception> mCallback;
    private Exception mExp;

    public RemoteModifiedCheckTask(IModifyRightsFile file,
                                   ITaskCallback<Result, Exception> callback) {
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
            return mFile.checkRemoteRightsModifiedThenUpdate();
        } catch (InvalidRMClientException | IOException | SessionInvalidException
                | TokenAccessDenyException | RmsRestAPIException e) {
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
                mCallback.onTaskExecuteFailed(mExp == null ? new Exception("Unknown error.") : mExp);
            }
        }
    }

    public class Result implements IResult {

    }
}
