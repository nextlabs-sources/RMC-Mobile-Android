package com.skydrm.sdk.nxl.token;

import android.support.annotation.NonNull;

import java.util.Objects;

class DecryptToken implements IToken {
    private String mDuid;
    private String mToken;
    private String mOtp;

    DecryptToken(String duid, String token, String otp) {
        this.mDuid = duid;
        this.mToken = token;
        this.mOtp = otp;
    }

    @Override
    public String getDuid() {
        return mDuid;
    }

    @Override
    public String getTokenStr() {
        return mToken;
    }

    @Override
    public String getOtp() {
        return mOtp;
    }

    @NonNull
    @Override
    public String toString() {
        return "{" + mDuid + ":" + mToken + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DecryptToken that = (DecryptToken) o;
        return Objects.equals(mDuid, that.mDuid) &&
                Objects.equals(mToken, that.mToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mDuid, mToken);
    }
}
