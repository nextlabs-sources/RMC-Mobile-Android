package com.skydrm.rmc.engine.eventBusMsg;

import com.skydrm.sdk.rms.rest.user.User;

/**
 * Created by aning on 11/9/2017.
 */

public class ChangeExpiryDateEvent {
    public final User.IExpiry iExpiry;

    public ChangeExpiryDateEvent(User.IExpiry iExpiry) {
        this.iExpiry = iExpiry;
    }
}
