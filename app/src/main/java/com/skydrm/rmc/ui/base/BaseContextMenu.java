package com.skydrm.rmc.ui.base;

import android.content.Context;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.sdk.rms.user.IRmUser;

public abstract class BaseContextMenu extends BottomSheetDialogFragment {
    protected Context mCtx;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCtx = context;
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        if (isAdded()) {
            return;
        }
        super.show(manager, tag);
    }

    @Override
    public void dismiss() {
        if (!isAdded()) {
            return;
        }
        super.dismiss();
    }

    protected boolean isVisible(View v) {
        if (v == null) {
            return false;
        }
        return v.getVisibility() == View.VISIBLE;
    }

    protected boolean isTenantAdmin() {
        IRmUser rmUser = null;
        try {
            rmUser = SkyDRMApp.getInstance().getSession().getRmUser();
        } catch (InvalidRMClientException e) {
            e.printStackTrace();
        }
        if (rmUser == null) {
            return false;
        }
        return rmUser.isTenantAdmin();
    }

}
