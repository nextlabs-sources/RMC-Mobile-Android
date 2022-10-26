package com.skydrm.rmc.ui.project.feature.member.task;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.datalayer.repo.project.IMember;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.sdk.exception.RmsRestAPIException;

import java.util.ArrayList;
import java.util.List;

public class GetMemberTask extends LoadTask<Void, List<IMember>> {
    private IProject mProject;
    private boolean refresh;
    private Exception mExp;

    private ITaskCallback<Result, Exception> mCallback;

    private boolean pending;

    public GetMemberTask(IProject p, boolean refresh,
                  ITaskCallback<Result, Exception> callback) {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
        this.mProject = p;
        this.refresh = refresh;
        this.mCallback = callback;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mCallback != null) {
            mCallback.onTaskPreExecute();
        }
    }

    @Override
    protected List<IMember> doInBackground(Void... voids) {
        try {
            List<IMember> ret = new ArrayList<>();
            if (refresh) {
                List<IMember> syncMembers = mProject.syncMember();
                ret.addAll(syncMembers);
                if (pending) {
                    List<IMember> pendingMembers = mProject.syncPendingMember();
                    ret.addAll(pendingMembers);
                }
            } else {
                List<IMember> listMembers = mProject.listMember();
                ret.addAll(listMembers);
                if (pending) {
                    List<IMember> pendingMembers = mProject.syncPendingMember();
                    ret.addAll(pendingMembers);
                }
            }
            return ret;
        } catch (InvalidRMClientException e) {
            mExp = e;
        } catch (RmsRestAPIException e) {
            mExp = e;
        } catch (SessionInvalidException e) {
            mExp = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<IMember> members) {
        super.onPostExecute(members);
        if (mExp != null) {
            if (mCallback != null) {
                mCallback.onTaskExecuteFailed(mExp);
            }
        } else {
            if (mCallback != null) {
                mCallback.onTaskExecuteSuccess(new Result(members));
            }
        }
    }

    public class Result implements IResult {
        public List<IMember> mMembers;

        public Result(List<IMember> members) {
            this.mMembers = members;
        }
    }
}
