package com.skydrm.rmc.ui.widget.dialogfragment;

import android.app.Application;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import com.skydrm.rmc.R;
import com.skydrm.rmc.errorHandler.ErrorCode;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by hhu on 4/10/2017.
 */

public class NetworkErrorDialogFragment extends AttachedDialogFragment {

    public static NetworkErrorDialogFragment newInstance() {
        NetworkErrorDialogFragment dialogFragment = new NetworkErrorDialogFragment();
        Bundle args = new Bundle();
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.app_name).setMessage(ErrorCode.E_IO_NO_NETWORK)
                .setPositiveButton(R.string.setting, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectNetwork();
                    }
                })
                .setNegativeButton(R.string.common_cancel_initcap, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setCancelable(false);
        return builder.create();
    }

    private void selectNetwork() {
        Intent intent = new Intent(Settings
                .ACTION_WIFI_SETTINGS);
        if ((mContext instanceof Application)) {
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        }
        mContext.startActivity(intent);
    }
}
