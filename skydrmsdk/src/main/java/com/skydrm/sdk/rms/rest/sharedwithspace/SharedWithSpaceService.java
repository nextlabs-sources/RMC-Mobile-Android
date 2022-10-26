package com.skydrm.sdk.rms.rest.sharedwithspace;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.skydrm.sdk.Config;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.rest.ISharedWithSpaceService;
import com.skydrm.sdk.rms.user.IRmUser;
import com.skydrm.sdk.utils.DevLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

public class SharedWithSpaceService extends RestAPI.RestServiceBase implements ISharedWithSpaceService {

    public SharedWithSpaceService(IRmUser user, OkHttpClient httpClient,
                                  Config config, DevLog log) {
        super(user, httpClient, config, log);
    }

    @Override
    public ListFileResult listFile(ListFileParams params) throws RmsRestAPIException {
        String url = config.getSharedwithmeListURL();
        StringBuilder urlParams = new StringBuilder(url);
        if (params.getPage() != -1 && params.getSize() != -1) {
            urlParams.append("page=").append(params.getPage())
                    .append("&size=").append(params.getSize());
        }
        urlParams.append(urlParams.length() == 0 ? "orderBy=" : "&orderBy=")
                .append(params.getOrderBy());
        urlParams.append(urlParams.length() == 0 ? "q=" : "&q=")
                .append(params.getQ());
        if (!TextUtils.isEmpty(params.getSearchStr())) {
            urlParams.append(urlParams.length() == 0 ? "searchString=" : "&searchString=")
                    .append(params.getSearchStr());
        }
        urlParams.append(urlParams.length() == 0 ? "fromSpace" : "&fromSpace=")
                .append(params.getFromSpaceType());
        urlParams.append(urlParams.length() == 0 ? "spaceId" : "&spaceId=")
                .append(params.getSpaceId());

        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);

        Request request = builder
                .url(urlParams.toString())
                .get()
                .build();

