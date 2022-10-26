package com.skydrm.rmc.engine.eventBusMsg;

import android.support.annotation.IntDef;

/**
 * Created by hhu on 5/23/2017.
 */

public class TabSelectEvent {
    public static final int TAB_ALL = 0x001 << 2;
    public static final int TAB_FAVORITE = 0x002 << 2;
    public static final int TAB_OFFLINE = 0x003 << 2;
    private int position = TAB_ALL;

    public TabSelectEvent(int selectPosition) {
        switch (selectPosition) {
            case 0:
                position = TAB_ALL;
                break;
            case 1:
                position = TAB_FAVORITE;
                break;
            case 2:
                position = TAB_OFFLINE;
                break;
        }
    }

    public int getPosition() {
        return position;
    }

    @IntDef({TAB_ALL, TAB_FAVORITE, TAB_OFFLINE})
    public @interface Tab {

    }
}
