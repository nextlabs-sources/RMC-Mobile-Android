package com.skydrm.rmc.engine;

import android.support.annotation.Nullable;

import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.types.favorite.AllRepoFavFileRequestParas;
import com.skydrm.sdk.rms.types.favorite.FavoriteList;
import com.skydrm.sdk.rms.types.favorite.OneRepoFavFiles;
import com.skydrm.sdk.rms.types.favorite.ReposFavorite;
import com.skydrm.sdk.rms.types.favorite.UnfavoriteList;
import com.skydrm.sdk.utils.ParseJsonUtils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by aning on 12/13/2016.
 * this class used to handle about mark\ unmark file as favorite and offline,
 * including favorite and offline drive synchronous
 */
@Deprecated
public class FileMarkManager {
    private static final DevLog log = new DevLog(FileMarkManager.class.getSimpleName());
    // action flag
    private static FileMarkManager mInstance = null;
    //null object

    private ConcurrentHashMap<String, FavoriteList> favCache = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, UnfavoriteList> unfavCache = new ConcurrentHashMap<>();

    private SkyDRMApp app = SkyDRMApp.getInstance();

    private FileMarkManager() {

    }

    public static FileMarkManager getmInstance() {
        if (mInstance == null) {
            synchronized (FileMarkManager.class) {
                if (mInstance == null) {
                    mInstance = new FileMarkManager();
                }
            }
        }
        return mInstance;
    }

    public void addFavoriteFileCacheSendToRms(String rmsRepoId, INxFile file, INxFile parent) {
        // sanity check
        if (rmsRepoId == null) { // GoogleDrive may do not have repoID
            return;
        }
        if (file == null) {
            return;
        }
        if (parent == null) {
            return;
        }
        // pack param to meet rms requiement
        FavoriteList.Item item = new FavoriteList.Item();
        item.pathId = file.getCloudFileID();
        item.displayPath = file.getDisplayPath();
        item.fileSize = file.getSize();
        item.lastModifiedTime = file.getLastModifiedTimeLong();
        item.parentFileId = parent.getCloudFileID();

        // cached this item
        FavoriteList list = favCache.get(rmsRepoId);
        if (list == null) {
            list = new FavoriteList(rmsRepoId);
            if (!list.lists.contains(item)) {
                list.lists.add(item);
                favCache.put(rmsRepoId, list);
            }
        } else {
            if (!list.lists.contains(item)) {
                list.lists.add(item);
            }
        }
    }

    public void addUnFavoriteFileCacheSendToRms(String rmsRepoId, INxFile file) {
        // sanity check
        if (rmsRepoId == null) { // GoogleDrive may do not have repoID
            return;
        }
        if (file == null) {
            return;
        }

        // pack param to meet rms requiement
        UnfavoriteList.Item item = new UnfavoriteList.Item();
        item.pathId = file.getCloudFileID();
        item.displayPath = file.getDisplayPath();

        // cached this item
        UnfavoriteList list = unfavCache.get(rmsRepoId);
        if (list == null) {
            list = new UnfavoriteList(rmsRepoId);
            if (!list.lists.contains(item)) {
                list.lists.add(item);
                unfavCache.put(rmsRepoId, list);
            }
        } else {
            if (!list.lists.contains(item)) {
                list.lists.add(item);
            }
        }
    }

    /**
     * used to synchronize the favorite & unfaroite files into server
     * note: the function  must be called in background thread
     */
    public void sendToRMS() throws RmsRestAPIException {
        // the set for record mark or unmark favorite
/*        log.v("send Fav-updates to RMS");
        try {
            for (FavoriteList i : favCache.values()) {
                RmUser user = SkyDRMApp.getInstance().getSession().getRmUser();
                SkyDRMApp.getInstance().getSession()
                        .getRmsRestAPI().getFavoriteService(user)
                        .markAsFavorite(i);

                favCache.remove(i.rmsID);
            }
            for (UnfavoriteList i : unfavCache.values()) {
                RmUser user = SkyDRMApp.getInstance().getSession().getRmUser();
                SkyDRMApp.getInstance().getSession()
                        .getRmsRestAPI().getFavoriteService(user)
                        .unmarkAsFavorite(i);

                unfavCache.remove(i.rmsId);
            }
            // clear the map
            favCache.clear();
            unfavCache.clear();
        } catch (Exception e) {
            throw new RmsRestAPIException(e.getMessage());
        }*/


    }

    /**
     * Get all repository favorite files(now only support myDrive and myVault) in an aggregated way,
     * means the returned result is that for each drive, has its individual array aggregation of favorite files.
     * Note: the function must be executed in sub thread.
     */
    public ReposFavorite getAllRepoFavFiles() throws RmsRestAPIException {
        log.v("getAllRepoFavFiles from RMS");
        try {
            SkyDRMApp.Session2 session = app.getSession();
            ReposFavorite favoriteFilesInAllRepos = session
                    .getRmsRestAPI()
                    .getFavoriteService(session.getRmUser())
                    .getFavoriteFilesInAllRepos();
            log.v("check result:" + favoriteFilesInAllRepos.toString());
            return favoriteFilesInAllRepos;
        } catch (SessionInvalidException | InvalidRMClientException e) {
            throw new RmsRestAPIException(e.getMessage());
        }
    }

    /**
     * Get all repository favorite files(now only support myDrive and myVault) in a list fashion,
     * means the returned result is a list for all repository.
     * Note: the function must be executed in sub thread.
     *
     * @param paras {@link AllRepoFavFileRequestParas} request parameters, it supports pagination, sorting and search.
     */
    public List<ParseJsonUtils.AllRepoFavoListBean> getAllRepoFavFileList(AllRepoFavFileRequestParas paras) throws RmsRestAPIException {
        log.v("getAllRepoFavFileList from RMS");
        try {
            SkyDRMApp.Session2 session = app.getSession();
            List<ParseJsonUtils.AllRepoFavoListBean> allRepoFavFileList = session
                    .getRmsRestAPI()
                    .getFavoriteService(session.getRmUser())
                    .getFavoriteFileListInAllRepos(paras);
            log.v("check result:" + allRepoFavFileList.toString());
            return allRepoFavFileList;
        } catch (SessionInvalidException | InvalidRMClientException e) {
            throw new RmsRestAPIException(e.getMessage());
        }
    }

    /**
     * Get one specify repository favorite files
     * Note: the function must be executed in sub thread.
     *
     * @param repositoryId repo id
     * @param lastModified optional
     */
    public OneRepoFavFiles getSpecifiedRepoFavFiles(String repositoryId, @Nullable String lastModified) throws RmsRestAPIException {
        log.v("getSpecifiedRepoFiles from RMS");
        try {
            SkyDRMApp.Session2 session = app.getSession();
            OneRepoFavFiles oneRepoFavFiles = session
                    .getRmsRestAPI()
                    .getFavoriteService(session.getRmUser())
                    .getFavoriteFilesInOneRepo(repositoryId, lastModified);
            log.v("check result:" + oneRepoFavFiles.toString());
            return oneRepoFavFiles;
        } catch (SessionInvalidException | InvalidRMClientException e) {
            throw new RmsRestAPIException(e.getMessage());
        }
    }

    public enum MarkStatus {
        FAVORITE, OFFLINE
    }


}
