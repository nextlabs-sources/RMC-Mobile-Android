package com.skydrm.rmc.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;

import com.skydrm.rmc.ui.common.IDestroyable;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.rms.rest.user.User;

import java.io.File;

public interface IViewBuilder extends IDestroyable {
    View buildRoot(Context ctx);

    void showLoading(int type);

    void hideLoading(int type);

    void bindFingerPrint(INxlFileFingerPrint fingerPrint, File workingFile, boolean nxl);

    void configureShareOrProtectButton(Button button);

    void updateExpiry(User.IExpiry expiry);

    void wrapContacts(Intent data);
}
