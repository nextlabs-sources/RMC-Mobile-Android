package com.skydrm.rmc.engine.eventBusMsg;

/**
 * Created by aning on 5/17/2017.
 */

public class ShowConvertProgressEvent {
    private int convertType;
    private int progressValue;
    public ShowConvertProgressEvent(int convertType, int progressValue) {
        this.convertType = convertType;
        this.progressValue = progressValue;
    }
    public int getConvertType() {
        return this.convertType;
    }

    public int getProgressValue() {
        return this.progressValue;
    }

    public static class ConvertType {
        public static int OFFICE_CONVERT = 0x01;
        public static int THREE_D_CONVERT = 0x02;
    }
}
