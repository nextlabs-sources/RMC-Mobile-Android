package com.skydrm.rmc.engine;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.reposystem.localrepo.helper.Helper;
import com.skydrm.rmc.utils.NxCommonUtils;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.exception.NotNxlFileException;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.exception.TokenAccessDenyException;
import com.skydrm.sdk.rms.types.SendLogRequestValue;

import java.io.File;
import java.io.FileNotFoundException;

import static com.skydrm.rmc.engine.FileOperation.readNxlFingerPrint;

/**
 * Created by hhu on 4/18/2018.
 */

public class LogSystem {
    private static DevLog log = new DevLog(LogSystem.class.getSimpleName());

    /**
     * This method is used to send allow view log to rms.
     *
     * @param file target file
     * @param duid duid of the target file
     */
    public static void sendAllowViewLog(@NonNull File file, @NonNull String duid) {
        //send allow view log.
        SkyDRMApp.getInstance().getSession().sendLogToServer(duid,
                SendLogRequestValue.OperationType.VIEW,
                file.getName(),
                file.getPath(),
                SendLogRequestValue.AccessResult.Allowed, "");
    }

    /**
     * when view file in offline mode,we should record log first then send them to rms when network
     * is available.
     * [duid,operationId,deviceType,fileName,filePath,accessResult,AccessTime,activityData]
     *
     * @param file
     * @param duid
     */
    public static void recordViewLog(@NonNull File file, @NonNull String duid) {
//        OfflineLog offlineLog = new OfflineLog();
//        //duid
//        offlineLog.setDuid(duid);
//        //filename
//        offlineLog.setFileName(file.getName());
//        //filepath
//        offlineLog.setFilePath(file.getPath());
//        //operationId[SendLogRequestValue.OperationType.VIEW]
//        offlineLog.setOperationId(SendLogRequestValue.OperationType.VIEW);
//        //deviceType
//        offlineLog.setDeviceType(Integer.valueOf(NxCommonUtils.getDeviceType()));
//        //accessResult[allow(1)|deny(0)]
//        offlineLog.setAccessResult(SendLogRequestValue.AccessResult.Allowed);
//        //accessTime.
//        offlineLog.setAccessTime(System.currentTimeMillis());
//        //activityData.
//        offlineLog.setActivityData("");

        SkyDRMApp.getInstance().getDBProvider().insertActivityLogItem(duid,
                SendLogRequestValue.OperationType.VIEW, Integer.valueOf(NxCommonUtils.getDeviceType()),
                file.getName(), file.getPath(),
                SendLogRequestValue.AccessResult.Allowed, System.currentTimeMillis(), "");
    }

    public static void recordDenyViewLog(@NonNull File file, @NonNull String duid) {
        SkyDRMApp.getInstance().getDBProvider().insertActivityLogItem(duid,
                SendLogRequestValue.OperationType.VIEW, Integer.valueOf(NxCommonUtils.getDeviceType()),
                file.getName(), file.getPath(),
                SendLogRequestValue.AccessResult.Denied, System.currentTimeMillis(), "");
    }

    /**
     * This method is used to send deny view log to rms.
     * when file decrypt failed INxlFileFingerPrint is null.
     * need to extract fileFingerPrint from nxl file header.
     *
     * @param file     target file
     * @param context  this params is used to display the loading dialog.[can be nullable]
     * @param viewType this params is used to judge whether need to preview file.[can be nullable]
     */
    public static void sendDenyViewLog(@NonNull final File file,
                                       @Nullable Context context, @Nullable String viewType) {
        // send deny view log
        readNxlFingerPrint(context, file, false, viewType, new FileOperation.
                IGetFingerPrintCallback() {
            @Override
            public void onGetFingerPrintFinished(INxlFileFingerPrint fingerPrint) {
                if (fingerPrint != null) {
                    String duid = fingerPrint.getDUID();
                    SkyDRMApp.getInstance().getSession().sendLogToServer(duid,
                            SendLogRequestValue.OperationType.VIEW,
                            file.getName(),
                            file.getPath(),
                            SendLogRequestValue.AccessResult.Denied, "");
                } else {
                    log.e("In LogSystem:get nxlFile fingerPrint failed!");
                }
            }
        });
    }

