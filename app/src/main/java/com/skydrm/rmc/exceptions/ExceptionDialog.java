package com.skydrm.rmc.exceptions;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.skydrm.rmc.R;
import com.skydrm.rmc.errorHandler.IErrorResult;


public class ExceptionDialog {
    static public void showUI(final Activity activity, String msg, boolean isOK, boolean isCancel, final boolean isActivityFinish, final IErrorResult callback) {
        if (!activity.isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(R.string.app_name)
                    .setMessage(msg);
            if (isOK) {
                builder.setPositiveButton(R.string.common_ok_uppercase, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (callback != null) {
                            callback.okHandler();
                        }
                        if (isActivityFinish) {
                            activity.finish();
                        }
                    }
                });
            }

            if (isCancel) {
                builder.setNegativeButton(R.string.common_cancel_initcap, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (callback != null) {
                            callback.cancelHandler();
                        }
                    }
                });
            }
            builder.setCancelable(false);
            builder.show();
        }
    }

    static public void showSimpleUI(Context context, String msg) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.app_name)
                .setMessage(msg)
                .setPositiveButton(R.string.common_ok_uppercase, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(true)
                .show();
    }
}
