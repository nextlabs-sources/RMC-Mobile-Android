package com.skydrm.rmc.ui.service.favorite.model.eventmsg;


import com.skydrm.rmc.utils.sort.SortType;

/**
 * Created by hhu on 8/24/2017.
 */

public class FavoriteSortEvent {
    private SortType sortType;

    public FavoriteSortEvent(SortType sortType) {
        this.sortType = sortType;
    }

    public SortType getSortType() {
        return sortType;
    }
}
