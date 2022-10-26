package com.skydrm.rmc.reposystem.remoterepo;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.reposystem.IRemoteRepo;
import com.skydrm.rmc.reposystem.remoterepo.box.NxBox;
import com.skydrm.rmc.reposystem.remoterepo.dropbox.NxDropBox2;
//import com.skydrm.rmc.reposystem.remoterepo.googledrive2.NxGoogleDrive2;
import com.skydrm.rmc.reposystem.remoterepo.googledrive2.NxGoogleDrive3;
import com.skydrm.rmc.reposystem.remoterepo.mydrive.MyDriveSDK;
//import com.skydrm.rmc.reposystem.remoterepo.onedrive2.NxOneDrive2;
//import com.skydrm.rmc.reposystem.remoterepo.sharepoint.NXSharePoint;
import com.skydrm.rmc.reposystem.remoterepo.onedrive2.NxOneDrive3;
import com.skydrm.rmc.reposystem.remoterepo.sharepointonline.NXSharePointOnPremise;
import com.skydrm.rmc.reposystem.remoterepo.sharepointonline.NXSharePointOnline;
import com.skydrm.rmc.reposystem.remoterepo.sharepointonline.NXSharePointOnline2;
import com.skydrm.rmc.reposystem.types.BoundService;

import javax.annotation.Nonnull;

/**
 * This class is designed as a factory to create supported remote repo sites,including:
 * -dropbox
 * -sharepoint online
 * -sharepoint
 * -onedrive
 * -google drive
 * Notice: user must not depend on the concrete class ,used IServiceOperation instead
 */
public class RemoteRepoFactory {
    public static
    @Nonnull
    IRemoteRepo create(BoundService service) throws Exception {
        IRemoteRepo rt;
        switch (service.type) {
            case DROPBOX:
                rt = new NxDropBox2(service.accountToken);
                break;
            case ONEDRIVE:
                rt = new NxOneDrive3(service.accountToken);
                break;
            case GOOGLEDRIVE:
                rt = new NxGoogleDrive3(service.accountToken);
                break;
            case MYDRIVE:
                rt = new MyDriveSDK(SkyDRMApp.getInstance().getSession().getRmsRestAPI(),
                        SkyDRMApp.getInstance().getSession().getRmUser());
                break;
            case BOX:
                rt = new NxBox(service.accountToken);
                break;
            case SHAREPOINT:
                rt = new NXSharePointOnPremise(service.accountID,service.accountToken);
//                rt = new NXSharePoint(service.accountID, service.account, service.accountToken);
                break;
            case SHAREPOINT_ONLINE:
//                rt = new NXSharePointOnline(service.account, service.accountID, service.accountToken);
                rt = new NXSharePointOnline2(service);
                break;
//            case SHAREPOINT_ONPREMISE:
//                rt = new NXSharePointOnPremise(service.account, service.accountName, service.accountToken);
//                break;
            default:
                throw new RuntimeException("error:this service type dost not supported");
        }
        return rt;
    }
}
