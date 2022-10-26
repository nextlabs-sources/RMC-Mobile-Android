package com.skydrm.rmc.engine.eventBusMsg;

/**
 * Created by aning on 5/22/2017.
 */

public class OperateResultNotifyEvent {
    private boolean bSucceed;
    private String mResultMsg;
    public OperateResultNotifyEvent(boolean resultStatus, String resultMsg) {
        this.bSucceed = resultStatus;
        this.mResultMsg = resultMsg;
    }

    public boolean isbSucceed() {
        return bSucceed;
    }

    public String getResultMsg() {
        return mResultMsg;
    }
}
