package com.skydrm.rmc.ui.common;

import com.skydrm.rmc.datalayer.repo.base.INxlFile;

public class LeftMenuItemClickListener implements NxlAdapter.OnLeftMenuItemClickListener {
    @Override
    public void onButton01Click(INxlFile f, int pos, int type) {
        if (type == NxlAdapter.TYPE_DELETE) {
            deleteFile(f, pos);
        }
    }

    private void deleteFile(INxlFile f, int pos) {

    }
}
