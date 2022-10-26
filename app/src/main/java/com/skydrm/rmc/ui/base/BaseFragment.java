package com.skydrm.rmc.ui.base;

import com.skydrm.rmc.utils.commonUtils.ToastUtil;

/**
 * Created by hhu on 5/3/2017.
 */

public abstract class BaseFragment extends BaseLazyFragment implements IBaseView {

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
    public void showLoading(String msg) {
        toggleShowLoading(true, msg);
    }

    @Override
    public void hideLoading() {
        toggleShowLoading(false, null);
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

    protected boolean checkAttachedActivityNullable() {
        if (_activity == null) {
            log.e("internal error:_activity is null");
            return true;
        }
        return false;
    }

    protected void onPremiseLogin() {

    }

    @Override
    protected void networkConnected(String extraInfo) {

    }

    @Override
    protected void networkDisconnected() {

    }

    protected void showToast(String msg) {
        if (_activity != null) {
            ToastUtil.showToast(_activity, msg);
        }
    }

    protected void showToast(int resId) {
        if (_activity != null) {
            ToastUtil.showToast(_activity, resId);
        }
    }

    protected void finishParent() {
        if (_activity != null) {
            _activity.finish();
        }
    }

}
