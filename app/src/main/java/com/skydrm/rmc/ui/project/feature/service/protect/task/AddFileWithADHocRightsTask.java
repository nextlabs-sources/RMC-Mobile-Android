package com.skydrm.rmc.ui.project.feature.service.protect.task;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.engine.FileOperation;
import com.skydrm.rmc.engine.LogSystem;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.rmc.ui.project.feature.service.core.MarkException;
import com.skydrm.rmc.ui.project.feature.service.core.MarkerStatus;
import com.skydrm.rmc.utils.FileUtils;
import com.skydrm.sdk.exception.NotNxlFileException;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.exception.TokenAccessDenyException;
import com.skydrm.sdk.policy.Expiry;
import com.skydrm.sdk.policy.Obligations;
import com.skydrm.sdk.policy.Rights;
import com.skydrm.sdk.rms.rest.project.file.UploadFileResult;

import java.io.File;
import java.io.FileNotFoundException;

public class AddFileWithADHocRightsTask extends LoadTask<Void, Boolean> {
    private IProject mProject;
    private String mMembershipId;
    private File mWorkingFile;
    private Rights mRights;
    private Obligations mObligations;
    private Expiry mExpiry;

    private int mProjectId;
    private String mParentPathId;

    private ITaskCallback<Result, Exception> mCallback;

    private Exception mExp;

    public AddFileWithADHocRightsTask(IProject project, String membershipId, File file,
                                      Rights rights, Obligations obligations, Expiry expiry,
                                      int projectId, String parentPathId,
                                      ITaskCallback<Result, Exception> callback) {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
        this.mProject = project;
        this.mMembershipId = membershipId;
        this.mWorkingFile = file;
        this.mRights = rights;
        this.mObligations = obligations;
        this.mExpiry = expiry;
        this.mProjectId = projectId;
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
            nxlPath = FileOperation.protectFile(mMembershipId, mWorkingFile, mRights, mObligations, mExpiry);
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
            //If upload success then send log.
            UploadFileResult result = FileOperation.uploadProjectFile(mProjectId, nxlFile, mParentPathId, null);
            if (result != null) {
                // Send log.
                LogSystem.sendProtectLog(nxlFile);
                // Delete the tmp protected file.
                FileUtils.deleteFile(nxlPath);

                try {
                    if (mProject != null) {
                        mProject.syncFile(mParentPathId, false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return true;
            }
        } catch (InvalidRMClientException e) {
            FileUtils.deleteFile(nxlPath);
            mExp = e;
        } catch (RmsRestAPIException e) {
            FileUtils.deleteFile(nxlPath);
            mExp = e;
        } catch (SessionInvalidException e) {
            FileUtils.deleteFile(nxlPath);
            mExp = e;
        } catch (NotNxlFileException e) {
            FileUtils.deleteFile(nxlPath);
            mExp = e;
        } catch (TokenAccessDenyException e) {
            FileUtils.deleteFile(nxlPath);
            mExp = e;
        } catch (FileNotFoundException e) {
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
                        new MarkException(MarkerStatus.STATUS_FAILED_COMMON, "Try protecting file failed.") :
                        mExp);
            }
        }
    }

    public class Result implements IResult {

    }
}
