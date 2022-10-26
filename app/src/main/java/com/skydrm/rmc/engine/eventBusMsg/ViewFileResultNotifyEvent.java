package com.skydrm.rmc.engine.eventBusMsg;

/**
 * Created by aning on 5/23/2017.
 */

public class ViewFileResultNotifyEvent extends OperateResultNotifyEvent {
    public ViewFileResultNotifyEvent(boolean resultStatus, String resultMsg) {
        super(resultStatus, resultMsg);
    }
}
