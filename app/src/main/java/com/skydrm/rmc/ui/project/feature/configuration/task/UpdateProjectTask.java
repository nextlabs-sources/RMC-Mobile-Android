package com.skydrm.rmc.ui.project.feature.configuration.task;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.sdk.exception.RmsRestAPIException;

public class UpdateProjectTask extends LoadTask<Void, Boolean> {
    private IProject mProject;
    private ITaskCallback<Result, Exception> mCallback;

    private Exception mExp;
    private String mProjectName;
    private String mDesc;
    private String mInvitationMsg;

    public UpdateProjectTask(IProject p, String name, String desc, String msg,
                             ITaskCallback<Result, Exception> callback) {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
        this.mProject = p;
        this.mProjectName = name;
        this.mDesc = desc;
        this.mInvitationMsg = msg;
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
        try {
            return mProject.update(mProjectName, mDesc, mInvitationMsg);
        } catch (RmsRestAPIException e) {
            mExp = e;
        } catch (SessionInvalidException e) {
            mExp = e;
        } catch (InvalidRMClientException e) {
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
