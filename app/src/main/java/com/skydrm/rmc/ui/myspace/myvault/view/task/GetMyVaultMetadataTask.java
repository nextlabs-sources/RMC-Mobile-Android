package com.skydrm.rmc.ui.myspace.myvault.view.task;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.datalayer.repo.myvault.IMyVaultFile;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.sdk.rms.rest.myVault.MyVaultMetaDataResult;

public class GetMyVaultMetadataTask extends LoadTask<Void, MyVaultMetaDataResult> {
    private IMyVaultFile mFile;
    private ITaskCallback<Result, Exception> mCallback;

    private Exception mExp;

    public GetMyVaultMetadataTask(IMyVaultFile f, ITaskCallback<Result, Exception> c) {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
        this.mFile = f;
        this.mCallback = c;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mCallback != null) {
            mCallback.onTaskPreExecute();
        }
    }

    @Override
    protected MyVaultMetaDataResult doInBackground(Void... voids) {
        try {
            return mFile.getMetadata();
        } catch (Exception e) {
            mExp = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(MyVaultMetaDataResult r) {
        super.onPostExecute(r);
        if (mExp == null) {
            if (mCallback != null) {
                mCallback.onTaskExecuteSuccess(new Result(r));
            }
        } else {
            if (mCallback != null) {
                mCallback.onTaskExecuteFailed(mExp);
            }
        }
    }

    public class Result implements LoadTask.IResult {
        public MyVaultMetaDataResult result;

        public Result(MyVaultMetaDataResult r) {
            result = r;
        }
    }
}