        String response = checkResponse(executeNetRequest(request));
        try {
            JSONObject responseObj = new JSONObject(response);
            int code = responseObj.optInt("statusCode");
            String message = responseObj.optString("message");

            if (code == 200) {
                return new Gson().fromJson(response, ListFileResult.class);
            } else if (code == 400) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.Common);
            } else if (code == 401) {
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else if (code == 404) {
                throw new RmsRestAPIException("Invalid file.", RmsRestAPIException.ExceptionDomain.FileNotFound, code);
            } else {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException(e.getMessage(), e);
        }
    }

    @Override
    public ReShareFileResult reShareFile(String transactionId, String transactionCode,
                                         String spaceId, List<String> recipients) throws RmsRestAPIException {
        if (recipients == null || recipients.size() == 0) {
            throw new RmsRestAPIException("Invalid shared with recipients.",
                    RmsRestAPIException.ExceptionDomain.Common);
        }
        JSONObject postObj = new JSONObject();
        try {
            JSONObject parametersObj = new JSONObject();
            parametersObj.put("transactionId", transactionId);
            parametersObj.put("transactionCode", transactionCode);
            parametersObj.put("spaceId", spaceId);

            JSONArray recipientsArr = new JSONArray();
            for (String s : recipients) {
                JSONObject recipientObj = new JSONObject();
                recipientObj.put("projectId", s);
                recipientsArr.put(recipientObj);
            }
            parametersObj.put("recipients", recipientsArr);

            postObj.put("parameters", parametersObj);
        } catch (JSONException e) {
            throw new RmsRestAPIException(e.getMessage(), e);
        }

        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);

        Request request = builder
                .url(config.getSharedWithMeReshareURL())
                .post(RequestBody.create(MediaType.parse("application/json"), postObj.toString()))
                .build();

        String response = executeNetRequest(request);
        try {
            JSONObject responseObj = new JSONObject(response);
            int statusCode = responseObj.optInt("statusCode");
            String message = responseObj.optString("message");
            if (statusCode == 200) {
                return new Gson().fromJson(response, ReShareFileResult.class);
            } else if (statusCode == 400) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.Invalid_Transaction, statusCode);
            } else if (statusCode == 401) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.AuthenticationFailed, statusCode);
            } else if (statusCode == 403) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.File_Share_Deny, statusCode);
            } else if (statusCode == 4001) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.File_Revoked, statusCode);
            } else {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.Common, statusCode);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException(e.getMessage(), e);
        }
    }

    @Override
    public String downloadFile(String localPath, String spaceId, boolean forViewer,
                               String transactionId, String transactionCode,
                               RestAPI.DownloadListener listener, int... args) throws RmsRestAPIException {
        File f = new File(localPath);
        if (f.isDirectory() || !f.exists()) {
            throw new RmsRestAPIException("Invalid file status.",
                    RmsRestAPIException.ExceptionDomain.Common);
        }

        JSONObject postObj = new JSONObject();
        try {
            JSONObject parametersObj = new JSONObject();
            parametersObj.put("transactionId", transactionId);
            parametersObj.put("transactionCode", transactionCode);
            parametersObj.put("forViewer", forViewer);
            for (int i = 0; i < args.length; ++i) {
                if (i == 0) {
                    parametersObj.put("start", args[0]);
                } else if (i == 1) {
                    parametersObj.put("length", args[1]);
                }
            }
            parametersObj.put("spaceId", spaceId);
            postObj.put("parameters", parametersObj);
        } catch (JSONException e) {
            throw new RmsRestAPIException(e.getMessage(), e);
        }

        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);

        Request request = builder
                .url(config.getSharedWithMeDownloadURL())
                .post(RequestBody.create(MediaType.parse("application/json"), postObj.toString()))
                .build();

        Response response;
        Call call;
        try {
            call = httpClient.newCall(request);
            response = call.execute();
        } catch (IOException e) {
            log.e(e);
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.NetWorkIOFailed);
        }
        // prepare to receive application/octet-stream with Listener
        if (!response.isSuccessful()) {
            if (response.code() == 401) {
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, 401);
            } else if (response.code() == 403) {
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AccessDenied, 403);
            } else if (response.code() == 404) {
                throw new RmsRestAPIException("Invalid file.", RmsRestAPIException.ExceptionDomain.FileNotFound, response.code());
            } else if (response.code() == 500) {
                throw new RmsRestAPIException("Internal server error.", RmsRestAPIException.ExceptionDomain.InternalServerError, response.code());
            } else {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, response.code());
            }
        }
        String filename = response.header("Content-Disposition");
        if (filename != null && !filename.isEmpty()) {
            filename = filename.substring(filename.lastIndexOf("'") + 1);
            try {
                filename = URLDecoder.decode(filename, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
                throw new RmsRestAPIException(e.getMessage(), e);
            }
        }
        //download progress callback.
        try {
            final int DOWNLOAD_CHUNK_SIZE = 2048;
            BufferedSource source = response.body().source();
            BufferedSink sink = Okio.buffer(Okio.sink(f));
            long contentLength = response.body().contentLength();
            long totalRead = 0;
            long hasRead;
            while ((hasRead = source.read(sink.buffer(), DOWNLOAD_CHUNK_SIZE)) != -1) {
                totalRead += hasRead;
                int progress = (int) (totalRead * 100.0 / contentLength);
                if (listener != null) {
                    listener.current(progress);
                }
            }
            if (listener != null) {
                listener.current(100);
            }
            sink.writeAll(source);
            sink.flush();
            sink.close();
        } catch (FileNotFoundException e) {
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.FileNotFound);
        } catch (IOException e) {
            f.delete();
            if (call.isCanceled()) {
                if (listener != null) {
                    listener.cancel();
                }
            } else {
                throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.FileIOFailed);
            }
        } catch (Exception e) {
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
        }
        return filename;
    }

    @Override
    public FileMetadata getMetadata(String transactionId, String transactionCode, String spaceId)
            throws RmsRestAPIException {
        String url = config.getSharedWithMeMetadataURL(transactionId, transactionCode);

        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);

        Request request = builder
                .url(url.concat("?spaceId=").concat(spaceId))
                .get()
                .build();

        String response = checkResponse(executeNetRequest(request));
        try {
            JSONObject responseObj = new JSONObject(response);
            int statusCode = responseObj.optInt("statusCode");
            String message = responseObj.optString("message");

            if (statusCode == 200) {
                return new Gson().fromJson(response, FileMetadata.class);
            } else if (statusCode == 401) {
                throw new RmsRestAPIException(message,
                        RmsRestAPIException.ExceptionDomain.AuthenticationFailed, statusCode);
            } else if (statusCode == 404) {
                throw new RmsRestAPIException("Invalid file.",
                        RmsRestAPIException.ExceptionDomain.FileNotFound, statusCode);
            } else if (statusCode == 4001) {
                throw new RmsRestAPIException(message,
                        RmsRestAPIException.ExceptionDomain.InvitationExpired, statusCode);
            } else if (statusCode == 4002) {
                throw new RmsRestAPIException(message,
                        RmsRestAPIException.ExceptionDomain.InvitationAlreadyDeclined, statusCode);
            } else if (statusCode == 4006) {
                throw new RmsRestAPIException(message,
                        RmsRestAPIException.ExceptionDomain.InvitationAlreadyRevoked, statusCode);
            } else if (statusCode == 500) {
                throw new RmsRestAPIException(message,
                        RmsRestAPIException.ExceptionDomain.InternalServerError, statusCode);
            } else {
                throw new RmsRestAPIException(message,
                        RmsRestAPIException.ExceptionDomain.Common, statusCode);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException(e.getMessage(), e);
        }
    }

    @Override
    public String decryptFile(String localPath,
                              String transactionId, String transactionCode,
                              String spaceId,
                              RestAPI.DownloadListener listener) throws RmsRestAPIException {
        File f = new File(localPath);
        if (f.isDirectory() || !f.exists()) {
            throw new RmsRestAPIException("Invalid file status.",
                    RmsRestAPIException.ExceptionDomain.Common);
        }

        JSONObject postObj = new JSONObject();
        try {
            JSONObject parametersObj = new JSONObject();
            parametersObj.put("transactionCode", transactionCode);
            parametersObj.put("transactionId", transactionId);
            parametersObj.put("spaceId", spaceId);

            postObj.put("parameters", parametersObj);
        } catch (JSONException e) {
            throw new RmsRestAPIException(e.getMessage(), e);
        }

        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);

        Request request = builder
                .url(config.getSharedWithMeDecryptURL())
                .post(RequestBody.create(MediaType.parse("application/json"), postObj.toString()))
                .build();

        Call call;
        Response response;
        try {
            call = httpClient.newCall(request);
            response = call.execute();
        } catch (IOException e) {
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.NetWorkIOFailed);
        }
        if (!response.isSuccessful()) {
            if (response.code() == 401) {
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, 401);
            } else if (response.code() == 403) {
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AccessDenied, 403);
            } else {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, response.code());
            }
        }
        String filename = response.header("Content-Disposition");
        if (filename != null && !filename.isEmpty()) {
            filename = filename.substring(filename.lastIndexOf("'") + 1);
            try {
                filename = URLDecoder.decode(filename, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
                throw new RmsRestAPIException(e.getMessage(), e);
            }
        }

        //download progress callback.
        try {
            final int DOWNLOAD_CHUNK_SIZE = 2048;
            BufferedSource source = response.body().source();
            BufferedSink sink = Okio.buffer(Okio.sink(f));
            long contentLength = response.body().contentLength();
            long totalRead = 0;
            long hasRead;
            while ((hasRead = source.read(sink.buffer(), DOWNLOAD_CHUNK_SIZE)) != -1) {
                totalRead += hasRead;
                int progress = (int) (totalRead * 100.0 / contentLength);
                listener.current(progress);
            }
            listener.current(100);
            sink.writeAll(source);
            sink.flush();
            sink.close();
        } catch (FileNotFoundException e) {
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.FileNotFound);
        } catch (IOException e) {
            f.delete();
            if (call.isCanceled()) {
                listener.cancel();
            } else {
                throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.FileIOFailed);
            }
        } catch (Exception e) {
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
        }
        return filename;
    }

}
