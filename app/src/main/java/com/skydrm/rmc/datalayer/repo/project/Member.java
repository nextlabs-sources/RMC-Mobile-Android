package com.skydrm.rmc.datalayer.repo.project;


import android.os.Parcel;
import android.os.Parcelable;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.dbbridge.IDBProjectItem;
import com.skydrm.rmc.dbbridge.project.DBProjectItem;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.utils.sort.IMemberSortable;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.rest.project.member.ListMemberResult;
import com.skydrm.sdk.rms.rest.project.member.MemberDetailResult;
import com.skydrm.sdk.rms.rest.project.member.PendingInvitationResult;

import java.util.Objects;

public class Member implements IMember, IMemberDetail, IPendingMember, IMemberSortable, Parcelable {
    int _id = -1;
    private int mUserId;
    private String mDisplayName;
    private String mEmail;
    private long mCreationTime;

    private String mInviterDisplayName;
    private String mInviterEmail;

    private int mInvitationId;
    private String mInviteeEmail;
    private long mInviteTime;

    private boolean isPending;
    private boolean isOwner;

    private IDBProjectItem.IMember mDBItem;

    private Member(String inviterDisplayName, String inviterEmail,
                   int invitationId, String inviteeEmail, long inviteTime) {
        this.mInviterDisplayName = inviterDisplayName;
        this.mInviterEmail = inviterEmail;
        this.mInvitationId = invitationId;
        this.mInviteeEmail = inviteeEmail;
        this.mInviteTime = inviteTime;
        this.isOwner = false;
        this.isPending = true;
    }

    private Member(int userId, String displayName, String email, long creationTime, boolean isOwner) {
        this.mUserId = userId;
        this.mDisplayName = displayName;
        this.mEmail = email;
        this.mCreationTime = creationTime;
        this.isOwner = isOwner;
        this.isPending = false;
    }

    private Member(IDBProjectItem.IMember m, boolean isOwner) {
        this.mDBItem = m;

        this._id = m.getProjectMemberTBPK();
        this.mUserId = m.getUserId();
        this.mDisplayName = m.getDisplayName();
        this.mEmail = m.getEmail();
        this.mCreationTime = m.getCreationTime();

        this.isOwner = isOwner;
        this.isPending = false;
    }


