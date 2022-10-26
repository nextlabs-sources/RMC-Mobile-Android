package com.skydrm.sdk.rms.rest.viewer;

import com.google.gson.Gson;
import com.skydrm.sdk.Config;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.rest.IRemoteViewService;
import com.skydrm.sdk.rms.types.RemoteViewProjectFileParas;
import com.skydrm.sdk.rms.types.RemoteViewRepoFileParas;
import com.skydrm.sdk.rms.types.RemoteViewResult2;
import com.skydrm.sdk.rms.user.IRmUser;
import com.skydrm.sdk.utils.ConvertProgress;
import com.skydrm.sdk.utils.DevLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class RemoteView extends RestAPI.RestServiceBase implements IRemoteViewService {
    private Call mCall;

    public RemoteView(IRmUser user, OkHttpClient httpClient, Config config, DevLog log) {
        super(user, httpClient, config, log);
    }

    @Override
    public RemoteViewResult2 remoteViewLocalFile(String tenantId, File file, RestAPI.IConvertListener convertListener) throws RmsRestAPIException {
        // prepare request body
        JSONObject postJson = new JSONObject();
        JSONObject parameters = new JSONObject();

        String encodeFileName;
        try {
            encodeFileName = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8.name());
            parameters.put("userName", user.getName());
            parameters.put("tenantId", user.getTenantId());
            parameters.put("tenantName", tenantId);
//            parameters.put("fileName", encodeFileName);
            // as http://bugs.cn.nextlabs.com/show_bug.cgi?id=44906 said file name in json does not need to urlencode
            parameters.put("fileName", file.getName());

            // offset: The time difference between UTC time and local time, in minutes. Used for generating timestamp for watermarks,
            // if you don't fill anything watermark is based on server time which is UTC-0
            // parameters.put("offset", "-480");

            // supported operations: View File Info 1; Print 2; Protect 4; Share 8; Download 16.
            // clients can either use operations from RMS or implement their own(pass this para 0, if don't fill this para, ideally no buttons will be displayed)
            parameters.put("operations", 0);
            postJson.put("parameters", parameters);
        } catch (JSONException e) {
            log.i("failed prepare post data in remoteViewer-" + e.toString());
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
        } catch (UnsupportedEncodingException e) {
            log.i("unsupported url encoding in remoteViewer-" + e.toString());
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
        }

        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("API-input", postJson.toString())
                .addFormDataPart("file", encodeFileName,
                        RequestBody.create(MediaType.parse("application/octet-stream"), file))
                .build();

        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getRemoteViewLocalURL())
                .post(body)
                .build();

        // simulate the progress of conversion
        ConvertProgress convertThread = new ConvertProgress(file, convertListener);
        convertThread.start();

        Response response;
        String responseString;
        try {
            mCall = httpClient.newCall(request);
            response = mCall.execute();
            responseString = response.body().string();
            log.i("remoteViewer:\n" + responseString);
        } catch (IOException e) {
            if (mCall.isCanceled()) {  // user cancel.
                convertThread.cancel();
                return null;
            } else {
                throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.NetWorkIOFailed);
            }
        }

        try {
            JSONObject jo = new JSONObject(responseString);
            if (!jo.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = jo.getInt("statusCode");
            if (code == 200) {
                return new Gson().fromJson(responseString, RemoteViewResult2.class);
            } else if (code == 400) {
                throw new RmsRestAPIException("Invalid request data", RmsRestAPIException.ExceptionDomain.MalformedRequest, code);
            } else if (code == 401) {
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else if (code == 403) {
                throw new RmsRestAPIException("Access Denied", RmsRestAPIException.ExceptionDomain.AccessDenied, code);
            } else if (code == 404) {
                throw new RmsRestAPIException("File not found", RmsRestAPIException.ExceptionDomain.NotFound, code);
            } else if (code == 415) {
                throw new RmsRestAPIException("File type not supported", RmsRestAPIException.ExceptionDomain.FileTypeNotSupported, code);
            } else if (code == 500) {
                throw new RmsRestAPIException("Internal Server Error.", RmsRestAPIException.ExceptionDomain.InternalServerError, code);
            } else if (code == 5007) {
                throw new RmsRestAPIException("Invalid/corrupt NXL file.", RmsRestAPIException.ExceptionDomain.InvalidNxlFile, code);
            } else if (code == 5008) {
                throw new RmsRestAPIException("Missing dependencies. Assembly files are not supported as of now.", RmsRestAPIException.ExceptionDomain.MissingDependencies, code);
            } else {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }

    }

    @Override
    public RemoteViewResult2 remoteViewRepoFile(RemoteViewRepoFileParas paras) throws RmsRestAPIException {
        // prepare data
        JSONObject postJSON = new JSONObject();
        try {
            JSONObject parameters = new JSONObject();
            parameters.put("repoId", paras.getRepoId());
            parameters.put("pathId", paras.getPathId());
            parameters.put("pathDisplay", paras.getPathDisplay());
            // parameters.put("offset", paras.getOffset());
            parameters.put("repoName", paras.getRepoName());
            parameters.put("repoType", paras.getRepoType());
            parameters.put("email", paras.getEmail());
            parameters.put("tenantName", paras.getTenantName());
            parameters.put("lastModifiedDate", paras.getLastModifiedDate());
            parameters.put("operations", paras.getOperation());
            postJSON.put("parameters", parameters);
        } catch (JSONException e) {
            log.i("failed prepare post data in remoteViewer-" + e.toString());
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
        }
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                postJSON.toString());
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getRemoteViewRepoURL())
                .post(body)
                .build();

        String responseString = executeNetRequest(request);
        log.i("remoteViewRepoFile:\n" + responseString);

        // parse response
        try {
            JSONObject jo = new JSONObject(responseString);
            if (!jo.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = jo.getInt("statusCode");
            if (code == 200) {
                return new Gson().fromJson(responseString, RemoteViewResult2.class);
            } else if (code == 400) {
                throw new RmsRestAPIException("Invalid request data", RmsRestAPIException.ExceptionDomain.MalformedRequest, code);
            } else if (code == 401) {
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else if (code == 403) {
                throw new RmsRestAPIException("Access Denied", RmsRestAPIException.ExceptionDomain.AccessDenied, code);
            } else if (code == 404) {
                throw new RmsRestAPIException("File not found", RmsRestAPIException.ExceptionDomain.NotFound, code);
            } else if (code == 415) {
                throw new RmsRestAPIException("File type not supported", RmsRestAPIException.ExceptionDomain.FileTypeNotSupported, code);
            } else if (code == 500) {
                throw new RmsRestAPIException("Internal Server Error.", RmsRestAPIException.ExceptionDomain.InternalServerError, code);
            } else if (code == 5007) {
                throw new RmsRestAPIException("Invalid/corrupt NXL file.", RmsRestAPIException.ExceptionDomain.InvalidNxlFile, code);
            } else if (code == 5008) {
                throw new RmsRestAPIException("Missing dependencies. Assembly files are not supported as of now.", RmsRestAPIException.ExceptionDomain.MissingDependencies, code);
            } else {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }

    }

    // will use this api in next release.
    @Override
    public RemoteViewResult2 remoteViewProjectFile(RemoteViewProjectFileParas paras) throws RmsRestAPIException {
        // prepare data
        JSONObject postJSON = new JSONObject();
        try {
            JSONObject parameters = new JSONObject();
            parameters.put("projectId", paras.getProjectId());
            parameters.put("pathId", paras.getPathId());
            parameters.put("pathDisplay", paras.getPathDisplay());
            // parameters.put("offset", paras.getOffset());
            parameters.put("email", paras.getEmail());
            parameters.put("tenantName", paras.getTenantName());
            parameters.put("lastModifiedDate", paras.getLastModifiedDate());
            parameters.put("operations", paras.getOperation());
            postJSON.put("parameters", parameters);
        } catch (JSONException e) {
            log.i("failed prepare post data in remoteViewer-" + e.toString());
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
        }
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                postJSON.toString());
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getRemoteViewProjectURL())
                .post(body)
                .build();

        String responseString = executeNetRequest(request);
        log.i("remoteViewProjectFile:\n" + responseString);

        // parse response
        try {
            JSONObject jo = new JSONObject(responseString);
            if (!jo.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = jo.getInt("statusCode");
            if (code == 200) {
                return new Gson().fromJson(responseString, RemoteViewResult2.class);
            } else if (code == 400) {
                throw new RmsRestAPIException("Invalid request data", RmsRestAPIException.ExceptionDomain.MalformedRequest, code);
            } else if (code == 401) {
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else if (code == 403) {
                throw new RmsRestAPIException("Access Denied", RmsRestAPIException.ExceptionDomain.AccessDenied, code);
            } else if (code == 404) {
                throw new RmsRestAPIException("File not found", RmsRestAPIException.ExceptionDomain.NotFound, code);
            } else if (code == 415) {
                throw new RmsRestAPIException("File type not supported", RmsRestAPIException.ExceptionDomain.FileTypeNotSupported, code);
            } else if (code == 500) {
                throw new RmsRestAPIException("Internal Server Error.", RmsRestAPIException.ExceptionDomain.InternalServerError, code);
            } else if (code == 5007) {
                throw new RmsRestAPIException("Invalid/corrupt NXL file.", RmsRestAPIException.ExceptionDomain.InvalidNxlFile, code);
            } else if (code == 5008) {
                throw new RmsRestAPIException("Missing dependencies. Assembly files are not supported as of now.", RmsRestAPIException.ExceptionDomain.MissingDependencies, code);
            } else {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public void cancel() {
        if (mCall != null) {
            mCall.cancel();
        }
    }
}
