package com.skydrm.sdk.rms.rest;

import android.support.annotation.NonNull;

import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.ui.uploadprogress.ProgressRequestListener;

import org.json.JSONObject;

import java.io.File;

public interface IMyDriveService {
    JSONObject myDriveList(String path) throws RmsRestAPIException;

    boolean myDriveDelete(String path) throws RmsRestAPIException;

    boolean myDriveCreateFolder(String parentPathId, String name) throws RmsRestAPIException;

    void myDriveDownload(String path, long fileSize,
                         String localPath, RestAPI.DownloadListener listener) throws RmsRestAPIException;

    String myDriveUpload(String parentPathId, String fileName, File file) throws RmsRestAPIException;

    String myDriveUploadProgress(String parentPathId, String fileName, File file,
                                 @NonNull ProgressRequestListener progressRequestListener) throws RmsRestAPIException;

    JSONObject myDriveStorageUsed() throws RmsRestAPIException;

    String myDriveCreatePublicShare(String path) throws RmsRestAPIException;
}
