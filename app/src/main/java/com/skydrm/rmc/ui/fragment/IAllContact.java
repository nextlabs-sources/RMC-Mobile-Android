package com.skydrm.rmc.ui.fragment;

import com.skydrm.rmc.domain.NXFileItem;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.ui.base.IBaseContract;
import com.skydrm.rmc.utils.sort.SortType;

public interface IAllContact {
    interface IView extends IBaseContract.IBaseView<NXFileItem> {
        void showNoRepoView(boolean active);

        void updateCategoryBarStatus(boolean active, String pathId);
    }

    interface IPresenter extends IBaseContract.IBasePresenter<INxFile, SortType> {
        boolean needInterceptBackPress();

        void enterFolder(INxFile folder);

        void refreshRepo();

        void back();
    }
}
