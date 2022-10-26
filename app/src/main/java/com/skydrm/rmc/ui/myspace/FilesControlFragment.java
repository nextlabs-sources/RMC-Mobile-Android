package com.skydrm.rmc.ui.myspace;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Gravity;
import android.view.View;

import com.skydrm.rmc.R;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.engine.eventBusMsg.RepositorySelectEvent;
import com.skydrm.rmc.reposystem.RepoBindHelper;
import com.skydrm.rmc.ui.base.BaseFragment;
import com.skydrm.rmc.ui.base.BaseFragmentStatePagerAdapter;
import com.skydrm.rmc.ui.base.NxlBaseFragment;
import com.skydrm.rmc.ui.myspace.base.MySpaceBaseFragment;
import com.skydrm.rmc.ui.myspace.base.MySpaceFileBaseFragment;
import com.skydrm.rmc.ui.service.favorite.model.eventmsg.FavoriteSortEvent;
import com.skydrm.rmc.ui.service.favorite.view.FavoriteSortMenu;
import com.skydrm.rmc.ui.service.search.SearchActivity;
import com.skydrm.rmc.ui.widget.popupwindow.MySpaceSortMenu;
import com.skydrm.rmc.utils.sort.SortType;

import org.greenrobot.eventbus.EventBus;

public class FilesControlFragment extends MySpaceBaseFragment {
    private SortType mSpaceSortType = SortType.NAME_ASCEND;
    private RepoBindHelper mRepoBindHelper;

    public static FilesControlFragment newInstance() {
        return new FilesControlFragment();
    }

    public MySpaceFileBaseFragment getAllFragment() {
        BaseFragment frag = mAdapter.getFragment(mCurIdx);
        if (frag == null) {
            return null;
        }
        if (frag instanceof MySpaceFileBaseFragment) {
            return (MySpaceFileBaseFragment) frag;
        }
        return null;
    }

    @Override
    protected void onUserFirstVisible() {
        super.onUserFirstVisible();
        mRepoBindHelper = new RepoBindHelper(_activity);
    }

    @Override
    protected boolean resolveBundle(Bundle args) {
        return true;
    }

    @Override
    protected String getToolBarTitleText() {
        return getString(R.string.mySpace);
    }

    @Override
    protected BaseFragmentStatePagerAdapter createAdapter(FragmentManager fm) {
        return new MySpacePageAdapter(fm);
    }

    @Override
    protected int getOffscreenPageLimit() {
        return mAdapter.getCount();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int TASK = getResources().getInteger(R.integer.req_pick_a_cloud_service);
        if (requestCode == TASK) {
            if (resultCode == Activity.RESULT_OK) {
                String name = data.getStringExtra(getString(R.string.picked_cloud_name));
                mRepoBindHelper.executeAccountAsyncTask(name);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void showSortMenu() {
        if (mCurIdx == 0) {
            final MySpaceSortMenu sortMenu = new MySpaceSortMenu(_activity, false, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // modifyed by osmond, what you choose is what you get
                    // EventBus.getDefault().post(sortType);
                }
            });
            sortMenu.setOnRepoListItemClickListener(new MySpaceSortMenu.OnRepoListItemClickListener() {
                @Override
                public void onUpdateUI(SortType sortType) {
                    EventBus.getDefault().post(new RepositorySelectEvent(sortType));
                }
            });
            sortMenu.setOnSortByItemSelectListener(new MySpaceSortMenu.OnSortByItemSelectListener() {
                @Override
                public void onItemSelected(SortType type) {
                    mSpaceSortType = type;
                    // add by osmond, what you choose is what you get
                    EventBus.getDefault().post(type);
                }
            });
            sortMenu.setSortType(mSpaceSortType);
            sortMenu.showAtLocation(mRootView, Gravity.TOP, 0, 0);
        } else if (mCurIdx == 1) {
            FavoriteSortMenu sortMenu = new FavoriteSortMenu(_activity, new FavoriteSortMenu.OnSortMenuClickListener() {
                @Override
                public void onSortMenuClick(SortType sortType) {
                    EventBus.getDefault().post(new FavoriteSortEvent(sortType));
                }
            });
            sortMenu.showAtLocation(mRootView, Gravity.TOP, 0, 0);
        } else {
            NxlBaseFragment cur = getCurrentFragment();
            if (cur != null) {
                cur.showSortMenu();
            }
        }
    }

    @Override
    protected void lunchSearchPage() {
        Intent i = new Intent(_activity, SearchActivity.class);
        if (mCurIdx == 0) {
            i.setAction(Constant.ACTION_SEARCH_REPO_SYSTEM);
        } else if (mCurIdx == 1) {
            i.setAction(Constant.ACTION_SEARCH_FAVORITE);
        } else if (mCurIdx == 2) {
            i.setAction(Constant.ACTION_SEARCH_OFFLINE);
        } else if (mCurIdx == 3) {
            i.setAction(Constant.ACTION_SEARCH_SHARED_BY_ME);
        } else if (mCurIdx == 4) {
            i.setAction(Constant.ACTION_SEARCH_SHARED_WITH_ME);
        }
        startActivity(i);
    }

}
