package com.skydrm.rmc.ui.myspace.sharewithme.view;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Gravity;

import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.ui.base.BaseSortMenu;
import com.skydrm.rmc.ui.base.NxlBaseFragment;
import com.skydrm.rmc.ui.common.NxlAdapter;
import com.skydrm.rmc.ui.common.NxlFileType;
import com.skydrm.rmc.ui.common.SortMenu;
import com.skydrm.rmc.ui.project.feature.files.IFileContact;
import com.skydrm.rmc.ui.myspace.sharewithme.SortEventMsg;
import com.skydrm.rmc.ui.myspace.sharewithme.presenter.SharedWithMePresenter;
import com.skydrm.rmc.utils.sort.SortType;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by hhu on 7/26/2017.
 */

public class SharedWithMeFragment extends NxlBaseFragment {
    private SortType mSortType = SortType.TIME_DESCEND;
    private SortMenu mSortMenu;
    private SharedWithMeFileMenu mFileCtxMenu;

    public static SharedWithMeFragment newInstance() {
        return new SharedWithMeFragment();
    }

    @Override
    public void showSortMenu() {
        if (mSortMenu == null) {
            mSortMenu = new SortMenu(_activity);
            mSortMenu.setOnSortItemClickListener(new BaseSortMenu.OnSortItemClickListener() {
                @Override
                public void onSortItemClick(SortType sortType) {
                    mSortType = sortType;
                    mPresenter.sort(sortType);
                }
            });
        }
        mSortMenu.showAtLocation(mRootView, Gravity.TOP, 0, 0);
    }

    @Override
    protected boolean showToolbar() {
        return false;
    }

    @Override
    protected void onToolbarNavigationClick() {

    }

    @Override
    protected boolean isBindEventBusHere() {
        return true;
    }

    @Override
    protected boolean resolveBundle(Bundle arguments) {
        return true;
    }

    @Override
    protected IFileContact.IPresenter createPresenter() {
        return new SharedWithMePresenter(this, mSortType);
    }

    @Override
    protected int getFileType() {
        return NxlFileType.SHARED_WITH_ME.getValue();
    }

    @Override
    protected String getRootPathId() {
        return "/";
    }

    @Override
    protected void initViewAndEvents() {
        super.initViewAndEvents();
        mAdapter.setOnMenuToggleListener(new NxlAdapter.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(INxlFile f, int pos) {
                showFileMenu(f, pos);
            }
        });
        mAdapter.setDisableLeftSwipeMenu(true);
        mAdapter.setDisableRightSwipeMenu(true);
        initFileCtxMenu();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void sort(SortEventMsg msg) {
        mPresenter.sort(msg.getSortType());
    }

    private void initFileCtxMenu() {
        mFileCtxMenu = SharedWithMeFileMenu.newInstance();
    }

    private void showFileMenu(INxlFile f, int position) {
        FragmentManager fm = getFragmentManager();
        if (fm == null) {
            return;
        }
        mFileCtxMenu.setSharedWithMeFile(f);
        mFileCtxMenu.setPosition(position);
        mFileCtxMenu.setOnItemClickListener(new SharedWithMeFileMenu.OnItemClickListener() {
            @Override
            public void onSetOffline(INxlFile f, int pos, boolean offline) {
                if (offline) {
                    unMarkAsOffline(f, pos);
                } else {
                    markAsOffline(f, pos);
                }
            }
        });
        mFileCtxMenu.show(fm, this.getClass().getSimpleName());
    }
}
