package com.skydrm.rmc.ui.base;

/**
 * Created by hhu on 5/3/2017.
 */

public interface IBaseView {

    /**
     * show loading message
     *
     * @param msg
     */
    void showLoading(String msg);

    /**
     * hide loading
     */
    void hideLoading();

    /**
     * show error message
     */
    void showError(String msg);

    /**
     * show exception message
     */
    void showException(String msg);

    /**
     * show net error
     */
    void showNetError();

    void showEmpty(String msg);

    void hideEmpty();

    void showNoRepoView(String msg);

    void hideNoRepoView();

    void restoreView();
}
