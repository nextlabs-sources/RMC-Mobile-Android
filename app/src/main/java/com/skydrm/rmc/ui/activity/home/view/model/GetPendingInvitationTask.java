package com.skydrm.rmc.ui.activity.home.view.model;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.datalayer.RepoFactory;
import com.skydrm.rmc.datalayer.repo.base.RepoType;
import com.skydrm.rmc.datalayer.repo.project.IInvitePending;
import com.skydrm.rmc.datalayer.repo.project.ProjectRepo;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.sdk.exception.RmsRestAPIException;

import java.util.List;

public class GetPendingInvitationTask extends LoadTask<Void, List<IInvitePending>> {
    private ITaskCallback<GetPendingInvitationTask.Result, Exception> mCallback;
    private Exception mException;

    public GetPendingInvitationTask(ITaskCallback<GetPendingInvitationTask.Result, Exception> callback) {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
        this.mCallback = callback;
    }

    @Override
    protected List<IInvitePending> doInBackground(Void... voids) {
        ProjectRepo repo = (ProjectRepo) RepoFactory.getRepo(RepoType.TYPE_PROJECT);
        try {
            return repo.syncPendingInvitation();
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
    protected void onPostExecute(List<IInvitePending> result) {
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

    public class Result implements IResult {
        public List<IInvitePending> mPendingResult;

        public Result(List<IInvitePending> result) {
            this.mPendingResult = result;
        }
    }
}
