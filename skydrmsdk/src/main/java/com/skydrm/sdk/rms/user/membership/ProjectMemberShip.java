package com.skydrm.sdk.rms.user.membership;

import android.os.Parcel;

public class ProjectMemberShip extends BaseMemberShip {
    private int mProjectId;

    public ProjectMemberShip(String id, int type, String tokenGroupName, int projectId) {
        super(id, type, tokenGroupName);
        this.mProjectId = projectId;
    }

    ProjectMemberShip(Parcel in) {
        super(in);
        this.mProjectId = in.readInt();
    }

    public int getProjectId() {
        return mProjectId;
    }

    @Override
    public int describeContents() {
        return super.describeContents();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(mProjectId);
    }
}
