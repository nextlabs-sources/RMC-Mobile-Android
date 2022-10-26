package com.skydrm.rmc.ui.activity.home;

import com.skydrm.rmc.ui.service.protect.IProtectService;

import java.io.File;

public class PhotographMsg {
    public File mPhoto;
    public String mPathId;
    public IProtectService mService;

    public PhotographMsg(File f) {
        this.mPhoto = f;
    }

    public PhotographMsg(File f, String pathId, IProtectService service) {
        this.mPhoto = f;
        this.mPathId = pathId;
        this.mService = service;
    }
}
