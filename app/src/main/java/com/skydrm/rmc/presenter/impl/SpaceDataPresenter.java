package com.skydrm.rmc.presenter.impl;

import com.skydrm.rmc.reposystem.IRemoteRepo;
import com.skydrm.rmc.reposystem.types.INxFile;

/**
 * Created by hhu on 5/18/2017.
 */
@Deprecated
public class SpaceDataPresenter implements IRemoteRepo.IListFilesCallback {
    @Override
    public void onFinishedList(boolean taskStatus, INxFile file, String errorMsg) {

    }
}
