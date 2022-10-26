package com.skydrm.rmc.datalayer.repo.base;

import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.service.offline.IOfflineCallback;
import com.skydrm.sdk.exception.RmsRestAPIException;

import java.io.IOException;
import java.util.List;

public interface INxlFile extends IFileType {
    int PROCESS = 0x01;
    int MARK_ERROR = 0x02;

    String getName();

    String getPathId();

    String getPathDisplay();

    long getLastModifiedTime();

    long getCreationTime();

    boolean isFolder();

    List<INxlFile> getChildren();

    String getParent();

    /**
     * Download nxl file from rms.[run on background thread]
     *
     * @param type
     * @param listener
     * @throws SessionInvalidException
     * @throws InvalidRMClientException
     * @throws RmsRestAPIException
     */
    void download(int type, DownloadListener listener) throws SessionInvalidException,
            InvalidRMClientException, RmsRestAPIException, IOException;

    /**
     * Delete nxl file existing in rms.[run on background thread.]
     *
     * @throws SessionInvalidException
     * @throws InvalidRMClientException
     * @throws RmsRestAPIException
     */
    void delete() throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException;

    void clearCache();

    long getCacheSize();

    void markAsOffline(IOfflineCallback callback);

    void unMarkAsOffline();

    void markAsFavorite();

    void unMarkAsFavorite();

    interface DownloadListener {
        void onProgress(int i);

        void onComplete();

        void cancel();
    }
}
