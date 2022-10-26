package com.skydrm.rmc.ui.myspace.sharewithme;

import com.skydrm.rmc.utils.sort.SortType;

/**
 * Created by hhu on 8/2/2017.
 */

public class SortEventMsg {
    private SortType sortType;

    public SortEventMsg(SortType type) {
        this.sortType = type;
    }

    public SortType getSortType() {
        return sortType;
    }
}
