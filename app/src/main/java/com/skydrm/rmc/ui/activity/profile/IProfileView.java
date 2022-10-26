package com.skydrm.rmc.ui.activity.profile;

import android.app.Activity;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by hhu on 5/8/2017.
 */

public interface IProfileView {
    void onUserLogout(Activity activity);

    void showUserDetailView(FrameLayout container);

    void showChangePasswordView(View root);

    void showCleanCacheView();

    void showLicenceView();

    void loadHelpView();

    void showContactView();

    boolean handleBackKey();
}
