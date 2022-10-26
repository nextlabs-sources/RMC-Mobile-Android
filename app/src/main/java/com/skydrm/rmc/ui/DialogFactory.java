package com.skydrm.rmc.ui;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.skydrm.rmc.DevLog;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by hhu on 12/22/2016.
 */

@Deprecated
public class DialogFactory {
    public static final String TAG = "DialogFactory";
    private static final String DEFAULT_DIALOG_TITLE = "SkyDRM";
    private static final String DEFAULT_POSITIVE_BUTTON = "Ok";
    private static final String DEFAULT_NEGATIVE_BUTTON = "Cancel";
    private static volatile DialogFactory mInstance;
    private ProgressDialog uploadDialog;
    private DevLog log = new DevLog(DialogFactory.class.getSimpleName());

    private IDialogCanceler mIDialogCanceler;

    private DialogFactory() {
    }

    public static DialogFactory getInstance() {
        if (mInstance == null) {
            synchronized (DialogFactory.class) {
                if (mInstance == null) {
                    mInstance = new DialogFactory();
                }
            }
        }
        return mInstance;
    }

    public void createProgressDialog(@NonNull Context context, int style, String dialogTitle, String dialogMessage, int max, TYPE type) {
        switch (type) {
            case UPLOAD:
                createDialog(context, style, dialogTitle, dialogMessage, max);
                break;
            case DOWNLOAD:

                break;
        }
    }

    public void createDialog(@NonNull Context context, int style, String dialogTitle, String dialogMessage, int max) {
        uploadDialog = new ProgressDialog(context);
        uploadDialog.setProgressStyle(style);
        uploadDialog.setCanceledOnTouchOutside(false);
        if (!TextUtils.isEmpty(dialogTitle)) {
            uploadDialog.setTitle(dialogTitle);
        } else {
            uploadDialog.setTitle(DEFAULT_DIALOG_TITLE);
        }
        uploadDialog.setMessage(dialogMessage);
        uploadDialog.setMax(max);
        uploadDialog.show();
    }

    /**
     * This method is used to create single choice dialog
     *
     * @param context         Context Dialog create needed
     * @param itemLists       Single choice item lists
     * @param defaultItem     the default item selet of the single choice list
     * @param onClickListener callback of item click and positive button and negative button click
     */
    public void createSingleChoiceDialog(@NonNull Context context, String[] itemLists, int defaultItem, DialogInterface.OnClickListener onClickListener) {
        final AlertDialog.Builder singleChoiceBuilder = new AlertDialog.Builder(context);
        singleChoiceBuilder.setTitle(DEFAULT_DIALOG_TITLE);
        singleChoiceBuilder.setSingleChoiceItems(itemLists, defaultItem, onClickListener);
        singleChoiceBuilder.setPositiveButton(DEFAULT_POSITIVE_BUTTON, onClickListener);
        singleChoiceBuilder.setNegativeButton(DEFAULT_NEGATIVE_BUTTON, onClickListener);
        singleChoiceBuilder.show();
    }

    /**
     * This method is uesed to create select dialog before user delete a file
     *
     * @param context         the context Dialog create needed
     * @param dialogTitle     the dialog title user input
     * @param msg             the message show in the dialog
     * @param positiveBtName  the positive button name
     * @param negativeBtName  the negative button name
     * @param onClickListener callback of click positive button and negative button
     */
    @Deprecated
    public void createDeleteDialog(@NonNull Context context, String dialogTitle, String msg,
                                   String positiveBtName, String negativeBtName,
                                   final OnDialogClickListener onClickListener) {
        AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(context);
        deleteBuilder.setTitle(TextUtils.isEmpty(dialogTitle) ? DEFAULT_DIALOG_TITLE : dialogTitle)
                .setMessage(msg)
                .setPositiveButton(TextUtils.isEmpty(positiveBtName) ? DEFAULT_POSITIVE_BUTTON :
                        positiveBtName, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onClickListener.onPositiveClick(dialog, which);
                    }
                })
                .setNegativeButton(TextUtils.isEmpty(negativeBtName) ? DEFAULT_NEGATIVE_BUTTON :
                        negativeBtName, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onClickListener.onNegativeClick(dialog, which);
                    }
                })
                .setCancelable(false)
                .show();
    }

    public void hintUserSetNetWork(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        AlertDialog mWifiDialog = builder.setTitle("Network setting")
                .setMessage("No network").setPositiveButton("Setting", new DialogInterface
                        .OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings
                                .ACTION_WIFI_SETTINGS);
                        if ((context instanceof Application)) {
                            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                        }
                        context.startActivity(intent);
                    }
                }).setNegativeButton("Cancel", null).create();
        mWifiDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        mWifiDialog.show();
    }

    public void setDialogProgress(int progress) {
        if (uploadDialog == null) {
            return;
        }
        uploadDialog.setProgress(progress);
    }

    public void dismissDialog() {
        if (uploadDialog == null) {
            return;
        }

        uploadDialog.dismiss();
        uploadDialog = null;
    }

    public void cancelDialog(IDialogCanceler iDialogCanceler) {
        mIDialogCanceler = iDialogCanceler;
        if (uploadDialog != null) {
            uploadDialog.setOnKeyListener(new OnKeyListener());
        } else {
            log.e("uploadDialog is null,set OnKeyListener failed");
        }
    }

    public enum TYPE {
        UPLOAD,
        DOWNLOAD
    }

    public interface IDialogCanceler {
        void onCanceled();
    }

    public interface OnDialogClickListener {
        void onPositiveClick(DialogInterface dialog, int which);

        void onNegativeClick(DialogInterface dialog, int which);
    }

    private class OnKeyListener implements DialogInterface.OnKeyListener {

        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (uploadDialog != null && uploadDialog.isShowing()) {
                    uploadDialog.dismiss();
                    uploadDialog = null;
                    if (mIDialogCanceler != null) {
                        mIDialogCanceler.onCanceled();
                    }
                }
            }
            return true;
        }
    }
}
