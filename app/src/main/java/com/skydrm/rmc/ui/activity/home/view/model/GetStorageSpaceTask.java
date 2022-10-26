package com.skydrm.rmc.ui.activity.home.view.model;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.sdk.exception.RmsRestAPIException;

import org.json.JSONObject;

public class GetStorageSpaceTask extends LoadTask<Void, LoadTask.IResult> {
    private ITaskCallback<Result, Exception> mCallback;
    private Exception mException;

    GetStorageSpaceTask(ITaskCallback<Result, Exception> callback) {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
        this.mCallback = callback;
    }

    @Override
    protected IResult doInBackground(Void... voids) {
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        try {
            JSONObject responseObj = session.getRmsRestAPI()
                    .getMyDriveService(session.getRmUser())
                    .myDriveStorageUsed();
            String total = responseObj.optString("quota");
            String usage = responseObj.optString("usage");
            String myVault = responseObj.optString("myVaultUsage");
            final long totalLong = Long.parseLong(total);
            final long usageLong = Long.parseLong(usage);
            final long myVaultUsageLong = Long.parseLong(myVault);
            final long myDriveUsageLong = usageLong - myVaultUsageLong;
            return new Result(totalLong, usageLong, myDriveUsageLong, myVaultUsageLong);
        } catch (RmsRestAPIException e) {
            mException = e;
        } catch (SessionInvalidException e) {
            mException = e;
        } catch (InvalidRMClientException e) {
            mException = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(IResult result) {
        super.onPostExecute(result);
        if (result != null) {
            if (mCallback != null) {
                mCallback.onTaskExecuteSuccess((Result) result);
            }
        } else {
            if (mCallback != null) {
                mCallback.onTaskExecuteFailed(mException == null ?
                        new Exception("Fail to get result.") :
                        mException);
            }
        }
    }

    public class Result implements LoadTask.IResult {
        public long mTotal;
        public long mUsage;
        public long mMyDriveUsage;
        public long mMyVaultUsage;

        Result(long total, long usage,
               long myDriveUsage, long myVaultUsage) {
            this.mTotal = total;
            this.mUsage = usage;
            this.mMyDriveUsage = myDriveUsage;
            this.mMyVaultUsage = myVaultUsage;
        }
    }
}
