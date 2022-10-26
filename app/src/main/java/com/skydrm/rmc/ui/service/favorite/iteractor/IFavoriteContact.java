package com.skydrm.rmc.ui.service.favorite.iteractor;

import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.ui.base.IBaseContract;
import com.skydrm.rmc.ui.service.favorite.model.bean.FavoriteItem;
import com.skydrm.rmc.utils.sort.SortType;

public interface IFavoriteContact {
    interface IView extends IBaseContract.IBaseView<FavoriteItem> {

    }

    interface IPresenter extends IBaseContract.IBasePresenter<INxlFile, SortType> {

    }
}
