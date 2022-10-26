package com.skydrm.rmc.filemark;

import android.support.annotation.Nullable;

import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.types.favorite.AllRepoFavFileRequestParas;
import com.skydrm.sdk.rms.types.favorite.FavoriteList;
import com.skydrm.sdk.rms.types.favorite.OneRepoFavFiles;
import com.skydrm.sdk.rms.types.favorite.ReposFavorite;
import com.skydrm.sdk.rms.user.IRmUser;
import com.skydrm.sdk.utils.ParseJsonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by aning on 8/17/2017.
 */

public class FavoriteMarkImpl implements IFavoriteMark {
    private static final DevLog log = new DevLog(FavoriteMarkImpl.class.getSimpleName());
    private static FavoriteMarkImpl instance = null;
    // the mark cache set <repoId, list>
    private ConcurrentHashMap<String, FavoriteList> favMap = new ConcurrentHashMap<>();

    private SkyDRMApp app = SkyDRMApp.getInstance();

    public FavoriteMarkImpl() {
    }

    static public FavoriteMarkImpl getInstance() {
        if (instance == null) {
            synchronized (FavoriteMarkImpl.class) {
                if (instance == null) {
                    instance = new FavoriteMarkImpl();
                }
            }
        }
        return instance;
    }

    @Override
    public void addMarkFileCacheSet(String repoId, IMarkItem markItem) {
        // sanity check
        if (repoId == null) { // GoogleDrive may do not have repoID
            return;
        }
        if (markItem == null) {
            return;
        }

        // pack param to meet rms requirement
        FavoriteList.Item item = new FavoriteList.Item();
        item.pathId = markItem.getPathId();
        item.displayPath = markItem.getDisplayPath();
        item.fileSize = markItem.getFileSize();
        item.lastModifiedTime = markItem.getLastModifiedTime();
        item.parentFileId = markItem.getParentFileId();
        item.isMark = !markItem.isUnMark();

        // cached this item
        FavoriteList list = favMap.get(repoId);
        if (list == null) {
            list = new FavoriteList(repoId);
            if (!list.lists.contains(item)) { // judge whether do mark or un-mark for the same file by pathId
                list.lists.add(item);
                favMap.put(repoId, list);
            } else {
                // get original item and reset the mark flag.
                int index = list.lists.indexOf(item);
                if (index >= 0 && index < list.lists.size()) {
                    FavoriteList.Item oldItem = list.lists.get(index);
                    oldItem.isMark = item.isMark;
                }
            }
        } else {
            if (!list.lists.contains(item)) { // judge whether do mark or un-mark for the same file by pathId
                list.lists.add(item);
            } else {
                // get original item and reset the mark flag.
                int index = list.lists.indexOf(item);
                if (index >= 0 && index < list.lists.size()) {
                    FavoriteList.Item oldItem = list.lists.get(index);
                    oldItem.isMark = item.isMark;
                }
            }
        }
    }

    /**
     * Used to synchronize the favorite & unfaroite files into rms
     * Note: the function  must be called in background thread
     */
    @Override
    public void syncMarkedFileToRms() throws RmsRestAPIException {
        // the set for record mark or unmark favorite
        log.v("syncMarkedFileToRms");
        try {
            IRmUser user = SkyDRMApp.getInstance().getSession().getRmUser();

            List<FavoriteList.Item> favList = new ArrayList<>();
            List<FavoriteList.Item> unFavList = new ArrayList<>();

            for (FavoriteList i : favMap.values()) {

                // build mark and un-mark set.
                for (FavoriteList.Item item : i.lists) {
                    if (item.isMark) {
                        favList.add(item);
                    } else {
                        unFavList.add(item);
                    }
                }

                // sync mark files
                if (favList.size() > 0) {
                    SkyDRMApp.getInstance().getSession()
                            .getRmsRestAPI().getFavoriteService(user)
                            .markAsFavorite(i.rmsID, favList);
                }

                // sync un-mark files
                if (unFavList.size() > 0) {
                    SkyDRMApp.getInstance().getSession()
                            .getRmsRestAPI().getFavoriteService(user)
                            .unmarkAsFavorite(i.rmsID, unFavList);
                }

                // clear
                favMap.remove(i.rmsID);
                favList.clear();
                unFavList.clear();
            }

            // clear the map
            favMap.clear();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get all repository favorite files(now only support myDrive and myVault) in an aggregated way,
     * Note: the function must be executed in sub thread.
     */
    @Override
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
     * Note: the function must be executed in sub thread.
     *
     * @param paras {@link AllRepoFavFileRequestParas} request parameters, it supports pagination, sorting and search.
     */
    @Override
    public List<ParseJsonUtils.AllRepoFavoListBean> getAllRepoFavFileList(AllRepoFavFileRequestParas paras) throws RmsRestAPIException {
        log.v("getAllRepoFavFileList from RMS");
        try {
            SkyDRMApp.Session2 session = app.getSession();
            List<ParseJsonUtils.AllRepoFavoListBean> allRepoFavFileList = session
                    .getRmsRestAPI()
                    .getFavoriteService(session.getRmUser())
                    .getFavoriteFileListInAllRepos(paras);
            if (allRepoFavFileList != null) {
                log.v("check result:" + allRepoFavFileList.toString());
            }
            return allRepoFavFileList;
        } catch (SessionInvalidException | InvalidRMClientException e) {
            throw new RmsRestAPIException(e.getMessage());
        }
    }

    /**
     * Get favorites files in a repository or all repositories while repository_id specified as all.
     * Note: the function must be executed in sub thread.
     *
     * @param repositoryId repo id
     * @param lastModified optional
     */
    @Override
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

    public int getFavFileSize() {
        return favMap.size();
    }
}
