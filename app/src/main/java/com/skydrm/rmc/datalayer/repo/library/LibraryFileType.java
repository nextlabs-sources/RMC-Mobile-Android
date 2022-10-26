package com.skydrm.rmc.datalayer.repo.library;

import com.skydrm.rmc.datalayer.repo.base.IFileType;

import java.io.File;

public class LibraryFileType implements IFileType {
    private File mFile;

    LibraryFileType(File f) {
        this.mFile = f;
    }

    public String getName() {
        return mFile.getName();
    }

    public long getFileSize() {
        return mFile.length();
    }

    public long getLastModified() {
        return mFile.lastModified();
    }

    public long getCreationTime() {
        return mFile.lastModified();
    }

    public String getLocalPath() {
        return mFile.getPath();
    }

    @Override
    public String getPathId() {
        return mFile.getPath();
    }

    @Override
    public boolean isFolder() {
        return mFile.isDirectory();
    }
}
