package com.skydrm.rmc.ui.myspace.sharewithme;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.datalayer.repo.sharedwithme.ISharedWithMeFile;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.rest.sharewithme.SharedWithMeReshareResult;

import java.util.List;

public class ReShareTask extends LoadTask<Void, SharedWithMeReshareResult> {
    private ISharedWithMeFile mSWMF;
    private List<String> mMembers;
    private String mComments;

    private ITaskCallback<IResult, Exception> mCallback;
    private Exception mExp;

    public ReShareTask(ISharedWithMeFile file, List<String> members, String comments,
                       ITaskCallback<IResult, Exception> callback) {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
        this.mSWMF = file;
        this.mMembers = members;
        this.mComments = comments;
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
    protected SharedWithMeReshareResult doInBackground(Void... voids) {
        if (mSWMF == null) {
            return null;
        }
        try {
            return mSWMF.reShare(mMembers, mComments);
        } catch (SessionInvalidException
                | InvalidRMClientException
                | RmsRestAPIException e) {
            mExp = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(SharedWithMeReshareResult result) {
        super.onPostExecute(result);
        if (mExp == null) {
            if (mCallback != null) {
                mCallback.onTaskExecuteSuccess(new Result(result.getResults().getNewSharedList(),
                        result.getResults().getAlreadySharedList()));
            }
        } else {
            if (mCallback != null) {
                mCallback.onTaskExecuteFailed(mExp);
            }
        }
    }

    public class Result implements IResult {
        public List<String> mNewSharedEmails;
        public List<String> mAlreadySharedEmails;

        public Result(List<String> newSharedEmails, List<String> alreadySharedEmails) {
            this.mNewSharedEmails = newSharedEmails;
            this.mAlreadySharedEmails = alreadySharedEmails;
        }
    }
}
