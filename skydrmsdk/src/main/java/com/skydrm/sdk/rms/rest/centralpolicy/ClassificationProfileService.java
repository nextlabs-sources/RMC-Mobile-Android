package com.skydrm.sdk.rms.rest.centralpolicy;

import android.support.annotation.NonNull;

import com.skydrm.sdk.Config;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.rest.IClassificationProfileService;
import com.skydrm.sdk.rms.user.IRmUser;
import com.skydrm.sdk.utils.DevLog;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by hhu on 4/8/2018.
 */

public class ClassificationProfileService extends RestAPI.RestServiceBase implements IClassificationProfileService {

    public ClassificationProfileService(IRmUser user, OkHttpClient httpClient, Config config, DevLog log) {
        super(user, httpClient, config, log);
    }

    @Override
    public String getClassificationProfile(@NonNull String tenantId) throws RmsRestAPIException {
        String classificationProfileURL = config.getClassificationProfileURL(tenantId);
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder.url(classificationProfileURL)
                .get()
                .build();

        String response = executeNetRequest(request);
        log.i("RESTFUL_getClassificationProfile response\n" + response);

        try {
            JSONObject resultObj = new JSONObject(response);

            int statusCode = resultObj.optInt("statusCode");
            String msg = resultObj.optString("message");
            if (statusCode == 200) {
                return response;
            } else if (statusCode == 400) {
                throw new RmsRestAPIException(msg, RmsRestAPIException.ExceptionDomain.MISSING_REQUIRED_PARAMETERS, 400);
            } else if (statusCode == 401) {
                throw new RmsRestAPIException(msg, RmsRestAPIException.ExceptionDomain.AuthenticationFailed, 401);
            } else if (statusCode == 403) {
                throw new RmsRestAPIException(msg, RmsRestAPIException.ExceptionDomain.AccessDenied, 403);
            } else if (statusCode == 4001) {
                throw new RmsRestAPIException(msg, RmsRestAPIException.ExceptionDomain.INVALID_LABEL, 4001);
            } else if (statusCode == 4002) {
                throw new RmsRestAPIException(msg, RmsRestAPIException.ExceptionDomain.INVALID_LABEL, 4002);
            } else if (statusCode == 4003) {
                throw new RmsRestAPIException(msg, RmsRestAPIException.ExceptionDomain.INVALID_LABEL, 4003);
            } else if (statusCode == 4004) {
                throw new RmsRestAPIException(msg, RmsRestAPIException.ExceptionDomain.INVALID_LABEL, 4004);
            } else if (statusCode == 4005) {
                throw new RmsRestAPIException(msg, RmsRestAPIException.ExceptionDomain.INVALID_LABEL, 4005);
            } else if (statusCode == 500) {
                throw new RmsRestAPIException(msg, RmsRestAPIException.ExceptionDomain.InternalServerError, 500);
            } else {
                throw new RmsRestAPIException("Failed to parse response.", RmsRestAPIException.ExceptionDomain.Common, statusCode);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public void updateClassificationProfile(@NonNull String tenantId) throws RmsRestAPIException {

    }
}
