package com.skydrm.rmc.ui.service.offline.display.msg;


import com.skydrm.rmc.utils.sort.SortType;

@Deprecated
public class OfflineSortMsg {
    private SortType mSortType;

    public OfflineSortMsg(SortType sortType) {
        this.mSortType = sortType;
    }

    public SortType getSortType() {
        return mSortType;
    }
}
