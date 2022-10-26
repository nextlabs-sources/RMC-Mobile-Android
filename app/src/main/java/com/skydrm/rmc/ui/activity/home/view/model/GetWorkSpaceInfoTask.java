package com.skydrm.rmc.ui.activity.home.view.model;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.datalayer.RepoFactory;
import com.skydrm.rmc.datalayer.repo.base.IBaseRepo;
import com.skydrm.rmc.datalayer.repo.base.RepoType;
import com.skydrm.rmc.datalayer.repo.workspace.WorkSpaceInfo;
import com.skydrm.rmc.datalayer.repo.workspace.WorkSpaceRepo;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.sdk.exception.RmsRestAPIException;

public class GetWorkSpaceInfoTask extends LoadTask<Void, WorkSpaceInfo> {
    private ITaskCallback<Result, Exception> mCallback;
    private Exception mExp;

    public GetWorkSpaceInfoTask(ITaskCallback<Result, Exception> callback) {
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
    protected WorkSpaceInfo doInBackground(Void... voids) {
        IBaseRepo base = RepoFactory.getRepo(RepoType.TYPE_WORKSPACE);
        if (base == null) {
            return null;
        }
        if (base instanceof WorkSpaceRepo) {
            WorkSpaceRepo repo = (WorkSpaceRepo) base;
            try {
                return repo.getWorkSpaceInfo();
            } catch (SessionInvalidException e) {
                e.printStackTrace();
                mExp = e;
            } catch (InvalidRMClientException e) {
                e.printStackTrace();
                mExp = e;
            } catch (RmsRestAPIException e) {
                e.printStackTrace();
                mExp = e;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(WorkSpaceInfo result) {
        super.onPostExecute(result);
        if (result != null) {
            if (mCallback != null) {
                mCallback.onTaskExecuteSuccess(new Result(result.getUsage(),
                        result.getQuota(),
                        result.getTotalFiles()));
            }
        } else {
            if (mCallback != null) {
                mCallback.onTaskExecuteFailed(mExp);
            }
        }
    }

    public class Result implements IResult {
        public long mUsage;
        public long mQuota;
        public int mTotalFiles;

        public Result(long usage, long quota, int totalFiles) {
            this.mUsage = usage;
            this.mQuota = quota;
            this.mTotalFiles = totalFiles;
        }
    }
}
