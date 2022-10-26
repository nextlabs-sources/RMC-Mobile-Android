package com.skydrm.rmc.ui.project.feature.files.view;

import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.ui.common.NxlFileType;

public class SharedWithMeFragment extends ProjectFileBaseFragment {

    public static SharedWithMeFragment newInstance() {
        return new SharedWithMeFragment();
    }

    @Override
    protected int getFileType() {
        return NxlFileType.SHARED_WITH_ME.getValue();
    }

    @Override
    protected void onFolderClick(INxlFile f, int pos) {

    }

    @Override
    protected boolean isBindEventBusHere() {
        return false;
    }
}
