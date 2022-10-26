package com.skydrm.rmc.ui.project.feature.configuration.task;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.rest.project.GetProjectMetadataResult;

public class GetMetadataTask extends LoadTask<Void, GetProjectMetadataResult> {
    private IProject mProject;
    private ITaskCallback<Result, Exception> mCallback;
    private Exception mExp;

    public GetMetadataTask(IProject p, ITaskCallback<Result, Exception> callback) {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
        this.mProject = p;
        this.mCallback = callback;
    }

    @Override
    protected GetProjectMetadataResult doInBackground(Void... voids) {
        try {
            return mProject.getMetadata();
        } catch (InvalidRMClientException e) {
            mExp = e;
        } catch (SessionInvalidException e) {
            mExp = e;
        } catch (RmsRestAPIException e) {
            mExp = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(GetProjectMetadataResult result) {
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
        public GetProjectMetadataResult metadata;

        public Result(GetProjectMetadataResult metadata) {
            this.metadata = metadata;
        }
    }
}
