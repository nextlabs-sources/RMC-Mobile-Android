package com.skydrm.rmc.engine.eventBusMsg;

import com.skydrm.sdk.INxlFileFingerPrint;

/**
 * Created by aning on 2/8/2018.
 */

public class ViewPageToSharedWithMeEvent {
    public final INxlFileFingerPrint mNxlFileFingerPrint;

    public ViewPageToSharedWithMeEvent(INxlFileFingerPrint nxlFileFingerPrint) {
        this.mNxlFileFingerPrint = nxlFileFingerPrint;
    }
}
