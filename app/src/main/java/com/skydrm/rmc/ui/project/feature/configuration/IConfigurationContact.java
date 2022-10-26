package com.skydrm.rmc.ui.project.feature.configuration;

public interface IConfigurationContact {
    interface IView {
        void updateMetadata(String invitationMsg);

        void showLoadingIndicator(boolean active);

        void showSuccessView(String msg);

        void showErrorView(Exception e);
    }

    interface IPresenter {
        void getMetadata();

        void updateProject(String name, String description, String invitationMsg);

        void onDestroy();
    }
}
