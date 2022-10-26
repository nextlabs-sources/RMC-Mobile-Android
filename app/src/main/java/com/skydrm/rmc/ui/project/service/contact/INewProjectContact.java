package com.skydrm.rmc.ui.project.service.contact;

import com.skydrm.rmc.datalayer.repo.project.IProject;

import java.util.List;

public interface INewProjectContact {
    interface IView {
        void onCreatingProject();

        void onCreateSucceed(IProject project);

        void onCreateFailed(Exception e);
    }

    interface IPresenter {
        void newProject(String name, String description,
                        List<String> emails, String invitationMsg);

        void onDestroy();
    }
}
