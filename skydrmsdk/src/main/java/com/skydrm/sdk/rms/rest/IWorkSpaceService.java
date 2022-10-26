package com.skydrm.sdk.rms.rest;

import com.skydrm.sdk.INxlRights;
import com.skydrm.sdk.INxlTags;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.rest.workspace.CreateFolderResult;
import com.skydrm.sdk.rms.rest.workspace.DeleteItemResult;
import com.skydrm.sdk.rms.rest.workspace.DownloadResult;
import com.skydrm.sdk.rms.rest.workspace.FileMetadata;
import com.skydrm.sdk.rms.rest.workspace.FolderMetadata;
import com.skydrm.sdk.rms.rest.workspace.FolderMetadataParam;
import com.skydrm.sdk.rms.rest.workspace.ListFileParam;
import com.skydrm.sdk.rms.rest.workspace.ListFileResult;
import com.skydrm.sdk.rms.rest.workspace.ReClassifyResult;
import com.skydrm.sdk.rms.rest.workspace.UploadFileResult;
import com.skydrm.sdk.ui.uploadprogress.ProgressRequestListener;

public interface IWorkSpaceService {
    ListFileResult listFile(ListFileParam param) throws RmsRestAPIException;

    UploadFileResult uploadFile(String filePath, String parentPathId, int type,
                                ProgressRequestListener listener)
            throws RmsRestAPIException;

    UploadFileResult uploadFile(String filePath, String parentPathId,
                                INxlRights rights, INxlTags tags,
                                ProgressRequestListener listener)
            throws RmsRestAPIException;

    CreateFolderResult createFolder(String parentPathId, String name, boolean autoRename)
            throws RmsRestAPIException;

    DeleteItemResult deleteItem(String pathId) throws RmsRestAPIException;

    FileMetadata getFileMetadata(String pathId) throws RmsRestAPIException;

    FolderMetadata getFolderMetadata(FolderMetadataParam param) throws RmsRestAPIException;

    DownloadResult downloadFile(String localPath, String pathId, int type,
                                RestAPI.DownloadListener listener, int... args)
            throws RmsRestAPIException;

    ReClassifyResult reClassifyFile(String fileName, String parentPathId, String fileTags)
            throws RmsRestAPIException;
}
