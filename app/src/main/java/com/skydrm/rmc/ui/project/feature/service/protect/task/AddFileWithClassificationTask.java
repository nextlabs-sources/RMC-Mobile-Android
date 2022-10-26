package com.skydrm.rmc.ui.project.feature.service.protect.task;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.datalayer.repo.project.Project;
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
import com.skydrm.sdk.rms.rest.project.file.UploadFileResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Set;

public class AddFileWithClassificationTask extends LoadTask<Void, Boolean> {
    private IProject mProject;
    private File mWorkingFile;
    private String mMembershipId;
    private Map<String, Set<String>> mTags;

    private int mProjectId;
    private String mParentPathId;
    private Exception mExp;

    private ITaskCallback<Result, Exception> mCallback;

    public AddFileWithClassificationTask(IProject project, File f, String membershipId, Map<String, Set<String>> tags,
                                         int projectId, String parentPathId,
                                         ITaskCallback<Result, Exception> callback) {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
        this.mProject = project;
        this.mWorkingFile = f;
        this.mMembershipId = membershipId;
        this.mTags = tags;
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
            nxlPath = FileOperation.protectFile(mWorkingFile, mMembershipId, mTags);
        } catch (FileNotFoundException e) {
            mExp = e;
        } catch (RmsRestAPIException e) {
            mExp = e;
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
            UploadFileResult result = FileOperation.uploadProjectFile(mProjectId, nxlFile,
                    mParentPathId, null);
            if (result != null) {
                // Send log.
                LogSystem.sendProtectLog(nxlFile);

                // Delete the tmp protected file.
                FileUtils.deleteFile(nxlPath);

                try {
                    // Refresh the file list.
                    if (mProject != null) {
                        mProject.syncFile(mParentPathId,
                                false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return true;
            }
        } catch (SessionInvalidException e) {
            FileUtils.deleteFile(nxlPath);
            mExp = e;
        } catch (InvalidRMClientException e) {
            FileUtils.deleteFile(nxlPath);
            mExp = e;
        } catch (RmsRestAPIException e) {
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
