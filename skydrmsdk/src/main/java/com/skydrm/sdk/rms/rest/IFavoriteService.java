package com.skydrm.sdk.rms.rest;

import android.support.annotation.Nullable;

import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.types.favorite.AllRepoFavFileRequestParas;
import com.skydrm.sdk.rms.types.favorite.FavoriteList;
import com.skydrm.sdk.rms.types.favorite.OneRepoFavFiles;
import com.skydrm.sdk.rms.types.favorite.ReposFavorite;
import com.skydrm.sdk.utils.ParseJsonUtils;

import java.util.List;

public interface IFavoriteService {
    /**
     * This API is used to get all the favorite files in all the repositories of the (user+tenant) in an aggregated way.
     */
    ReposFavorite getFavoriteFilesInAllRepos() throws RmsRestAPIException;

    /**
     * This API is used to get all the favorite files in all the repositories of the (user+tenant) in a list fashion,
     * which means the results are a list of repoContents instead of aggregated jsonRepoFiles by different repositories.
     * Besides, it supports pagination, sorting and search.
     *
     * @param paras {@link AllRepoFavFileRequestParas} request value.
     */
    List<ParseJsonUtils.AllRepoFavoListBean> getFavoriteFileListInAllRepos(AllRepoFavFileRequestParas paras) throws RmsRestAPIException;

    /**
     * This API is used to get favorites files in a repository or all repositories while repository_id specified as all (with an optional lastModified parameter).
     *
     * @param repositoryId repository id
     * @param lastModified optional para, If lastModified parameter is not present or lastModified date is more than 30 days before the current date,
     *                     a complete list of favorite files will be returned, otherwise only the files that are marked/unmarked after lastModified will be returned.
     *                     lastModified should be in unix (epoch) time.
     */
    OneRepoFavFiles getFavoriteFilesInOneRepo(String repositoryId, @Nullable String lastModified) throws RmsRestAPIException;

    /**
     * This API is used to mark files in a repository as favorite
     */
    void markAsFavorite(String repoId, List<FavoriteList.Item> itemList) throws RmsRestAPIException;

    /**
     * This API is used to unmark files in a repository as favorite
     */
    void unmarkAsFavorite(String repoId, List<FavoriteList.Item> itemList) throws RmsRestAPIException;
}
