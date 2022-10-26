package com.skydrm.rmc.reposystem;

import android.support.annotation.Nullable;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.reposystem.exception.FileUploadException;
import com.skydrm.rmc.reposystem.exception.FolderCreateException;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.utils.sort.SortType;
import com.skydrm.sdk.utils.ParseJsonUtils;

import java.io.File;
import java.util.List;

/**
 * Created by oye on 7/10/2017.
 */

public interface IRepoSystem {

    /**
     * - will clear the previous data
     */
    void create(File mountPoint, String userID);

    void close();

    void changeState(RunningMode newRunningMode);

    RunningMode getState();

    void setSortType(SortType type);

    void attachWorkingFolderObserver(IWorkingFolderObserver observer);


    void attach(@Nullable List<BoundService> list);

    void detach(@Nullable BoundService service);

    /*
        for system activate
         */
    void activate();

    void activateRepo(BoundService service) throws Exception;

    void deactivateRepo(BoundService service) throws Exception;


    void selectOnlyMyDrive() throws Exception;

    /**
     * set repo-system to show only one repo's content by {@param service}
     */
    void selectOnlyOneRepo(BoundService service) throws Exception;

    /**
     * set repo-system to show all user linked repos' content
     *
     * @throws Exception
     */
    void selectAllRepo() throws Exception;

    void updateRepo(BoundService service);

    boolean isInSyntheticRoot();

    List<INxFile> listRoot(IRemoteRepo.IListFilesCallback callback) throws Exception;

    /**
     * Mandatorily use network to sync current working folder
     * Known used:
     * - home activity, pull down to refresh
     */
    void syncWorkingFolder(IRemoteRepo.IListFilesCallback callback) throws Exception;

    /**
     * Get local working folder directly, for typical usage scenario ,
     * get the current repo's local caches ,NO USE SOCKET
     * Known used:
     * - home activity, timer task of  refresh, i.e.  UI calls this method periodically
     * - home activity, sort the contents of this folder
     */
    List<INxFile> listFolder() throws Exception;


    /**
     * get a document content, the document must linked with a BoundService
     * <p/>
     * remarks:
     * - this method will change focused repo
     * - repoSystem can find the @{document} is belong to which living repo
     * - if not find the host of the @{document}, throw Exception
     */

    @Nullable
    File getFile(INxFile document, IRemoteRepo.IDownLoadCallback callback) throws Exception;

    /**
     * get partial file content, mainly used to get nxl file head rights.
     *
     * @param document living repo @{document}
     * @param start    the byte offset to be read for the file content
     * @param length   the length to be read
     * @param callback download callback
     */
    @Nullable
    File getFilePartialContent(INxFile document, int start, int length, IRemoteRepo.IDownLoadCallback callback) throws Exception;

    /**
     * give caller additional select to upload file by boundService, the service which represent
     * a ILocalRepo must be currently activated in RepoSystem.
     *
     * @param selService   for null , use current focused repo
     * @param parentFolder
     * @param fileName
     * @param localFile
     * @param callback
     * @throws FileUploadException
     */
    void uploadFile(BoundService selService,
                    INxFile parentFolder,
                    String fileName,
                    File localFile,
                    IRemoteRepo.IUploadFileCallback callback) throws FileUploadException;


    /**
     * delete INxFile form repo system, the service can be extracted from INxFile.getService()
     * - for doc , delete it self
     * - for folder, recursive delete all items in this folder
     *
     * @param file
     */
    void deleteFile(INxFile file);

    void createFolder(String folderName) throws FolderCreateException;

    //
    void createFolder(BoundService service, String folderName) throws FolderCreateException;

    void createFolder(BoundService service, INxFile parent, String folderName) throws FolderCreateException;

    INxFile findWorkingFolder() throws Exception;

    List<INxFile> enterFolder(INxFile folder, IRemoteRepo.IListFilesCallback callback) throws Exception;

    void updateFilesMark3(List<ParseJsonUtils.AllRepoFavoListBean> list);

    List<INxFile> getFavoriteFiles();

    List<INxFile> getOfflineFiles();

    /**
     * this method will change current working folder
     */
    INxFile backToParent();

    INxFile getParent(INxFile child, boolean byService);

    @Deprecated
    void clearCache(SkyDRMApp.ClearCacheListener listener);

    void clearCache();

    void clearRepoCache(BoundService boundService, SkyDRMApp.ClearCacheListener listener);

    long calReposCacheSize();

    void getRepoInformation(BoundService boundService, ILocalRepo.IRepoInfoCallback callback);

    int getSizeOfLivingRepo();


    void markAsFavorite(INxFile file);

    void unmarkAsFavorite(INxFile file);

    void markAsOffline(INxFile file);

    void unmarkAsOffline(INxFile file);

    List<BoundService> getStockedNotSpoiledServiceInRepoSystem();

    /**
     * copy/clone a whole tree in specific repo by {@param service}
     *
     * @param service which repo you want to copy
     * @return
     */
    INxFile folderTreeClone(BoundService service);

    void saveToLocal();

    ILocalRepo findInStockRepo(BoundService service);

    @Nullable
    ILocalRepo findInLivingRepo(BoundService service);

    enum RunningMode {
        FAVORITE,
        OFFLINE,
        SYNTHETIC,
        FOCUSED
    }
}
