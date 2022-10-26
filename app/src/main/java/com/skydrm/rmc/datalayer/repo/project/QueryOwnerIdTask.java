package com.skydrm.rmc.datalayer.repo.project;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.dbbridge.DBProvider;
import com.skydrm.rmc.dbbridge.IDBProjectItem;
import com.skydrm.rmc.dbbridge.IOwner;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.sdk.INxlClient;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.rms.user.IRmUser;
import com.skydrm.sdk.rms.user.membership.IMemberShip;
import com.skydrm.sdk.rms.user.membership.ProjectMemberShip;

import java.io.File;
import java.util.List;

public class QueryOwnerIdTask extends LoadTask<Void, Integer> {
    private File mWorkingFile;
    private ITaskCallback<Result, Exception> mCallback;

    private Exception mExp;

    public QueryOwnerIdTask(File f, ITaskCallback<Result, Exception> callback) {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
        this.mWorkingFile = f;
        this.mCallback = callback;
        mExp = new Exception("Unknown error");
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mCallback != null) {
            mCallback.onTaskPreExecute();
        }
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        int retVal = -1;
        if (mWorkingFile == null) {
            return retVal;
        }
        if (!mWorkingFile.exists() || mWorkingFile.isDirectory()) {
            return retVal;
        }
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        if (session == null) {
            return retVal;
        }
        INxlClient rmsClient = session.getRmsClient();
        if (rmsClient == null) {
            return retVal;
        }
        try {
            INxlFileFingerPrint fp = rmsClient.extractFingerPrint(mWorkingFile.getPath());
            if (fp == null) {
                return retVal;
            }
            String ownerID = fp.getOwnerID();
            if (ownerID == null || ownerID.isEmpty()) {
                return retVal;
            }
            IRmUser rmUser = session.getRmUser();
            List<IMemberShip> memberships = rmUser.getMemberships();
            if (memberships == null || memberships.size() == 0) {
                return retVal;
            }
            int projectId = -1;
            for (IMemberShip memberShip : memberships) {
                if (memberShip == null) {
                    continue;
                }
                if (ownerID.equals(memberShip.getId())) {
                    if (memberShip instanceof ProjectMemberShip) {
                        ProjectMemberShip pms = (ProjectMemberShip) memberShip;
                        projectId = pms.getProjectId();
                        break;
                    }
                }
            }
            if (projectId == -1) {
                return retVal;
            }
            DBProvider dbProvider = SkyDRMApp.getInstance().getDBProvider();
            IDBProjectItem dbItem = dbProvider.queryProjectItemByProjectID(projectId);
            if (dbItem == null) {
                return retVal;
            }
            IOwner owner = dbItem.getOwner();
            if (owner == null) {
                return retVal;
            }
            return owner.getUserId();
        } catch (Exception e) {
            mExp = e;
        }
        return retVal;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        if (integer != -1) {
            if (mCallback != null) {
                mCallback.onTaskExecuteSuccess(new Result(integer));
            }
        } else {
            if (mCallback != null) {
                mCallback.onTaskExecuteFailed(mExp);
            }
        }
    }

    public class Result implements IResult {
        public int ownerId;

        public Result(int ownerId) {
            this.ownerId = ownerId;
        }
    }
}
