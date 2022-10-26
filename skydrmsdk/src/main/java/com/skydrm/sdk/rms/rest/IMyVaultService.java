package com.skydrm.sdk.rms.rest;

import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.rest.myVault.MyVaultDownloadHeader;
import com.skydrm.sdk.rms.rest.myVault.MyVaultUploadFileResult;
import com.skydrm.sdk.rms.rest.myVault.MyVaultFileListResult;
import com.skydrm.sdk.rms.rest.myVault.MyVaultListFileRequestParas;
import com.skydrm.sdk.rms.rest.myVault.MyVaultMetaDataResult;
import com.skydrm.sdk.rms.rest.myVault.MyVaultUploadFileParams;
import com.skydrm.sdk.ui.uploadprogress.ProgressRequestListener;

public interface IMyVaultService {
    /**
     * This method is used to upload a nxl file to MyVault{only NXL file can be uploaded}
     *
     * @param params   Upload params bind to the target nxl file.
     * @param listener Uploading progress listener
     * @return MyVault file uploaded result from rms
     * @throws RmsRestAPIException When uploading nxl file to MyVault exceptions may occurred.
     */
    MyVaultUploadFileResult uploadFileToMyVault(MyVaultUploadFileParams params,
                                                ProgressRequestListener listener) throws RmsRestAPIException;

    MyVaultFileListResult listMyVaultFile(MyVaultListFileRequestParas requestParas) throws RmsRestAPIException;

    MyVaultDownloadHeader downloadMyVaultFile(String pathId, String localPath, int type, RestAPI.DownloadListener listener, int... args) throws RmsRestAPIException;

    MyVaultMetaDataResult getMyVaultFileMetaData(String duid, String filePathId) throws RmsRestAPIException;

    boolean deleteMyVaultFile(String duid, String filePathId) throws RmsRestAPIException;
}
