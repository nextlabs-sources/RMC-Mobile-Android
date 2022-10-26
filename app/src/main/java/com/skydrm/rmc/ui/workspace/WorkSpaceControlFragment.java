package com.skydrm.rmc.ui.workspace;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;

import com.skydrm.rmc.R;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.ui.base.BaseFragmentStatePagerAdapter;
import com.skydrm.rmc.ui.base.NxlBaseFragment;
import com.skydrm.rmc.ui.myspace.base.MySpaceBaseFragment;
import com.skydrm.rmc.ui.project.service.SwitchProjectActivity;
import com.skydrm.rmc.ui.service.search.SearchActivity;

public class WorkSpaceControlFragment extends MySpaceBaseFragment implements Toolbar.OnMenuItemClickListener {

    public static WorkSpaceControlFragment newInstance() {
        return new WorkSpaceControlFragment();
    }

    public boolean needInterceptBackPress() {
        NxlBaseFragment child = getCurrentFragment();
        if (child != null) {
            return child.needInterceptBackPress();
        }
        return false;
    }

    public void handleBackPress() {
        NxlBaseFragment child = getCurrentFragment();
        if (child != null) {
            child.interceptBackPress();
        }
    }

    @Override
    protected boolean resolveBundle(Bundle args) {
        return true;
    }

    @Override
    protected String getToolBarTitleText() {
        return getString(R.string.name_workspace);
    }

    @Override
    protected void onToolBarNavigate() {
        Intent intent = new Intent(_activity, SwitchProjectActivity.class);
        intent.putExtra(Constant.KEY, Constant.FLAG_FROM_WORKSPACE);
        startActivity(intent);
    }

    @Override
    protected boolean isTabScrollable() {
        return false;
    }

    @Override
    protected BaseFragmentStatePagerAdapter createAdapter(FragmentManager fm) {
        return new WorkSpacePageAdapter(fm);
    }

    @Override
    protected int getOffscreenPageLimit() {
        return mAdapter.getCount();
    }

    @Override
    protected void showCtxMenu() {
        FragmentManager fm = getFragmentManager();
        if (fm != null) {
            WorkSpaceContextMenu ctxMenu = WorkSpaceContextMenu.newInstance();
            NxlBaseFragment currentFrag = getCurrentFragment();
            ctxMenu.setCurrentPathId(currentFrag == null ? "/" : currentFrag.getCurrentPathId());
            ctxMenu.show(fm, WorkSpaceContextMenu.class.getSimpleName());
        }
    }

    @Override
    protected void showSortMenu() {
        NxlBaseFragment child = getCurrentFragment();
        if (child != null) {
            child.showSortMenu();
        }
    }

    @Override
    protected void lunchSearchPage() {
        Intent i = new Intent(_activity, SearchActivity.class);
        if (mCurIdx == 0) {
            i.setAction(Constant.ACTION_SEARCH_WORKSPACE_FILES);
        } else {
            i.setAction(Constant.ACTION_SEARCH_WORKSPACE_OFFLINE_FILES);
        }
        _activity.startActivity(i);
    }


}
