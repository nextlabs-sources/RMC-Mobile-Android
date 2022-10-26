package com.skydrm.rmc.dbbridge.project;

import android.os.Parcel;
import android.os.Parcelable;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.database.table.project.ProjectBean;
import com.skydrm.rmc.database.table.project.ProjectMemberBean;
import com.skydrm.rmc.dbbridge.IDBProjectItem;
import com.skydrm.rmc.dbbridge.IOwner;
import com.skydrm.rmc.dbbridge.base.Owner;

import java.util.ArrayList;
import java.util.List;

public class DBProjectItem implements IDBProjectItem {
    private ProjectBean mRaw;

    public DBProjectItem(ProjectBean raw) {
        this.mRaw = raw;
    }

    private DBProjectItem(Parcel in) {
        this.mRaw = in.readParcelable(ProjectBean.class.getClassLoader());
    }

    public static final Creator<DBProjectItem> CREATOR = new Creator<DBProjectItem>() {
        @Override
        public DBProjectItem createFromParcel(Parcel in) {
            return new DBProjectItem(in);
        }

        @Override
        public DBProjectItem[] newArray(int size) {
            return new DBProjectItem[size];
        }
    };

    @Override
    public int getProjectTBPK() {
        return mRaw._id;
    }

    @Override
    public int getUserTBPK() {
        return mRaw._user_id;
    }

    @Override
    public int getId() {
        return mRaw.id;
    }

    @Override
    public String getParentTenantId() {
        return mRaw.parentTenantId;
    }

    @Override
    public String getParentTenantName() {
        return mRaw.parentTenantName;
    }

    @Override
    public String getTokenGroupName() {
        return mRaw.tokenGroupName;
    }

    @Override
    public String getName() {
        if (mRaw == null) {
            return "";
        }
        return mRaw.name;
    }

    @Override
    public String getDescription() {
        return mRaw.description;
    }

    @Override
    public String getDisplayName() {
        return mRaw.displayName;
    }

    @Override
    public long getCreationTime() {
        return mRaw.creationTime;
    }

    @Override
    public long getConfigurationModified() {
        return mRaw.configurationModified;
    }

    @Override
    public int getTotalMembers() {
        return mRaw.totalMembers;
    }

    @Override
    public int getTotalFiles() {
        return mRaw.totalFiles;
    }

    @Override
    public boolean isOwnedByMe() {
        return mRaw.isOwnedByMe;
    }

    @Override
    public IOwner getOwner() {
        return Owner.newByJson(mRaw.ownerRawJson);
    }

    @Override
    public String getAccountType() {
        return mRaw.accountType;
    }

    @Override
    public long getTrialEndTime() {
        return mRaw.trialEndTime;
    }

    @Override
    public String getExpiry() {
        return mRaw.expiry;
    }

    @Override
    public String getWatermark() {
        return mRaw.watermark;
    }

    @Override
    public String getClassificationRaw() {
        return mRaw.classification;
    }

    @Override
    public List<IMember> getMember() {
        List<IMember> ret = new ArrayList<>();
        List<ProjectMemberBean> members = mRaw.members;
        if (members == null || members.size() == 0) {
            return ret;
        }
        for (ProjectMemberBean m : members) {
            ret.add(new Member(m));
        }
        return ret;
    }

    @Override
    public long getUsage() {
        return SkyDRMApp.getInstance()
                .getDBProvider()
                .queryProjectItemUsage(mRaw._id);
    }

    @Override
    public long getQuota() {
        return SkyDRMApp.getInstance()
                .getDBProvider()
                .queryProjectItemQuota(mRaw._id);
    }

    @Override
    public long getLastRefreshMillis() {
        return SkyDRMApp.getInstance().
                getDBProvider()
                .queryProjectItemLastRefreshMillis(mRaw._id);
    }

    @Override
    public void setClassification(String raw) {
        SkyDRMApp.getInstance()
                .getDBProvider()
                .updateProjectItemClassification(mRaw._id, raw);
        mRaw.classification = raw;
    }

    @Override
    public void updateAccessCount() {
        SkyDRMApp.getInstance()
                .getDBProvider()
                .updateProjectItemAccessCount(mRaw._id);
    }

    @Override
    public void updateTrialEndTime(long time) {
        SkyDRMApp.getInstance()
                .getDBProvider()
                .updateProjectItemTrialEndTime(mRaw._id, time);
    }

    @Override
    public void updateLastAccessTime(long time) {
        SkyDRMApp.getInstance()
                .getDBProvider()
                .updateProjectItemLastAccessTime(mRaw._id, time);
    }

    @Override
    public void updateLastRefreshTime() {
        SkyDRMApp.getInstance()
                .getDBProvider()
                .updateProjectItemLastRefreshMillis(mRaw._id);
    }

    @Override
    public void increaseTotalCount() {
        mRaw.totalFiles++;
        SkyDRMApp.getInstance()
                .getDBProvider()
                .updateProjectItemTotalFiles(mRaw._id, mRaw.totalFiles);
    }

    @Override
    public long getLastAccessTime() {
        return mRaw.lastAccessTime;
    }

    @Override
    public void delete() {
        // delete project item exists in db.
        // all project files will be deleted on cascade.
        SkyDRMApp.getInstance()
                .getDBProvider()
                .deleteProjectItem(mRaw._id);
        // reset db item in mem.
        mRaw = null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mRaw, flags);
    }

    public static class Member implements IMember, Parcelable {
        ProjectMemberBean mMemberRaw;

        public Member(ProjectMemberBean raw) {
            this.mMemberRaw = raw;
        }

        Member(Parcel in) {
            mMemberRaw = in.readParcelable(ProjectMemberBean.class.getClassLoader());
        }

        public static final Creator<Member> CREATOR = new Creator<Member>() {
            @Override
            public Member createFromParcel(Parcel in) {
                return new Member(in);
            }

            @Override
            public Member[] newArray(int size) {
                return new Member[size];
            }
        };

        @Override
        public int getProjectMemberTBPK() {
            return mMemberRaw._id;
        }

        @Override
        public int getProjectTBPK() {
            return mMemberRaw._project_id;
        }

        @Override
        public int getUserId() {
            return mMemberRaw.userId;
        }

        @Override
        public String getDisplayName() {
            return mMemberRaw.displayName;
        }

        @Override
        public String getEmail() {
            return mMemberRaw.email;
        }

        @Override
        public long getCreationTime() {
            return mMemberRaw.creationTime;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(mMemberRaw, flags);
        }
    }
}
