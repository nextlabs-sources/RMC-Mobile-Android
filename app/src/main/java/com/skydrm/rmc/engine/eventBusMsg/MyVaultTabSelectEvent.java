package com.skydrm.rmc.engine.eventBusMsg;

/**
 * Created by hhu on 5/26/2017.
 */

public class MyVaultTabSelectEvent {
    private int position;

    public MyVaultTabSelectEvent(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
