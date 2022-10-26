package com.skydrm.sdk.nxl.token;

public interface IToken {
    String getDuid();

    String getTokenStr();

    String getOtp();

    interface IExpiry {
        boolean isExpired(long now);
    }
}
