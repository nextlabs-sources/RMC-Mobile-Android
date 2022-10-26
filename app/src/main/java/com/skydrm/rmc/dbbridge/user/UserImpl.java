package com.skydrm.rmc.dbbridge.user;

import com.skydrm.rmc.database.table.membership.Membership;
import com.skydrm.rmc.database.table.user.User;
import com.skydrm.rmc.dbbridge.IMembership;
import com.skydrm.rmc.dbbridge.IUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UserImpl implements IUser {
    private User mRaw;
    private IPreference mPreference;

    public UserImpl(User raw) {
        this.mRaw = raw;
        mPreference = raw.preference;
    }

    @Override
    public int getUserTBPK() {
        return mRaw._id;
    }

    @Override
    public int getServerTBPK() {
        return mRaw._server_id;
    }

    @Override
    public String getName() {
        return mRaw.name;
    }

    @Override
    public String getEmail() {
        return mRaw.email;
    }

    @Override
    public int getRMSUserID() {
        return mRaw.userId;
    }

    @Override
    public int getIdpType() {
        return mRaw.idpType;
    }

    @Override
    public long getTTL() {
        return mRaw.ttl;
    }

    @Override
    public String getTicket() {
        return mRaw.ticket;
    }

    @Override
    public String getTenantId() {
        return mRaw.tenantId;
    }

    @Override
    public String getDefaultTenant() {
        return mRaw.defaultTenant;
    }

    @Override
    public String getDefaultTenantURL() {
        return mRaw.defaultTenantUrl;
    }

    @Override
    public List<IMembership> getMemberships() {
        List<IMembership> ret = new ArrayList<>();
        for (Membership m : mRaw.memberships) {
            ret.add(new MembershipImpl(m));
        }
        return ret;
    }

    @Override
    public boolean isProjectAdmin() {
        return mRaw.isProjectAdmin;
    }

    @Override
    public boolean isTenantAdmin() {
        return mRaw.isTenantAdmin;
    }

    @Override
    public IPreference getPreference() {
        return mPreference;
    }

    @Override
    public String getUserRawJson() {
        return mRaw.user_raw_json;
    }
}
