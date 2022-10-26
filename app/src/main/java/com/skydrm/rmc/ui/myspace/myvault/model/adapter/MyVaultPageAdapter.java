package com.skydrm.rmc.ui.myspace.myvault.model.adapter;

import android.support.v4.app.FragmentManager;

import com.skydrm.rmc.ui.base.BaseFragment;
import com.skydrm.rmc.ui.base.BaseFragmentStatePagerAdapter;
import com.skydrm.rmc.ui.myspace.myvault.view.fragment.AllFragment;
import com.skydrm.rmc.ui.myspace.myvault.view.fragment.DeletedFragment;
import com.skydrm.rmc.ui.myspace.myvault.view.fragment.ProtectedFragment;
import com.skydrm.rmc.ui.myspace.myvault.view.fragment.RevokedFragment;
import com.skydrm.rmc.ui.myspace.myvault.view.fragment.SharedFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hhu on 5/9/2017.
 */

public class MyVaultPageAdapter extends BaseFragmentStatePagerAdapter {

    public MyVaultPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    protected List<String> getPageTitles() {
        List<String> ret = new ArrayList<>();
        ret.add("All");
        ret.add("Shared");
        ret.add("Protected");
        ret.add("Revoked");
        ret.add("Deleted");
        return ret;
    }

    @Override
    protected List<BaseFragment> getFragments() {
        List<BaseFragment> ret = new ArrayList<>();
        ret.add(AllFragment.newInstance());
        ret.add(SharedFragment.newInstance());
        ret.add(ProtectedFragment.newInstance());
        ret.add(RevokedFragment.newInstance());
        ret.add(DeletedFragment.newInstance());
        return ret;
    }

}
