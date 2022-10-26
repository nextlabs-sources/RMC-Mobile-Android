package com.skydrm.rmc.ui.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.skydrm.rmc.R;

import java.lang.ref.WeakReference;

/**
 * Created by hhu on 11/23/2017.
 */

@Deprecated
public class LoadingDialog extends DialogFragment {
    private ProgressBar mPbProgress;
    private boolean cancelable = true;
    private Context mCtx;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mCtx = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public static LoadingDialog newInstance() {
        return new LoadingDialog();
    }

    public void setProgress(int progress) {
        mPbProgress.setProgress(progress);
    }

    public void setDialogCancelable(boolean cancelable) {
        this.cancelable = cancelable;
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        if (mCtx == null || manager == null) {
            return;
        }
        if (isAdded()) {
            return;
        }
        try {
            super.show(manager, tag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dismiss() {
        if (mCtx == null) {
            return;
        }
        Activity act = (Activity) mCtx;
        if (act.isFinishing() || act.isDestroyed()) {
            return;
        }

        try {
            super.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_loading_dialog, null);
        builder.setCancelable(cancelable);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPbProgress = view.findViewById(R.id.progress);
    }
}
