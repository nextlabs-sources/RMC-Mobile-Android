package com.skydrm.rmc.ui.myspace.myvault.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.skydrm.rmc.R;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.engine.eventBusMsg.ProtectCompleteNotifyEvent;
import com.skydrm.rmc.engine.eventBusMsg.ShareCompleteNotifyEvent;
import com.skydrm.rmc.ui.base.BaseFragmentStatePagerAdapter;
import com.skydrm.rmc.ui.base.NxlBaseFragment;
import com.skydrm.rmc.ui.myspace.base.MySpaceBaseFragment;
import com.skydrm.rmc.ui.myspace.myvault.model.adapter.MyVaultPageAdapter;
import com.skydrm.rmc.ui.service.search.SearchActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MyVaultControlFragment extends MySpaceBaseFragment {

    public static MyVaultControlFragment newInstance() {
        return new MyVaultControlFragment();
    }

    @Override
    protected boolean resolveBundle(Bundle arguments) {
        return true;
    }

    @Override
    protected String getToolBarTitleText() {
        return getString(R.string.mySpace);
    }

    @Override
    protected BaseFragmentStatePagerAdapter createAdapter(FragmentManager fm) {
        return new MyVaultPageAdapter(fm);
    }

    @Override
    protected int getOffscreenPageLimit() {
        return mAdapter.getCount();
    }

    @Override
    protected void showSortMenu() {
        NxlBaseFragment cur = getCurrentFragment();
        if (cur != null) {
            cur.showSortMenu();
        }
    }

    @Override
    protected void lunchSearchPage() {
        Intent i = new Intent(_activity, SearchActivity.class);
        i.setAction(Constant.ACTION_SEARCH_MYVAULT);
        if (mCurIdx == 1) {
            i.putExtra(Constant.ACTION_KEY_MY_VAULT, Constant.TAB_MY_VAULT_ACTIVE_SHARES);
        }
        startActivity(i);
    }

    @Override
    protected boolean isBindEventBusHere() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNormalFileShared(ShareCompleteNotifyEvent event) {
        if (mCurIdx == 0 || mCurIdx == 1) {
            NxlBaseFragment current = getCurrentFragment();
            if (current != null) {
                current.refreshCurrent();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNormalFileProtected(ProtectCompleteNotifyEvent event) {
        if (mCurIdx == 0 || mCurIdx == 1) {
            NxlBaseFragment current = getCurrentFragment();
            if (current != null) {
                current.refreshCurrent();
            }
        }
    }

}
