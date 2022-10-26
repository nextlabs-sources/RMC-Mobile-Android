package com.skydrm.sdk.rms.user.membership;

import android.os.Parcel;

public class BaseMemberShip implements IMemberShip {
    private String mId;
    private int mType;
    private String mTokenGroupName;

    BaseMemberShip(String id, int type, String tokenGroupName) {
        this.mId = id;
        this.mType = type;
        this.mTokenGroupName = tokenGroupName;
    }

    BaseMemberShip(Parcel in) {
        this.mId = in.readString();
        this.mType = in.readInt();
        this.mTokenGroupName = in.readString();
    }

    @Override
    public String getId() {
        return mId;
    }

    @Override
    public int getType() {
        return mType;
    }

    @Override
    public String getTokenGroupName() {
        return mTokenGroupName;
    }


    public static Creator<IMemberShip> CREATOR = new Creator<IMemberShip>() {
        @Override
        public IMemberShip createFromParcel(Parcel in) {
            return new BaseMemberShip(in);
        }

        @Override
        public IMemberShip[] newArray(int size) {
            return new BaseMemberShip[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeInt(mType);
        dest.writeString(mTokenGroupName);
    }
}
