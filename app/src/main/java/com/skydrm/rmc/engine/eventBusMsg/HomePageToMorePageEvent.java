package com.skydrm.rmc.engine.eventBusMsg;

import com.skydrm.rmc.engine.enumData.FileFrom;
import com.skydrm.rmc.engine.enumData.FileType;
import com.skydrm.rmc.reposystem.types.INxFile;

/**
 * Created by aning on 5/10/2017.
 */

public class HomePageToMorePageEvent {
    private INxFile clickFileItem;
    private FileType fileType;
    private FileFrom fileFrom;

    public HomePageToMorePageEvent(INxFile clickFileItem, FileType fileType, FileFrom fileFrom) {
        this.clickFileItem = clickFileItem;
        this.fileType = fileType;
        this.fileFrom = fileFrom;
    }

    public INxFile getClickFileItem() {
        return clickFileItem;
    }

    public FileType getFileType() {
        return fileType;
    }

    public FileFrom getFileFrom() {
        return fileFrom;
    }

}
