package com.skydrm.rmc.ui.workspace;

import android.content.Intent;

import com.skydrm.rmc.ui.base.BaseLibraryActivity;
import com.skydrm.rmc.ui.common.NxlFileType;
import com.skydrm.rmc.ui.project.feature.files.IFileContact;
import com.skydrm.rmc.utils.sort.SortType;

public class WorkSpaceLibraryActivity extends BaseLibraryActivity {
    private String mAction;

    @Override
    protected IFileContact.IPresenter createPresenter() {
        return new WorkSpacePresenter(this, SortType.NAME_ASCEND);
    }

    @Override
    protected boolean resolveIntent(Intent i) {
        if (i == null) {
            return false;
        }
        mAction = i.getAction();
        return true;
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
