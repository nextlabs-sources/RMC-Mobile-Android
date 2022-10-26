package com.skydrm.rmc.domain.impl;

import com.skydrm.rmc.domain.ILocalFile;
import com.skydrm.rmc.utils.sort.IBaseSortable;

import java.io.File;
import java.io.Serializable;

/**
 * Created by hhu on 12/22/2016.
 */

public class LocalFileImpl implements ILocalFile, Serializable, IBaseSortable {
    private File file;
    private boolean isChecked;
    private boolean visible;
    private boolean isFolder;
    private int color;
    private boolean cached;

    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public void setSelected(boolean checked) {
        isChecked = checked;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setFolder(boolean folder) {
        this.isFolder = folder;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public boolean isChecked() {
        return isChecked;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public String getSortableName() {
        return file.getName();
    }

    @Override
    public long getSortableSize() {
        return file.length();
    }

    @Override
    public long getSortableTime() {
        return file.lastModified();
    }

    @Override
    public boolean isFolder() {
        return isFolder;
    }

    @Override
    public boolean cached() {
        return false;
    }

    @Override
    public void setCached(boolean cache) {
        this.cached = cache;
    }

    @Override
    public int getColor() {
        return color;
    }
}
