package com.skydrm.rmc.ui.activity.home.view.model;

import android.support.annotation.IntDef;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.datalayer.RepoFactory;
import com.skydrm.rmc.datalayer.repo.base.IBaseRepo;
import com.skydrm.rmc.datalayer.repo.base.RepoType;
import com.skydrm.rmc.datalayer.repo.myvault.MyVaultRepo;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.datalayer.repo.project.ProjectRepo;
import com.skydrm.rmc.datalayer.repo.sharedwithme.SharedWithMeRepo;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.sdk.exception.RmsRestAPIException;

import java.util.ArrayList;
import java.util.List;

public class GetProjectsTask extends LoadTask<Void, List<IProject>> {
    private boolean sync;
    private ITaskCallback<Result, Exception> mCallback;

    private Exception mException;
    private int mType;
    private boolean resetStatus;

    public GetProjectsTask(@Type int type, boolean resetStatus,
                           boolean sync, ITaskCallback<Result, Exception> callback) {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
        this.sync = sync;
        this.resetStatus = resetStatus;
        this.mType = type;
        this.mCallback = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mCallback == null) {
            return;
        }
        mCallback.onTaskPreExecute();
    }

    @Override
    protected List<IProject> doInBackground(Void... voids) {
        ProjectRepo repo = (ProjectRepo) RepoFactory.getRepo(RepoType.TYPE_PROJECT);
        if (repo == null) {
            return null;
        }
        try {
            List<IProject> ret = sync ? repo.syncProject(mType) : repo.listProject(mType);
            if (resetStatus) {
                RepoFactory.updateResetAllOperationStatus();
            }
            return ret;
        } catch (SessionInvalidException
                | InvalidRMClientException
                | RmsRestAPIException e) {
            e.printStackTrace();
            mException = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<IProject> result) {
        super.onPostExecute(result);
        if (mException != null) {
            if (mCallback == null) {
                return;
            }
            mCallback.onTaskExecuteFailed(mException);
        } else {
            if (mCallback == null) {
                return;
            }
            mCallback.onTaskExecuteSuccess(new Result(result));
        }
    }

    public class Result implements LoadTask.IResult {
        private List<IProject> mResults;

        public Result(List<IProject> results) {
            this.mResults = results;
        }

        public List<IProject> getCreatedByMe() {
            List<IProject> createdByMe = new ArrayList<>();
            if (mResults == null || mResults.size() == 0) {
                return createdByMe;
            }
            for (IProject p : mResults) {
                if (p.isOwnedByMe())
                    createdByMe.add(p);
            }
            return createdByMe;
        }

        public List<IProject> getInvitedByOther() {
            List<IProject> invitedByOther = new ArrayList<>();
            if (mResults == null || mResults.size() == 0) {
                return invitedByOther;
            }
            for (IProject p : mResults) {
                if (!p.isOwnedByMe())
                    invitedByOther.add(p);
            }
            return invitedByOther;
        }
    }

    @IntDef({0, 1, 2,})
    public @interface Type {

    }
}
