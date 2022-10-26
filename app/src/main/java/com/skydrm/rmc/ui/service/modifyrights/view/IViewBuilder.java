package com.skydrm.rmc.ui.service.modifyrights.view;

import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.skydrm.rmc.ui.common.IDestroyable;
import com.skydrm.sdk.INxlFileFingerPrint;

public interface IViewBuilder extends IDestroyable {
    void configureToolbar(Toolbar toolbar);

    View buildRoot(Context ctx);

    void bindFingerPrint(INxlFileFingerPrint fp);

    void configureOperateButton(Button button);

    void showLoading();

    void hideLoading();

    boolean needInterceptBackPress();

    void interceptBackPress();
}
