package com.skydrm.rmc.ui.activity.server;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.skydrm.rmc.R;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.ui.widget.customcontrol.SafeProgressDialog;
import com.skydrm.rmc.utils.SharePreferUtils;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.sdk.Factory;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class ServerTypeSelectActivity extends BaseActivity {
    private ProgressDialog mLoadingDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_server_type);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.ll_personal_account_group, R.id.ll_company_account_group})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_personal_account_group:
                invokePersonalAction();
                break;
            case R.id.ll_company_account_group:
                invokeCompanyAction();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.CONFIG_SERVER && data != null) {
            if (resultCode == RESULT_OK) {
                boolean onPremise = data.getBooleanExtra("on_premise", false);
                String routerUrl = data.getStringExtra("router_url");
                Intent intent = new Intent();
                intent.putExtra("on_premise", onPremise);
                intent.putExtra("router_url", routerUrl);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        }
    }

    private void invokePersonalAction() {
        configServer(Factory.getPersonalCountURL());
    }

    private void configServer(final String routerUrl) {
        ServerConfig.configServer(routerUrl, new ServerConfig.Callback() {

            @Override
            public void onInvoking() {
                showLoadingDialog();
            }

            @Override
            public void onInvoked(String serverURL) {
                dismissDialog();
                bindServerUrl(routerUrl, serverURL);
            }

            @Override
            public void onFailed(String msg) {
                dismissDialog();
                ToastUtil.showToast(getApplicationContext(), msg);
            }
        });
    }

    private void showLoadingDialog() {
        mLoadingDialog = SafeProgressDialog.show(this,
                getString(R.string.wait_load), "", true);
    }

    private void dismissDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
            mLoadingDialog = null;
        }
    }

    private void bindServerUrl(String routerUrl, String rmsUrl) {
        SharePreferUtils.setParams(getApplicationContext(), Constant.INPUT_SERVER_URL, routerUrl);
        configFactory(rmsUrl);
        storeServerUrl(rmsUrl);
        //notify bind result.
        Intent intent = new Intent();
        intent.putExtra("on_premise", false);
        intent.putExtra("router_url", routerUrl);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void configFactory(String url) {
        Factory.changeRMServer(url);
    }

    private void storeServerUrl(String url) {
        SharePreferUtils.setParams(getApplicationContext(), Constant.CONFIGED_SERVER_URL, url);
    }

    private void invokeCompanyAction() {
        Intent i = new Intent(this, CompanyAccountSelectActivity.class);
        startActivityForResult(i, Constant.CONFIG_SERVER);
    }
}