    /**
     * This method is used to send protected log to rms.
     *
     * @param context   when displayUI is true need to pass it else it can be nullable.
     * @param displayUI true means show loading dialog|false means no loading dialog show.
     * @param nxlFile   target file need to be send log.
     */
    public static void sendProtectLog(@Nullable Context context, boolean displayUI, final File nxlFile, final boolean releaseTmpFile) {
        //When protect file success then send log to rms.
        // send log
        try {
            readNxlFingerPrint(context, nxlFile, displayUI, Constant.VIEW_TYPE_NORMAL,
                    new FileOperation.IGetFingerPrintCallback() {
                        @Override
                        public void onGetFingerPrintFinished(INxlFileFingerPrint fingerPrint) {
                            //we may need clear tmp protected nxl file local.
                            if (releaseTmpFile) {
                                releaseTmpFile();
                            }
                            if (fingerPrint != null) {
                                String duid = fingerPrint.getDUID();
                                SkyDRMApp.getInstance().getSession().sendLogToServer(duid,
                                        SendLogRequestValue.OperationType.PROTECT,
                                        nxlFile.getName(),
                                        nxlFile.getPath(),
                                        SendLogRequestValue.AccessResult.Allowed, "");
                            } else {
                                log.e("In LogSystem: get nxlFile fingerPrint failed!");
                            }
                        }

                        private void releaseTmpFile() {
                            log.d("release Tmp file success.");
                            if (nxlFile != null && nxlFile.exists()) {
                                Helper.deleteFile(nxlFile);
                            }
                        }
                    });
        } catch (Exception e) {
            log.e(e);
        }
    }

    public static boolean sendProtectLog(File nxlFile) throws FileNotFoundException,
            RmsRestAPIException, NotNxlFileException, TokenAccessDenyException {
        if (nxlFile == null) {
            return false;
        }
        if (nxlFile.isDirectory()) {
            return false;
        }
        INxlFileFingerPrint fp = SkyDRMApp
                .getInstance()
                .getSession()
                .getRmsClient()
                .extractFingerPrint(nxlFile.getPath());
        if (fp == null) {
            return false;
        }
        String duid = fp.getDUID();
        if (duid == null || duid.isEmpty()) {
            return false;
        }
        SkyDRMApp.getInstance().getSession().sendLogToServer(duid,
                SendLogRequestValue.OperationType.PROTECT,
                nxlFile.getName(),
                nxlFile.getPath(),
                SendLogRequestValue.AccessResult.Allowed, "");

        return true;
    }

    public static boolean sendShareLog(File nxlFile) throws FileNotFoundException,
            RmsRestAPIException, NotNxlFileException, TokenAccessDenyException {
        if (nxlFile == null || nxlFile.isDirectory()) {
            return false;
        }
        if (!nxlFile.exists()) {
            return false;
        }
        INxlFileFingerPrint fp = SkyDRMApp
                .getInstance()
                .getSession()
                .getRmsClient()
                .extractFingerPrint(nxlFile.getPath());
        if (fp == null) {
            return false;
        }
        String duid = fp.getDUID();
        if (duid == null || duid.isEmpty()) {
            return false;
        }

        SkyDRMApp.getInstance().getSession().sendLogToServer(duid,
                SendLogRequestValue.OperationType.SHARE,
                nxlFile.getName(),
                nxlFile.getPath(),
                SendLogRequestValue.AccessResult.Allowed, "");

        return true;
    }

    public static void sendClassifyLog(String duid, String fileName, String pathDisplay) {
        SkyDRMApp.getInstance().getSession().sendLogToServer(duid,
                SendLogRequestValue.OperationType.CLASSIFY,
                fileName,
                pathDisplay,
                SendLogRequestValue.AccessResult.Allowed, "");
    }
}
