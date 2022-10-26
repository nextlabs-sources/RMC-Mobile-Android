package com.skydrm.rmc.ui.fragment.protect;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;

import com.skydrm.rmc.R;
import com.skydrm.rmc.ui.fragment.IViewBuilder;
import com.skydrm.rmc.ui.widget.customcontrol.rights.ADHocRightsDisplayView;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.rms.rest.user.User;

import java.io.File;

public class NxlViewBuilder implements IViewBuilder {
    private ADHocRightsDisplayView mADHocRightsDisplayView;

    public NxlViewBuilder(Context ctx) {
        if (ctx != null) {
            ToastUtil.showToast(ctx, ctx.getString(R.string.hint_msg_already_a_protected_file));
        }
    }


    @Override
    public View buildRoot(Context ctx) {
        mADHocRightsDisplayView = new ADHocRightsDisplayView(ctx);
        return mADHocRightsDisplayView;
    }

    @Override
    public void showLoading(int type) {
        if (mADHocRightsDisplayView != null) {
            mADHocRightsDisplayView.showLoadingRightsLayout();
        }
    }

    @Override
    public void hideLoading(int type) {
        if (mADHocRightsDisplayView != null) {
            mADHocRightsDisplayView.hideLoadingRightsLayout();
        }
    }

    @Override
    public void bindFingerPrint(INxlFileFingerPrint fp, File workingFile, boolean nxl) {
        if (nxl) {
            if (fp != null) {
                mADHocRightsDisplayView.displayRights(fp);
                mADHocRightsDisplayView.showWatermark(fp.getDisplayWatermark());
                mADHocRightsDisplayView.showValidity(fp.formatString());
            }
        }
    }

    @Override
    public void configureShareOrProtectButton(Button button) {
        if (button != null) {
            button.setEnabled(false);
        }
    }

    @Override
    public void updateExpiry(User.IExpiry expiry) {

    }

    @Override
    public void wrapContacts(Intent data) {

    }

    @Override
    public void onReleaseResource() {

    }
}
