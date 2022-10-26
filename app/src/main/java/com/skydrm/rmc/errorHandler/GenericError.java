package com.skydrm.rmc.errorHandler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.sdk.exception.RmsRestAPIException;

public class GenericError {
    static public void showUI(final Activity activity,
                              String msg,
                              boolean isOK,
                              boolean isCancel,
                              final boolean isActivityFinish,
                              final IErrorResult callback) {
        if (activity != null && !activity.isFinishing()) {
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

    /**
     * Used to handle common exception domain for {@link com.skydrm.sdk.exception.RmsRestAPIException}
     *
     * @param context
     * @param bInvokedInMainThread this method if be invoked in myspace thread
     * @param rmsRestAPIException  request rms API exception
     */
    static public void handleCommonException(final Context context,
                                             final boolean bInvokedInMainThread,
                                             final RmsRestAPIException rmsRestAPIException) {
        if (rmsRestAPIException == null) {
            return;
        }
        if (context == null || !(context instanceof Activity) || ((Activity) context).isFinishing()) {
            return;
        }

        if (bInvokedInMainThread) {
            switch (rmsRestAPIException.getDomain()) {
                case AuthenticationFailed:
                    SkyDRMApp.getInstance().getSession().sessionExpiredHandler(context);
                    break;
                case NetWorkIOFailed:
                    ToastUtil.showToast(context, context.getResources().getString(R.string.excep_network_unavailable));
                    break;
                case AccessDenied:
                    ToastUtil.showToast(context, context.getResources().getString(R.string.access_denied));
                    break;
                case MalformedRequest:
                    ToastUtil.showToast(context, context.getResources().getString(R.string.invalid_tenant));
                    break;
                case FILE_EXPIRED:
                    ToastUtil.showToast(context.getApplicationContext(), rmsRestAPIException.getMessage());
                    break;
                default:
                    ToastUtil.showToast(context, context.getResources().getString(R.string.excep_operation_failed));
                    break;
            }
        } else {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (rmsRestAPIException.getDomain()) {
                        case AuthenticationFailed:
                            SkyDRMApp.getInstance().getSession().sessionExpiredHandler(context);
                            break;
                        case NetWorkIOFailed:
                            ToastUtil.showToast(context, context.getResources().getString(R.string.excep_network_unavailable));
                            break;
                        case AccessDenied:
                            ToastUtil.showToast(context, context.getResources().getString(R.string.access_denied));
                            break;
                        case MalformedRequest:
                            ToastUtil.showToast(context, context.getResources().getString(R.string.invalid_tenant));
                            break;
                        default:
                            ToastUtil.showToast(context, context.getResources().getString(R.string.excep_operation_failed));
                            break;
                    }
                }
            });
        }
    }
}
