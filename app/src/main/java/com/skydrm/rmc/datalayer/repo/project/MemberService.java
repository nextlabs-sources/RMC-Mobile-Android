package com.skydrm.rmc.datalayer.repo.project;

import android.os.Parcel;
import android.os.Parcelable;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.dbbridge.DBProvider;
import com.skydrm.rmc.dbbridge.IDBProjectItem;
import com.skydrm.rmc.dbbridge.IOwner;
import com.skydrm.rmc.dbbridge.base.Owner;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.rest.project.member.ListMemberParam;
import com.skydrm.sdk.rms.rest.project.member.ListMemberResult;
import com.skydrm.sdk.rms.rest.project.member.PendingInvitationResult;
import com.skydrm.sdk.rms.rest.project.member.ProjectInvitationResult;
import com.skydrm.sdk.rms.rest.project.member.ProjectPendingInvitationsParas;

import java.util.ArrayList;
import java.util.List;

class MemberService implements Parcelable {
    private int mId;
    private int _project_id;
    private IOwner mOwner;

    MemberService(int id, int _id, IOwner owner) {
        this.mId = id;
        this._project_id = _id;
        this.mOwner = owner;
    }

    private MemberService(Parcel in) {
        mId = in.readInt();
        _project_id = in.readInt();
        mOwner = in.readParcelable(Owner.class.getClassLoader());
    }

    public static final Creator<MemberService> CREATOR = new Creator<MemberService>() {
        @Override
        public MemberService createFromParcel(Parcel in) {
            return new MemberService(in);
        }

        @Override
        public MemberService[] newArray(int size) {
            return new MemberService[size];
        }
    };

    String invite(List<String> emails, String invitationMsg)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        if (emails == null || emails.size() == 0) {
            return "";
        }

        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        ProjectInvitationResult result = session.getRmsRestAPI()
                .getProjectService(session.getRmUser())
                .inviteMember(mId, emails, invitationMsg);

        if (result == null) {
            return "";
        }
        ProjectInvitationResult.ResultsBean results = result.getResults();
        if (results == null) {
            return "";
        }

        List<String> nowInvited = results.getNowInvited();
        if (nowInvited != null && nowInvited.size() != 0) {
            List<IMember> remotes = syncInternal();
            List<IMember> locals = newByDBMember(listInternal());
            filterOutModifiedItems(remotes, locals);

            List<String> alreadyMembers = results.getAlreadyMembers();
            StringBuilder alreadyInviteHintMessage = new StringBuilder();
            for (int i = 0; i < nowInvited.size(); i++) {
                alreadyInviteHintMessage.append(alreadyMembers.get(i));
                if (i != nowInvited.size() - 1) {
                    alreadyInviteHintMessage.append(",");
                }
            }
            String s1 = alreadyInviteHintMessage.toString();
            String sendMessage = "";
            if (!s1.isEmpty()) {
                sendMessage = sendMessage
                        .concat(s1)
                        .concat(nowInvited.size() > 0 ? "have" : "has")
                        .concat("been invited.")
                        .concat("\n");
            }
            return sendMessage;
        }

        List<String> alreadyMembers = results.getAlreadyMembers();
        StringBuilder alreadyMemberHintMessage = new StringBuilder();
        if (alreadyMembers != null && alreadyMembers.size() != 0) {
            for (int i = 0; i < alreadyMembers.size(); i++) {
                alreadyMemberHintMessage.append(alreadyMembers.get(i));
                if (i != alreadyMembers.size() - 1) {
                    alreadyMemberHintMessage.append(",");
                }
            }
        }

