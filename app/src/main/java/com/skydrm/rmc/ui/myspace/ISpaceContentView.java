package com.skydrm.rmc.ui.myspace;

import com.skydrm.rmc.domain.NXFileItem;
import com.skydrm.rmc.reposystem.types.INxFile;

import java.util.List;

/**
 * Created by hhu on 5/18/2017.
 */

public interface ISpaceContentView {
    void initializing();

    void showEmptyView();

    void showNoRepositoryView();

    void onError(String msg);

    void showLoading();

    void changeCategoryBarStatus(boolean show, INxFile clickFileName);

    void notifySpecificItemChanged(List<NXFileItem> allItems, int changedPosition);

    void notifyDataChanged(List<NXFileItem> nxFileItems);

    void onRefreshDone();
}
