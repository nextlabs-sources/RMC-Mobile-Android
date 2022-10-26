package com.skydrm.rmc.ui.service.modifyrights.task;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.rmc.ui.service.modifyrights.IModifyRightsFile;
import com.skydrm.rmc.ui.service.modifyrights.IModifyRightsService;
import com.skydrm.sdk.exception.RmsRestAPIException;

public class ReClassifyTask extends LoadTask<Void, Boolean> {
    private IModifyRightsService mService;
    private IModifyRightsFile mFile;
    private String mFileName;
    private String mParentPathId;
    private String mTags;

    private Exception mExp;
    private ITaskCallback<Result, Exception> mCallback;

    public ReClassifyTask(IModifyRightsService service,
                          IModifyRightsFile file,
                          String fileName,
                          String parentPathId,
                          String tags,
                          ITaskCallback<Result, Exception> callback) {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
        this.mService = service;
        this.mFile = file;
        this.mFileName = fileName;
        this.mParentPathId = parentPathId;
        this.mTags = tags;
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
            boolean result = mService.modifyRights(mFileName, mParentPathId, mTags);
            if (result) {
                if (mFile == null) {
                    return false;
                }
                mFile.handleModifyFileRights();
                return true;
            }
            return false;
        } catch (RmsRestAPIException | SessionInvalidException | InvalidRMClientException e) {
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
