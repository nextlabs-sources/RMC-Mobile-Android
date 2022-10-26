package com.skydrm.sdk.rms.user;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.skydrm.sdk.rms.user.membership.BaseMemberShip;
import com.skydrm.sdk.rms.user.membership.IMemberShip;
import com.skydrm.sdk.rms.user.membership.ProjectMemberShip;
import com.skydrm.sdk.rms.user.membership.SystemBucketMemberShip;
import com.skydrm.sdk.rms.user.membership.TenantMemberShip;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RmUser implements IRmUser, Parcelable {
    private int mUserId;
    private String mTicket;
    private String mTenantId;
    private String mTokenGroupName;
    private long mTtl;
    private String mName;
    private String mEmail;
    // idp type (this area is used to judge the 3rd party account login our app)
    private int mIdpType;
    private List<IMemberShip> mMemberShips;
    private String mDefaultTenant;
    private String mDefaultTenantUrl;

    private String mMembershipId;

    private volatile boolean isTenantAdmin;
    private volatile boolean isProjectAdmin;
    private volatile boolean isADHocEnabled = true;

    private RmUser() {
        mUserId = -1;
        mTicket = "";
        mTenantId = "";
        mTokenGroupName = "";
        mTtl = -1;
        mName = "";
        mEmail = "";
        mIdpType = -1;
        mMemberShips = null;
        mDefaultTenant = "";
        mDefaultTenantUrl = "";
        isProjectAdmin = false;
        isTenantAdmin = false;
    }

    private RmUser(Parcel in) {
        mUserId = in.readInt();
        mTicket = in.readString();
        mTenantId = in.readString();
        mTokenGroupName = in.readString();
        mTtl = in.readLong();
        mName = in.readString();
        mEmail = in.readString();
        mIdpType = in.readInt();
        mMemberShips = in.createTypedArrayList(BaseMemberShip.CREATOR);
        mDefaultTenant = in.readString();
        mDefaultTenantUrl = in.readString();
        isTenantAdmin = in.readInt() == 1;
        isProjectAdmin = in.readInt() == 1;
    }

    public static final Creator<RmUser> CREATOR = new Creator<RmUser>() {
        @Override
        public RmUser createFromParcel(Parcel in) {
            return new RmUser(in);
        }

        @Override
        public RmUser[] newArray(int size) {
            return new RmUser[size];
        }
    };

    @Override
    public int getUserId() {
        return mUserId;
    }

    @Override
    public String getUserIdStr() {
        return Integer.toString(mUserId);
    }

    @Override
    public String getTicket() {
        return mTicket;
    }

    @Override
    public String getTenantId() {
        return mTenantId;
    }

    @Override
    public String getTokenGroupName() {
        return mTokenGroupName;
    }

    @Override
    public long getTtl() {
        return mTtl;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getEmail() {
        return mEmail;
    }

    @Override
    public int getIdpType() {
        return mIdpType;
    }

    @Override
    public String getDefaultTenant() {
        return mDefaultTenant;
    }

    @Override
    public String getDefaultTenantUrl() {
        return mDefaultTenantUrl;
    }

    @Override
    public boolean isOwner(String ownerId) {
        if (ownerId == null || ownerId.isEmpty() || ownerId.length() < 5) {
            return false;
        }
        return ownerId.equals(mMembershipId);
    }

    @Override
    public boolean isProjectAdmin() {
        return isProjectAdmin;
    }

    @Override
    public boolean isTenantAdmin() {
        return isTenantAdmin;
    }

    @Override
    public boolean isADHocEnabled() {
        return isADHocEnabled;
    }

    public void setTenantAdmin(boolean tenantAdmin) {
        isTenantAdmin = tenantAdmin;
    }

    public void setProjectAdmin(boolean projectAdmin) {
        isProjectAdmin = projectAdmin;
    }

    public void setADHocEnabled(boolean ADHocEnabled) {
        isADHocEnabled = ADHocEnabled;
    }

    @Override
    public List<IMemberShip> getMemberships() {
        return mMemberShips;
    }

    @Override
    public void setName(String name) {
        this.mName = name;
    }

    @Override
    public void updateOrInsertMembershipItem(IMemberShip memberShip) {
        //reject service
        if (mMemberShips == null || mMemberShips.size() == 0) {
            return;
        }
        if (memberShip instanceof ProjectMemberShip) {
            ProjectMemberShip pms = (ProjectMemberShip) memberShip;
            for (IMemberShip m : mMemberShips) {
                if (m.getTokenGroupName().equals(pms.getTokenGroupName())
                        && m.getId().equals(pms.getId())
                        && m.getType() == pms.getType()) {
                    return;
                }
            }
            mMemberShips.add(memberShip);
        }
    }

    @Override
    public String getMembershipId() {
        return mMembershipId;
    }

    @Override
    public String toString() {
        //rebuild jason string inorder one of them changed by user current the rmUsername
        // can be changed by user ,other text area may be changed by user in future
        JSONObject responseObj = new JSONObject();
        try {
            JSONObject extraObject = new JSONObject();
            extraObject.put("userId", mUserId);
            extraObject.put("ticket", mTicket);
            extraObject.put("tenantId", mTenantId);
            extraObject.put("tokenGroupName", mTokenGroupName);
            extraObject.put("ttl", mTtl);
            extraObject.put("name", mName);
            extraObject.put("email", mEmail);
            extraObject.put("idpType", mIdpType);
            extraObject.put("defaultTenant", mDefaultTenant);
            extraObject.put("defaultTenantUrl", mDefaultTenantUrl);
            JSONArray membershipsArray = new JSONArray();
            for (IMemberShip m : mMemberShips) {
                JSONObject membershipObject = new JSONObject();
                membershipObject.put("type", m.getType());
                membershipObject.put("id", m.getId());
                membershipObject.put("tokenGroupName", m.getTokenGroupName());

                if (m instanceof ProjectMemberShip) {
                    ProjectMemberShip pms = (ProjectMemberShip) m;
                    membershipObject.put("projectId", pms.getProjectId());
                } else if (m instanceof TenantMemberShip) {
                    TenantMemberShip tms = (TenantMemberShip) m;
                    membershipObject.put("tenantId", tms.getTenantId());
                }

                membershipsArray.put(membershipObject);
            }
            extraObject.put("memberships", membershipsArray);

            responseObj.put("extra", extraObject);
            return responseObj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static IRmUser buildFromJson(@NonNull String userRawJson) {
        RmUser ret = new RmUser();
        if (userRawJson.isEmpty()) {
            return ret;
        }
        try {
            JSONObject userObj = new JSONObject(userRawJson);
            JSONObject extraObj = userObj.optJSONObject("extra");
            if (extraObj == null) {
                return ret;
            }
            ret.mUserId = extraObj.optInt("userId");
            ret.mTicket = extraObj.optString("ticket");
            ret.mTenantId = extraObj.optString("tenantId");
            ret.mTokenGroupName = extraObj.optString("tokenGroupName");
            ret.mTtl = extraObj.optLong("ttl");
            ret.mName = extraObj.optString("name");
            ret.mEmail = extraObj.optString("email");
            ret.mIdpType = extraObj.optInt("idpType");
            ret.mDefaultTenant = extraObj.optString("defaultTenant");
            ret.mDefaultTenantUrl = extraObj.optString("defaultTenantUrl");
            ret.mMemberShips = buildMemberShip(extraObj.optJSONArray("memberships"));

            //Find current user' membership id.
            for (IMemberShip m : ret.mMemberShips) {
                if (ret.getTokenGroupName().equals(m.getTokenGroupName())) {
                    ret.mMembershipId = m.getId();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ret;
    }

    private static List<IMemberShip> buildMemberShip(JSONArray membershipsArr) {
        List<IMemberShip> ret = new ArrayList<>();
        if (membershipsArr == null || membershipsArr.length() == 0) {
            return ret;
        }
        for (int i = 0; i < membershipsArr.length(); i++) {
            JSONObject membershipObj = membershipsArr.optJSONObject(i);
            int type = membershipObj.optInt("type");
            String id = membershipObj.optString("id");
            String tokenGroupName = membershipObj.optString("tokenGroupName");

            if (type == 0) {
                String tenantId = membershipObj.optString("tenantId");
                TenantMemberShip t = new TenantMemberShip(id, type, tokenGroupName, tenantId);
                ret.add(t);
            } else if (type == 1) {
                int projectId = membershipObj.optInt("projectId");
                ProjectMemberShip p = new ProjectMemberShip(id, type, tokenGroupName, projectId);
                ret.add(p);
            } else if (type == 2) {
                SystemBucketMemberShip b = new SystemBucketMemberShip(id, type, tokenGroupName);
                ret.add(b);
            }
        }

        return ret;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mUserId);
        dest.writeString(mTicket);
        dest.writeString(mTenantId);
        dest.writeString(mTokenGroupName);
        dest.writeLong(mTtl);
        dest.writeString(mName);
        dest.writeString(mEmail);
        dest.writeInt(mIdpType);
        dest.writeTypedList(mMemberShips);
        dest.writeString(mDefaultTenant);
        dest.writeString(mDefaultTenantUrl);
        dest.writeInt(isTenantAdmin ? 1 : 0);
        dest.writeInt(isProjectAdmin ? 1 : 0);
    }
}
