package com.skydrm.rmc.ui.project.service.contact.task;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.datalayer.RepoFactory;
import com.skydrm.rmc.datalayer.repo.base.RepoType;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.datalayer.repo.project.ProjectRepo;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.sdk.exception.RmsRestAPIException;

import java.util.List;

public class NewProjectTask extends LoadTask<Void, IProject> {
    private String mName;
    private String mDesc;
    private List<String> mEmails;
    private String mInvitationMsg;

    private ITaskCallback<Result, Exception> mCallback;
    private Exception mException;

    public NewProjectTask(String name, String desc,
                          List<String> emails, String invitationMsg,
                          ITaskCallback<Result, Exception> callback) {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
        this.mName = name;
        this.mDesc = desc;
        this.mEmails = emails;
        this.mInvitationMsg = invitationMsg;
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
    protected IProject doInBackground(Void... voids) {
        ProjectRepo repo = (ProjectRepo) RepoFactory.getRepo(RepoType.TYPE_PROJECT);
        try {
            return repo.createProject(mName, mDesc, mEmails, mInvitationMsg);
        } catch (SessionInvalidException e) {
            mException = e;
        } catch (InvalidRMClientException e) {
            mException = e;
        } catch (RmsRestAPIException e) {
            mException = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(IProject result) {
        super.onPostExecute(result);
        if (result != null) {
            if (mCallback != null) {
                mCallback.onTaskExecuteSuccess(new Result(result));
            }
        } else {
            if (mCallback != null) {
                mCallback.onTaskExecuteFailed(mException == null ?
                        new Exception("Unknown error") : mException);
            }
        }
    }

    public class Result implements IResult {
        public IProject project;

        public Result(IProject p) {
            this.project = p;
        }
    }
}
