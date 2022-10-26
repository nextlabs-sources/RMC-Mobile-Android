package com.skydrm.rmc.ui.service.protect;

import android.content.Context;
import android.view.View;

import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.rms.rest.user.User;

import java.io.File;

public interface IViewBuilder {
    boolean isPreviewNeeded();

    boolean needInterceptBackPress();

    void interceptBackPress();

    View buildRoot(Context ctx);

    void showLoading(int type);

    void hideLoading(int type);

    void bindFingerPrint(INxlFileFingerPrint fp, File f, boolean nxl);

    void updateExpiry(User.IExpiry expiry);

    void updateExtractStatus(boolean checked);

    void updateParentPath(String parentPathId);

    void onAddFilePerformed();
}
