package com.skydrm.rmc.reposystem;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.engine.eventBusMsg.account.UserLinkedRepoChangedEvent;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.reposystem.types.RmsRepoInfo;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.types.RmsAddRepoResult;
import com.skydrm.sdk.rms.types.RmsUserLinkedRepos;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Background Task call RMS-SDK:repositoryGet to get all repos
 * UI task, notify caller each events
 * - add support for EventBus, when sync ok, send event onto bus
 */
public final class UserReposSyncPolicy {
    // Msg
    private static final int MESSAGE_POST_PROGRESS = 0x2;
    // msg_result
    private static final int MESSAGE_POST_SYNC_DONE = MESSAGE_POST_PROGRESS + 1;
    private static final int MESSAGE_POST_SYNC_FAILED = MESSAGE_POST_SYNC_DONE + 1;
    private static DevLog log = new DevLog(UserReposSyncPolicy.class.getSimpleName());
    private static SkyDRMApp app = SkyDRMApp.getInstance();

    private static UIListener sListener = new UIListener() {
        @Override
        public void progressing(String msg) {
        }

        @Override
        public void result(boolean status) {
        }
    };

    private static InternalHandler sHandler = new InternalHandler();
    private static Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            // get all repos maintained by rms
            try {
                sHandler.sendProgressMessage("syncing with server");
                SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
                List<RmsRepoInfo> repos = RmsRepoInfo.fromResultBean(session.getRmsRestAPI()
                        .getRepositoryService(session.getRmUser()).repositoryGet());
                // as RMS required we must fetch all repos access token individually,
                // I don't like this fucking code
                for (RmsRepoInfo r : repos) {
                    // filter out S3 as mydrive
//                    if (TextUtils.equals(r.rmsType, RmsRepoInfo.TYPE_NEXTLABS_MYDRIVE)) {
//                        continue;
//                    }
                    if (RmsRepoInfo.matchMyDrive(r)) {
                        continue;
                    }
                    //filter out SharePoint OnPremise
                    if (TextUtils.equals(r.rmsType, RmsRepoInfo.TYPE_SHAREPOINT)) {
                        continue;
                    }
                    try {
                        r.rmsToken = session.getRmsRestAPI()
                                .getRepositoryService(session.getRmUser())
                                .getAccessTokenByRepoID(r.rmsRepoId);
                    } catch (Exception e) {
                        log.e(e);
                    }
                }
                sHandler.sendProgressMessage("analyze result");
                // update policy
                if (!repos.isEmpty()) {
                    analyzeResult(repos);
                } else {
                    policyRmsEmptyContent();
                }
                sHandler.sendEmptyMessage(MESSAGE_POST_SYNC_DONE);
                EventBus.getDefault().post(new UserLinkedRepoChangedEvent());
            } catch (Exception e) {
                log.e(e);
                sHandler.sendEmptyMessage(MESSAGE_POST_SYNC_FAILED);
            } finally {
                // do release
            }
        }
    };
    private static long syncedMillis = 0;

    public static long getSyncedMillis() {
        return syncedMillis;
    }

    private static void setSyncedMillis(long syncedMillis) {
        UserReposSyncPolicy.syncedMillis = syncedMillis;
    }

    public static void syncing(@NonNull Executor executor, @NonNull UIListener listener) {
        sListener = listener;
        executor.execute(mRunnable);
    }

    private static void analyzeResult(List<RmsRepoInfo> rmsRepos) {
        List<BoundService> locals = SkyDRMApp.getInstance().getUserLinkedRepos();
        //
        // find all wanting add repos
        //
        List<RmsRepoInfo> addingDropboxs = new ArrayList<>();
        List<RmsRepoInfo> addingOneDrives = new ArrayList<>();
        List<RmsRepoInfo> addingGoogleDrives = new ArrayList<>();
        List<RmsRepoInfo> addingSharePointOnlines = new ArrayList<>();
        List<RmsRepoInfo> addingBoxes = new ArrayList<>();
        List<RmsRepoInfo> addingSharePoints = new ArrayList<>();
        //
        // find all wanting update repos
        //
        List<BoundService> updateRepos = new ArrayList<>();
        //
        // find all wanting delete repos
        //
        List<BoundService> deletingDropboxs = new ArrayList<>();
        List<BoundService> deletingOnedrives = new ArrayList<>();
        List<BoundService> deletingGoogleDrives = new ArrayList<>();
        List<BoundService> deletingSharepointOnlines = new ArrayList<>();
        List<BoundService> deletingBoxes = new ArrayList<>();
        List<BoundService> deletingSharePoints = new ArrayList<>();
        //
        // analyze each rms-repo,
        //      if it need to insert into locals
        //      if the corresponding local one needs to update
        //
        for (RmsRepoInfo r : rmsRepos) {
            // extract dropbox, other services can follow this code-block
            if (RmsRepoInfo.matchDropBox(r)) {
                // this r dose not have same meanings item in Locals ,
                // reverse thinking, if match one, no need to insert,
                boolean needInsertDropBox = true;
                // trying to find r,s is the some one and need to be updated
                for (BoundService s : locals) {
                    if (BoundService.matchDropbox(s) && matchDropbox(s, r)) {
                        needInsertDropBox = false;
                        if (isModifiedRepoInfo(s, r)) {
                            updateRepos.add(s);
                        }
                        break;
                    }
                }
                if (needInsertDropBox) {
                    addingDropboxs.add(r);
                }
            } else if (RmsRepoInfo.matchOneDrive(r)) {
                boolean needInsertOnedrive = true;
                for (BoundService s : locals) {
                    if (BoundService.matchOnedrive(s) && matchOnedrive(s, r)) {
                        needInsertOnedrive = false;
                        if (isModifiedRepoInfo(s, r)) {
                            updateRepos.add(s);
                        }
                        break;
                    }
                }
                if (needInsertOnedrive) {
                    addingOneDrives.add(r);
                }


            } else if (RmsRepoInfo.matchSharepointOnline(r)) {
                boolean needInsertSharepointOnline = true;
                for (BoundService s : locals) {
                    if (BoundService.matchSharepointOnline(s) && matchSharepointOnline(s, r)) {
                        needInsertSharepointOnline = false;
                        if (isModifiedRepoInfo(s, r)) {
                            updateRepos.add(s);
                        }
                        break;
                    }
                }
                if (needInsertSharepointOnline) {
                    addingSharePointOnlines.add(r);
                }
            } else if (RmsRepoInfo.matchGoogleDrive(r)) {
                boolean needInsertGoogleDrive = true;
                for (BoundService s : locals) {
                    if (BoundService.matchGoogleDrive(s) && matchGoogleDrive(s, r)) {
                        needInsertGoogleDrive = false;
                        if (isModifiedRepoInfo(s, r)) {
                            updateRepos.add(s);
                        }
                        break;
                    }
                }
                if (needInsertGoogleDrive) {
                    addingGoogleDrives.add((r));
                }
            } else if (RmsRepoInfo.matchBox(r)) {
                boolean needInsertBox = true;
                for (BoundService s : locals) {
                    if (BoundService.matchBox(s) && matchBox(s, r)) {
                        needInsertBox = false;
                        if (isModifiedRepoInfo(s, r)) {
                            updateRepos.add(s);
                        }
                        break;
                    }
                }
                if (needInsertBox) {
                    addingBoxes.add(r);
                }
            } else if (RmsRepoInfo.matchSharePoint(r)) {
                boolean needInsertSharePoint = true;
                for (BoundService s : locals) {
                    if (BoundService.matchSharePoint(s) && matchSharePoint(s, r)) {
                        needInsertSharePoint = false;
                        if (isModifiedRepoInfo(s, r)) {
                            updateRepos.add(s);
                        }
                        break;
                    }
                }
                if (needInsertSharePoint) {
                    addingSharePoints.add(r);
                }
            }
        }

        sHandler.sendProgressMessage("working...");
        // result
        if (!addingDropboxs.isEmpty()) {
            policyNewAddedDropboxRepos(addingDropboxs);
        }
        if (!addingOneDrives.isEmpty()) {
            policyNewAddedOnedriveRepos(addingOneDrives);
        }
        if (!addingSharePointOnlines.isEmpty()) {
            policyNewAddedSharepointOnlineRepos(addingSharePointOnlines);
        }
        if (!addingGoogleDrives.isEmpty()) {
            policyNewAddedGoogleDriveRepos(addingGoogleDrives);
        }
        if (!addingBoxes.isEmpty()) {
            policyNewAddedBoxRepos(addingBoxes);
        }
        if (!addingSharePoints.isEmpty()) {
            policyNewAddedSharePointRepos(addingSharePoints);
        }

        if (!updateRepos.isEmpty()) {
            policyUpdateRepos(updateRepos);
        }


        for (BoundService s : locals) {
            if (BoundService.matchDropbox(s)) {
                boolean needDelDrpobox = true;
                for (RmsRepoInfo r : rmsRepos) {
                    if (RmsRepoInfo.matchDropBox(r) && matchDropbox(s, r)) {
                        needDelDrpobox = false;
                        break;
                    }
                }
                if (needDelDrpobox) {
                    deletingDropboxs.add(s);
                }
            } else if (BoundService.matchOnedrive(s)) {
                boolean needDelOnedrive = true;
                for (RmsRepoInfo r : rmsRepos) {
                    if (RmsRepoInfo.matchOneDrive(r) && matchOnedrive(s, r)) {
                        needDelOnedrive = false;
                        break;
                    }
                }
                if (needDelOnedrive) {
                    deletingOnedrives.add(s);
                }
            } else if (BoundService.matchSharepointOnline(s)) {
                boolean needDelSharepointOnline = true;
                for (RmsRepoInfo r : rmsRepos) {
                    if (RmsRepoInfo.matchSharepointOnline(r) && matchSharepointOnline(s, r)) {
                        needDelSharepointOnline = false;
                        break;
                    }
                }
                if (needDelSharepointOnline) {
                    deletingSharepointOnlines.add(s);
                }
            } else if (BoundService.matchGoogleDrive(s)) {
                boolean needDelGoogleDrive = true;
                for (RmsRepoInfo r : rmsRepos) {
                    if (RmsRepoInfo.matchGoogleDrive(r) && matchGoogleDrive(s, r)) {
                        needDelGoogleDrive = false;
                        break;
                    }
                }
                if (needDelGoogleDrive) {
                    deletingGoogleDrives.add(s);
                }
            } else if (BoundService.matchBox(s)) {
                boolean needDelBox = true;
                for (RmsRepoInfo r : rmsRepos) {
                    if (RmsRepoInfo.matchBox(r) && matchBox(s, r)) {
                        needDelBox = false;
                        break;
                    }
                }
                if (needDelBox) {
                    deletingBoxes.add(s);
                }
            } else if (BoundService.matchSharePoint(s)) {
                boolean needDelSharePoint = true;
                for (RmsRepoInfo r : rmsRepos) {
                    if (RmsRepoInfo.matchSharePoint(r) && matchSharePoint(s, r)) {
                        needDelSharePoint = false;
                        break;
                    }
                }
                if (needDelSharePoint) {
                    deletingSharePoints.add(s);
                }
            }
        }

        sHandler.sendProgressMessage("working...");
        if (!deletingDropboxs.isEmpty()) {
            policyDelDropBoxRepos(deletingDropboxs);
        }
        if (!deletingOnedrives.isEmpty()) {
            policyDelOnedriveRepos(deletingOnedrives);
        }
        if (!deletingSharepointOnlines.isEmpty()) {
            policyDelSharepointOnlineRepos(deletingSharepointOnlines);
        }
        if (!deletingGoogleDrives.isEmpty()) {
            policyDelGoogleDriveRepos(deletingGoogleDrives);
        }
        if (!deletingBoxes.isEmpty()) {
            policyDelBoxRepos(deletingBoxes);
        }
        if (!deletingSharePoints.isEmpty()) {
            policyDelSharePointRepos(deletingSharePoints);
        }
    }

    private static boolean isModifiedRepoInfo(BoundService s, RmsRepoInfo r) {
        boolean rt = false;
        if (!TextUtils.equals(s.rmsNickName, r.rmsName)) {
            s.rmsNickName = r.rmsName;
            rt = true;
        }
        if (!TextUtils.equals(s.rmsToken, r.rmsToken)) {
            s.accountToken = r.rmsToken;
            s.rmsToken = r.rmsToken;
            //update data base
            app.dbUpdateRepoToken(s);
            rt = true;
        }
        //  an exceptional case, RMS update the RMSREPO_ID
        if (!TextUtils.equals(s.rmsRepoId, r.rmsRepoId)) {
            s.rmsRepoId = r.rmsRepoId;
            app.dbUpdateRepoRmsId(s);
        }
        return rt;
    }

    /*
        for item by local and rms is actually the same one
     */
    private static boolean matchDropbox(BoundService s, RmsRepoInfo r) {
        if (s.type != BoundService.ServiceType.DROPBOX) {
            return false;
        }
        if (!TextUtils.equals(r.rmsType, RmsRepoInfo.TYPE_DROPBOX)) {
            return false;
        }
        if (!TextUtils.equals(s.account, r.rmsAccountName)) {
            return false;
        }
        if (!TextUtils.equals(s.accountID, r.rmsAccountId))
            return false;
        return true;
    }

    private static boolean matchOnedrive(BoundService s, RmsRepoInfo r) {
        if (s.type != BoundService.ServiceType.ONEDRIVE) {
            return false;
        }
        if (!TextUtils.equals(r.rmsType, RmsRepoInfo.TYPE_ONEDRIVE)) {
            return false;
        }
        // shit RMS mangole the accountName, instead use DisplayName
//            if (!TextUtils.equals(s.account, r.rmsAccountName)) {
//                return false;
//            }
        if (!TextUtils.equals(s.accountID, r.rmsAccountId)) {
            return false;
        }

        return true;
    }

    private static boolean matchSharepointOnline(BoundService s, RmsRepoInfo r) {
        if (s.type != BoundService.ServiceType.SHAREPOINT_ONLINE) {
            return false;
        }
        if (!TextUtils.equals(r.rmsType, RmsRepoInfo.TYPE_SHAREPOINT_ONLINE)) {
            return false;
        }
        if (!TextUtils.equals(s.account, r.rmsAccountName)) {
            return false;
        }
        if (!TextUtils.equals(s.accountID, r.rmsAccountId)) {
            return false;
        }
        return true;
    }

    private static boolean matchGoogleDrive(BoundService s, RmsRepoInfo r) {
        if (s.type != BoundService.ServiceType.GOOGLEDRIVE) {
            return false;
        }
        if (!TextUtils.equals(r.rmsType, RmsRepoInfo.TYPE_GOOGLEDRIVE)) {
            return false;
        }
        if (!TextUtils.equals(s.account, r.rmsAccountName)) {
            return false;
        }
        if (!TextUtils.equals(s.accountID, r.rmsAccountId)) {
            return false;
        }
        return true;
    }


    private static boolean matchBox(BoundService s, RmsRepoInfo r) {
        if (s.type != BoundService.ServiceType.BOX) {
            return false;
        }
        if (!TextUtils.equals(r.rmsType, RmsRepoInfo.TYPE_BOX)) {
            return false;
        }
        if (!TextUtils.equals(s.account, r.rmsAccountName)) {
            return false;
        }
        if (!TextUtils.equals(s.accountID, r.rmsAccountId))
            return false;
        return true;
    }

    private static boolean matchSharePoint(BoundService s, RmsRepoInfo r) {
        if (s.type != BoundService.ServiceType.SHAREPOINT) {
            return false;
        }
        if (!TextUtils.equals(r.rmsType, RmsRepoInfo.TYPE_SHAREPOINT)) {
            return false;
        }
//        if (!TextUtils.equals(s.accountName, r.rmsAccountName)) {
//            return false;
//        }
        if (!TextUtils.equals(s.accountID, r.rmsAccountId))
            return false;
        return true;
    }

    private static void policyNewAddedDropboxRepos(List<RmsRepoInfo> rmsRepos) {
        // sanity check
        if (rmsRepos == null || rmsRepos.isEmpty()) {
            throw new RuntimeException("expect at least one item");
        }
        for (RmsRepoInfo r : rmsRepos) {
            // chang RmsRepoInfo into BoundService compitable
            BoundService s = new BoundService(
                    BoundService.ServiceType.DROPBOX,
                    BoundService.DROPBOX,
                    r.rmsAccountName,
                    r.rmsAccountId,
                    r.rmsToken,
                    1,
                    r.rmsRepoId,
                    r.rmsName,
                    r.rmsIsShared,
                    r.rmsToken,
                    r.rmsPreference,
                    r.rmsCreationTime
            );
            // add this item into db
            app.dbInsertRepo(s);
            // activate this repo
            try {
                app.getRepoSystem().activateRepo(s);
            } catch (Exception e) {
                log.e(e);
            }
        }
    }

    private static void policyDelDropBoxRepos(List<BoundService> dropboxRepos) {
        // sanity check
        if (dropboxRepos == null || dropboxRepos.isEmpty()) {
            throw new RuntimeException("expect at least one item");
        }
        //
        for (BoundService s : dropboxRepos) {
            try {
                app.getRepoSystem().detach(s);
                app.dbDelRepo(s);
            } catch (Exception e) {
                Toast.makeText(app, "Exception: remove this service's local files", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private static void policyNewAddedOnedriveRepos(List<RmsRepoInfo> rmsRepos) {
        // sanity check
        if (rmsRepos == null || rmsRepos.isEmpty()) {
            throw new RuntimeException("expect at least one item");
        }
        for (RmsRepoInfo r : rmsRepos) {
            BoundService s = new BoundService(
                    BoundService.ServiceType.ONEDRIVE,
                    BoundService.ONEDRIVE,
                    r.rmsAccountName,
                    r.rmsAccountId,
                    r.rmsToken,
                    1,
                    r.rmsRepoId,
                    r.rmsName,
                    r.rmsIsShared,
                    r.rmsToken,
                    r.rmsPreference,
                    r.rmsCreationTime
            );
            // add this itme into db
            app.dbInsertRepo(s);

            // activate this repo
            try {
                app.getRepoSystem().activateRepo(s);
            } catch (Exception e) {
                log.e(e);
            }
        }

    }

    private static void policyDelOnedriveRepos(List<BoundService> onddriveRepos) {
        // sanity check
        if (onddriveRepos == null || onddriveRepos.isEmpty()) {
            throw new RuntimeException("expect at least one item");
        }
        for (BoundService s : onddriveRepos) {

            try {
                app.getRepoSystem().detach(s);
                app.dbDelRepo(s);
            } catch (Exception e) {
                Toast.makeText(app, "Exception: remove this service's local files", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private static void policyNewAddedSharepointOnlineRepos(List<RmsRepoInfo> rmsRepos) {
        // sanity check
        if (rmsRepos == null || rmsRepos.isEmpty()) {
            throw new RuntimeException("expect at least one item");
        }
        for (RmsRepoInfo r : rmsRepos) {
            // chang RmsRepoInfo into BoundService compitable
            BoundService s = new BoundService(
                    BoundService.ServiceType.SHAREPOINT_ONLINE,
                    BoundService.SHAREPOINT_ONLINE,
                    r.rmsAccountName,
                    r.rmsAccountId,
                    r.rmsToken,
                    0,
                    r.rmsRepoId,
                    r.rmsName,
                    r.rmsIsShared,
                    r.rmsToken,
                    r.rmsPreference,
                    r.rmsCreationTime
            );
            app.dbInsertRepo(s);

            // activate this repo
            try {
                app.getRepoSystem().activateRepo(s);
            } catch (Exception e) {
                log.e(e);
            }

        }
    }

    private static void policyDelSharepointOnlineRepos(List<BoundService> sharepointOnlineRepos) {
        // sanity check
        if (sharepointOnlineRepos == null || sharepointOnlineRepos.isEmpty()) {
            throw new RuntimeException("expect at least one item");
        }
        //
        for (BoundService s : sharepointOnlineRepos) {

            try {
                app.getRepoSystem().detach(s);
                app.dbDelRepo(s);
            } catch (Exception e) {
                Toast.makeText(app, "Exception: remove this service's local files", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private static void policyNewAddedGoogleDriveRepos(List<RmsRepoInfo> rmsRepos) {
        // sanity check
        if (rmsRepos == null || rmsRepos.isEmpty()) {
            throw new RuntimeException("expect at least one item");
        }
        //
        for (RmsRepoInfo r : rmsRepos) {
            // chang RmsRepoInfo into BoundService compitable
            BoundService s = new BoundService(
                    BoundService.ServiceType.GOOGLEDRIVE,
                    BoundService.GOOGLEDRIVE,
                    r.rmsAccountName,
                    r.rmsAccountId,
                    r.rmsToken,
                    0,
                    r.rmsRepoId,
                    r.rmsName,
                    r.rmsIsShared,
                    r.rmsToken,
                    r.rmsPreference,
                    r.rmsCreationTime
            );
            app.dbInsertRepo(s);

            // activate this repo
            try {
                app.getRepoSystem().activateRepo(s);
            } catch (Exception e) {
                log.e(e);
            }
        }
    }

    private static void policyDelGoogleDriveRepos(List<BoundService> googleDriveRepos) {
        // sanity check
        if (googleDriveRepos == null || googleDriveRepos.isEmpty()) {
            throw new RuntimeException("expect at least one item");
        }

        for (BoundService s : googleDriveRepos) {
            try {
                app.getRepoSystem().detach(s);
                app.dbDelRepo(s);
            } catch (Exception e) {
                Toast.makeText(app, "Exception: remove this service's local files", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }


    private static void policyNewAddedBoxRepos(List<RmsRepoInfo> rmsRepos) {
        // sanity check
        if (rmsRepos == null || rmsRepos.isEmpty()) {
            throw new RuntimeException("expect at least one item");
        }
        //
        for (RmsRepoInfo r : rmsRepos) {
            // chang RmsRepoInfo into BoundService compitable
            BoundService s = new BoundService(
                    BoundService.ServiceType.BOX,
                    BoundService.BOX,
                    r.rmsAccountName,
                    r.rmsAccountId,
                    r.rmsToken,
                    0,
                    r.rmsRepoId,
                    r.rmsName,
                    r.rmsIsShared,
                    r.rmsToken,
                    r.rmsPreference,
                    r.rmsCreationTime
            );
            app.dbInsertRepo(s);

            // activate this repo
            try {
                app.getRepoSystem().activateRepo(s);
            } catch (Exception e) {
                log.e(e);
            }
        }
    }

    private static void policyDelBoxRepos(List<BoundService> boxRepos) {
        // sanity check
        if (boxRepos == null || boxRepos.isEmpty()) {
            throw new RuntimeException("expect at least one item");
        }

        for (BoundService s : boxRepos) {
            try {
                app.getRepoSystem().detach(s);
                app.dbDelRepo(s);
            } catch (Exception e) {
                Toast.makeText(app, "Exception: remove this service's local files", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private static void policyNewAddedSharePointRepos(List<RmsRepoInfo> rmsRepos) {
        // sanity check
        if (rmsRepos == null || rmsRepos.isEmpty()) {
            throw new RuntimeException("expect at least one item");
        }
        //
        for (RmsRepoInfo r : rmsRepos) {
            // chang RmsRepoInfo into BoundService compitable
            BoundService s = new BoundService(
                    BoundService.ServiceType.SHAREPOINT,
                    BoundService.SHAREPOINT,
                    r.rmsAccountName,
                    r.rmsAccountId,
                    r.rmsToken,
                    0,
                    r.rmsRepoId,
                    r.rmsName,
                    r.rmsIsShared,
                    r.rmsToken,
                    r.rmsPreference,
                    r.rmsCreationTime
            );
            app.dbInsertRepo(s);

            // activate this repo
            try {
                app.getRepoSystem().activateRepo(s);
            } catch (Exception e) {
                log.e(e);
            }
        }
    }

    private static void policyDelSharePointRepos(List<BoundService> sharePointRepos) {
        // sanity check
        if (sharePointRepos == null || sharePointRepos.isEmpty()) {
            throw new RuntimeException("expect at least one item");
        }

        for (BoundService s : sharePointRepos) {
            try {
                app.getRepoSystem().detach(s);
                app.dbDelRepo(s);
            } catch (Exception e) {
                Toast.makeText(app, "Exception: remove this service's local files", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private static void policyUpdateRepos(List<BoundService> repos) {
        // update data base
        for (BoundService s : repos) {
            try {
                app.dbUpdateRepoNickName(s);
                app.dbUpdateRepoToken(s);
                app.getRepoSystem().updateRepo(s);
            } catch (Exception e) {
                log.e(e);
            }
        }
    }

    private static void policyRmsEmptyContent() {
        // rms has no content, del some local repos

        // dropbox and 100% match RMS
        List<BoundService> deletingDropbox = new ArrayList<>();
        List<BoundService> deletingOneDrive = new ArrayList<>();
        List<BoundService> deletingSharePoint = new ArrayList<>();
        List<BoundService> deletingSharePointOnline = new ArrayList<>();

        for (BoundService s : app.getUserLinkedRepos()) {
            if (BoundService.matchDropbox(s)) {
                deletingDropbox.add(s);
            }
            if (BoundService.matchOnedrive(s)) {
                deletingOneDrive.add(s);
            }
            if (BoundService.matchSharepointOnline(s)) {
                deletingSharePointOnline.add(s);
            }
            if (BoundService.matchSharePoint(s)) {
                deletingSharePoint.add(s);
            }
        }
        if (!deletingDropbox.isEmpty()) {
            policyDelDropBoxRepos(deletingDropbox);
        }
        if (!deletingOneDrive.isEmpty()) {
            policyDelOnedriveRepos(deletingOneDrive);
        }
        if (!deletingSharePointOnline.isEmpty()) {
            policyDelSharepointOnlineRepos(deletingSharePointOnline);
        }
        if (deletingSharePoint.isEmpty()) {
            policyDelSharePointRepos(deletingSharePoint);
        }
    }

    private static void policyRemoveAll(List<RmsRepoInfo> rmsRepos) {
        log.v("remove all repos");
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        try {
            for (RmsRepoInfo r : rmsRepos) {
                session.getRmsRestAPI().getRepositoryService(session.getRmUser()).repositoryRemove(r.rmsRepoId);
            }
        } catch (Exception e) {
            log.e(e);
        }

    }

    /**
     * add s.RmsID
     */
    public static boolean addNewRepoToRMS(BoundService s, @Nullable RestAPI.Listener listener) throws RmsRestAPIException {
        // as RMS defined, One drive only accept displayName, not the account name
        String mangledAccount = s.account;
        String mangledToken = s.accountToken;
        if (s.type == BoundService.ServiceType.ONEDRIVE) {
            // very ugly and stupid code
            mangledAccount = s.accountName;
        }
        if (s.type == BoundService.ServiceType.SHAREPOINT) {
            mangledAccount = s.accountID;
        }
        //prepare item
        RmsUserLinkedRepos.ResultsBean.RepoItemsBean items = RmsUserLinkedRepos.ResultsBean.RepoItemsBean.buildDefault();
        items.setName(s.rmsNickName);
        items.setType(s.type.toRMSType());
        items.setIsShared(s.rmsIsShared);
        items.setAccountId(s.accountID);
        items.setToken(mangledToken);
        items.setAccountName(mangledAccount);
        // call
        try {
            SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
            RmsAddRepoResult result = session.getRmsRestAPI().getRepositoryService(session.getRmUser()).repositoryAdd(items, listener);
            int statusCode = result.getStatusCode();
            if (200 == statusCode && result.getResults().getRepoId() != null) {
                // update RmsRepoId
                s.rmsRepoId = result.getResults().getRepoId();

                // update RmsToken
                // for One Drive ,only to store refreshToken Is ok

                if (s.type == BoundService.ServiceType.ONEDRIVE) {
                    s.rmsToken = mangledToken;
                } else {
                    s.rmsToken = s.accountToken;  // is the accountToken
                }
                return true;
            } else if (4001 == statusCode) {
                throw new RmsRestAPIException("Repository Name Too Long.", RmsRestAPIException.ExceptionDomain.NameTooLong);
            } else if (4003 == statusCode) {
                throw new RmsRestAPIException("Repository Name containing illegal special characters.",
                        RmsRestAPIException.ExceptionDomain.NamingViolation);
            } else if (409 == statusCode) {
                throw new RmsRestAPIException("There is already a repository with the given name.",
                        RmsRestAPIException.ExceptionDomain.RepoNameCollided);
            } else if (304 == statusCode) {
                throw new RmsRestAPIException("Repository name has already existed.", RmsRestAPIException.ExceptionDomain.RepoAlreadyExist);
            }
        } catch (SessionInvalidException | InvalidRMClientException | RmsRestAPIException e) {
            throw new RmsRestAPIException(e.getMessage());
        }
        return false;
    }

    public interface UIListener {
        void progressing(@NonNull String msg);

        void result(boolean status);
    }

    private static class InternalHandler extends Handler {
        public InternalHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_POST_PROGRESS:
                    sListener.progressing((String) msg.obj);
                    break;
                case MESSAGE_POST_SYNC_DONE:
                    setSyncedMillis(System.currentTimeMillis());
                    sListener.result(true);
                    break;
                case MESSAGE_POST_SYNC_FAILED:
                    setSyncedMillis(System.currentTimeMillis());
                    sListener.result(false);
                    break;
            }
        }

        public void sendProgressMessage(@NonNull String msg) {
            Message message = this.obtainMessage();
            message.what = MESSAGE_POST_PROGRESS;
            message.obj = msg;
            this.sendMessage(message);
        }
    }

}
