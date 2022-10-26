package com.skydrm.rmc.ui.activity.server;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.skydrm.rmc.BuildConfig;
import com.skydrm.rmc.R;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.domain.UserAccount;
import com.skydrm.rmc.ui.activity.server.cache.AccountCache;
import com.skydrm.rmc.ui.activity.server.cache.ICacheCallback;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.ui.widget.LoadingDialog;
import com.skydrm.rmc.ui.widget.LoadingDialog2;
import com.skydrm.rmc.ui.widget.customcontrol.SafeProgressDialog;
import com.skydrm.rmc.ui.widget.DropdownMenu;
import com.skydrm.rmc.utils.SharePreferUtils;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.sdk.Factory;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by hhu on 4/23/2018.
 */

public class CompanyAccountSelectActivity extends BaseActivity {
    private static final String KEY_REMEMBER_STATUS = "key_remember_status";

    @BindView(R.id.ll_view_container)
    LinearLayout mLlSubViewContainer;
    @BindView(R.id.bt_next)
    Button mBtNext;
    TextInputLayout mTextInputLayout;

    private Context mCtx;

    private EditText mEtInputURL;
    private boolean mRememberURL = true;
    private AccountCache mAccountCache = new AccountCache();
    private List<UserAccount> mUserAccounts = new ArrayList<>();
    private DropdownMenu mDropdownMenu;
    private boolean isOnPremise;

