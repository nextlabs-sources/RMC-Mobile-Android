package com.skydrm.rmc.ui.common;

import com.skydrm.rmc.datalayer.repo.base.INxlFile;

public class NxlFileItem {
    private INxlFile nxlFile;
    private String title;

    public NxlFileItem(INxlFile nxlFile, String title) {
        this.nxlFile = nxlFile;
        this.title = title;
    }

    public INxlFile getNxlFile() {
        return nxlFile;
    }

    public String getTitle() {
        return title;
    }
}
