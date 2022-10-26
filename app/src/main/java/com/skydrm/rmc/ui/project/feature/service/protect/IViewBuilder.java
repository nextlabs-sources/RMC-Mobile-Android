package com.skydrm.rmc.ui.project.feature.service.protect;

import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.skydrm.rmc.ui.common.IDestroyable;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.rms.rest.user.User;

import java.io.File;


public interface IViewBuilder extends IDestroyable {
    boolean isPreviewNeeded();

    View buildRoot(Context ctx);

    void showLoading(int type);

    void hideLoading(int type);

    void bindFingerPrint(INxlFileFingerPrint fp, File f, boolean nxl);

    void configButton(Button button);

    void configNavigator(Toolbar toolbar);

    void updateExpiry(User.IExpiry expiry);

    void updateExtractStatus(boolean checked);

    void updateParentPath(String parentPathId);

    boolean needInterceptBackPress();

    void interceptBackPress();
}
