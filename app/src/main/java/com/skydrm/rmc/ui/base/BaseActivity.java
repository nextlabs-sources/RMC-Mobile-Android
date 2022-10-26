package com.skydrm.rmc.ui.base;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.user.GetTenantPreferencesTask;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.ui.common.ActivityManager;
import com.skydrm.rmc.ui.service.contact.ContactActivity;
import com.skydrm.rmc.utils.permission.PermissionCallback;
import com.skydrm.rmc.utils.permission.PermissionHelper;

import java.io.Serializable;
import java.util.List;

/**
 * Created by hhu on 4/26/2017.
 */

public class BaseActivity extends SupportActivity implements BaseLazyFragment.OnFragmentOpenDrawerListener
        , BaseLazyFragment.SwitchToProjectHome, BaseLazyFragment.OnNavigationToolClickListener,
        PermissionCallback, IBaseView {
    protected static final int REQUEST_PERMISSION = 123;
    protected static DevLog log = new DevLog(BaseActivity.class.getSimpleName());
    private CheckPermissionListener checkPermissionListener;
    protected ViewHelperController mViewHelperController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.getDefault().addActivity(this);
    }

    @Override
    protected View getLoadingTargetView() {
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.getDefault().removeActivity(this);
    }

    protected void setViewHelperController(View target) {
        if (target == null) {
            return;
        }
        this.mViewHelperController = new ViewHelperController(target);
    }

    public void launchTo(Class to) {
        Intent intent = new Intent();
        intent.setClass(this, to);
        startActivity(intent);
    }

    public void launchTo(Class to, String name, Serializable value) {
        Intent intent = new Intent();
        Bundle extra = new Bundle();
        extra.putSerializable(name, value);
        intent.putExtras(extra);
        intent.setClass(this, to);
        startActivity(intent);
    }

    public void initToolbar(Toolbar toolbar, String title) {
        toolbar.setNavigationIcon(R.drawable.icon_back_3);
        toolbar.setTitle(title);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    protected void lunchContactPageWithResult(int requestCode) {
        Intent i = new Intent(this, ContactActivity.class);
        startActivityForResult(i, requestCode);
    }

    public void hideSoftInput(View view) {
        if (view == null) {
            return;
        }
        view.clearFocus();
        InputMethodManager imm = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onDrawerOpen() {

    }

    @Override
    public void toProjects() {

    }

    public void checkPermission(CheckPermissionListener listener, int resString, String... permissions) {
        checkPermissionListener = listener;
        if (PermissionHelper.hasPermissions(this, permissions)) {
            if (checkPermissionListener != null) {
                checkPermissionListener.superPermission();
            }
        } else {
            PermissionHelper.requestPermissions(this, getString(resString),
                    REQUEST_PERMISSION, permissions);
        }
    }

    public boolean checkPermissionNeverAskAgain(@Nullable DialogInterface.OnClickListener listener, List<String> deniedPermissions) {
        return PermissionHelper.checkDeniedPermissionsNeverAskAgain(this, getResources().getString(R.string.permission_storage_rationale),
                android.R.string.ok, android.R.string.cancel, listener, deniedPermissions);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == PermissionHelper.SETTINGS_REQUEST_CODE) {
//
//        }
    }

    @Override
    public void onPermissionGranted(int requestCode, List<String> permissions) {

    }

    @Override
    public void onPermissionDenied(int requestCode, List<String> permissions) {
        if (checkPermissionListener != null) {
            checkPermissionListener.onPermissionDenied();
        }
    }

    @Override
    public void onPermissionAllGranted() {
        if (checkPermissionListener != null) {
            checkPermissionListener.superPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void showLoading(String msg) {
        toggleShowLoading(true, msg);
    }

    @Override
    public void hideLoading() {
        toggleShowLoading(false, "");
    }

    @Override
    public void showError(String msg) {
        toggleShowError(true, msg, null);
    }

    @Override
    public void showException(String msg) {
        toggleShowError(true, msg, null);
    }

    @Override
    public void showNetError() {
        toggleNetworkError(true, null);
    }

    @Override
    public void showEmpty(String msg) {
        toggleEmpty(true, msg);
    }

    @Override
    public void hideEmpty() {
        toggleEmpty(false, "");
    }

    @Override
    public void showNoRepoView(String msg) {
        toggleNoRepositoryView(true, msg, null);
    }

    @Override
    public void hideNoRepoView() {
        toggleNoRepositoryView(false, "", null);
    }

    @Override
    public void restoreView() {
        toggleRestoreView();
    }

    @Override
    public void onNavigationStart(NavigationType type) {

    }

    @Override
    public void onNavigationToRepo(BoundService service) {

    }

    public interface CheckPermissionListener {
        void superPermission();

        void onPermissionDenied();
    }

    /**
     * toggle show loading
     *
     * @param toggle
     */
    private void toggleShowLoading(boolean toggle, String msg) {
        if (null == mViewHelperController) {
            throw new NullPointerException("You must return a right target view for loading");
        }
        if (toggle) {
            mViewHelperController.showLoading(msg);
        } else {
            mViewHelperController.restore();
        }
    }

    protected void toggleEmpty(boolean toggle, String msg) {
        if (null == mViewHelperController) {
            throw new NullPointerException("You must return a right target view for loading");
        }
        if (toggle) {
            mViewHelperController.showEmpty(msg);
        } else {
            mViewHelperController.restore();
        }
    }

    protected void toggleNoRepositoryView(boolean toggle, String msg, View.OnClickListener onClickListener) {
        if (null == mViewHelperController) {
            throw new NullPointerException("You must return a right target view for loading");
        }
        if (toggle) {
            mViewHelperController.showNoRepositoryView(msg, onClickListener);
        } else {
            mViewHelperController.restore();
        }
    }

    /**
     * toggle show error
     *
     * @param toggle
     */
    protected void toggleShowError(boolean toggle, String msg, View.OnClickListener onClickListener) {
        if (null == mViewHelperController) {
            throw new NullPointerException("You must return a right target view for loading");
        }
        if (toggle) {
            mViewHelperController.showError(msg, onClickListener);
        } else {
            mViewHelperController.restore();
        }
    }

    /**
     * toggle show network error
     *
     * @param toggle
     */
    protected void toggleNetworkError(boolean toggle, View.OnClickListener onClickListener) {
        if (null == mViewHelperController) {
            throw new NullPointerException("You must return a right target view for loading");
        }
        if (toggle) {
            mViewHelperController.showNetworkError(onClickListener);
        } else {
            mViewHelperController.restore();
        }
    }

    protected void toggleRestoreView() {
        if (null == mViewHelperController) {
            throw new NullPointerException("You must return a right target view for loading");
        }
        mViewHelperController.restore();
    }

    protected void syncTenantPreferences() {
        GetTenantPreferencesTask task = new GetTenantPreferencesTask();
        task.run();
    }

    protected boolean isSupportWorkSpace() {
        return SkyDRMApp.getInstance().isOnPremise();
    }

}

