package com.skydrm.rmc.ui.myspace.myvault.view.task;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.datalayer.repo.myvault.IMyVaultFile;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.sdk.exception.RmsRestAPIException;

import java.io.FileNotFoundException;
import java.util.List;

public class ShareTask extends LoadTask<Void, Boolean> {
    private IMyVaultFile mFile;
    private List<String> mRights;
    private List<String> mEmails;
    private String mCmt;

    private ITaskCallback<Result, Exception> mCallback;
    private Exception mExp;

    public ShareTask(IMyVaultFile f, List<String> rights, List<String> emails,
                     ITaskCallback<Result, Exception> callback) {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
        this.mFile = f;

        this.mRights = rights;
        this.mEmails = emails;
        this.mCallback = callback;
    }

    public void setComment(String cmt) {
        this.mCmt = cmt;
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
            return mFile.share(mRights, mEmails, mCmt);
        } catch (FileNotFoundException
                | RmsRestAPIException e) {
            mExp = e;
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (mExp == null) {
            if (mCallback != null) {
                mCallback.onTaskExecuteSuccess(new Result());
            }
        } else {
            if (mCallback != null) {
                mCallback.onTaskExecuteFailed(mExp);
            }
        }
    }

    public class Result implements IResult {

    }
}
