package com.skydrm.rmc.ui.base;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.skydrm.rmc.ui.common.IDestroyable;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseFragmentStatePagerAdapter extends FragmentStatePagerAdapter implements IDestroyable {
    private List<String> mPageTitles = new ArrayList<>();
    protected List<BaseFragment> mFrags = new ArrayList<>();

    protected abstract List<String> getPageTitles();

    protected abstract List<BaseFragment> getFragments();

    public BaseFragmentStatePagerAdapter(FragmentManager fm) {
        super(fm);
        mPageTitles.addAll(getPageTitles());
        mFrags.addAll(getFragments());
    }

    public BaseFragment getFragment(int pos) {
        if (pos < 0 || pos >= mFrags.size()) {
            return null;
        }
        return mFrags.get(pos);
    }

    @Override
    public Fragment getItem(int i) {
        return mFrags.get(i);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mPageTitles.get(position);
    }

    @Override
    public int getCount() {
        return mPageTitles.size();
    }

    @Override
    public void onReleaseResource() {
        mFrags.clear();
    }
}
