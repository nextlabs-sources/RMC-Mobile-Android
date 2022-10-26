package com.skydrm.rmc.engine.eventBusMsg;

import com.skydrm.rmc.domain.NXFileItem;

import java.util.List;

/**
 * Created by hhu on 5/24/2017.
 */

public class FavoriteDataUpdateEvent {
    private List<NXFileItem> favoriteItems;

    public FavoriteDataUpdateEvent(List<NXFileItem> favoriteItems) {
        this.favoriteItems = favoriteItems;
    }

    public List<NXFileItem> getFavoriteItems() {
        return favoriteItems;
    }
}
