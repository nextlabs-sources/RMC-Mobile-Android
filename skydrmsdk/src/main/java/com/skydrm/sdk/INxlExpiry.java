package com.skydrm.sdk;

import com.skydrm.sdk.policy.Expiry;

/**
 * Created by oye on 11/8/2017.
 */

public interface INxlExpiry {

    // determine whether has expired
    boolean isExpired();

    boolean isExpired(long currentMills);

    boolean isFuture();

    // used for UI level to display str for Client
    String formatString();

    Expiry getExpiry();
}
