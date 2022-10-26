package com.skydrm.rmc.engine.eventBusMsg;

import com.skydrm.rmc.domain.NXFileItem;

import java.util.List;

/**
 * Created by hhu on 5/24/2017.
 */

public class OfflineDataUpdateEvent {
    private List<NXFileItem> offlineItems;

    public OfflineDataUpdateEvent(List<NXFileItem> items) {
        this.offlineItems = items;
    }

    public List<NXFileItem> getOfflineItems() {
        return offlineItems;
    }
}
