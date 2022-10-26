package com.skydrm.rmc.ui.service.offline.core;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.repo.project.SharedWithProjectFile;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.ui.service.offline.architecture.IOffline;
import com.skydrm.rmc.ui.service.offline.architecture.OfflineHandler;
import com.skydrm.rmc.ui.service.offline.architecture.OfflineRequest;
import com.skydrm.rmc.ui.service.offline.architecture.OfflineStatus;
import com.skydrm.rmc.ui.service.offline.exception.OfflineException;
import com.skydrm.sdk.INxlClient;

public class OfflineClearHandler extends OfflineHandler {
    private ICallback mCallback;

    OfflineClearHandler(ICallback callback) {
        this.mCallback = callback;
    }

    @Override
    public void handleRequest(OfflineRequest request) throws OfflineException {
        if (request == null) {
            throw new OfflineException(OfflineStatus.STATUS_FAILED, "OfflineDownloadHandler request must not be null.");
        }
        IOffline offline = request.getOffline();
        if (offline == null) {
            throw new OfflineException(OfflineStatus.STATUS_FAILED, "OfflineDownloadHandler offlineFile must not be null.");
        }
        try {
            if (offline instanceof SharedWithProjectFile) {
                SharedWithProjectFile swpf = (SharedWithProjectFile) offline;
                int id = swpf.getId();
                String membershipId = swpf.getMembershipId();
                deActiveOfflineFile(offline.getLocalPath(), 1, id, membershipId);
            } else {
                deActiveOfflineFile(offline.getLocalPath());
            }
        } catch (InvalidRMClientException e) {
            throw new OfflineException(OfflineStatus.STATUS_TOKEN_DEACTIVE_FAILED, e.getMessage(), e);
        }
        offline.updateOfflineStatus(false);
        if (mCallback != null) {
            mCallback.onClear();
        }
    }

    private boolean deActiveOfflineFile(String localPath) throws OfflineException {
        INxlClient nxlClient = SkyDRMApp.getInstance().getSession().getRmsClient();
        try {
            return nxlClient.updateOfflineStatus(localPath, false);
        } catch (Exception e) {
            throw new OfflineException(OfflineStatus.STATUS_TOKEN_DEACTIVE_FAILED, e.getMessage(), e);
        }
    }

    private boolean deActiveOfflineFile(String localPath,
                                        int sharedSpaceType,
                                        int sharedSpaceId,
                                        String sharedSpaceUserMembership) throws OfflineException {
        INxlClient nxlClient = SkyDRMApp.getInstance().getSession().getRmsClient();
        try {
            return nxlClient.updateOfflineStatus(localPath,
                    sharedSpaceType, sharedSpaceId, sharedSpaceUserMembership,
                    false);
        } catch (Exception e) {
            throw new OfflineException(OfflineStatus.STATUS_TOKEN_DEACTIVE_FAILED, e.getMessage(), e);
        }
    }

    public interface ICallback {
        void onClear();
    }
}
