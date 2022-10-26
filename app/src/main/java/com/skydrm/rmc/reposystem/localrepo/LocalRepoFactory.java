package com.skydrm.rmc.reposystem.localrepo;

import com.skydrm.rmc.reposystem.ILocalRepo;
import com.skydrm.rmc.reposystem.IWorkingFolderObserver;
import com.skydrm.rmc.reposystem.types.BoundService;

import java.io.File;


public class LocalRepoFactory {
    static public ILocalRepo create(BoundService service, File mountPoint, IWorkingFolderObserver observer) throws Exception {
        switch (service.type) {
            case DROPBOX:
            case SHAREPOINT_ONLINE:
            case SHAREPOINT:
            case ONEDRIVE:
            case GOOGLEDRIVE:
            case MYDRIVE:
            case BOX:
                return new LocalRepoBase(service, mountPoint, observer);
            default:
                throw new RuntimeException("error:this service type dost not supported");
        }
    }
}
