package com.skydrm.rmc.ui.project.feature.files.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Gravity;

import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.base.NxlDoc;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.ui.base.BaseSortMenu;
import com.skydrm.rmc.ui.base.NxlBaseFragment;
import com.skydrm.rmc.ui.common.NxlAdapter;
import com.skydrm.rmc.ui.common.NxlItemHelper;
import com.skydrm.rmc.ui.common.RightMenuItemClickListener;
import com.skydrm.rmc.ui.common.SortMenu;
import com.skydrm.rmc.ui.project.common.FileContextMenu;
import com.skydrm.rmc.ui.project.feature.files.IFileContact;
import com.skydrm.rmc.ui.project.feature.files.ProjectFilePresenter;
import com.skydrm.rmc.ui.project.service.ProjectOperateActivity;
import com.skydrm.rmc.ui.service.share.ShareActivity;
import com.skydrm.rmc.utils.sort.SortType;

public abstract class ProjectFileBaseFragment extends NxlBaseFragment implements IFileContact.IView {
    protected IProject mProject;
    protected FileContextMenu mFileCtxMenu;
    private SortType mSortType = SortType.TIME_DESCEND;
    public SortMenu mSortMenu;

    public NxlAdapter getFileAdapter() {
        return mAdapter;
    }

    @Override
    public void showSortMenu() {
        if (mSortMenu == null) {
            mSortMenu = new SortMenu(_activity);
            mSortMenu.setOnSortItemClickListener(new BaseSortMenu.OnSortItemClickListener() {
                @Override
                public void onSortItemClick(SortType type) {
                    mSortType = type;
                    mPresenter.sort(type);
                }
            });
        }
        mSortMenu.showAtLocation(mRootView, Gravity.BOTTOM, 0, 0);
    }

    @Override
    protected boolean showToolbar() {
        return false;
    }

    @Override
    protected void onToolbarNavigationClick() {

    }

    @Override
    protected boolean resolveBundle(Bundle arguments) {
        if (arguments == null) {
            return false;
        }
        mProject = arguments.getParcelable(Constant.PROJECT_DETAIL);
        return mProject != null;
    }

    @Override
    protected IFileContact.IPresenter createPresenter() {
        return new ProjectFilePresenter(mProject, this, mSortType);
    }

    @Override
    protected void onFileItemClick(INxlFile f, int pos) {
        NxlItemHelper.viewFile(_activity, mProject.getId(), mProject.getName(), f);
    }

    @Override
    protected String getRootPathId() {
        return "/";
    }

    @Override
    protected void initViewAndEvents() {
        super.initViewAndEvents();
        mAdapter.setCreatedByMe(mProject.isOwnedByMe());
        initFileCtxMenu();
    }

    @Override
    protected void initEvents() {
        super.initEvents();
        mAdapter.setOnMenuToggleListener(new NxlAdapter.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(INxlFile f, int pos) {
                mFileCtxMenu.setFile(f);
                mFileCtxMenu.setPosition(pos);
                mFileCtxMenu.show(getFragmentManager(), FileContextMenu.class.getSimpleName());
            }
        });
        mAdapter.setOnLeftMenuItemClickListener(new NxlAdapter.OnLeftMenuItemClickListener() {
            @Override
            public void onButton01Click(INxlFile f, final int pos, int type) {
                deleteFile(f, pos);
            }
        });
        mAdapter.setOnRightMenuItemClickListener(new RightMenuItemClickListener(_activity,
                mProject.getId(), mProject.getName()));
    }

    private void initFileCtxMenu() {
        mFileCtxMenu = FileContextMenu.newInstance();
        mFileCtxMenu.setProjectId(mProject.getId());
        mFileCtxMenu.setProjectName(mProject.getName());
        mFileCtxMenu.setOwnerByMe(mProject.isOwnedByMe());
        mFileCtxMenu.setOnMarkOfflineClickListener(new FileContextMenu.OnMarkOfflineClickListener() {
            @Override
            public void onClick(INxlFile f, boolean offline, int pos) {
                if (offline) {
                    unMarkAsOffline(f, pos);
                } else {
                    markAsOffline(f, pos);
                }
            }
        });
        mFileCtxMenu.setOnShareItemClickListener(new FileContextMenu.OnShareItemClickListener() {
            @Override
            public void onAddToProjectItemClick(INxlFile f, int pos) {
                addToProject(f, pos);
            }

            @Override
            public void onShareToPersonItemClick(INxlFile f, int pos) {
                share(f, pos);
            }
        });
        mFileCtxMenu.setOnModifyRightsClickListener(new FileContextMenu.OnModifyRightsClickListener() {
            @Override
            public void onModifyRights(INxlFile f, int pos) {
                modifyRights(f, pos);
            }
        });
        mFileCtxMenu.setOnDeleteButtonClickListener(new NxlItemHelper.OnDeleteButtonClickListener() {
            @Override
            public void onClick(INxlFile f) {
                mPresenter.delete(f, -1);
            }
        });
    }


    private void addToProject(INxlFile f, int pos) {
        Intent i = new Intent(_activity, ProjectOperateActivity.class);
        i.setAction(Constant.ACTION_LUNCH_PROJECT_SELECT_FRAGMENT);
        i.putExtra(Constant.PROJECT_FILE_ENTRY, (NxlDoc) f);
        i.putExtra(Constant.PROJECT_DETAIL, (Parcelable) mProject);
        _activity.startActivity(i);
    }

    private void shareToPerson(INxlFile f, int pos) {
        Intent i = new Intent(_activity, ProjectOperateActivity.class);
        i.setAction(Constant.ACTION_CHECK_THEN_SHARE_TO_PERSON);
        i.putExtra(Constant.PROJECT_FILE_ENTRY, (NxlDoc) f);
        i.putExtra(Constant.PROJECT_DETAIL, (Parcelable) mProject);
        _activity.startActivity(i);
    }

    protected void share(INxlFile f, int pos) {
        Intent i = new Intent(_activity, ShareActivity.class);
        i.putExtra(Constant.SHARING_SERVICE, (Parcelable) mProject);
        i.putExtra(Constant.SHARING_ENTRY, (Parcelable) f);
        _activity.startActivity(i);
    }

    private void modifyRights(INxlFile f, int pos) {
        Intent i = new Intent(_activity, ProjectOperateActivity.class);
        i.setAction(Constant.ACTION_CHECK_THEN_MODIFY_RIGHTS);
        i.putExtra(Constant.MODIFY_RIGHTS_ENTRY, (NxlDoc) f);
        i.putExtra(Constant.MODIFY_RIGHTS_SERVICE, (Parcelable) mProject);
        _activity.startActivity(i);
    }

}
