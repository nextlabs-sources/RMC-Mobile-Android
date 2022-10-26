package com.skydrm.rmc.ui.myspace.myvault.view.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Gravity;

import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.ui.base.BaseSortMenu;
import com.skydrm.rmc.ui.base.NxlBaseFragment;
import com.skydrm.rmc.ui.common.NxlAdapter;
import com.skydrm.rmc.ui.common.RightMenuItemClickListener;
import com.skydrm.rmc.ui.common.SortMenu;
import com.skydrm.rmc.ui.myspace.myvault.presenter.MyVaultPresenter;
import com.skydrm.rmc.ui.myspace.myvault.view.widget.MyVaultFileMenu;
import com.skydrm.rmc.ui.project.feature.files.IFileContact;
import com.skydrm.rmc.utils.sort.SortType;

public abstract class VaultBaseFragment extends NxlBaseFragment {
    protected SortType mSortType = SortType.TIME_DESCEND;
    private SortMenu mSortMenu;
    protected MyVaultFileMenu mFileMenu;

    @Override
    protected boolean showToolbar() {
        return false;
    }

    @Override
    protected void onToolbarNavigationClick() {

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
    protected boolean resolveBundle(Bundle arguments) {
        return true;
    }

    @Override
    protected IFileContact.IPresenter createPresenter() {
        return new MyVaultPresenter(this, mSortType);
    }

    @Override
    protected String getRootPathId() {
        return "/";
    }

    @Override
    protected void initViewAndEvents() {
        super.initViewAndEvents();
        mAdapter.setDisableLeftSwipeMenu(true);
        mAdapter.setOnRightMenuItemClickListener(new RightMenuItemClickListener(_activity));
        mAdapter.setOnMenuToggleListener(new NxlAdapter.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(INxlFile f, int pos) {
                showFileCtxMenu(f, pos);
            }
        });
        initFileCtxMenu();
    }

    @Override
    public void notifyItemDelete(int pos) {
        mAdapter.setDeleted(pos);
    }

    private void initFileCtxMenu() {
        mFileMenu = MyVaultFileMenu.newInstance();
        mFileMenu.setOnItemClickListener(new MyVaultFileMenu.OnItemClickListener() {
            @Override
            public void onSetFavorite(INxlFile f, int pos, boolean favorite) {
                if (favorite) {
                    unMarkAsFavorite(f, pos);
                } else {
                    markAsFavorite(f, pos);
                }
            }

            @Override
            public void onSetOffline(INxlFile f, int pos, boolean offline) {
                if (offline) {
                    unMarkAsOffline(f, pos);
                } else {
                    markAsOffline(f, pos);
                }
            }

            @Override
            public void onDeleteFile(INxlFile f, int pos) {
                deleteFile(f, pos);
            }
        });
    }

    protected void showFileCtxMenu(INxlFile f, int pos) {
        FragmentManager fm = getFragmentManager();
        if (fm == null) {
            return;
        }
        mFileMenu.setFile(f);
        mFileMenu.setPosition(pos);
        mFileMenu.show(fm, MyVaultFileMenu.class.getSimpleName());
    }
}
