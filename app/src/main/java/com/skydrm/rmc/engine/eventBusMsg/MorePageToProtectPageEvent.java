package com.skydrm.rmc.engine.eventBusMsg;

import com.skydrm.rmc.engine.enumData.CmdOperate;
import com.skydrm.rmc.engine.enumData.FileFrom;
import com.skydrm.rmc.reposystem.types.INxFile;

import java.io.File;

/**
 * Created by aning on 5/10/2017.
 */

public class MorePageToProtectPageEvent {
    private File workingFile;
    private INxFile clickFileItem;
    private CmdOperate cmdOperate;
    private boolean bNxlFile;

    public MorePageToProtectPageEvent(File workingFile, INxFile clickFileItem, CmdOperate cmdOperate, boolean bNxlFile) {
        this.workingFile = workingFile;
        this.clickFileItem = clickFileItem;
        this.cmdOperate = cmdOperate;
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

    public boolean isbNxlFile() {
        return bNxlFile;
    }

}
