package com.skydrm.sdk.rms.user.membership;

import android.os.Parcelable;

public interface IMemberShip extends Parcelable {
    String getId();

    int getType();

    String getTokenGroupName();
}
