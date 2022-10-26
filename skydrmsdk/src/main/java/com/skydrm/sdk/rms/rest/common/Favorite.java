package com.skydrm.sdk.rms.rest.common;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.skydrm.sdk.Config;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.rest.IFavoriteService;
import com.skydrm.sdk.rms.types.favorite.AllRepoFavFileRequestParas;
import com.skydrm.sdk.rms.types.favorite.FavoriteList;
import com.skydrm.sdk.rms.types.favorite.OneRepoFavFiles;
import com.skydrm.sdk.rms.types.favorite.ReposFavorite;
import com.skydrm.sdk.rms.user.IRmUser;
import com.skydrm.sdk.utils.DevLog;
import com.skydrm.sdk.utils.ParseJsonUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class Favorite extends RestAPI.RestServiceBase implements IFavoriteService {

    public Favorite(IRmUser user, OkHttpClient httpClient, Config config, DevLog log) {
        super(user, httpClient, config, log);
    }

    @Override
    public ReposFavorite getFavoriteFilesInAllRepos() throws RmsRestAPIException {
        String userId = user.getUserIdStr();
        String ticket = user.getTicket();
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getAllFavoriteOfflineFiles())
                .get()
                .build();
        String responseString = executeNetRequest(request);
        log.i("getFavoriteFilesInAllRepos:\t" + "userId:" + userId + "\t" + "ticket:" + ticket + "\n" + responseString);

        try {
            JSONObject jo = new JSONObject(responseString);
            if (!jo.has("statusCode")) {
                log.e("failed parse response");
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = jo.getInt("statusCode");
            if (code == 200) {
                return new Gson().fromJson(jo.getJSONObject("results").toString(), ReposFavorite.class);
            } else if (code == 401) {
                log.e("Authentication failed");
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else {
                log.e("failed parse response");
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JsonSyntaxException | JSONException e) {
            log.e("failed parse response:" + e.getMessage());
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }

    }

    @Override
    public List<ParseJsonUtils.AllRepoFavoListBean> getFavoriteFileListInAllRepos(AllRepoFavFileRequestParas paras) throws RmsRestAPIException {
        if (paras == null) {
            throw new RmsRestAPIException("paras is null", RmsRestAPIException.ExceptionDomain.Common);
        }
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(buildUrl(paras))
                .get()
                .build();
        String responseString = executeNetRequest(request);
        log.i("getFavoriteFileListInAllRepos:\t" + "\n" + responseString);

        try {
            JSONObject jo = new JSONObject(responseString);
            if (!jo.has("statusCode")) {
                log.e("failed parse response");
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = jo.getInt("statusCode");
            if (code == 200) {
                return ParseJsonUtils.parseResultJson(responseString);
            } else if (code == 401) {
                log.e("Authentication failed");
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else {
                log.e("failed parse response");
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JsonSyntaxException | JSONException e) {
            log.e("failed parse response:" + e.getMessage());
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public OneRepoFavFiles getFavoriteFilesInOneRepo(String repositoryId, @Nullable String lastModified) throws RmsRestAPIException {
        if (TextUtils.isEmpty(repositoryId)) {
            throw new RmsRestAPIException("repositoryId is null or empty.", RmsRestAPIException.ExceptionDomain.Common);
        }
        String urlPath = config.getOneRepoFavoriteURL();
        urlPath = urlPath.replace("{repository_id}", repositoryId);
        if (!TextUtils.isEmpty(lastModified)) {
            urlPath += "?";
            urlPath += lastModified;
        }

        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(urlPath)
                .get()
                .build();
        String responseString = executeNetRequest(request);
        log.i("getFavoriteFilesInOneRepo:\t" + "\n" + responseString);

        try {
            JSONObject jo = new JSONObject(responseString);
            if (!jo.has("statusCode")) {
                log.e("failed parse response");
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = jo.getInt("statusCode");
            if (code == 200) {
                return new Gson().fromJson(jo.getJSONObject("results").toString(), OneRepoFavFiles.class);
            } else if (code == 401) {
                log.e("Authentication failed");
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else {
                log.e("failed parse response");
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JsonSyntaxException | JSONException e) {
            log.e("failed parse response:" + e.getMessage());
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }

    }

    @Override
    public void markAsFavorite(String repoId, List<FavoriteList.Item> itemList) throws RmsRestAPIException {
        // sanity check
        if (itemList == null) {
            throw new RmsRestAPIException("list is null", RmsRestAPIException.ExceptionDomain.Common);
        }
        if (repoId == null || repoId.isEmpty()) {
            throw new RmsRestAPIException("rmsId is null or empty", RmsRestAPIException.ExceptionDomain.Common);
        }

        // prepare data
        JSONObject postJSON = new JSONObject();
        try {
            JSONObject parameters = new JSONObject();
            JSONArray files = new JSONArray();
            {
                for (FavoriteList.Item i : itemList) {
                    JSONObject favItem = new JSONObject();
                    favItem.put("pathId", i.pathId);
                    favItem.put("pathDisplay", i.displayPath);
                    favItem.put("parentFileId", i.parentFileId);
                    favItem.put("fileSize", i.fileSize);
                    favItem.put("fileLastModified", i.lastModifiedTime);
                    // add into array
                    files.put(favItem);
                }
            }
            parameters.put("files", files);
            postJSON.put("parameters", parameters);
        } catch (JSONException e) {
            log.e(e);
            throw new RmsRestAPIException("failed prepare post data in markAsFavorite", RmsRestAPIException.ExceptionDomain.Common);
        }
        // build body
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                postJSON.toString());
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getSyncFavoriteURL(repoId))
                .post(body)
                .build();
        Response response;
        String responseString;
        // send request
        try {
            response = httpClient.newCall(request).execute();
            responseString = response.body().string();
            log.i("markAsFavorite\n" + responseString);
        } catch (IOException e) {
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.NetWorkIOFailed);
        }
        // parse result
        try {
            JSONObject j = new JSONObject(responseString);
            if (!j.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = j.getInt("statusCode");
            if (code >= 200 && code < 300) {
                return;
            } else if (code == 400) {
                throw new RmsRestAPIException("Malformed request", RmsRestAPIException.ExceptionDomain.MalformedRequest, code);
            } else if (code == 401) {
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else if (code == 404) {
                throw new RmsRestAPIException("Parent Folder missing", RmsRestAPIException.ExceptionDomain.NotFound, code);
            } else if (code == 500) {
                throw new RmsRestAPIException("Internal Server Error", RmsRestAPIException.ExceptionDomain.InternalServerError, code);
            } else {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }

    }

    @Override
    public void unmarkAsFavorite(String repoId, List<FavoriteList.Item> itemList) throws RmsRestAPIException {

        // sanity check
        if (itemList == null) {
            throw new RmsRestAPIException("list is null", RmsRestAPIException.ExceptionDomain.Common);
        }
        if (repoId == null || repoId.isEmpty()) {
            throw new RmsRestAPIException("rmsId is null or empty", RmsRestAPIException.ExceptionDomain.Common);
        }

        // prepare data
        JSONObject postJSON = new JSONObject();
        try {
            JSONObject parameters = new JSONObject();
            JSONArray files = new JSONArray();
            {
                for (FavoriteList.Item i : itemList) {
                    JSONObject favItem = new JSONObject();
                    favItem.put("pathId", i.pathId);
                    favItem.put("pathDisplay", i.displayPath);
                    // add into array
                    files.put(favItem);
                }
            }
            parameters.put("files", files);
            postJSON.put("parameters", parameters);
        } catch (JSONException e) {
            log.e(e);
            throw new RmsRestAPIException("failed prepare post data in markAsFavorite", RmsRestAPIException.ExceptionDomain.Common);
        }
        // build body
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                postJSON.toString());
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getSyncFavoriteURL(repoId))
                .delete(body)
                .build();
        Response response;
        String responseString;
        // send request
        try {
            response = httpClient.newCall(request).execute();
            responseString = response.body().string();
            log.i("unmarkAsFavorite\n" + responseString);
        } catch (IOException e) {
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.NetWorkIOFailed);
        }
        // parse result
        try {
            JSONObject j = new JSONObject(responseString);
            if (!j.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = j.getInt("statusCode");
            if (code >= 200 && code < 300) {
                return;
            } else if (code == 400) {
                throw new RmsRestAPIException("Malformed request", RmsRestAPIException.ExceptionDomain.MalformedRequest, code);
            } else if (code == 401) {
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else if (code == 404) {
                throw new RmsRestAPIException("Parent Folder missing", RmsRestAPIException.ExceptionDomain.NotFound, code);
            } else if (code == 500) {
                throw new RmsRestAPIException("Internal Server Error", RmsRestAPIException.ExceptionDomain.InternalServerError, code);
            } else {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    private String buildUrl(AllRepoFavFileRequestParas requestParas) {
        String urlPath = config.getAllFavoriteFileList();
        StringBuilder stringBuilder = new StringBuilder();
        if (requestParas.getPage() != -1 && requestParas.getSize() != -1) {
            stringBuilder.append("?page=");
            stringBuilder.append(requestParas.getPage());
            stringBuilder.append("&size=");
            stringBuilder.append(requestParas.getSize());
        }

        if (!TextUtils.isEmpty(requestParas.getOrderBy())) {
            if (stringBuilder.length() == 0) {
                stringBuilder.append("?orderBy=");
                stringBuilder.append(requestParas.getOrderBy());
            } else {
                stringBuilder.append("&orderBy=");
                stringBuilder.append(requestParas.getOrderBy());
            }
        }

        if (!TextUtils.isEmpty(requestParas.getSearchText())) {
            if (stringBuilder.length() == 0) {
                stringBuilder.append("?q.fileName=");
                stringBuilder.append(requestParas.getSearchText());
            } else { // have para before it, need add "&"
                stringBuilder.append("&q.fileName=");
                stringBuilder.append(requestParas.getSearchText());
            }
        }
        urlPath += stringBuilder.toString();
        return urlPath;
    }
}