    protected Member(Parcel in) {
        _id = in.readInt();
        mUserId = in.readInt();
        mDisplayName = in.readString();
        mEmail = in.readString();
        mCreationTime = in.readLong();
        mInviterDisplayName = in.readString();
        mInviterEmail = in.readString();
        mInvitationId = in.readInt();
        mInviteeEmail = in.readString();
        mInviteTime = in.readLong();
        isPending = in.readByte() != 0;
        isOwner = in.readByte() != 0;

        mDBItem = in.readParcelable(DBProjectItem.Member.class.getClassLoader());
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
    public int getUserId() {
        return mUserId;
    }

    @Override
    public String getDisplayName() {
        return mDisplayName;
    }

    @Override
    public String getEmail() {
        return mEmail;
    }

    @Override
    public long getCreationTime() {
        return mCreationTime;
    }

    @Override
    public boolean isPending() {
        return isPending;
    }

    @Override
    public boolean isOwner() {
        return isOwner;
    }

    @Override
    public int getInvitationId() {
        return mInvitationId;
    }

    @Override
    public String getInviteeEmail() {
        return mInviteeEmail;
    }

    @Override
    public String getInviterDisplayName() {
        return mInviterDisplayName;
    }

    @Override
    public String getInviterEmail() {
        return mInviterEmail;
    }

    @Override
    public long getInviteTime() {
        return mInviteTime;
    }

    @Override
    public String resendInvitation() throws SessionInvalidException,
            InvalidRMClientException, RmsRestAPIException {
        if (!isPending) {
            return "";
        }
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        String response = session.getRmsRestAPI()
                .getProjectService(session.getRmUser())
                .resendInvitation(mInvitationId);
        if (response == null || response.isEmpty()) {
            return "";
        }
        return "Invitation has been resend.";
    }

    @Override
    public String revokeInvitation() throws SessionInvalidException,
            InvalidRMClientException, RmsRestAPIException {
        if (!isPending) {
            return "";
        }
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        String response = session.getRmsRestAPI()
                .getProjectService(session.getRmUser())
                .revokeInvitation(mInvitationId);
        if (response == null || response.isEmpty()) {
            return "";
        }
        return "Member has been revoked.";
    }

    @Override
    public String remove() throws RmsRestAPIException,
            SessionInvalidException, InvalidRMClientException {
        int projectId = getProjectId();
        if (projectId == -1) {
            return "";
        }
        SkyDRMApp app = SkyDRMApp.getInstance();

        SkyDRMApp.Session2 session = app.getSession();
        String response = session.getRmsRestAPI()
                .getProjectService(session.getRmUser())
                .removeMember(projectId, mUserId);
        app.getDBProvider().deleteProjectMemberItem(_id);

        if (response == null || response.isEmpty()) {
            return "";
        }
        return "Member has been removed.";
    }

    @Override
    public IMemberDetail getDetail() throws SessionInvalidException,
            InvalidRMClientException, RmsRestAPIException {
        int projectId = getProjectId();
        if (projectId == -1) {
            return null;
        }
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        MemberDetailResult memberDetail = session.getRmsRestAPI()
                .getProjectService(session.getRmUser())
                .getMemberDetail(projectId, mUserId);
        if (memberDetail != null) {
            MemberDetailResult.ResultsBean results = memberDetail.getResults();
            if (results != null) {
                MemberDetailResult.ResultsBean.DetailBean detail = results.getDetail();
                this.mInviterDisplayName = detail.getInviterDisplayName();
                this.mInviterEmail = detail.getInviterEmail();
                this.mUserId = detail.getUserId();
                this.mDisplayName = detail.getDisplayName();
                this.mEmail = detail.getEmail();
                this.mCreationTime = detail.getCreationTime();
            }
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return mUserId == member.mUserId &&
                mCreationTime == member.mCreationTime &&
                Objects.equals(mDisplayName, member.mDisplayName) &&
                Objects.equals(mEmail, member.mEmail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mUserId, mDisplayName, mEmail, mCreationTime);
    }

    @Override
    public String getSortableName() {
        if (isPending) {
            return mInviteeEmail;
        }
        return mDisplayName;
    }

    @Override
    public long getSortableSize() {
        return 0;
    }

    @Override
    public long getSortableTime() {
        if (isPending) {
            return mInviteTime;
        }
        return mCreationTime;
    }

    @Override
    public boolean isFolder() {
        return false;
    }

    @Override
    public String getMemberType() {
        if (isPending) {
            return TITLE_PENDING;
        }
        return TITLE_ACTIVE;
    }

    private int getProjectId() {
        int ret = -1;
        if (mDBItem == null) {
            return ret;
        }

        int _project_id = mDBItem.getProjectTBPK();
        if (_project_id == -1) {
            return ret;
        }

        IDBProjectItem projectItem = SkyDRMApp.getInstance()
                .getDBProvider()
                .queryProjectItem(_project_id);
        ret = projectItem.getId();
        return ret;
    }

    static Member newByDBItem(IDBProjectItem.IMember i, boolean isOwner) {
        return new Member(i, isOwner);
    }

    static Member newByRemoteItem(ListMemberResult.ResultsBean.DetailBean.MembersBean i, boolean isOwner) {
        return new Member(i.getUserId(), i.getDisplayName(),
                i.getEmail(), i.getCreationTime(), isOwner);
    }

    static Member newByPendingItem(PendingInvitationResult.ResultsBean.PendingListBean.InvitationsBean i) {
        return new Member(i.getInviterDisplayName(), i.getInviterEmail(), i.getInvitationId(),
                i.getInviteeEmail(), i.getInviteTime());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(_id);
        dest.writeInt(mUserId);
        dest.writeString(mDisplayName);
        dest.writeString(mEmail);
        dest.writeLong(mCreationTime);
        dest.writeString(mInviterDisplayName);
        dest.writeString(mInviterEmail);
        dest.writeInt(mInvitationId);
        dest.writeString(mInviteeEmail);
        dest.writeLong(mInviteTime);
        dest.writeByte((byte) (isPending ? 1 : 0));
        dest.writeByte((byte) (isOwner ? 1 : 0));
        dest.writeParcelable((DBProjectItem.Member) mDBItem, flags);
    }
}