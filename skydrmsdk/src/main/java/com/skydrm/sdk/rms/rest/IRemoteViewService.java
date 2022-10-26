package com.skydrm.sdk.rms.rest;

import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.types.RemoteViewProjectFileParas;
import com.skydrm.sdk.rms.types.RemoteViewRepoFileParas;
import com.skydrm.sdk.rms.types.RemoteViewResult2;

import java.io.File;

public interface IRemoteViewService {
    /**
     * The api used to remote view local file through rms
     *
     * @param tenantId        tenant id
     * @param file            the file will be viewed
     * @param convertListener convert progress listener
     * @throws RmsRestAPIException {@link RmsRestAPIException} Exception.
     */
    RemoteViewResult2 remoteViewLocalFile(final String tenantId,
                                          final File file,
                                          final RestAPI.IConvertListener convertListener) throws RmsRestAPIException;

    /**
     * The api used to remote view repository file through rms
     *
     * @param paras {@link RemoteViewRepoFileParas} the request parameters
     * @throws RmsRestAPIException {@link RmsRestAPIException} Exception.
     */
    RemoteViewResult2 remoteViewRepoFile(final RemoteViewRepoFileParas paras) throws RmsRestAPIException;

    /**
     * The api used to remote view project file through rms
     *
     * @param paras {@link RemoteViewProjectFileParas} the request parameters
     * @throws RmsRestAPIException {@link RmsRestAPIException} Exception.
     */
    RemoteViewResult2 remoteViewProjectFile(final RemoteViewProjectFileParas paras) throws RmsRestAPIException;

    /**
     * used to cancel remote view.
     */
    void cancel();
}
