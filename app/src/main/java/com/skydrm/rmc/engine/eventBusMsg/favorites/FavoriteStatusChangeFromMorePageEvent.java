package com.skydrm.rmc.engine.eventBusMsg.favorites;

import com.skydrm.rmc.reposystem.types.INxFile;

/**
 * Created by hhu on 8/28/2017.
 */

public class FavoriteStatusChangeFromMorePageEvent {
    public INxFile mFile;
    public boolean favorite;

    public FavoriteStatusChangeFromMorePageEvent(INxFile f, boolean favorite) {
        this.mFile = f;
        this.favorite = favorite;
    }
}
