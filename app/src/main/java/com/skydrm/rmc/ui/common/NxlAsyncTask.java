package com.skydrm.rmc.ui.common;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.datalayer.RepoFactory;
import com.skydrm.rmc.datalayer.repo.NxlRepo;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.base.RepoType;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.sdk.exception.RmsRestAPIException;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class NxlAsyncTask extends LoadTask<Void, List<INxlFile>> {
    private RepoType mRepoType;
    private boolean sync;
    private ITaskCallback<Result, Exception> mCallback;

    private int mType;
    private Exception mExp;

    public NxlAsyncTask(boolean sync, RepoType type,
                        ITaskCallback<Result, Exception> callback) {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
        this.sync = sync;
        this.mRepoType = type;
        this.mCallback = callback;
    }

    public void setType(int type) {
        this.mType = type;
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
        try {
            if (mType == NxlFileType.OFFLINE.getValue()) {
                NxlRepo myVault = (NxlRepo) RepoFactory.getRepo(RepoType.TYPE_MYVAULT);
                NxlRepo sharedWitheMe = (NxlRepo) RepoFactory.getRepo(RepoType.TYPE_SHARED_WITH_ME);
                List<INxlFile> mvOfs = myVault.list(NxlFileType.OFFLINE.getValue());
                List<INxlFile> swOfs = sharedWitheMe.list(NxlFileType.OFFLINE.getValue());
                List<INxlFile> ret = new ArrayList<>();
                if (mvOfs != null && mvOfs.size() != 0) {
                    ret.addAll(mvOfs);
                }
                if (swOfs != null && swOfs.size() != 0) {
                    ret.addAll(swOfs);
                }
                return ret;
            }
            NxlRepo repo = (NxlRepo) RepoFactory.getRepo(mRepoType);
            return sync ? repo.sync(mType) : repo.list(mType);
        } catch (Exception e) {
            mExp = e;
        }
        return null;
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
        public List<INxlFile> files;

        public Result(List<INxlFile> files) {
            this.files = files;
        }
    }
}
