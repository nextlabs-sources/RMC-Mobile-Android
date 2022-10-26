package com.skydrm.rmc.ui.project.service;

import android.view.View;

import com.skydrm.rmc.datalayer.repo.project.IInvitePending;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.utils.sort.SortType;

import java.util.List;

public interface IProjectContact {
    interface IView {
        void onInitialize(boolean active);

        void showLoadingIndicator(boolean active);

        void showCreatedByMeProjects(List<IProject> createdByMe);

        void showInvitedByOtherProjects(List<IProject> invitedByOther);

        void showErrorView(Exception e);
    }

    interface IPresenter {
        void initialize();

        void refresh();

        void sort(SortType sortType);

        void updateSortType(SortType sortType);

        /**
         * @param type 0 = all,1 = createdByMe,2 = invitedByOther.
         */
        void getProjects(int type);

        /**
         * @param type 0 = all,1 = createdByMe,2 = invitedByOther.
         */
        void getProjectsAndResetOperationStatus(int type);

        void getPendingInvitation();

        void acceptInvitation(IInvitePending project, View loadingBar);

        void denyInvitation(IInvitePending project, String reason, View loadingBar);

        void onDestroy();
    }
}
