package com.skydrm.rmc.ui.myspace.myvault.view.interactor;

import com.skydrm.sdk.INxlFileFingerPrint;

import java.util.List;

public interface IVaultFileShareContact {
    interface IView {
        void updateRightsAndValidity(List<String> rights, long start, long end);

        void updateRightsAndValidity(INxlFileFingerPrint fp);

        void setShareIndicator(boolean active);

        void setLoadingRightsView(boolean active);

        void updateRecipients(List<String> added, List<String> removed);

        void showShareSuccessView();

        void showReShareSuccessView(List<String> newSharedEmails, List<String> alreadySharedEmails);

        void showErrorView(Exception e);
    }

    interface IPresenter {
        void getMetadata();

        void getFingerPrint();

        void share(List<String> rights, List<String> emails, String cmt);

        void updateRecipients(List<String> added, List<String> removed, String cmt);

        void reShare(List<String> emails, String cmt);

        void onDestroy();
    }
}
