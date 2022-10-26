package com.skydrm.rmc.ui.service.protect.task;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.rmc.ui.service.protect.IProtectService;
import com.skydrm.sdk.exception.RmsRestAPIException;

import java.io.File;

public class UploadNxlFileTask extends LoadTask<Void, Boolean> {
    private IProtectService mService;
    private String mParentPathId;
    private File mNxlFile;

    private ITaskCallback<Result, Exception> mCallback;
    private Exception mExp;

    public UploadNxlFileTask(IProtectService service,
                             String pathId, File file,
                             ITaskCallback<Result, Exception> callback) {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
        this.mService = service;
        this.mParentPathId = pathId;
        this.mNxlFile = file;
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
        try {
            if (mService == null) {
                return false;
            }
            if (mNxlFile == null || !mNxlFile.exists() || mNxlFile.isDirectory()) {
                return false;
            }
            if (mParentPathId == null || mParentPathId.isEmpty()) {
                return false;
            }
            return mService.upload(mNxlFile, mParentPathId);
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
                mCallback.onTaskExecuteFailed(mExp == null ?
                        new Exception("Required parameters is missing.") : mExp);
            }
        }
    }

    public class Result implements IResult {

    }
}
