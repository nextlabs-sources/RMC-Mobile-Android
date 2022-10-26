package com.skydrm.sdk.rms.types;

import com.skydrm.sdk.policy.Watermark;

/**
 * Created by aning on 12/20/2016.
 */

public class HeartbeatResponse {
    private Watermark mWatermark;

    public void setWatermark(Watermark watermark) {
        mWatermark = watermark;
    }

    public Watermark getWatermark() {
        return mWatermark;
    }

    // others
}
