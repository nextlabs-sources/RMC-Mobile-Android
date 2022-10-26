package com.skydrm.rmc.ui.myspace.myvault.view.interactor;

import android.content.Context;

import com.skydrm.sdk.rms.rest.myVault.MyVaultMetaDataResult;

public interface IVaultFileInfoContact {
    interface IView {
        void setLoadingIndicator(boolean active);

        void updateMetadata(MyVaultMetaDataResult result);

        void showErrorView(Exception e);

        void onRevokeRights();
    }

    interface IPresenter {
        void getMetadata();

        void copyLink(Context ctx, String text);

        void revokeRights();

        void onDestroy();
    }
}
