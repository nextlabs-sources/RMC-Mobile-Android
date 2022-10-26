package com.skydrm.rmc.ui.project.feature.files.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.dbbridge.IOwner;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.ui.base.BaseFragment;
import com.skydrm.rmc.ui.base.BaseFragmentStatePagerAdapter;
import com.skydrm.rmc.ui.base.NxlBaseFragment;
import com.skydrm.rmc.ui.myspace.base.MySpaceBaseFragment;
import com.skydrm.rmc.ui.project.common.ProjectContextMenu;
import com.skydrm.rmc.ui.project.feature.configuration.UpdateNameMsg;
import com.skydrm.rmc.ui.project.service.SwitchProjectActivity;
import com.skydrm.rmc.ui.service.search.SearchActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class ProjectFileControlFragment extends MySpaceBaseFragment {
    private InternalParamBundle mParamBundle;

    public static ProjectFileControlFragment newInstance() {
        return new ProjectFileControlFragment();
    }

    @Override
    protected boolean resolveBundle(Bundle args) {
        mParamBundle = new InternalParamBundle(args);
        return args != null;
    }

    @Override
    protected String getToolBarTitleText() {
        return mParamBundle.projectName;
    }

    @Override
    protected void onToolBarNavigate() {
        Intent intent = new Intent(_activity, SwitchProjectActivity.class);
        intent.putExtra(Constant.KEY, Constant.FLAG_FROM_PROJECT);
        intent.putExtra("project_name", mParamBundle.projectName);
        intent.putExtra("project_id", mParamBundle.projectId);
        startActivity(intent);
    }

    @Override
    protected BaseFragmentStatePagerAdapter createAdapter(FragmentManager fm) {
        return new FileControlPageAdapter(fm);
    }

    @Override
    protected int getOffscreenPageLimit() {
        return mAdapter.getCount();
    }

    @Override
    protected void showCtxMenu() {
        ProjectContextMenu ctxMenu = ProjectContextMenu.newInstance();
        ctxMenu.setProject(mParamBundle.project);
        ctxMenu.setHideGoToAllProjectsMenuItem(true);
        String currentPathId = "/";
        NxlBaseFragment currentFrag = getCurrentFragment();
        if (currentFrag != null) {
            currentPathId = currentFrag.getCurrentPathId();
        }
        ctxMenu.setCurrentPathId(currentPathId);
        ctxMenu.show(getFragmentManager(), ProjectContextMenu.class.getSimpleName());
    }

    @Override
    protected void showSortMenu() {
        Fragment base = mAdapter.getFragment(mCurIdx);
        if (base instanceof ProjectFileBaseFragment) {
            ProjectFileBaseFragment fileBaseFrag = (ProjectFileBaseFragment) base;
            fileBaseFrag.showSortMenu();
        }
    }

    @Override
    protected void lunchSearchPage() {
        Intent i = new Intent(_activity, SearchActivity.class);
        if (mCurIdx == 0 || mCurIdx == 2 || mCurIdx == 3) {
            i.setAction(Constant.ACTION_SEARCH_PROJECT_FILES);
        } else {
            i.setAction(Constant.ACTION_SEARCH_PROJECT_OFFLINE_FILES);
        }
        i.putExtra(Constant.PROJECT_DETAIL, (Parcelable) mParamBundle.project);
        i.putExtra("project_id", mParamBundle.projectId);
        i.putExtra("project_name", mParamBundle.projectName);
        i.putExtra("is_created_by_me", mParamBundle.isCreatedByMe);
        _activity.startActivity(i);
    }

    @Override
    protected boolean isBindEventBusHere() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveUpdateNameMsg(UpdateNameMsg msg) {
        mToolbar.setTitle(msg.name);
    }

    private static class InternalParamBundle {
        private IProject project;
        private int projectId = -1;
        private boolean isCreatedByMe;
        private String projectName;
        private String description;
        private IOwner owner;

        InternalParamBundle(Bundle arguments) {
            if (arguments != null) {
                project = arguments.getParcelable(Constant.PROJECT_DETAIL);
                if (project != null) {
                    projectId = project.getId();
                    isCreatedByMe = project.isOwnedByMe();
                    projectName = project.getName();
                    description = project.getDescription();
                    owner = project.getOwner();
                }
            }
        }
    }

    private class FileControlPageAdapter extends BaseFragmentStatePagerAdapter {

        FileControlPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        protected List<String> getPageTitles() {
            List<String> ret = new ArrayList<>();
            ret.add("All");
            ret.add("Offline");
            ret.add("Shared from this project");
            ret.add("Shared with this project");
            return ret;
        }

        @Override
        protected List<BaseFragment> getFragments() {
            List<BaseFragment> ret = new ArrayList<>();
            FileFragment allFrag = FileFragment.newInstance();
            allFrag.setArguments(getArguments());
            ret.add(allFrag);

            OfflineFragment offlineFrag = OfflineFragment.newInstance();
            offlineFrag.setArguments(getArguments());
            ret.add(offlineFrag);

            SharedByMeFragment byMeFrag = SharedByMeFragment.newInstance();
            byMeFrag.setArguments(getArguments());
            ret.add(byMeFrag);

            SharedWithMeFragment withMeFrag = SharedWithMeFragment.newInstance();
            withMeFrag.setArguments(getArguments());
            ret.add(withMeFrag);
            return ret;
        }
    }
}
