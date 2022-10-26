package com.skydrm.rmc.reposystem;

import com.skydrm.rmc.reposystem.exception.FileUploadException;
import com.skydrm.rmc.reposystem.exception.FolderCreateException;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.reposystem.types.INxFile;

import java.io.File;
import java.util.List;
import java.util.Map;


public interface ILocalRepo {
    /**
     * first point to config this repo
     * - one local repo must associate a remote repo represented by BoundService
     */
    void install(File mountPoint, BoundService service) throws Exception;

    /**
     * point to delete this repo, be used when client want to delete this repo
     */
    void uninstall();

    /**
     * user will use it ,
     * - good point to prepare res
     */
    void activate();

    /**
     * user do not focus on it now
     * - good point to save/release res
     */
    void deactivate();

    void setObserver(IWorkingFolderObserver observer);

    /**
     * @param marks < CloudPathId, bIsFav>
     */
    void updateBatchedFavoriteMarks(Map<String, Boolean> marks);

    /**
     * user can mark a file(folder or document) as favorite
     *
     * @param file
     */
    void markAsFavorite(INxFile file);

    void unmarkAsFavorite(INxFile file);

    /**
     * user can mark a file(folder or document) for which it can be accessed in offline mode
     * that means a file can get access from local not the remote
     *
     * @param file
     */
    void markAsOffline(INxFile file);

    void unmarkAsOffline(INxFile file);

    /**
     * Get a list holds all files  marked as favorite by client in this repo
     */
    List<INxFile> getFavoriteDocuments();

    /**
     * Get a list holds all files  marked as offline mode by client in this repo
     */
    List<INxFile> getOfflineDocuments();

    /**
     * a local repo must be associate with a bound service through which to indicate the remote repo
     *
     * @return
     */
    BoundService getLinkedService();

    IRemoteRepo getRemoteRepo();


    /**
     * Get this repo's root immediately if cached , or ues asyn task
     * <p>
     * if {@param callback} is null , return local cache directly
     * </p>
     */
    INxFile getRoot(IRemoteRepo.IListFilesCallback callback) throws Exception;

    /**
     * get the latest root from remote repo , not async method
     * this method can not be running at UI thread
     */
    INxFile syncRoot();

    /**
     * get local repo's working folder directly ,
     * working folder is the current working folder in which client point
     */
    INxFile getWorkingFolder() throws Exception;

    /**
     * Mandatory to get latest info from remote repo
     * <p/>
     * <p>
     * Typical usage: UI will mandatorily refresh the current working folder
     * </p>
     *
     * @param callback
     * @throws Exception {@param callback} is null
     */
    void syncWorkingFolder(IRemoteRepo.IListFilesCallback callback) throws Exception;

    /**
     * get a document content, if not cached at local, download it
     *
     * @param document
     * @param callback
     * @throws Exception {@param document} can not downcast to NxDocument
     *                   {@param callback} is null
     */

    File getDocument(INxFile document, IRemoteRepo.IDownLoadCallback callback) throws Exception;

    /**
     *  get partial file content, mainly used to get nxl file head rights.
     *  @param document living repo @{document}
     *  @param start the byte offset to be read for the file content
     *  @param length the length to be read
     *  @param callback download callback
     */
    File getDocumentPartialContent(INxFile document, int start, int length, IRemoteRepo.IDownLoadCallback callback) throws Exception;

    /**
     * upload file must catch FileUploadException (at least for NamingCollision, NamingViolation)
     * two situation:
     * - local side detect
     * - server side detect : require check at callback result
     */
    void uploadFile(INxFile parentFolder, String fileName, File localFile, IRemoteRepo.IUploadFileCallback callback) throws FileUploadException;

    void updateFile(INxFile parentFolder, INxFile updateFile, File localFile, IRemoteRepo.IUploadFileCallback callback) throws Exception;

    /**
     * use this method to get contents of {@param folder}
     *
     * @param folder
     * @param callback
     * @param isChangeWorkingFolder true, change this reps is working folder as {@param folder}
     * @return
     * @throws Exception
     */
    List<INxFile> listFolder(INxFile folder, IRemoteRepo.IListFilesCallback callback, boolean isChangeWorkingFolder) throws Exception;


    void createFolder(INxFile parentFolder, String subFolderName) throws FolderCreateException;

    void delete(INxFile file) throws Exception;

    /**
     * Get parent of current working folder
     */
    INxFile backToParent();

    INxFile getParent(INxFile child);

    INxFile folderTreeClone();

    /**
     * clear the file that stored local, but not marked as OFFLINE&FAVORITE
     */
    void clearCache();

    long calCacheSize();

    void getRepoInfo(IRepoInfoCallback callback);

    void notifyToSaveItself();

    interface IRepoInfoCallback {
        void result(boolean status, RepoInfo info, String errorMsg);
    }
}
