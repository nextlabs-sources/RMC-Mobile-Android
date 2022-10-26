package com.skydrm.rmc.ui.project.service.message;

public class MsgNavigate2TargetFolder {
    public String mPathDisplay;
    public String mPathId;

    public MsgNavigate2TargetFolder(String pathId, String pathDisplay) {
        this.mPathId = pathId;
        this.mPathDisplay = pathDisplay;
    }
}
