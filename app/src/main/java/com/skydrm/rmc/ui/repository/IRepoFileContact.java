package com.skydrm.rmc.ui.repository;

import com.skydrm.rmc.domain.NXFileItem;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.ui.base.IBaseContract;
import com.skydrm.rmc.utils.sort.SortType;

public interface IRepoFileContact {
    interface IRepoFileView extends IBaseContract.IBaseView<NXFileItem> {

    }

    interface IRepoFilePresenter extends IBaseContract.IBasePresenter<INxFile, SortType> {
        boolean isRoot();

        INxFile getDestFolder();

        void navigateBack(String parentPathId);
    }
}
