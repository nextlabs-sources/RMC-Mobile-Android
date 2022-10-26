package com.skydrm.sdk.rms.rest.common;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.skydrm.sdk.Config;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.rest.ILogService;
import com.skydrm.sdk.rms.types.FetchLogRequestParas;
import com.skydrm.sdk.rms.types.FetchLogResult;
import com.skydrm.sdk.rms.types.SendLogRequestValue;
import com.skydrm.sdk.rms.user.IRmUser;
import com.skydrm.sdk.utils.CsvWriter;
import com.skydrm.sdk.utils.DevLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringWriter;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static com.skydrm.sdk.utils.NxCommonUtils.gzipCompress;


public class Log extends RestAPI.RestServiceBase implements ILogService {

    public Log(IRmUser user, OkHttpClient httpClient, Config config, DevLog log) {
        super(user, httpClient, config, log);
    }

    @Override
    public boolean sendLogToRms(SendLogRequestValue logRequestValue) throws RmsRestAPIException {
        // generate request csv
        String[] arrayData = {logRequestValue.getmDuid(),
                logRequestValue.getmOwnerId(),
                String.valueOf(logRequestValue.getmUserId()),
                String.valueOf(logRequestValue.getmOperationId()),
                logRequestValue.getmDeviceId(),
                String.valueOf(logRequestValue.getmDeviceType()),
                logRequestValue.getmRepositoryId(),
                logRequestValue.getmFilePathId(),
                logRequestValue.getmFileName(),
                logRequestValue.getmFilePath(),
                logRequestValue.getmAppName(),
                logRequestValue.getmAppPath(),
                logRequestValue.getmAppPublisher(),
                String.valueOf(logRequestValue.getmAccessResult()),
                String.valueOf(logRequestValue.getmAccessTime()),
                logRequestValue.getmActivityData()
        };
        // use a tiny CSV lib to in utils package
        StringWriter stringWriter = new StringWriter();
        try {
            CsvWriter csvWriter = new CsvWriter(stringWriter, ',');
            csvWriter.setForceQualifier(true);
            csvWriter.writeRecord(arrayData);
            csvWriter.close();
        } catch (IOException e) {
            log.e(e);
        }

        log.v("log.csv:" + stringWriter.toString());

        // gzip
        byte[] data = gzipCompress(stringWriter.toString());
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/csv"), data);
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getSendLogURL())
                .put(requestBody)
                .addHeader("Content-Encoding", "gzip")
                .build();
        String responseString = executeNetRequest(request);
        log.v("sendLogToRms:\n" + responseString);
        try {
            JSONObject jo = new JSONObject(responseString);
            if (!jo.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = jo.getInt("statusCode");
            if (code == 200) {
                return true;
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
    public FetchLogResult fetchActivityLog(String duid, FetchLogRequestParas requestValue) throws RmsRestAPIException {
        // build the request para into url by requestValue
        String urlPath = config.getFetchLogURL();
        urlPath = urlPath.replace("{DUID}", duid);
        StringBuilder stringBuilder = new StringBuilder();
        if (requestValue.getCount() != -1 && requestValue.getCount() != -1) {
            stringBuilder.append("start=");
            stringBuilder.append(requestValue.getStart());
            stringBuilder.append("&count=");
            stringBuilder.append(requestValue.getCount());
        }

        if (!TextUtils.isEmpty(requestValue.getSearchField())) {
            if (stringBuilder.length() == 0) {
                stringBuilder.append("searchField=");
                stringBuilder.append(requestValue.getSearchField());
            } else { // have para before it, need add "&"
                stringBuilder.append("&searchField=");
                stringBuilder.append(requestValue.getSearchField());
            }
        }

        if (!TextUtils.isEmpty(requestValue.getSearchText())) {
            if (stringBuilder.length() == 0) {
                stringBuilder.append("searchText=");
                stringBuilder.append(requestValue.getSearchText());
            } else { // have para before it, need add "&"
                stringBuilder.append("&searchText=");
                stringBuilder.append(requestValue.getSearchText());
            }
        }

        if (stringBuilder.length() == 0) {
            stringBuilder.append("orderBy=");
            stringBuilder.append(requestValue.getOrderBy());
        } else {
            stringBuilder.append("&orderBy=");
            stringBuilder.append(requestValue.getOrderBy());
        }

        if (stringBuilder.length() == 0) {
            stringBuilder.append("orderByReverse=");
            stringBuilder.append(requestValue.isOrderByReverse());
        } else {
            stringBuilder.append("&orderByReverse=");
            stringBuilder.append(requestValue.isOrderByReverse());
        }
        urlPath += stringBuilder.toString();

        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(urlPath)
                .get()
                .build();

        String responseString = executeNetRequest(request);
        log.v("fetchActivityLog:\n" + responseString);
        try {
            JSONObject jo = new JSONObject(responseString);
            if (!jo.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = jo.optInt("statusCode");
            String message = jo.optString("message");
            if (code == 200) {
                return new Gson().fromJson(responseString, FetchLogResult.class);
            } else if (code == 401) {
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else if (code == 403) {
                throw new RmsRestAPIException("Access denied", RmsRestAPIException.ExceptionDomain.AccessDenied, code);
            } else if (code == 404) {
                throw new RmsRestAPIException("Invalid file.", RmsRestAPIException.ExceptionDomain.FileNotFound, code);
            } else {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }
}
