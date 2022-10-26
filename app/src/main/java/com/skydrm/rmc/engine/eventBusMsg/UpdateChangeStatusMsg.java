package com.skydrm.rmc.engine.eventBusMsg;

/**
 * Created by hhu on 6/9/2017.
 */

public class UpdateChangeStatusMsg {
    private boolean dirty;

    public UpdateChangeStatusMsg(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean isDirty() {
        return dirty;
    }
}
