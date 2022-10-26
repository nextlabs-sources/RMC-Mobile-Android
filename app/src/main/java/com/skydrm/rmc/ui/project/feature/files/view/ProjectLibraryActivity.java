package com.skydrm.rmc.ui.project.feature.files.view;

import android.content.Intent;

import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.ui.base.BaseLibraryActivity;
import com.skydrm.rmc.ui.common.NxlFileType;
import com.skydrm.rmc.ui.project.feature.files.IFileContact;
import com.skydrm.rmc.ui.project.feature.files.ProjectFilePresenter;
import com.skydrm.rmc.utils.sort.SortType;

public class ProjectLibraryActivity extends BaseLibraryActivity implements IFileContact.IView {

    private String mAction;
    private IProject mProject;

    @Override
    protected boolean resolveIntent(Intent i) {
        if (i == null) {
            return false;
        }
        mAction = i.getAction();
        if (mAction == null || mAction.isEmpty()) {
            return false;
        }
        mProject = i.getParcelableExtra(Constant.PROJECT_DETAIL);

        return mProject != null;
    }

    @Override
    protected IFileContact.IPresenter createPresenter() {
        return new ProjectFilePresenter(mProject, this, SortType.NAME_ASCEND);
    }

    @Override
    protected int getFileType() {
        return NxlFileType.ALL.getValue();
    }

    @Override
    protected String getAction() {
        return mAction;
    }

}
