package com.skydrm.rmc.ui.base;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.skydrm.rmc.ui.common.FragmentAgent;


/**
 * Created by hhu on 4/26/2017.
 */

public abstract class SupportActivity extends AppCompatActivity {
    private FragmentAgent mFragmentAgent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragmentAgent = getFragmentAgent();
    }

    public FragmentAgent getFragmentAgent() {
        if (mFragmentAgent == null) {
            mFragmentAgent = new FragmentAgent(this);
        }
        return mFragmentAgent;
    }

    /**
     * get loading target view
     */
    protected abstract View getLoadingTargetView();

    public void loadRootFragment(@IdRes int containerID, @NonNull SupportFragment baseFragment) {
        mFragmentAgent.loadRootTransaction(getSupportFragmentManager(), containerID, baseFragment);
    }

    public void loadMultipleRootFragment(int containerId, int showPosition, String[] tags, @NonNull SupportFragment... toFragments) {
        mFragmentAgent.loadMultipleRootTransaction(getSupportFragmentManager(), containerId, showPosition, tags, toFragments);
    }

    public void replaceLoadRootFragment(@IdRes int containerID, @NonNull SupportFragment to, boolean addBackStack) {
        mFragmentAgent.replaceLoadRootTransaction(getSupportFragmentManager(), containerID, to, addBackStack);
    }

    /**
     * if the hide fragment pass null,you should use loadMultipleRootFragment to initialize fragment
     *
     * @param showFragment fragment need to be show on page
     * @param hideFragment fragment will be hided.(allow pass null)
     */
    public void showHideFragment(@NonNull SupportFragment showFragment, @Nullable SupportFragment hideFragment) {
        mFragmentAgent.showHideTransaction(getSupportFragmentManager(), showFragment, hideFragment);
    }

    public void popupFragment() {
        mFragmentAgent.popupFragment(getSupportFragmentManager());
    }

    /**
     * need update in case the fragment is the same one by data not the same.
     *
     * @param fragmentClass fragment's name (according to the tag to find cached fragment)
     * @param <T>           only the fragment extends from SupportFragment can use the method
     * @return cached fragment in memory.
     */
    public <T extends SupportFragment> T findChildFragment(@NonNull Class<T> fragmentClass) {
        return mFragmentAgent.findStackFragment(fragmentClass, null, getSupportFragmentManager());
    }

    public <T extends SupportFragment> T findChildFragment(@NonNull String fragmentTag) {
        return mFragmentAgent.findStackFragment(null, fragmentTag, getSupportFragmentManager());
    }
}
