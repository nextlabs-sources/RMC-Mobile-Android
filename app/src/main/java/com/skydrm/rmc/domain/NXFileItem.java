package com.skydrm.rmc.domain;

import com.skydrm.rmc.reposystem.types.INxFile;

/**
 * item in for recycler view list
 */
public class NXFileItem {
    private INxFile mNXFile;
    private String mTitle;
    private boolean selected;

    public NXFileItem(INxFile nxFile, String title) {
        mNXFile = nxFile;
        mTitle = title;
    }

    public INxFile getNXFile() {
        return mNXFile;
    }

    public void setNXFile(INxFile nxFile) {
        mNXFile = nxFile;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
