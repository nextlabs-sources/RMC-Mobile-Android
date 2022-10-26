package com.skydrm.rmc.ui.service.fileinfo;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.exception.TokenAccessDenyException;

import java.io.IOException;

public class FileInfoTask extends LoadTask<Void, INxlFileFingerPrint> {
    private IFileInfo mFileInfo;

    private ITaskCallback<Result, Exception> mCallback;
    private Exception mExp;

    public FileInfoTask(IFileInfo info, ITaskCallback<Result, Exception> callback) {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
        this.mFileInfo = info;
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
    protected INxlFileFingerPrint doInBackground(Void... voids) {
        if (mFileInfo == null) {
            return null;
        }
        try {
            return mFileInfo.getFingerPrint();
        } catch (RmsRestAPIException | SessionInvalidException | InvalidRMClientException
                | IOException | TokenAccessDenyException e) {
            mExp = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(INxlFileFingerPrint result) {
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
        public INxlFileFingerPrint fp;

        public Result(INxlFileFingerPrint fp) {
            this.fp = fp;
        }
    }
}
