package com.skydrm.rmc.ui.widget.customcontrol;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by aning on 4/21/2017.
 * -- fix all bugs that "View not attached to window manager."
 */

public class SafeProgressDialog extends ProgressDialog {

    private Activity mParentActivity;

    public SafeProgressDialog(Context context) {
        super(context);
        mParentActivity = (Activity) context;
    }

    public static SafeProgressDialog showDialog(Context context,
                                                CharSequence title,
                                                CharSequence message,
                                                boolean indeterminate) {
        SafeProgressDialog dialog = new SafeProgressDialog(context);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setIndeterminate(indeterminate);
        dialog.setCancelable(false);
        dialog.setOnCancelListener(null);
        if ((Activity) context != null && !((Activity) context).isFinishing()) {
            dialog.show();
        }
        return dialog;
    }

    // for Convert Office & 3D progress dialog.
    public static SafeProgressDialog showDialog(Context context,
                                                CharSequence title,
                                                CharSequence message,
                                                int style,
                                                int resId,
                                                boolean cancel) {
        SafeProgressDialog dialog = new SafeProgressDialog(context);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setProgressStyle(style);
        dialog.setIcon(resId);
        dialog.setCanceledOnTouchOutside(cancel);
        if ((Activity) context != null && !((Activity) context).isFinishing() && !dialog.isShowing()) {
            dialog.show();
        }

        return dialog;
    }

    @Override
    public void dismiss() {
        if (mParentActivity != null && !mParentActivity.isFinishing()) {
            super.dismiss();
        }
    }
}
