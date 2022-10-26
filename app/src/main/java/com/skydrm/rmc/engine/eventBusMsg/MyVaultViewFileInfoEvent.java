package com.skydrm.rmc.engine.eventBusMsg;

import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.engine.enumData.FileFrom;
import com.skydrm.rmc.engine.enumData.FileType;

import java.io.File;

/**
 * Created by aning on 5/24/2017.
 */

public class MyVaultViewFileInfoEvent {
    private INxlFile myVaultFileEntry;
    private FileFrom fileFrom;
    private FileType fileType;
    private File workingFile;
    private boolean offline;

    public MyVaultViewFileInfoEvent(INxlFile myVaultFileEntry, FileType fileType, FileFrom fileFrom) {
        this.myVaultFileEntry = myVaultFileEntry;
        this.fileType = fileType;
        this.fileFrom = fileFrom;
    }

    public File getWorkingFile() {
        return workingFile;
    }

    public void setWorkingFile(File workingFile) {
        this.workingFile = workingFile;
    }

    public FileType getFileType() {
        return fileType;
    }

    public FileFrom getFileFrom() {
        return fileFrom;
    }

    public INxlFile getMyVaultFileEntry() {
        return myVaultFileEntry;
    }

    public boolean isOffline() {
        return offline;
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
    }
}
