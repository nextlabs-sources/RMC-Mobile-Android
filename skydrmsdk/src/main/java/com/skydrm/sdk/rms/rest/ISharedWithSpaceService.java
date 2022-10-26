package com.skydrm.sdk.rms.rest;

import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.rest.sharedwithspace.FileMetadata;
import com.skydrm.sdk.rms.rest.sharedwithspace.ListFileParams;
import com.skydrm.sdk.rms.rest.sharedwithspace.ListFileResult;
import com.skydrm.sdk.rms.rest.sharedwithspace.ReShareFileResult;

import java.util.List;

public interface ISharedWithSpaceService {
    ListFileResult listFile(ListFileParams params) throws RmsRestAPIException;

    ReShareFileResult reShareFile(String transactionId, String transactionCode,
                                  String spaceId, List<String> recipients) throws RmsRestAPIException;

    String downloadFile(String localPath, String spaceId, boolean forViewer,
                        String transactionId, String transactionCode,
                        RestAPI.DownloadListener listener, int... args) throws RmsRestAPIException;

    FileMetadata getMetadata(String transactionId, String transactionCode, String spaceId)
            throws RmsRestAPIException;

    String decryptFile(String localPath,
                       String transactionId, String transactionCode,
                       String spaceId,
                       RestAPI.DownloadListener listener)
            throws RmsRestAPIException;
}
