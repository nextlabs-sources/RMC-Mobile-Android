package com.skydrm.rmc.ui.common;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.base.IDataService;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.sdk.exception.RmsRestAPIException;

import java.util.ArrayList;
import java.util.List;

public class GetNxlDataTask extends LoadTask<Void, List<INxlFile>> {
    private IDataService[] mService;
    private String mPathId;
    private boolean sync;
    private int mType;
    private boolean recursively;

    private ITaskCallback<Result, Exception> mCallback;
    private Exception mExp;

    public GetNxlDataTask(IDataService[] service,
                          String pathId,
                          boolean sync,
                          int type,
                          ITaskCallback<Result, Exception> callback) {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
        this.mService = service;
        this.mPathId = pathId;
        this.sync = sync;
        this.mType = type;
        this.mCallback = callback;
    }

    public void setRecursively(boolean recursively) {
        this.recursively = recursively;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mCallback != null) {
            mCallback.onTaskPreExecute();
        }
    }

    @Override
    protected List<INxlFile> doInBackground(Void... voids) {
        List<INxlFile> ret = new ArrayList<>();
        if (mService == null || mService.length == 0) {
            return ret;
        }
        try {
            for (IDataService s : mService) {
                if (s == null) {
                    continue;
                }
                List<INxlFile> data = sync ? s.sync(mType, mPathId, recursively)
                        : s.list(mType, mPathId, recursively);
                if (data == null) {
                    continue;
                }
                ret.addAll(data);
            }
        } catch (SessionInvalidException
                | RmsRestAPIException
                | InvalidRMClientException e) {
            this.mExp = e;
        }
        return ret;
    }

    @Override
    protected void onPostExecute(List<INxlFile> result) {
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
        public List<INxlFile> data;

        Result(List<INxlFile> data) {
            this.data = data;
        }
    }

}
