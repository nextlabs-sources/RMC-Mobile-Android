package com.skydrm.rmc.ui.myspace.base;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.skydrm.rmc.R;
import com.skydrm.rmc.ui.base.BaseFragment;
import com.skydrm.rmc.ui.base.BaseFragmentStatePagerAdapter;
import com.skydrm.rmc.ui.base.NxlBaseFragment;
import com.skydrm.rmc.ui.widget.NoScrollViewPager;
import com.skydrm.rmc.ui.widget.popupwindow.HomeContextMenu2;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;

import butterknife.BindView;

public abstract class MySpaceBaseFragment extends BaseFragment implements Toolbar.OnMenuItemClickListener {
    @BindView(R.id.toolbar)
    protected Toolbar mToolbar;
    @BindView(R.id.tab_layout)
    TabLayout mTabLayout;
    @BindView(R.id.viewpager)
    protected NoScrollViewPager mViewPager;

    protected BaseFragmentStatePagerAdapter mAdapter;
    protected int mCurIdx;

    protected abstract boolean resolveBundle(Bundle args);

    protected abstract String getToolBarTitleText();

    protected boolean isTabScrollable() {
        return true;
    }

    protected abstract BaseFragmentStatePagerAdapter createAdapter(FragmentManager fm);

    protected abstract int getOffscreenPageLimit();

    protected abstract void showSortMenu();

    protected abstract void lunchSearchPage();

    protected boolean isHasOptionsMenu() {
        return true;
    }

    protected void onToolBarNavigate() {
        if (switchToProjectHome != null) {
            switchToProjectHome.toProjects();
        }
    }

    protected void showCtxMenu() {
        FragmentManager fm = getFragmentManager();
        if (fm == null) {
            return;
        }
        HomeContextMenu2 ctxMenu = HomeContextMenu2.newInstance();
        ctxMenu.setCreateProjectSiteVisibility(View.GONE);
        ctxMenu.setSubContextVisibility(View.VISIBLE);
        ctxMenu.show(fm, HomeContextMenu2.class.getSimpleName());
    }

    public NxlBaseFragment getCurrentFragment() {
        BaseFragment base = mAdapter.getFragment(mCurIdx);
        if (base instanceof NxlBaseFragment) {
            return (NxlBaseFragment) base;
        }
        return null;
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.fragment_files;
    }

    @Override
    protected boolean isBindEventBusHere() {
        return false;
    }

    @Override
    protected void onUserFirstVisible() {

    }

    @Override
    protected void onUserVisible() {
        NxlBaseFragment cur = getCurrentFragment();
        if (cur != null) {
            cur.setUserVisibleHint(true);
        }
    }

    @Override
    protected void onUserInvisible() {

    }

    @Override
    protected void initViewAndEvents() {
        if (!resolveBundle(getArguments())) {
            finishParent();
        }
        setHasOptionsMenu(isHasOptionsMenu());
        initToolbarNavi(mToolbar, true);
        mToolbar.setTitle(getToolBarTitleText());
        mToolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
        mToolbar.inflateMenu(R.menu.myspace);
        mToolbar.setOnMenuItemClickListener(this);

        //this is used to disable the setting menu item long click listener
        mRootView.findViewById(R.id.action_settings).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });
        //this is used to disable the search menu item item long click listener.
        mRootView.findViewById(R.id.action_search).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });
        mRootView.findViewById(R.id.to_projects).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onToolBarNavigate();
            }
        });
        mRootView.findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCtxMenu();
            }
        });
        mAdapter = createAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPager.setPageEnabled(false);
        mViewPager.setOffscreenPageLimit(getOffscreenPageLimit());
        mTabLayout.setTabMode(isTabScrollable() ? TabLayout.MODE_SCROLLABLE : TabLayout.MODE_FIXED);
        if (ViewCompat.isLaidOut(mTabLayout)) {
            mTabLayout.setupWithViewPager(mViewPager);
        } else {
            mTabLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    mTabLayout.setupWithViewPager(mViewPager);
                    mTabLayout.removeOnLayoutChangeListener(this);
                }
            });
        }
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mCurIdx = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected View getLoadingTargetView() {
        return null;
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_settings:
                showSortMenu();
                break;
            case R.id.action_search:
                lunchSearchPage();
                break;
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CommonUtils.releaseResource(mAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        CommonUtils.releaseResource(mAdapter);
    }

}
