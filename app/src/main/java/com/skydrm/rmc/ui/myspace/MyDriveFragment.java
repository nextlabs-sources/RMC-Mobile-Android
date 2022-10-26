package com.skydrm.rmc.ui.myspace;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.skydrm.rmc.R;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.engine.eventBusMsg.RepositorySelectMyDrive;
import com.skydrm.rmc.ui.fragment.AllPresenter;
import com.skydrm.rmc.ui.myspace.base.MySpaceFileBaseFragment;
import com.skydrm.rmc.ui.service.search.SearchActivity;
import com.skydrm.rmc.ui.widget.popupwindow.HomeContextMenu2;
import com.skydrm.rmc.ui.widget.popupwindow.MySpaceSortMenu;
import com.skydrm.rmc.utils.sort.SortType;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;

public class MyDriveFragment extends MySpaceFileBaseFragment implements Toolbar.OnMenuItemClickListener {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.to_projects)
    ImageButton mIb2Project;
    @BindView(R.id.fab)
    FloatingActionButton mFab;

    public static MyDriveFragment newInstance() {
        return new MyDriveFragment();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveMyDriveSelectEvent(RepositorySelectMyDrive event) {
        if (mPresenter != null) {
            mPresenter.refreshRepo();
        }
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.fragment_mydrive;
    }

    @Override
    protected void onUserVisible() {
//        if (mPresenter != null) {
//            mPresenter.list(TYPE_ALL, "");
//        }
    }

    @Override
    protected void initViewAndEvents() {
        super.initViewAndEvents();
        mPresenter = new AllPresenter(this);
        initToolbarNavi(mToolbar, true);
        mToolbar.setTitle(getString(R.string.mySpace));
        mToolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
        mToolbar.inflateMenu(R.menu.myspace);
        mToolbar.setOnMenuItemClickListener(this);

        mIb2Project.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToProjectHome.toProjects();
            }
        });

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCtxMenu();
            }
        });
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                showMySpaceSortMenu();
                break;
            case R.id.action_search:
                Intent intent = new Intent();
                intent.setAction(Constant.ACTION_SEARCH_REPO_SYSTEM);
                intent.setClass(_activity, SearchActivity.class);
                //Can only use lower 16 bits for requestCode
                startActivity(intent);
                break;
        }
        return true;
    }

    private void showMySpaceSortMenu() {
        final MySpaceSortMenu sortMenuMyDrive = new MySpaceSortMenu(_activity, true, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        sortMenuMyDrive.setOnSortByItemSelectListener(new MySpaceSortMenu.OnSortByItemSelectListener() {
            @Override
            public void onItemSelected(SortType type) {
                mSortType = type;
                sort(type);
            }
        });
        sortMenuMyDrive.setSortType(mSortType);
        sortMenuMyDrive.showAtLocation(mRootView, Gravity.TOP, 0, 0);
    }

    private void showCtxMenu() {
        FragmentManager fm = getFragmentManager();
        if (fm == null) {
            return;
        }
        HomeContextMenu2 menu = HomeContextMenu2.newInstance();
        menu.setCreateProjectSiteVisibility(View.GONE);
        menu.setSubContextVisibility(View.VISIBLE);
        menu.show(fm, HomeContextMenu2.class.getSimpleName());
    }
}
