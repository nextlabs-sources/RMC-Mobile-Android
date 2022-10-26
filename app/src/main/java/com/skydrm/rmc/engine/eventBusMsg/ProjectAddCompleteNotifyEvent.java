package com.skydrm.rmc.engine.eventBusMsg;

public class ProjectAddCompleteNotifyEvent {
    private String mParentPathId;

    public ProjectAddCompleteNotifyEvent(String mParentPathId) {
        this.mParentPathId = mParentPathId;
    }

    public String getParentPathId() {
        return mParentPathId;
    }

    public void setParentPathId(String mParentPathId) {
        this.mParentPathId = mParentPathId;
    }
}
