package com.skydrm.sdk.rms.rest.repository;

import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.skydrm.sdk.Config;
import com.skydrm.sdk.Factory;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.rest.IRepositoryService;
import com.skydrm.sdk.rms.types.RmsAddRepoResult;
import com.skydrm.sdk.rms.types.RmsRepoInfo;
import com.skydrm.sdk.rms.types.RmsUserLinkedRepos;
import com.skydrm.sdk.rms.user.IRmUser;
import com.skydrm.sdk.utils.DevLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * reference: https://bitbucket.org/nxtlbs-devops/rightsmanagement-wiki/wiki/RMS/RESTful%20API/Repository%20REST%20API
 */

public class Repository extends RestAPI.RestServiceBase implements IRepositoryService {

    public Repository(IRmUser user, OkHttpClient httpClient, Config config, DevLog log) {
        super(user, httpClient, config, log);
    }

    @Override
    public RmsUserLinkedRepos.ResultsBean repositoryGet() throws RmsRestAPIException {
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getRepositoryURL())
                .get()
                .build();

        String responseString = executeNetRequest(request);
        log.v("repositoryGet:\n" + responseString);

        try {
            //parse response
            JSONObject j = new JSONObject(responseString);
            if (!j.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = j.getInt("statusCode");
            if (code == 200) {
                RmsUserLinkedRepos rmsUserLinkedRepos = new Gson().fromJson(responseString, RmsUserLinkedRepos.class);
                return rmsUserLinkedRepos.getResults();
            } else if (code == 401) {
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public RmsAddRepoResult repositoryAdd(RmsUserLinkedRepos.ResultsBean.RepoItemsBean item, @Nullable RestAPI.Listener listener) throws RmsRestAPIException {
        if (listener == null) {
            listener = new RestAPI.Listener() {
                @Override
                public void progress(int current, int total) {

                }

                @Override
                public void currentState(String state) {

                }
            };
        }
        // prepare data
        JSONObject postJSON = new JSONObject();
        try {
            JSONObject parameters = new JSONObject();
            {
//                parameters.put("deviceId", Factory.getDeviceId());
//                parameters.put("deviceType", Factory.getDeviceType());
                // section repository
                JSONObject repository = new JSONObject();
                {
                    repository.put("name", item.getName());
                    repository.put("type", item.getType());
                    repository.put("isShared", item.isIsShared());
                    repository.put("accountName", item.getAccountName());
                    repository.put("accountId", item.getAccountId());
                    repository.put("token", item.getToken());
                    repository.put("preference", item.getPreference());
                    repository.put("creationTime", item.getCreationTime());
                }
                parameters.put("repository", repository.toString());
            }
            postJSON.put("parameters", parameters);
        } catch (JSONException e) {
            log.e("failed prepare post data in myDriveStorageUsed-" + e.toString(), e);
            throw new RmsRestAPIException("failed prepare post data in myDriveStorageUsed-", e);
        }
        {
            listener.currentState("prepare request");
            listener.progress(10, 100);
        }
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                postJSON.toString());
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getRepositoryURL())
                .post(body)
                .build();
        try {
            {
                listener.currentState("prepare to send");
                listener.progress(40, 100);
            }
            Response response = httpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new RmsRestAPIException("network failed" + response.code() + response.message());
            }
            {
                listener.currentState("analyze result");
                listener.progress(80, 100);
            }
            String responseString = response.body().string();
            try {
                JSONObject responseObj = new JSONObject(responseString);
                if (responseObj.has("statusCode")) {
                    int statusCode = responseObj.getInt("statusCode");
                    String message = "Unknown error.";
                    if (responseObj.has("message")) {
                        message = responseObj.getString("message");
                    }
                    if (statusCode != 200) {
                        throw new RmsRestAPIException(message);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            log.v("repositoryAdd:\n" + responseString);
            // 304: {"statusCode":304,"message":"Repository already exists","serverTime":1481702855356}
            return new Gson().fromJson(responseString, RmsAddRepoResult.class);
        } catch (IOException e) {
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.NetWorkIOFailed);
        }
    }

    @Override
    public boolean repositoryUpdate(String rmsRepoId, String repoNickName, String repoToken) throws RmsRestAPIException {
        // prepare data
        JSONObject postJSON = new JSONObject();
        try {
            JSONObject parameters = new JSONObject();
            {
                parameters.put("deviceId", Factory.getDeviceId());
                parameters.put("deviceType", Factory.getDeviceType());
                parameters.put("repoId", rmsRepoId);
                if (repoNickName != null && !repoNickName.isEmpty()) {
                    parameters.put("name", repoNickName);
                }
                if (repoToken != null && !repoToken.isEmpty()) {
                    parameters.put("token", repoToken);
                }
            }
            postJSON.put("parameters", parameters);
        } catch (JSONException e) {
            log.e("failed prepare post data in repositoryUpdate-" + e.toString(), e);
            throw new RmsRestAPIException("failed prepare post data in repositoryUpdate-", RmsRestAPIException.ExceptionDomain.Common);
        }
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                postJSON.toString());
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getRepositoryURL())
                .put(body)
                .build();

        String responseString = executeNetRequest(request);
        log.v("repositoryUpdate:\n" + responseString);
        try {
            JSONObject jo = new JSONObject(responseString);
            if (!jo.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = jo.getInt("statusCode");
            if (code == 200) {
                return true;
            } else if (code == 304) {
                throw new RmsRestAPIException("Repository name already exists", RmsRestAPIException.ExceptionDomain.RepoAlreadyExist, code);
            } else if (code == 400) {
                throw new RmsRestAPIException("Repository name is not valid", RmsRestAPIException.ExceptionDomain.RepoNameNotValid, code);
            } else if (code == 401) {
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else if (code == 404) {
                throw new RmsRestAPIException("Repository does not exist", RmsRestAPIException.ExceptionDomain.RepoNotExist, code);
            } else if (code == 409) {
                throw new RmsRestAPIException("There is already a repository with the given name", RmsRestAPIException.ExceptionDomain.RepoNameCollided, code);
            } else if (4001 == code) {
                throw new RmsRestAPIException("Repository Name Too Long.", RmsRestAPIException.ExceptionDomain.NameTooLong);
            } else if (4003 == code) {
                throw new RmsRestAPIException("Repository Name containing illegal special characters.",
                        RmsRestAPIException.ExceptionDomain.NamingViolation);
            } else {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public boolean repositoryUpdateNickName(String rmsRepoId, String repoNickName) throws RmsRestAPIException {
        return repositoryUpdate(rmsRepoId, repoNickName, null);
    }

    @Override
    public boolean repositoryUpdateToken(String rmsRepoId, String repoToken) throws RmsRestAPIException {
        return repositoryUpdate(rmsRepoId, null, repoToken);
    }

    @Override
    public String repositoryRemove(String repoId) throws RmsRestAPIException {
        // prepare data
        JSONObject postJSON = new JSONObject();
        try {
            JSONObject parameters = new JSONObject();
            {
                parameters.put("deviceId", Factory.getDeviceId());
                parameters.put("deviceType", Factory.getDeviceType());
                parameters.put("repoId", repoId);
            }
            postJSON.put("parameters", parameters);
        } catch (JSONException e) {
            log.e("failed prepare post data in myDriveStorageUsed-" + e.toString(), e);
            throw new RmsRestAPIException("failed prepare post data in repositoryRemove-", RmsRestAPIException.ExceptionDomain.Common);
        }
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                postJSON.toString());
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getRepositoryURL())
                .delete(body)
                .build();
        try {
            Response response = httpClient.newCall(request).execute();
            String responseString = response.body().string();
            log.v("repositoryRemove\n" + responseString);

            try {
                JSONObject jo = new JSONObject(responseString);
                if (!jo.has("statusCode")) {
                    throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
                }
                int code = jo.getInt("statusCode");
                // 204  delete successfully
                // 404 {"statusCode":404,"message":"Repository does not exist","serverTime":1481702950841}
                if (code >= 200 && code < 300) {
                    return responseString;
                } else if (code == 401) {
                    throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
                } else {
                    throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, code);
                }
            } catch (JSONException e) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
        } catch (IOException e) {
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.NetWorkIOFailed);
        }
    }


    @Override
    public String getAuthorizationURL(String type, String name, String siteURL) throws RmsRestAPIException {
        // prepare data
        JSONObject postJSON = new JSONObject();
        try {
            JSONObject parameters = new JSONObject();
            {
                parameters.put("type", type);
                parameters.put("name", name);
                parameters.put("platformId", Factory.getDeviceType());
                parameters.put("siteURL", siteURL);
            }
            postJSON.put("parameters", parameters);
        } catch (JSONException e) {
            log.e("failed prepare post data in getAuthorizationURL-" + e.toString(), e);
            throw new RmsRestAPIException("failed prepare post data in getAuthorizationURL-", RmsRestAPIException.ExceptionDomain.Common);
        }
        // prepare body
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                postJSON.toString());
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getRepositoryAuthUrl())
                .post(body)
                .build();
        // send and get result
        try {
            Response response = httpClient.newCall(request).execute();
            String responseString = response.body().string();
            log.w("getAuthorizationURL\n" + responseString);
            try {
                JSONObject jo = new JSONObject(responseString);
                if (!jo.has("statusCode")) {
                    throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
                }
                int code = jo.getInt("statusCode");
                // 204  delete successfully
                // 404 {"statusCode":404,"message":"Repository does not exist","serverTime":1481702950841}
                if (code >= 200 && code < 300) {
                    if (jo.has("results")) {
                        JSONObject results = jo.getJSONObject("results");
                        if (results.has("authURL")) {
                            String path = results.getString("authURL");
                            // RMS only return a path,  we need concatenate with rmsurl
                            path = config.getRMSURL() + path;
                            // as RMS defined, we must set user_id, ticket, tenant as request param
                            String[] params = new String[]{
                                    "userId", user.getUserIdStr(),
                                    "ticket", user.getTicket(),
                                    "tenantId", user.getTenantId(),
                                    "platformId", Factory.getDeviceType(),
                                    "clientId", Factory.getClientId()

                            };
                            // - build uril with params
                            StringBuilder buf = new StringBuilder();
                            String sep = "";
                            buf.append(URI.create(path).toASCIIString());
                            buf.append("&");
                            for (int i = 0; i < params.length; ) {
                                String key = params[i];
                                String value = params[i + 1];
                                if (key == null)
                                    throw new IllegalArgumentException("params[" + i + "] is null");
                                if (value != null) {
                                    buf.append(sep);
                                    sep = "&";
                                    buf.append(URLEncoder.encode(key, "UTF-8"));
                                    buf.append("=");
                                    buf.append(URLEncoder.encode(value, "UTF-8"));
                                }
                                i += 2;
                            }
                            path = buf.toString();
                            log.i("auth-URL:" + path);
                            return path;
                        } else {
                            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, code);
                        }
                    } else {
                        throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, code);
                    }
                } else if (code == 401) {
                    throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
                } else {
                    throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, code);
                }
            } catch (JSONException e) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
        } catch (IOException e) {
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.NetWorkIOFailed);
        }

    }

    @Override
    public RmsRepoInfo.ResultsBean getAuthorizationResultByURL(String url) throws RmsRestAPIException {
        // prepare body
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(url)
                .get()
                .build();

        String responseString = executeNetRequest(request);
        log.w("getAuthorizationResultByURL:\n" + responseString);

        try {
            //parse response
            JSONObject j = new JSONObject(responseString);
            if (!j.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = j.getInt("statusCode");
            if (code == 200) {
                RmsRepoInfo info = new Gson().fromJson(responseString, RmsRepoInfo.class);
                return info.getResults();
            } else if (code == 401) {
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }


    @Override
    public String getAccessTokenByRepoID(String repoID) throws RmsRestAPIException {
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getRepositoryAccessTokenUrl(repoID))
                .get()
                .build();

        String responseString = executeNetRequest(request);
        log.v("getAccessTokenByRepoID:\n" + responseString);
        try {
            //parse response
            JSONObject j = new JSONObject(responseString);
            if (!j.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = j.getInt("statusCode");
            if (code == 200) {
                JSONObject result = j.getJSONObject("results");
                return result.getString("accessToken");
            } else if (code == 401) {
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }
}
