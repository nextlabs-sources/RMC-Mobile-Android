package com.skydrm.rmc.engine.watermark;

/**
 * Created by aning on 11/4/2017.
 * <p>
 * EventBus event that watermark if is invalid, including empty or exceed 50 chars length.
 */

public class WatermarkSetInvalidEvent {
    public final boolean bSetInvalid;

    public WatermarkSetInvalidEvent(boolean bSetInvalid) {
        this.bSetInvalid = bSetInvalid;
    }
}
