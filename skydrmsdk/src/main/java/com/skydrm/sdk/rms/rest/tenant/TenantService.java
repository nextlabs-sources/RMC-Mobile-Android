package com.skydrm.sdk.rms.rest.tenant;

import com.google.gson.Gson;
import com.skydrm.sdk.Config;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.rest.ITenantService;
import com.skydrm.sdk.rms.user.IRmUser;
import com.skydrm.sdk.utils.DevLog;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class TenantService extends RestAPI.RestServiceBase implements ITenantService {

    public TenantService(IRmUser user, OkHttpClient httpClient, Config config, DevLog log) {
        super(user, httpClient, config, log);
    }

    @Override
    public TenantAdminResult getProjectAdmin(String tenantId) throws RmsRestAPIException {
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getGetProjectAdminURL(tenantId))
                .get()
                .build();
        String responseStr = executeNetRequest(request);
        if (responseStr == null || responseStr.isEmpty()) {
            throw new RmsRestAPIException("Failed to get response from rms.",
                    RmsRestAPIException.ExceptionDomain.Common);
        }

        try {
            JSONObject responseObj = new JSONObject(responseStr);
            int statusCode = responseObj.optInt("statusCode");
            String message = responseObj.optString("message");
            if (statusCode == 200) {
                return new Gson().fromJson(responseStr, TenantAdminResult.class);
            } else if (statusCode == 400) {
                throw new RmsRestAPIException(message,
                        RmsRestAPIException.ExceptionDomain.Missing_Request, statusCode);
            } else if (statusCode == 403) {
                throw new RmsRestAPIException(message,
                        RmsRestAPIException.ExceptionDomain.AuthenticationFailed, statusCode);
            } else if (statusCode == 500) {
                throw new RmsRestAPIException(message,
                        RmsRestAPIException.ExceptionDomain.InternalServerError, statusCode);
            } else {
                throw new RmsRestAPIException(message,
                        RmsRestAPIException.ExceptionDomain.Common, statusCode);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException(e.getMessage(),
                    RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public TenantPreferenceResult getTenantPreferences(String tenantId) throws RmsRestAPIException {
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getTenantPreferencesURL(tenantId))
                .get()
                .build();

        String responseStr = executeNetRequest(request);
        if (responseStr == null || responseStr.isEmpty()) {
            throw new RmsRestAPIException("Failed to get response from rms.",
                    RmsRestAPIException.ExceptionDomain.Common);
        }

        try {
            JSONObject responseObj = new JSONObject(responseStr);
            int statusCode = responseObj.optInt("statusCode");
            String message = responseObj.optString("message");
            if (statusCode == 200) {
                return new Gson().fromJson(responseStr, TenantPreferenceResult.class);
            } else if (statusCode == 400) {
                throw new RmsRestAPIException(message,
                        RmsRestAPIException.ExceptionDomain.Missing_Request, statusCode);
            } else if (statusCode == 403) {
                throw new RmsRestAPIException(message,
                        RmsRestAPIException.ExceptionDomain.AuthenticationFailed, statusCode);
            } else if (statusCode == 500) {
                throw new RmsRestAPIException(message,
                        RmsRestAPIException.ExceptionDomain.InternalServerError, statusCode);
            } else {
                throw new RmsRestAPIException(message,
                        RmsRestAPIException.ExceptionDomain.Common, statusCode);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException(e.getMessage(),
                    RmsRestAPIException.ExceptionDomain.Common);
        }
    }

}
