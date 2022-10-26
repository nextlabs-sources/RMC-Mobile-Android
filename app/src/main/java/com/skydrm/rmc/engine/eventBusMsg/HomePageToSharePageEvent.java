package com.skydrm.rmc.engine.eventBusMsg;

import com.skydrm.rmc.engine.enumData.CmdOperate;
import com.skydrm.rmc.engine.enumData.FileFrom;
import com.skydrm.rmc.reposystem.types.INxFile;

/**
 * Created by aning on 5/10/2017.
 */

public class HomePageToSharePageEvent {
    private INxFile clickFileItem;
    private CmdOperate cmdOperate;

    public HomePageToSharePageEvent(INxFile clickFileItem, CmdOperate cmdOperate) {
        this.clickFileItem = clickFileItem;
        this.cmdOperate = cmdOperate;
    }

    public INxFile getClickFileItem() {
        return clickFileItem;
    }

    public CmdOperate getCmdOperate() {
        return cmdOperate;
    }

}
