package com.skydrm.rmc.ui.service.createfolder;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.sdk.exception.RmsRestAPIException;

public class CreateFolderTask extends LoadTask<Void, Boolean> {
    private ICreateFolderService mService;
    private String mParentPathId;
    private String mName;
    private boolean autoRename;

    private ITaskCallback<Result, Exception> mCallback;
    private Exception mExp;

    public CreateFolderTask(ICreateFolderService service,
                            String parentPathId,
                            String name,
                            boolean autoRename,
                            ITaskCallback<Result, Exception> callback) {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
        this.mService = service;
        this.mParentPathId = parentPathId;
        this.mName = name;
        this.autoRename = autoRename;
        this.mCallback = callback;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            return mService.createFolder(mParentPathId, mName, autoRename);
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
                mCallback.onTaskExecuteFailed(mExp == null ?
                        new Exception("Operation failed.") :
                        mExp);
            }
        }
    }

    public class Result implements IResult {

    }
}
