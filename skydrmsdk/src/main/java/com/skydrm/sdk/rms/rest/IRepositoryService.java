package com.skydrm.sdk.rms.rest;

import android.support.annotation.Nullable;

import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.types.RmsAddRepoResult;
import com.skydrm.sdk.rms.types.RmsRepoInfo;
import com.skydrm.sdk.rms.types.RmsUserLinkedRepos;

public interface IRepositoryService {
    RmsUserLinkedRepos.ResultsBean repositoryGet() throws RmsRestAPIException;

    RmsAddRepoResult repositoryAdd(RmsUserLinkedRepos.ResultsBean.RepoItemsBean item,
                                   @Nullable RestAPI.Listener listener) throws RmsRestAPIException;

    boolean repositoryUpdate(String rmsRepoId,
                             String repoNickName,
                             String repoToken) throws RmsRestAPIException;

    boolean repositoryUpdateNickName(String rmsRepoId, String repoNickName) throws RmsRestAPIException;

    boolean repositoryUpdateToken(String rmsRepoId, String repoToken) throws RmsRestAPIException;

    String repositoryRemove(String repoId) throws RmsRestAPIException;

    String getAuthorizationURL(String type, String name, String siteURL) throws RmsRestAPIException;

    RmsRepoInfo.ResultsBean getAuthorizationResultByURL(String url) throws RmsRestAPIException;

    String getAccessTokenByRepoID(String repoID) throws RmsRestAPIException;
}
