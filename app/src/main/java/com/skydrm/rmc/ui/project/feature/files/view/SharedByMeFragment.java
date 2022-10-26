package com.skydrm.rmc.ui.project.feature.files.view;

import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.ui.common.NxlFileType;

public class SharedByMeFragment extends ProjectFileBaseFragment {

    public static SharedByMeFragment newInstance() {
        return new SharedByMeFragment();
    }

    @Override
    protected int getFileType() {
        return NxlFileType.SHARED_BY_ME.getValue();
    }

    @Override
    protected void onFolderClick(INxlFile f, int pos) {

    }

    @Override
    protected boolean isBindEventBusHere() {
        return false;
    }

}
