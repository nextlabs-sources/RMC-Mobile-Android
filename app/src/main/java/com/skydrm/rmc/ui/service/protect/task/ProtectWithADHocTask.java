package com.skydrm.rmc.ui.service.protect.task;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.engine.FileOperation;
import com.skydrm.rmc.engine.LogSystem;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.rmc.ui.service.protect.IProtectService;
import com.skydrm.rmc.utils.FileUtils;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.policy.Expiry;
import com.skydrm.sdk.policy.Obligations;
import com.skydrm.sdk.policy.Rights;

import java.io.File;
import java.io.FileNotFoundException;

public class ProtectWithADHocTask extends LoadTask<Void, Boolean> {
    private IProtectService mService;
    private String mMembershipId;
    private File mWorkingFile;
    private Rights mRights;
    private Obligations mObligations;
    private Expiry mExpiry;

    private String mParentPathId;

    private ITaskCallback<Result, Exception> mCallback;
    private Exception mExp;

    public ProtectWithADHocTask(IProtectService service, String membershipId, File file,
                                Rights rights, Obligations obligations, Expiry expiry,
                                String parentPathId,
                                ITaskCallback<Result, Exception> callback) {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
        this.mService = service;
        this.mMembershipId = membershipId;
        this.mWorkingFile = file;
        this.mRights = rights;
        this.mObligations = obligations;
        this.mExpiry = expiry;
        this.mParentPathId = parentPathId;
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
        String nxlPath = "";
        try {
            nxlPath = FileOperation.protectFile(mMembershipId, mWorkingFile,
                    mRights, mObligations, mExpiry);
        } catch (FileNotFoundException | RmsRestAPIException e) {
            this.mExp = e;
        }
        if (nxlPath == null || nxlPath.isEmpty()) {
            return false;
        }
        File nxlFile = new File(nxlPath);
        if (!nxlFile.exists()) {
            return false;
        }
        if (!nxlFile.isFile()) {
            FileUtils.deleteFile(nxlPath);
            return false;
        }

        try {
            boolean result = mService.upload(nxlFile, mParentPathId);
            if (result) {
                // Send log.
                LogSystem.sendProtectLog(nxlFile);
                // Delete the tmp protected file.
                FileUtils.deleteFile(nxlPath);

                return true;
            }
        } catch (Exception e) {
            FileUtils.deleteFile(nxlPath);
            mExp = e;
        }

        return false;
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
                mCallback.onTaskExecuteFailed(mExp == null ?
                        new Exception("Try protecting file failed.") :
                        mExp);
            }
        }
    }

    public class Result implements IResult {

    }
}
