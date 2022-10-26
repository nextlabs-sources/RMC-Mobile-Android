package com.skydrm.sdk.nxl.token;

import java.util.Objects;

class EncryptToken implements IToken {
    private String mDuid;
    private String mToken;
    private String mOtp;

    EncryptToken(String duid, String token, String otp) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EncryptToken that = (EncryptToken) o;
        return Objects.equals(mDuid, that.mDuid) &&
                Objects.equals(mToken, that.mToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mDuid, mToken);
    }
}
