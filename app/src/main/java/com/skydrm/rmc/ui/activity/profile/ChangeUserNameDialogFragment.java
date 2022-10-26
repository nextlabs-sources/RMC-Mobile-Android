package com.skydrm.rmc.ui.activity.profile;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.engine.eventBusMsg.account.UpdateUserAvatarEvent;
import com.skydrm.rmc.engine.eventBusMsg.account.UpdateUserNameEvent;
import com.skydrm.rmc.errorHandler.GenericError;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.widget.dialogfragment.AttachedDialogFragment;
import com.skydrm.rmc.ui.widget.SmartEditText;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.RestAPI;

import org.greenrobot.eventbus.EventBus;

import static com.skydrm.rmc.ExecutorPools.Select_Type.FIRED_BY_UI;

public class ChangeUserNameDialogFragment extends AttachedDialogFragment {
    private Activity mActivity;
    private SmartEditText mSmartEditText;
    private String rmUserName;
    private OnChangeNameOkListener listener = new OnChangeNameOkListener() {
        @Override
        public void onChanged() {

        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    public static ChangeUserNameDialogFragment newInstance() {
        Bundle args = new Bundle();
        ChangeUserNameDialogFragment fragment = new ChangeUserNameDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().setCanceledOnTouchOutside(false);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setCancelable(false);
        builder.setTitle(getString(R.string.app_name));
        // Get the layout inflater
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_username_dialog_fragment, null);
        TextInputLayout textInputLayout = view.findViewById(R.id.textInputLayout);
        mSmartEditText = view.findViewById(R.id.et_change_name);
        mSmartEditText.setParent(textInputLayout);

        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSmartEditText != null && mSmartEditText.isFocused()) {
                    hideSoftKeyboard();
                }
                dismiss();
            }
        });
        view.findViewById(R.id.change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mSmartEditText.getText().toString().trim();
                if (TextUtils.equals(username, rmUserName)
                        || TextUtils.isEmpty(username)
                        || mSmartEditText.isDirty()) {
                    if (TextUtils.isEmpty(username)) {
                        mSmartEditText.hint();
                        mSmartEditText.startAnimation();
                    }
                    if (mSmartEditText.isDirty()) {
                        mSmartEditText.startAnimation();
                    }
                } else {
                    //change display name
                    changeDisplayName(username);
                    hideSoftKeyboard();
                    dismiss();
                }
            }
        });
        mSmartEditText.setCheckUsername(true);
        try {
            rmUserName = SkyDRMApp.getInstance().getSession().getRmUser().getName();
            if (!TextUtils.isEmpty(rmUserName)) {
                mSmartEditText.setHint(rmUserName);
            }
            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder.create();
    }

    private void hideSoftKeyboard() {
        if (mSmartEditText == null) {
            return;
        }
        mSmartEditText.clearFocus();
        InputMethodManager imm = (InputMethodManager)
                mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSmartEditText.getWindowToken(), 0);
    }

    private void changeDisplayName(String username) {
        new DisplayNameAsyncTask(mContext, username).executeOnExecutor(ExecutorPools.SelectSmartly(FIRED_BY_UI));
    }

    public void setListener(OnChangeNameOkListener listener) {
        if (listener != null)
            this.listener = listener;
    }

    interface OnChangeNameOkListener {
        void onChanged();
    }

    private class DisplayNameAsyncTask extends AsyncTask<Void, Void, Boolean> {
        ProgressDialog loadingDialog;
        String displayName;
        boolean flag = false;
        String msg = "";
        private RmsRestAPIException mRmsRestAPIException;

        DisplayNameAsyncTask(Context context, String displayName) {
            this.displayName = displayName;
        }

        @Override
        protected void onPreExecute() {
            if (mContext != null && !((Activity) mContext).isFinishing()) {
                loadingDialog = ProgressDialog.show(mContext, "Waiting...", "");
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                SkyDRMApp.getInstance().getSession().getRmsRestAPI()
                        .getUserService(SkyDRMApp.getInstance().getSession().getRmUser())
                        .updateUserDisplayName(displayName, new RestAPI.IRequestCallBack<String>() {
                            @Override
                            public void onSuccess(String result) {
                                msg = result;
                                flag = true;
                            }

                            @Override
                            public void onFailed(int statusCode, String errorMsg) {
                                flag = false;
                            }
                        });
            } catch (RmsRestAPIException e) {
                mRmsRestAPIException = e;
                e.printStackTrace();
            } catch (SessionInvalidException | InvalidRMClientException e) {
                mRmsRestAPIException = new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.AuthenticationFailed);
                e.printStackTrace();
            }
            return flag;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (loadingDialog != null) {
                loadingDialog.dismiss();
            }
            if (result) {
                if (!TextUtils.isEmpty(displayName)) {
                    try {
                        //Display Name successfully updated.(hint user with the success msg from rms)
                        ToastUtil.showToast(mContext, msg);
                        //save new info into session
                        SkyDRMApp.getInstance().getSession().updateRmUserName(displayName);
                        //notify ui to update
                        EventBus.getDefault().post(new UpdateUserNameEvent());
                        EventBus.getDefault().post(new UpdateUserAvatarEvent());
                        listener.onChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                GenericError.handleCommonException(mContext, true, mRmsRestAPIException);
            }
        }
    }
}
