package com.skydrm.sdk.rms.user.membership;

import android.os.Parcel;

public class TenantMemberShip extends BaseMemberShip {
    private String mTenantId;

    public TenantMemberShip(String id, int type, String tokenGroupName, String tenantId) {
        super(id, type, tokenGroupName);
        this.mTenantId = tenantId;
    }

    TenantMemberShip(Parcel in) {
        super(in);
        this.mTenantId = in.readString();
    }

    public String getTenantId() {
        return mTenantId;
    }

    @Override
    public int describeContents() {
        return super.describeContents();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(mTenantId);
    }
}
