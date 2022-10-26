package com.skydrm.rmc.reposystem.remoterepo.sharepointonline;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.EditText;

import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SharePointAuthActivity extends BaseActivity {
    private static final boolean DEBUG = SkyDRMApp.getInstance().isDebug();
    private static final String SERVER_SITE = "http://rms-sp2013.qapf1.qalab01.nextlabs.com/sites/iosdev";
    private static final String DEBUG_SP_ONLINE_SITE_URL = "https://nextlabsdev.sharepoint.com/sites/jchen7301";
    private static final String USERNAME = "abraham.lincoln@qapf1.qalab01.nextlabs.com";
    private static final String PASSWORD = "abraham.lincoln";
    private EditText mCetSu;
    private EditText mCetUn;
    private EditText mCetPd;
    private boolean suErrShow;
    private boolean unErrShow;
    private boolean pdErrShow;
    private ProgressDialog mProgressDialog;
    private boolean bBindSPNeedSiteURL;
    private BoundService mBoundService;
    private boolean reAuth;
    private String spNickName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sharepoint_auth);
        resolveIntent();
        initViewAndEvent();
    }

    private void resolveIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(Constant.SP_BIND_SITE_URL)) {
                bBindSPNeedSiteURL = intent.getBooleanExtra(Constant.SP_BIND_SITE_URL, false);
            }
            if (intent.hasExtra(Constant.REPO_SP_NAME)) {
                spNickName = intent.getStringExtra(Constant.REPO_SP_NAME);
            }
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action) && TextUtils.equals(action, Constant.RE_AUTHENTICATION)) {
                reAuth = true;
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    mBoundService = (BoundService) extras.getSerializable(Constant.BOUND_SERVICE);
                }
            }
        }
    }

    private void initViewAndEvent() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mCetSu = (EditText) findViewById(R.id.cet_site_url);
        mCetUn = (EditText) findViewById(R.id.cet_username);
        mCetPd = (EditText) findViewById(R.id.cet_password);
        Button btAddAccount = (Button) findViewById(R.id.bt_add_account);

        if (bBindSPNeedSiteURL) {
            toolbar.setTitle(getString(R.string.name_sharepointonline));
            mCetUn.setVisibility(View.GONE);
            mCetPd.setVisibility(View.GONE);
//            if (DEBUG) {
//                mCetSu.setText(DEBUG_SP_ONLINE_SITE_URL);
//            }
        } else {
            toolbar.setTitle(getString(R.string.name_sharepoint));
            try {
                mCetUn.setText(SkyDRMApp.getInstance().getSession().getUserEmail());
                mCetUn.setEnabled(false);
//                if (DEBUG) {
//                    mCetSu.setText(SERVER_SITE);
//                    mCetUn.setText(USERNAME);
//                    mCetPd.setText(PASSWORD);
//                }
            } catch (InvalidRMClientException e) {
                e.printStackTrace();
            }
        }

        if (mBoundService != null) {
            mCetSu.setText(mBoundService.accountID);
            mCetSu.setEnabled(false);
            try {
                String email = SkyDRMApp.getInstance().getSession().getRmUser().getEmail();
                mCetUn.setText(email);
                mCetPd.setText("");
            } catch (InvalidRMClientException e) {
                e.printStackTrace();
            }
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btAddAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bindAccount();
            }
        });
        mCetSu.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (suErrShow) {
                    if (!TextUtils.isEmpty(s)) {
                        hintUser(mCetSu, "", false);
                    }
                    suErrShow = false;
                }
            }
        });
        mCetUn.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (unErrShow) {
                    if (!TextUtils.isEmpty(s)) {
                        hintUser(mCetUn, "", false);
                    }
                    unErrShow = false;
                }
            }
        });
        mCetPd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (pdErrShow) {
                    if (!TextUtils.isEmpty(s)) {
                        hintUser(mCetPd, "", false);
                    }
                    pdErrShow = false;
                }
            }
        });
    }

    private void bindAccount() {
        if (bBindSPNeedSiteURL) {
            setSPOnlineSiteURLBack();
        } else {
            handleInputResult();
        }
    }

    private void setSPOnlineSiteURLBack() {
        String su = mCetSu.getText().toString();
        if (checkUrlInput(su)) return;
        setResult(Activity.RESULT_OK, getIntent().putExtra(Constant.SP_BIND_SITE_URL, su));
        finish();
    }

    private void handleInputResult() {
        String su = mCetSu.getText().toString();
        String un = mCetUn.getText().toString();
        String pd = mCetPd.getText().toString();
        // check url input
        if (checkUrlInput(su)) return;
        //username input
        if (TextUtils.isEmpty(un)) {
            hintUser(mCetUn, getString(R.string.hint_msg_require_input_userName), true);
            mCetUn.requestFocus();
            unErrShow = true;
            return;
        }
        //check password input
        if (TextUtils.isEmpty(pd)) {
            hintUser(mCetPd, getString(R.string.hint_msg_require_input_password), true);
            mCetPd.requestFocus();
            pdErrShow = true;
            return;
        }
        startAuth(su, un, pd);
    }

    private boolean checkUrlInput(String su) {
        if (TextUtils.isEmpty(su)) {
            hintUser(mCetSu, getString(R.string.hint_msg_require_input_url), true);
            mCetSu.requestFocus();
            suErrShow = true;
            return true;
        } else {
            if (!isURLValidity(su)) {
                hintUser(mCetSu, "The URL format is invalid.", true);
                mCetSu.requestFocus();
                suErrShow = true;
                return true;
            }
        }
        return false;
    }

    private void startAuth(final String dm, final String un, final String pd) {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(this, "", getString(R.string.common_waiting_initcap_3dots));
        }
        SharePointAuthManager.startAuth(dm, un, pd, new SharePointAuthManager.IAuthCallback() {
            @Override
            public void onSuccess() {
                SharePointAuthManager.setAccount(new SharePointAuthManager.Account(dm, un, pd, spNickName));
                SharePointAuthManager.setAuthStatus(true);
                dismissDialog();
                finish();
            }

            @Override
            public void onFailed(String msg) {
                dismissDialog();
                ToastUtil.showToast(getApplicationContext(), msg);
            }
        });
    }

    private void dismissDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private void hintUser(EditText target, String msg, boolean animate) {
        ViewParent parentForAccessibility = target.getParentForAccessibility();
        if (parentForAccessibility instanceof TextInputLayout) {
            TextInputLayout inputLayout = (TextInputLayout) parentForAccessibility;
            inputLayout.setError(msg);
        }
        if (animate) {
            if (TextUtils.isEmpty(msg)) {
                return;
            }
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(target, "translationX",
                    0, 25, -25, 25, -25, 15, -15, 6, -6, 0);
            objectAnimator.start();
        }
    }

    private boolean isURLValidity(String url) {
        String regExp = "(ht|f)tp(s?)\\:\\/\\/[0-9a-zA-Z]([-.\\w]*[0-9a-zA-Z])*(:(0-9)*)*(\\/?)([a-zA-Z0-9\\-\\.\\?\\,\\'\\/\\\\\\+&amp;%\\$#_]*)?";
        Pattern pattern = Pattern.compile(regExp, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(url);
        return matcher.matches();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissDialog();
    }
}
