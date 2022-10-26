package com.skydrm.rmc.engine.eventBusMsg;

import com.skydrm.rmc.engine.enumData.CmdOperate;
import com.skydrm.rmc.engine.enumData.FileFrom;
import com.skydrm.rmc.reposystem.types.INxFile;

import java.io.File;

/**
 * Created by aning on 5/10/2017.
 */

public class ViewPageToSharePageEvent {
    private File workingFile;
    private INxFile clickFileItem;
    private CmdOperate cmdOperate;
    private FileFrom fileFrom;
    private boolean bNxlFile;

    public ViewPageToSharePageEvent(File workingFile, INxFile clickFileItem, CmdOperate cmdOperate, FileFrom fileFrom, boolean bNxlFile) {
        this.workingFile = workingFile;
        this.clickFileItem = clickFileItem;
        this.cmdOperate = cmdOperate;
        this.fileFrom = fileFrom;
        this.bNxlFile = bNxlFile;
    }

    public File getWorkingFile() {
        return workingFile;
    }

    public INxFile getClickFileItem() {
        return clickFileItem;
    }

    public CmdOperate getCmdOperate() {
        return cmdOperate;
    }

    public FileFrom getFileFrom() {
        return fileFrom;
    }

    public boolean isbNxlFile() {
        return bNxlFile;
    }

}
