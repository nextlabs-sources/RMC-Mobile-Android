package com.skydrm.rmc.ui.activity.profile;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.engine.eventBusMsg.UpdateChangeStatusMsg;
import com.skydrm.rmc.errorHandler.GenericError;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.ui.widget.SmartEditText;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.user.IRmUser;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;

import static com.skydrm.rmc.ExecutorPools.Select_Type.FIRED_BY_UI;


/**
 * version 3 will continue to use this activity
 */
public class PasswordActivity extends BaseActivity {
    private SmartEditText mEtOldPd;
    private SmartEditText mEtNewPd;
    private SmartEditText mEtNewPdConfirm;
    private ProgressBar mLoadingBar;
    private Button mBtChangePd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_password_activity);
        initView();
        EventBus.getDefault().register(this);
    }

    private void initView() {
        TextInputLayout textInputLayoutForOldPd = findViewById(R.id.textInputLayout_old_pd);
        mEtOldPd = findViewById(R.id.et_old_password);
        mEtOldPd.setParent(textInputLayoutForOldPd);

        TextInputLayout textInputLayoutForNewPd = findViewById(R.id.textInputLayout_new_pd);
        mEtNewPd = findViewById(R.id.et_new_password);
        mEtNewPd.setParent(textInputLayoutForNewPd);

        TextInputLayout textInputLayoutConfirm = findViewById(R.id.textInputLayout_confirm);
        mEtNewPdConfirm = findViewById(R.id.et_new_password_confirm);
        mEtNewPdConfirm.setParent(textInputLayoutConfirm);

        mEtOldPd.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && TextUtils.isEmpty(mEtOldPd.getText().toString())) {
                    mEtOldPd.hint();
                }
            }
        });
        mEtNewPd.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && TextUtils.isEmpty(mEtNewPd.getText().toString())) {
                    mEtNewPd.hint();
                }
            }
        });
        mEtNewPdConfirm.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && TextUtils.isEmpty(mEtNewPdConfirm.getText().toString())) {
                    mEtNewPdConfirm.hint();
                }
            }
        });

        findViewById(R.id.tv_back_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEtOldPd.isFocused()) {
                    mEtOldPd.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mEtOldPd.getWindowToken(), 0);
                } else if (mEtNewPd.isFocused()) {
                    mEtNewPd.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mEtNewPd.getWindowToken(), 0);
                } else if (mEtNewPdConfirm.isFocused()) {
                    mEtNewPdConfirm.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mEtNewPdConfirm.getWindowToken(), 0);
                }
                finishActivity();
            }
        });
        TextView mTvCurrentPdTitle = (TextView) findViewById(R.id.tv_current_pd_title);
        SpannableString currentPdTitleStr = new SpannableString("Current Password*");
        currentPdTitleStr.setSpan(new ForegroundColorSpan(getResources().getColor(android.R.color.holo_red_light)),
                currentPdTitleStr.length() - 1, currentPdTitleStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTvCurrentPdTitle.setText(currentPdTitleStr);

        TextView mTvNewPdTitle = (TextView) findViewById(R.id.tv_new_pd_title);
        SpannableString newPdTitleStr = new SpannableString("New Password*");
        newPdTitleStr.setSpan(new ForegroundColorSpan(getResources().getColor(android.R.color.holo_red_light)),
                newPdTitleStr.length() - 1, newPdTitleStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTvNewPdTitle.setText(newPdTitleStr);

        TextView mTvNewPdConfirmTitle = (TextView) findViewById(R.id.tv_new_pd_confirm_title);
        SpannableString newPdConfrimTitleStr = new SpannableString("Confirm New Password*");
        newPdConfrimTitleStr.setSpan(new ForegroundColorSpan(getResources().getColor(android.R.color.holo_red_light)),
                newPdConfrimTitleStr.length() - 1, newPdConfrimTitleStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTvNewPdConfirmTitle.setText(newPdConfrimTitleStr);

        mBtChangePd = (Button) findViewById(R.id.bt_change_password);
        mLoadingBar = (ProgressBar) findViewById(R.id.loading_bar);
        mBtChangePd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEtNewPd.getText().toString().equals(mEtOldPd.getText().toString())) {
                    mEtNewPd.setText("");
                    mEtNewPdConfirm.setText("");
                    mEtNewPdConfirm.startAnimation();
                    mEtNewPd.startAnimation();
                    ToastUtil.showToast(getApplicationContext(), "Your old password and new password cannot be the same. " +
                            "Please provide a different password.");
                } else {
                    String oldPassword = mEtOldPd.getText().toString();
                    String newPassword = mEtNewPd.getText().toString();
                    PasswordAysncTask task = new PasswordAysncTask(PasswordActivity.this);
                    task.setPassword(oldPassword, newPassword);
                    task.executeOnExecutor(ExecutorPools.SelectSmartly(FIRED_BY_UI));
                }
            }
        });
    }

    private void updateChangeBtStatus() {
        if (!mEtNewPdConfirm.isDirty() && !mEtOldPd.isDirty() && !mEtNewPd.isDirty()) {
            mBtChangePd.setEnabled(true);
        } else {
            mBtChangePd.setEnabled(false);
        }
    }

    private void finishActivity() {
        PasswordActivity.this.finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateChangeStatus(UpdateChangeStatusMsg msg) {
        mBtChangePd.setEnabled(msg.isDirty());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (mEtOldPd != null) {
            mEtOldPd.clear();
            mEtOldPd = null;
        }
        if (mEtNewPd != null) {
            mEtNewPd.clear();
            mEtNewPd = null;
        }
        if (mEtNewPdConfirm != null) {
            mEtNewPdConfirm.clear();
            mEtNewPdConfirm = null;
        }
    }

    private class PasswordAysncTask extends AsyncTask<Void, Void, Boolean> {
        private String oldPassword;
        private String newPassword;
        private WeakReference<Context> mContext;
        private RmsRestAPIException mRmsRestAPIException = null;

        PasswordAysncTask(Context context) {
            this.mContext = new WeakReference<>(context);
        }

        private void setPassword(String oldPassword, String newPassword) {
            this.oldPassword = oldPassword;
            this.newPassword = newPassword;
        }

        @Override
        protected void onPreExecute() {
            if (!isCancelled()) {
                mLoadingBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                IRmUser rmUser = SkyDRMApp.getInstance().getSession().getRmUser();
                if (rmUser == null) {
                    if (!this.isCancelled()) {
                        this.cancel(true);
                    }
                }
                SkyDRMApp.getInstance()
                        .getSession()
                        .getRmsRestAPI()
                        .getUserService(rmUser)
                        .changePassword(oldPassword, newPassword);
                return true;
            } catch (RmsRestAPIException e) {
                e.printStackTrace();
                mRmsRestAPIException = e;
            } catch (Exception e) {
                e.printStackTrace();
                mRmsRestAPIException = new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mLoadingBar.setVisibility(View.GONE);
            oldPassword = "";
            newPassword = "";
            if (result) {
                ToastUtil.showToast(getApplicationContext(), "Change password successfully and request to re-login.");
                //
                // as QA request, change password ok, need to re-login
                //
                SkyDRMApp.getInstance().logout(PasswordActivity.this);
            } else {
                switch (mRmsRestAPIException.getDomain()) {
                    // for rmUser section:
                    case InvalidPassword:
                        ToastUtil.showToast(mContext.get(),
                                mContext.get().getString(R.string.excep_incorrect_password));
                        mEtOldPd.setText("");
                        mEtOldPd.startAnimation();
                        break;
                    case TooManyAttemps:
                        ToastUtil.showToast(mContext.get(),
                                mContext.get().getString(R.string.excep_too_many_attemps));
                        break;
                    // end for rmUser section:
                    default:
                        GenericError.handleCommonException(mContext.get(), true, mRmsRestAPIException);
                }
            }
        }
    }
}
