package com.skydrm.rmc.filemark;

import android.support.annotation.Nullable;

import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.types.favorite.AllRepoFavFileList;
import com.skydrm.sdk.rms.types.favorite.AllRepoFavFileRequestParas;
import com.skydrm.sdk.rms.types.favorite.OneRepoFavFiles;
import com.skydrm.sdk.rms.types.favorite.ReposFavorite;
import com.skydrm.sdk.utils.ParseJsonUtils;

import java.util.List;

/**
 * Created by aning on 8/17/2017.
 */

public interface IFavoriteMark extends IFileMark {

    /**
     * Get all repository favorite files(now only support myDrive and myVault) in an aggregated way,
     *  means the returned result is that for each drive, has its individual array aggregation of favorite files.
     */
    ReposFavorite getAllRepoFavFiles() throws RmsRestAPIException;

    /**
     * Get all repository favorite files(now only support myDrive and myVault) in a list fashion,
     * means the returned result is a list for all repository.
     *
     *  @param paras {@link AllRepoFavFileRequestParas} request parameters, it supports pagination, sorting and search.
     */
    List<ParseJsonUtils.AllRepoFavoListBean> getAllRepoFavFileList(AllRepoFavFileRequestParas paras) throws RmsRestAPIException;

    /**
     * Get favorites files in a repository or all repositories while repository_id specified as all.
     *
     * @param repositoryId repo id
     * @param lastModified optional
     */
    OneRepoFavFiles getSpecifiedRepoFavFiles(String repositoryId, @Nullable String lastModified) throws RmsRestAPIException;
}
