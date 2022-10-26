package com.skydrm.rmc.engine.eventBusMsg;

/**
 * Created by aning on 5/22/2017.
 */

public class ConvertResultNotifyEvent extends OperateResultNotifyEvent {
    public ConvertResultNotifyEvent(boolean resultStatus, String resultMsg) {
        super(resultStatus, resultMsg);
    }
}
