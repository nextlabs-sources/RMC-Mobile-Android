package com.skydrm.rmc.dbbridge.user;

import com.skydrm.rmc.database.table.membership.Membership;
import com.skydrm.rmc.dbbridge.IMembership;

public class MembershipImpl implements IMembership {
    private Membership mRaw;

    MembershipImpl(Membership raw) {
        this.mRaw = raw;
    }

    @Override
    public String getId() {
        return mRaw.id;
    }

    @Override
    public int getType() {
        return mRaw.type;
    }

    @Override
    public String getTokenGroupName() {
        return mRaw.tenantId;
    }

    @Override
    public int getProjectId() {
        return mRaw.projectId;
    }
}
