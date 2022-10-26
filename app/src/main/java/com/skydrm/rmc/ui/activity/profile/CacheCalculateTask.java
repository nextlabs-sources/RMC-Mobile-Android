package com.skydrm.rmc.ui.activity.profile;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.RepoFactory;
import com.skydrm.rmc.ui.base.LoadTask;

public class CacheCalculateTask extends LoadTask<Void, Long> {
    private ITaskCallback<Result, String> mCallback;

    public CacheCalculateTask(ITaskCallback<Result, String> callback) {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
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
    protected Long doInBackground(Void... voids) {
        long cacheSize = 0;

        cacheSize += SkyDRMApp.getInstance().getRepoSystem().calReposCacheSize();
        cacheSize += RepoFactory.getCacheSize();

        return cacheSize;
    }

    @Override
    protected void onPostExecute(Long result) {
        super.onPostExecute(result);
        if (result != -1) {
            if (mCallback != null) {
                mCallback.onTaskExecuteSuccess(new Result(result));
            }
        } else {
            if (mCallback != null) {
                mCallback.onTaskExecuteFailed("Try calculate cache size failed.");
            }
        }
    }

    public class Result implements IResult {
        public long size;

        public Result(long size) {
            this.size = size;
        }
    }

}