    private LoadingDialog2 mLoadingDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_config);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        mCtx = this;
        initListener();
        initData();
    }

    private void initData() {
        mAccountCache.readCache(new ICacheCallback<UserAccount>() {
            @Override
            public void onCacheLoad(List<UserAccount> caches) {
                mUserAccounts.clear();
                mUserAccounts.addAll(caches);
                loadCompanySubView();
            }
        });
    }

    private void initListener() {
        mBtNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                invokeCompanyAction();
            }
        });
    }

    private void loadCompanySubView() {
        isOnPremise = true;
        if (mUserAccounts.size() == 0) {
            loadCompanyInputView();
        } else {
            loadCompanySelectView();
        }
    }

    private void loadCompanyInputView() {
        mLlSubViewContainer.removeAllViews();
        View cRoot = LayoutInflater.from(mCtx).inflate(R.layout.layout_sub_input_company_account,
                mLlSubViewContainer);
        mTextInputLayout = cRoot.findViewById(R.id.textInputLayout);
        mEtInputURL = cRoot.findViewById(R.id.et_url_input);
        Switch swRememberURL = cRoot.findViewById(R.id.sw_remember_url);
        mRememberURL = readRememberStatus();
        swRememberURL.setChecked(mRememberURL);

        swRememberURL.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mRememberURL = isChecked;
            }
        });
        //initErrorTextWithAppearance();
    }

    private void loadCompanySelectView() {
        mLlSubViewContainer.removeAllViews();
        View cRoot = LayoutInflater.from(mCtx).inflate(R.layout.layout_sub_select_company_account, mLlSubViewContainer);
        mDropdownMenu = cRoot.findViewById(R.id.dropdown_menu);
        TextView tvManageUrl = cRoot.findViewById(R.id.tv_manage_url);
        mDropdownMenu.setAccounts(mUserAccounts);
        tvManageUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lunchToSelectAccount();
            }
        });
    }

    private void lunchToSelectAccount() {
        Intent intent = new Intent(this, CompanyAccountListActivity.class);
        startActivity(intent);
    }

    private void invokeCompanyAction() {
        configSSLCert(BuildConfig.DEBUG);
        if (mUserAccounts.size() == 0) {
            invokeCompanyInputAction();
        } else {
            invokeCompanySelectAction();
        }
    }

    private void invokeCompanySelectAction() {
        if (mDropdownMenu == null) {
            return;
        }
        String selectItem = mDropdownMenu.getSelectItem();
        String wrapperURL = CommonUtils.generateWrapperURL(selectItem);
        configServer(wrapperURL, true);
    }

    private void invokeCompanyInputAction() {
        setErrorTextColor(mTextInputLayout, Color.parseColor("#F39696"));
        String url = mEtInputURL.getText().toString();
        if (!TextUtils.isEmpty(url)) {
            String wrapperURL = CommonUtils.generateWrapperURL(url);
            if (CommonUtils.isURLValidate(wrapperURL)) {
                hintUser("", true);
                configServer(wrapperURL, true);
            } else {
                hintUser(getResources().getString(R.string.hint_the_url_format_is_invalid), true);
            }
        } else {
            hintUser(getResources().getString(R.string.hint_the_url_is_empty), true);
        }
    }

    private boolean readRememberStatus() {
        return (boolean) SharePreferUtils.
                getParams(getApplicationContext(), KEY_REMEMBER_STATUS, true);
    }

    private void saveRememberStatus(boolean status) {
        SharePreferUtils.setParams(getApplicationContext(), KEY_REMEMBER_STATUS, status);
    }

    private void hintUser(String msg, boolean animate) {
        mTextInputLayout.setError(msg);
        if (animate) {
            if (TextUtils.isEmpty(msg)) {
                return;
            }
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mEtInputURL, "translationX",
                    0, 25, -25, 25, -25, 15, -15, 6, -6, 0);
            objectAnimator.start();
        }
    }

    public static void setErrorTextColor(TextInputLayout textInputLayout, int color) {
        try {
            Field fErrorView = TextInputLayout.class.getDeclaredField("mErrorView");
            if (fErrorView == null) {
                return;
            }
            fErrorView.setAccessible(true);
            TextView mErrorView = (TextView) fErrorView.get(textInputLayout);
            Field fCurTextColor = TextView.class.getDeclaredField("mCurTextColor");
            if (fCurTextColor == null || mErrorView == null) {
                return;
            }
            fCurTextColor.setAccessible(true);
            fCurTextColor.set(mErrorView, color);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void configServer(final String routerUrl, final boolean storeUrl) {
        ServerConfig.configServer(routerUrl, new ServerConfig.Callback() {

            @Override
            public void onInvoking() {
                showLoadingDialog();
            }

            @Override
            public void onInvoked(String serverURL) {
                dismissLoadingDialog();
                //store user input url for ui display only.
                storeUsrInputUrl(routerUrl, storeUrl);
                bindServerUrl(routerUrl, serverURL);
            }

            @Override
            public void onFailed(String msg) {
                dismissLoadingDialog();
                ToastUtil.showToast(getApplicationContext(), msg);
            }
        });
    }


    private void showLoadingDialog() {
        mLoadingDialog = LoadingDialog2.newInstance();
        mLoadingDialog.showModalDialog(mCtx);
    }

    private void dismissLoadingDialog() {
        if (mLoadingDialog != null) {
            mLoadingDialog.dismissDialog();
            mLoadingDialog = null;
        }
    }

    private void bindServerUrl(String routerUrl, String rmsUrl) {
        saveRememberStatus(mRememberURL);
        storeServerUrl(mRememberURL ? rmsUrl : "");
        configFactory(rmsUrl);
        //notify bind result.
        Intent intent = new Intent();
        intent.putExtra("on_premise", isOnPremise);
        intent.putExtra("router_url", routerUrl);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void storeUsrInputUrl(String routerURL, boolean storeUrl) {
        SharePreferUtils.setParams(getApplicationContext(), Constant.INPUT_SERVER_URL, routerURL);
        if (!mRememberURL) {
            return;
        }
        if (!storeUrl) {
            return;
        }
        if (TextUtils.isEmpty(routerURL)) {
            return;
        }
        if (mUserAccounts.size() == 0) {
            List<UserAccount.Item> items = new ArrayList<>();
            items.add(new UserAccount.Item(true, routerURL));
            mUserAccounts.add(new UserAccount(Constant.NAME_COMPANY_ACCOUNT, items));
            mAccountCache.writeCache(mUserAccounts);
        }
    }

    private void configFactory(String url) {
        Factory.changeRMServer(url);
    }

    private void storeServerUrl(String url) {
        SharePreferUtils.setParams(getApplicationContext(), Constant.CONFIGED_SERVER_URL, url);
    }

    /**
     * In SaaS situation set ignoreSSLCert = false, in PaaS situation will set ignoreSSLCert = true
     *
     * @param ignore true means ignoreSSLCert
     */
    private void configSSLCert(boolean ignore) {
        //Keep ignore ssl certification status in Factory.
        Factory.ignoreSSLCertStatus(ignore);
        //Store ignore ssl certification status in shared-preferences.
        SharePreferUtils.setParams(getApplicationContext(), Constant.IGNORE_SSL_CERT, ignore);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateUserAccount(MsgUpdateUserAccount msg) {
        if (msg.mUsrAccounts == null) {
            return;
        }
        mUserAccounts.clear();
        mUserAccounts.addAll(msg.mUsrAccounts);
        loadCompanySubView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissLoadingDialog();
        EventBus.getDefault().unregister(this);
    }
}
