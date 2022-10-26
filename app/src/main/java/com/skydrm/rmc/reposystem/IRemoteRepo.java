package com.skydrm.rmc.reposystem;

import android.support.annotation.Nullable;

import com.skydrm.rmc.reposystem.exception.FileDownloadException;
import com.skydrm.rmc.reposystem.exception.FileListException;
import com.skydrm.rmc.reposystem.exception.FileUploadException;
import com.skydrm.rmc.reposystem.exception.FolderCreateException;
import com.skydrm.rmc.reposystem.remoterepo.ICancelable;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.reposystem.types.NXDocument;
import com.skydrm.rmc.reposystem.types.NXFolder;

import java.io.File;


public interface IRemoteRepo {

    void updateToken(String accessToken);

    /**
     * Retrieve the immediate children( folder or document) indicated by {@param file}
     * - Background thread can use this method to update information
     *
     * @param file
     * @return
     */
    INxFile listFiles(NXFolder file) throws FileListException;

    /**
     * Download a document (not a folder) to  {@param localPath}
     *
     * @param document
     * @param localPath
     * @param callback
     */
    void downloadFile(INxFile document, String localPath, IDownLoadCallback callback);

    /**
     *  get partial file content, mainly used to get nxl file head rights.
     *  @param document living repo @{document}
     *  @param localPath file local path
     *  @param start the byte offset to be read for the file content
     *  @param length the length to be read
     *  @param callback download callback
     */
    void downloadFilePartial(INxFile document, String localPath, int start, int length, IDownLoadCallback callback);

    void uploadFile(INxFile parentFolder, String fileName, File localFile, IUploadFileCallback callback);

    void updateFile(INxFile parentFolder, INxFile updateFile, File localFile, IUploadFileCallback callback);

    void deleteFile(INxFile file);

    void createFolder(INxFile parentFolder, String subFolderName) throws FolderCreateException;

    boolean getInfo(RemoteRepoInfo info);

    interface IListFilesCallback {
        void onFinishedList(boolean taskStatus, INxFile file, String errorMsg);
    }

    interface IDownLoadCallback {
        // before running Download task ,give caller a handler that can abort this task
        void cancelHandler(ICancelable handler);

        void onFinishedDownload(boolean taskStatus, String localPath, @Nullable FileDownloadException e);

        void progressing(long newValue);
    }

    interface IUploadFileCallback {

        // before running Download task ,give caller a handler that can abort this task
        void cancelHandler(ICancelable handler);

        void onFinishedUpload(boolean taskStatus, @Nullable NXDocument uploadedDoc, @Nullable FileUploadException e);

        void progressing(long newValue);

    }

}
