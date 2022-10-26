package com.skydrm.rmc.ui.activity.home.view;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.ui.activity.home.view.model.GetStorageSpaceTask;
import com.skydrm.rmc.ui.activity.home.view.model.GetWorkSpaceInfoTask;
import com.skydrm.rmc.ui.activity.home.view.model.HomeData;

public class HomePresenter implements IHomeContact.IPresenter {
    private IHomeContact.IView mView;
    private IHomeData mData;
    private GetWorkSpaceInfoCallback mGetWorkSpaceInfoCallback;
    private GetMyDriveInfoCallback mGetMyDriveInfoCallback;

    public HomePresenter(IHomeContact.IView view) {
        this.mView = view;
        this.mData = new HomeData();
        this.mGetMyDriveInfoCallback = new GetMyDriveInfoCallback();
        this.mGetWorkSpaceInfoCallback = new GetWorkSpaceInfoCallback();
    }

    @Override
    public void initialize() {
        getUserInfo();
        if (!onPremiseLogin()) {
            getBandedRepo();
        }
        //async.
        getStorageUsed(onPremiseLogin());
    }

    @Override
    public void getUserInfo() {
        if (mView == null) {
            return;
        }
        mView.showUserName(mData.getUserName());
    }

    @Override
    public void getStorageUsed(boolean supportWorkSpace) {
        mData.getStorageUsed(mGetMyDriveInfoCallback);
        if (supportWorkSpace) {
            mData.getWorkSpaceStorage(mGetWorkSpaceInfoCallback);
        }
    }

    @Override
    public void getBandedRepo() {
        if (mView == null) {
            return;
        }
        mView.showBandedRepo(mData.getUserLinkedRepo());
    }

    @Override
    public void onDestroy() {
        if (mView != null) {
            mView = null;
        }
        if (mGetMyDriveInfoCallback != null) {
            mGetMyDriveInfoCallback = null;
        }
        if (mGetWorkSpaceInfoCallback != null) {
            mGetWorkSpaceInfoCallback = null;
        }
    }

    private boolean onPremiseLogin() {
        return SkyDRMApp.getInstance().isOnPremise();
    }

    class GetMyDriveInfoCallback implements GetStorageSpaceTask.ITaskCallback<GetStorageSpaceTask.Result, Exception> {

        @Override
        public void onTaskPreExecute() {

        }

        @Override
        public void onTaskExecuteSuccess(GetStorageSpaceTask.Result r) {
            if (mView != null && r != null) {
                mView.showStorageUsed(r.mTotal, r.mUsage, r.mMyDriveUsage, r.mMyVaultUsage);
            }
        }

        @Override
        public void onTaskExecuteFailed(Exception e) {
            if (mView != null) {
                mView.onError(e);
            }
        }
    }

    class GetWorkSpaceInfoCallback implements GetWorkSpaceInfoTask.ITaskCallback<GetWorkSpaceInfoTask.Result, Exception> {

        @Override
        public void onTaskPreExecute() {

        }

        @Override
        public void onTaskExecuteSuccess(GetWorkSpaceInfoTask.Result results) {
            if (mView != null && results != null) {
                mView.showWorkSpaceInfo(results.mUsage, results.mQuota, results.mTotalFiles);
            }
        }

        @Override
        public void onTaskExecuteFailed(Exception e) {
            if (mView == null) {
                return;
            }
            mView.onError(e);
        }
    }

}
