package com.skydrm.rmc.ui.project.feature.member.info;

import com.skydrm.rmc.datalayer.repo.project.IMember;
import com.skydrm.rmc.datalayer.repo.project.IMemberDetail;

public interface IMemberInfoContact {
    interface IView {
        void showInvitorName(IMemberDetail detail);

        void showLoadingIndicator(boolean active);

        void onActionSuccess(String msg, MemberActionTask.Type type, IMember target);

        void showErrorView(Exception e);
    }

    interface IPresenter {
        void getMemberDetail();

        void removeMember();

        void resendInvitation();

        void revokeInvitation();

        void onDestroy();
    }
}
