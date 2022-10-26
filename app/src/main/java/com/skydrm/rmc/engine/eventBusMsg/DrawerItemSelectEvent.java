package com.skydrm.rmc.engine.eventBusMsg;

import android.support.annotation.IntDef;

/**
 * Created by hhu on 6/15/2017.
 */

public class DrawerItemSelectEvent {
    public static final int FILES = 0x01 << 2;
    public static final int MYDRIVE = 0x02 << 2;
    public static final int MYVAULT = 0x03 << 2;
    private int driverType;
    private boolean reset;

    public DrawerItemSelectEvent(@SpaceType int spaceType, boolean reset) {
        this.driverType = spaceType;
        this.reset = reset;
    }

    public int getDriverType() {
        return driverType;
    }

    @IntDef({FILES, MYDRIVE, MYVAULT})
    @interface SpaceType {

    }

    public boolean isReset() {
        return reset;
    }
}
