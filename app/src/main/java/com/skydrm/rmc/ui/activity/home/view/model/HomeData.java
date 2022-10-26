package com.skydrm.rmc.ui.activity.home.view.model;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.ui.activity.home.view.IHomeData;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.sdk.rms.user.IRmUser;

import java.util.List;

public class HomeData implements IHomeData {

    @Override
    public String getUserName() {
        try {
            IRmUser rmUser = SkyDRMApp.getInstance().getSession().getRmUser();
            return rmUser.getName();
        } catch (InvalidRMClientException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void getStorageUsed(LoadTask.ITaskCallback<GetStorageSpaceTask.Result, Exception> callback) {
        new GetStorageSpaceTask(callback).run();
    }

    @Override
    public void getWorkSpaceStorage(LoadTask.ITaskCallback<GetWorkSpaceInfoTask.Result, Exception> callback) {
        new GetWorkSpaceInfoTask(callback).run();
    }

    @Override
    public List<BoundService> getUserLinkedRepo() {
        return SkyDRMApp.getInstance().getUserLinkedRepos();
    }

}
