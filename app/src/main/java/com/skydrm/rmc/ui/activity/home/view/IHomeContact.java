package com.skydrm.rmc.ui.activity.home.view;

import com.skydrm.rmc.reposystem.types.BoundService;

import java.util.List;

public interface IHomeContact {
    interface IView {
        void showUserName(String name);

        void showStorageUsed(long totalLong, long totalUsageSize, long myDriveUsageSize, long myVaultUsageSize);

        void showWorkSpaceInfo(long usage, long quota, int totalFiles);

        void showBandedRepo(List<BoundService> services);

        void onError(Exception e);
    }

    interface IPresenter {
        void initialize();

        void getUserInfo();

        void getStorageUsed(boolean supportWorkSpace);

        void getBandedRepo();

        void onDestroy();
    }
}
