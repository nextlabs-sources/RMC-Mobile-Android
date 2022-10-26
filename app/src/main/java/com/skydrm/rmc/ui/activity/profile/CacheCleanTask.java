package com.skydrm.rmc.ui.activity.profile;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.RepoFactory;
import com.skydrm.rmc.ui.base.LoadTask;

public class CacheCleanTask extends LoadTask<Void, Boolean> {
    private ITaskCallback<Result, String> mCallback;

    public CacheCleanTask(ITaskCallback<Result, String> callback) {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.REGULAR_BACK_GROUND));
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
        SkyDRMApp.getInstance().getRepoSystem().clearCache();
        RepoFactory.clearCache();
        return true;
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
                mCallback.onTaskExecuteFailed("");
            }
        }
    }

    public class Result implements IResult {

    }
}
