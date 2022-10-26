package com.skydrm.rmc.engine.eventBusMsg;


/**
 * Created by hhu on 5/23/2017.
 */

import com.skydrm.rmc.utils.sort.SortType;

/**
 * EventBus msg for select attached repository
 * (user select the repository list to select file system data display.)
 * the ui change site {@link {MySpaceSortMenu}}
 */
public class RepositorySelectEvent {
    private SortType sortType;

    public RepositorySelectEvent(SortType sortType) {
        this.sortType = sortType;
    }
}
