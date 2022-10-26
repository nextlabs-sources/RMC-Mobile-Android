package com.skydrm.rmc.engine.eventBusMsg;

import com.skydrm.rmc.domain.NXFileItem;

import java.util.List;

/**
 * Created by hhu on 5/24/2017.
 */

public class SpaceNotifyDataChangeEvent {
    private List<NXFileItem> newItems;

    public SpaceNotifyDataChangeEvent(List<NXFileItem> items) {
        this.newItems = items;
    }

    public List<NXFileItem> getNewItems() {
        return newItems;
    }
}
