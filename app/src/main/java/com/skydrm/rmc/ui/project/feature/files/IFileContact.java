package com.skydrm.rmc.ui.project.feature.files;

import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.ui.base.IBaseContract;
import com.skydrm.rmc.ui.common.NxlFileItem;
import com.skydrm.rmc.utils.sort.SortType;

public interface IFileContact {
    interface IView extends IBaseContract.IBaseView<NxlFileItem> {
    }

    interface IPresenter extends IBaseContract.IBasePresenter<INxlFile, SortType> {

    }
}
