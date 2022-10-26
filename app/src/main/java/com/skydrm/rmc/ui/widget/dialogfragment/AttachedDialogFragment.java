package com.skydrm.rmc.ui.widget.dialogfragment;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.DialogFragment;

/**
 * Created by hhu on 3/22/2017.
 */

public class AttachedDialogFragment extends DialogFragment {
    protected Context mContext;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }
}