        List<String> alreadyInvited = results.getAlreadyInvited();
        StringBuilder alreadyInviteHintMessage = new StringBuilder();
        if (alreadyInvited != null && alreadyInvited.size() != 0) {
            for (int i = 0; i < alreadyInvited.size(); i++) {
                alreadyInviteHintMessage.append(alreadyInvited.get(i));
                if (i != alreadyInvited.size() - 1) {
                    alreadyInviteHintMessage.append(",");
                }
            }
        }
        String s1 = alreadyMemberHintMessage.toString();
        String sendMessage = "";
        if (!s1.isEmpty()) {
            sendMessage = sendMessage
                    .concat("Operation target is already member:")
                    .concat(s1)
                    .concat("\n");
        }
        String s2 = alreadyInviteHintMessage.toString();
        if (!s2.isEmpty()) {
            sendMessage = sendMessage
                    .concat("And the following list is already invited:")
                    .concat(s2);
        }
        return sendMessage;
    }

    List<IMember> listMember() {
        return newByDBMember(listInternal());
    }

    List<IMember> syncMember()
            throws RmsRestAPIException, SessionInvalidException, InvalidRMClientException {
        List<IMember> remotes = syncInternal();
        List<IMember> locals = newByDBMember(listInternal());
        if (filterOutModifiedItems(remotes, locals)) {
            return listMember();
        }
        return locals;
    }

    List<IMember> syncPendingMember()
            throws RmsRestAPIException, SessionInvalidException, InvalidRMClientException {
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        PendingInvitationResult result = session.getRmsRestAPI()
                .getProjectService(session.getRmUser())
                .listPendingInvitations(mId, new ProjectPendingInvitationsParas());

        List<IMember> ret = new ArrayList<>();
        if (result == null) {
            return ret;
        }
        PendingInvitationResult.ResultsBean results = result.getResults();
        if (results == null) {
            return ret;
        }
        PendingInvitationResult.ResultsBean.PendingListBean pendingList = results.getPendingList();
        if (pendingList == null) {
            return ret;
        }
        List<PendingInvitationResult.ResultsBean.PendingListBean.InvitationsBean> invitations = pendingList.getInvitations();
        if (invitations == null || invitations.size() == 0) {
            return ret;
        }
        for (PendingInvitationResult.ResultsBean.PendingListBean.InvitationsBean i : invitations) {
            ret.add(Member.newByPendingItem(i));
        }
        return ret;
    }

    private boolean filterOutModifiedItems(List<IMember> remotes, List<IMember> locals) {
        DBProvider dbProvider = SkyDRMApp.getInstance().getDBProvider();
        if (remotes == null || remotes.size() == 0) {
            if (locals == null || locals.size() == 0) {
                return false;
            }
            return batchDelete(locals, dbProvider);
        }
        int upsertAffected = 0;
        for (IMember r : remotes) {
            if (r == null) {
                continue;
            }
            if (locals.contains(r)) {
                continue;
            }
            dbProvider.upsertProjectMemberItem(_project_id, r.getUserId(), r.getDisplayName(),
                    r.getEmail(), r.getCreationTime());
            upsertAffected++;
        }
        List<Integer> _ids = new ArrayList<>();
        for (IMember l : locals) {
            if (l == null) {
                continue;
            }
            if (remotes.contains(l)) {
                continue;
            }
            _ids.add(((Member) l)._id);
        }
        return dbProvider.batchDeleteProjectMemberItem(_ids) || upsertAffected > 0;
    }

    private boolean batchDelete(List<IMember> locals, DBProvider dbProvider) {
        List<Integer> _ids = new ArrayList<>();
        for (IMember m : locals) {
            if (m == null) {
                continue;
            }
            _ids.add(((Member) m)._id);
        }
        return dbProvider.batchDeleteProjectMemberItem(_ids);
    }

    private List<IDBProjectItem.IMember> listInternal() {
        return SkyDRMApp.getInstance()
                .getDBProvider()
                .queryAllProjectMember(_project_id);
    }

    private List<IMember> syncInternal() throws SessionInvalidException,
            InvalidRMClientException, RmsRestAPIException {
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        List<IMember> ret = new ArrayList<>();

        ListMemberResult result = session.getRmsRestAPI()
                .getProjectService(session.getRmUser())
                .listMember(mId, new ListMemberParam());

        if (result == null) {
            return ret;
        }
        ListMemberResult.ResultsBean results = result.getResults();
        if (results == null) {
            return ret;
        }
        List<ListMemberResult.ResultsBean.DetailBean.MembersBean> members = results.getDetail().getMembers();
        if (members == null || members.size() == 0) {
            return ret;
        }
        for (ListMemberResult.ResultsBean.DetailBean.MembersBean m : members) {
            ret.add(Member.newByRemoteItem(m, m.getUserId() == mOwner.getUserId()));
        }
        return ret;
    }

    private List<IMember> newByDBMember(List<IDBProjectItem.IMember> members) {
        List<IMember> ret = new ArrayList<>();
        if (members == null || members.size() == 0) {
            return ret;
        }
        for (IDBProjectItem.IMember m : members) {
            ret.add(Member.newByDBItem(m, m.getUserId() == mOwner.getUserId()));
        }
        return ret;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeInt(_project_id);
        dest.writeParcelable((Owner) mOwner, flags);
    }
}
