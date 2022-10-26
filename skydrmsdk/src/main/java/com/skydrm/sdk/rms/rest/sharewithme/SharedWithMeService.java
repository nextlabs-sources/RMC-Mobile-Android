package com.skydrm.sdk.rms.rest.sharewithme;

import   android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.skydrm.sdk.Config;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.rest.ISharedWithMeService;
import com.skydrm.sdk.rms.user.IRmUser;
import com.skydrm.sdk.utils.DevLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/**
 * Reference: https://bitbucket.org/nxtlbs-devops/rightsmanagement-wiki/wiki/RMS/RESTful%20API/Shared%20With%20Me%20REST%20API#markdown-header-download-file
 */

public class SharedWithMeService extends RestAPI.RestServiceBase implements ISharedWithMeService {
    private Call mCall;

    public SharedWithMeService(IRmUser user, OkHttpClient httpClient, Call call, Config config, DevLog log) {
        super(user, httpClient, config, log);
        this.mCall = call;
    }

    @Override
    public SharedWithMeListFileResult listFile(@NonNull SharedWithMeListFileRequestParams params) throws RmsRestAPIException {
        String listUrl = config.getSharedwithmeListURL();
        StringBuilder urlParams = new StringBuilder();
        if (params.getPage() != -1 && params.getSize() != -1) {
            urlParams.append("page=").append(params.getPage())
                    .append("&size=").append(params.getSize());
        }
        urlParams.append(urlParams.length() == 0 ? "orderBy=" : "&orderBy=").append(params.getOrderBy());
        urlParams.append(urlParams.length() == 0 ? "q=" : "&q=").append(params.getQ());
        if (!TextUtils.isEmpty(params.getSearchStr())) {
            urlParams.append(urlParams.length() == 0 ? "searchString=" : "&searchString=").append(params.getSearchStr());
        }
        listUrl += urlParams.toString();
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder.url(listUrl)
                .get()
                .build();

        String response = executeNetRequest(request);
        log.i("RESTFUL_listFile\n" + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (!jsonObject.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = jsonObject.optInt("statusCode");

            if (code == 200) {
                return new Gson().fromJson(response, SharedWithMeListFileResult.class);
            } else if (code == 400) {
                throw new RmsRestAPIException(jsonObject.getString("message"), RmsRestAPIException.ExceptionDomain.Common);
            } else if (code == 401) {
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public SharedWithMeReshareResult reShareFile(@NonNull String transactionId,
                                                 @NonNull String transactionCode,
                                                 @NonNull List<String> members,
                                                 @Nullable String comments) throws RmsRestAPIException {
        if (members.size() == 0) {
            throw new RmsRestAPIException("Invalid shared with members", RmsRestAPIException.ExceptionDomain.Common);
        }
        StringBuilder shareWith = new StringBuilder();
        for (int i = 0; i < members.size(); i++) {
            if (i == members.size() - 1) {
                shareWith.append(members.get(i));
            } else {
                shareWith.append(members.get(i)).append(",");
            }
        }
        JSONObject postJson = new JSONObject();
        try {
            JSONObject parameters = new JSONObject();
            parameters.put("transactionId", transactionId);
            parameters.put("transactionCode", transactionCode);
            parameters.put("shareWith", shareWith.toString());
            if (!TextUtils.isEmpty(comments)) {
                parameters.put("comment", comments);
            }
            postJson.put("parameters", parameters);
        } catch (JSONException e) {
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
        }

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), postJson.toString());
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder.url(config.getSharedWithMeReshareURL())
                .post(body)
                .build();

        String response = executeNetRequest(request);
        log.i("RESTFUL_reShareFile\n" + response);
        try {
            JSONObject responseObj = new JSONObject(response);
            if (!responseObj.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int statusCode = responseObj.optInt("statusCode");
            String message = responseObj.optString("message");
            if (statusCode == 200) {
                return new Gson().fromJson(response, SharedWithMeReshareResult.class);
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
            throw new RmsRestAPIException("Response:" + e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public String download(String id, String code, boolean bForViewer, File f, int start, int length, RestAPI.DownloadListener listener) throws RmsRestAPIException {
        // sanity check
        if (id == null || id.isEmpty()) {
            throw new RmsRestAPIException("invalid id param", RmsRestAPIException.ExceptionDomain.Common);
        }
        if (code == null || code.isEmpty()) {
            throw new RmsRestAPIException("invalid code param", RmsRestAPIException.ExceptionDomain.Common);
        }
        if (f == null || !f.canWrite()) {
            throw new RmsRestAPIException("invalid f param", RmsRestAPIException.ExceptionDomain.Common);
        }
        if (listener == null) {
            listener = new RestAPI.DownloadListener() {
                @Override
                public void current(int i) {

                }

                @Override
                public void cancel() {

                }
            };
        }

        // prepare data
        JSONObject postJSON = new JSONObject();
        try {
            JSONObject parameters = new JSONObject();
            parameters.put("transactionId", id);
            parameters.put("transactionCode", code);
            parameters.put("forViewer", String.format(Locale.getDefault(), "%b", bForViewer));
            parameters.put("start", String.format(Locale.getDefault(), "%d", start));
            parameters.put("length", String.format(Locale.getDefault(), "%d", length));
            postJSON.put("parameters", parameters);
        } catch (JSONException e) {
            log.e(e);
            throw new RmsRestAPIException("failed prepare post data in SharedWithMeService-download", RmsRestAPIException.ExceptionDomain.Common);
        }
        // prepare post body
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                postJSON.toString());
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getSharedWithMeDownloadURL())
                .post(body)
                .build();

        Response response;
        try {
            mCall = httpClient.newCall(request);
            response = mCall.execute();
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
            } else {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, response.code());
            }
        }
        String filename = response.header("Content-Disposition");
        if (filename == null || filename.isEmpty()) {
            filename = "unknown";
        } else {
            filename = filename.substring(filename.lastIndexOf("'") + 1);
            try {
                filename = URLDecoder.decode(filename, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
                throw new RmsRestAPIException(e.getMessage(), e);
            }
        }
        //Download need to notify caller the progressing
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
            if (mCall.isCanceled()) {
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
