package com.skydrm.sdk.rms.user.membership;

public class SystemBucketMemberShip extends ProjectMemberShip {

    public SystemBucketMemberShip(String id, int type, String tokenGroupName) {
        super(id, type, tokenGroupName, -1);
    }
}
