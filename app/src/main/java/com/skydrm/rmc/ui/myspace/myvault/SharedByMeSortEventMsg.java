package com.skydrm.rmc.ui.myspace.myvault;


import com.skydrm.rmc.utils.sort.SortType;

/**
 * Created by hhu on 8/2/2017.
 */

public class SharedByMeSortEventMsg {
    private SortType sortType;
    private boolean isSharedByMe = true;
    public SharedByMeSortEventMsg(SortType type) {
        this.sortType = type;
    }

    public SortType getSortType() {
        return sortType;
    }

    public boolean isSharedByMe() {
        return isSharedByMe;
    }
}
