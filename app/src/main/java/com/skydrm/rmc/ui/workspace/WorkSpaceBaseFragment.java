package com.skydrm.rmc.ui.workspace;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;

import com.skydrm.rmc.datalayer.RepoFactory;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.base.NxlDoc;
import com.skydrm.rmc.datalayer.repo.base.RepoType;
import com.skydrm.rmc.datalayer.repo.workspace.WorkSpaceRepo;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.ui.base.BaseSortMenu;
import com.skydrm.rmc.ui.base.NxlBaseFragment;
import com.skydrm.rmc.ui.common.NxlAdapter;
import com.skydrm.rmc.ui.common.RightMenuItemClickListener;
import com.skydrm.rmc.ui.common.SortMenu;
import com.skydrm.rmc.ui.project.feature.files.IFileContact;
import com.skydrm.rmc.ui.project.service.ProjectOperateActivity;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.rmc.utils.sort.SortType;

public abstract class WorkSpaceBaseFragment extends NxlBaseFragment implements IFileContact.IView {
    private SortType mSortType = SortType.TIME_DESCEND;
    private SortMenu mSortMenu;
    private WorkSpaceFileContextMenu mFileCtxMenu;

    protected abstract boolean isOfflineDisplay();
    
    @Override
    public void showSortMenu() {
        if (isActive()) {
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
    }

    @Override
    protected boolean resolveBundle(Bundle arguments) {
        return true;
    }

    @Override
    protected IFileContact.IPresenter createPresenter() {
        return new WorkSpacePresenter(this, mSortType);
    }

    @Override
    protected void initViewAndEvents() {
        super.initViewAndEvents();
        initFileCtxMenu();
    }

    private void initFileCtxMenu() {
        mFileCtxMenu = WorkSpaceFileContextMenu.newInstance();
        mFileCtxMenu.setOfflineView(isOfflineDisplay());
        mFileCtxMenu.setOnShareItemClickListener(new WorkSpaceFileContextMenu.OnShareItemClickListener() {
            @Override
            public void onAddToProject(INxlFile f, int pos) {
                ToastUtil.showToast(_activity, "AddToProject");
            }
        });
        mFileCtxMenu.setOnMarkOfflineClickListener(new WorkSpaceFileContextMenu.OnMarkOfflineClickListener() {
            @Override
            public void onClick(INxlFile f, boolean offline, int pos) {
                if (offline) {
                    unMarkAsOffline(f, pos);
                } else {
                    markAsOffline(f, pos);
                }
            }
        });
        mFileCtxMenu.setOnModifyRightsClickListener(new WorkSpaceFileContextMenu.OnModifyRightsClickListener() {
            @Override
            public void onModifyRights(INxlFile f, int pos) {
                modifyRights(f, pos);
            }
        });
        mFileCtxMenu.setOnDeleteButtonClickListener(new WorkSpaceFileContextMenu.OnDeleteButtonClickListener() {
            @Override
            public void onClick(INxlFile f, int pos) {
                deleteFile(f, pos);
            }
        });
    }

    @Override
    protected void initEvents() {
        super.initEvents();
        mAdapter.setOnMenuToggleListener(new NxlAdapter.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(INxlFile f, int pos) {
                showFileCtxMenu(f, pos);
            }
        });
        mAdapter.setOnLeftMenuItemClickListener(new NxlAdapter.OnLeftMenuItemClickListener() {
            @Override
            public void onButton01Click(INxlFile f, final int pos, int type) {
                deleteFile(f, pos);
            }
        });
        mAdapter.setOnRightMenuItemClickListener(new RightMenuItemClickListener(_activity));
    }

    private void showFileCtxMenu(INxlFile f, int pos) {
        mFileCtxMenu.setFile(f);
        mFileCtxMenu.setPosition(pos);
        mFileCtxMenu.show(getFragmentManager(), WorkSpaceFileContextMenu.class.getSimpleName());
    }

    private void modifyRights(INxlFile f, int pos) {
        Intent i = new Intent(_activity, ProjectOperateActivity.class);
        i.setAction(Constant.ACTION_CHECK_THEN_MODIFY_RIGHTS);
        i.putExtra(Constant.MODIFY_RIGHTS_ENTRY, (NxlDoc) f);
        i.putExtra(Constant.MODIFY_RIGHTS_SERVICE, (WorkSpaceRepo) RepoFactory.getRepo(RepoType.TYPE_WORKSPACE));
        _activity.startActivity(i);
    }
}
