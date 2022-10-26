package com.skydrm.rmc.ui.widget;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.skydrm.rmc.R;

public class LoadingDialog2 {
    private AlertDialog mAlertDialog;

    private LoadingDialog2() {

    }

    public static LoadingDialog2 newInstance() {
        return new LoadingDialog2();
    }

    public void showModalDialog(Context ctx) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setCancelable(false);
        mAlertDialog = builder.create();
        View root = View.inflate(ctx, R.layout.layout_loading, null);
        mAlertDialog.setView(root);
        mAlertDialog.show();
    }

    public void dismissDialog() {
        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
            mAlertDialog = null;
        }
    }

}
