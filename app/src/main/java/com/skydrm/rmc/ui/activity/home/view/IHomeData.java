package com.skydrm.rmc.ui.activity.home.view;

import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.ui.activity.home.view.model.GetStorageSpaceTask;
import com.skydrm.rmc.ui.activity.home.view.model.GetWorkSpaceInfoTask;
import com.skydrm.rmc.ui.base.LoadTask;

import java.util.List;

public interface IHomeData {
    String getUserName();

    void getStorageUsed(LoadTask.ITaskCallback<GetStorageSpaceTask.Result, Exception> callback);

    void getWorkSpaceStorage(LoadTask.ITaskCallback<GetWorkSpaceInfoTask.Result, Exception> callback);

    List<BoundService> getUserLinkedRepo();
}
