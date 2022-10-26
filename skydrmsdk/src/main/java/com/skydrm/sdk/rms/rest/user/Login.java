package com.skydrm.sdk.rms.rest.user;

import com.skydrm.sdk.Config;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.rest.ILoginService;
import com.skydrm.sdk.rms.user.IRmUser;
import com.skydrm.sdk.rms.user.RmUser;
import com.skydrm.sdk.utils.DevLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static com.skydrm.sdk.utils.NxCommonUtils.hexifyMD5;


public class Login extends RestAPI.RestServiceBase implements ILoginService {

    public Login(IRmUser user, OkHttpClient httpClient, Config config, DevLog log) {
        // notice user is null
        super(user, httpClient, config, log);
    }

    @Override
    public String getLoginURLbyTenant(String tenant) throws RmsRestAPIException {
        log.i("getLoginURLbyTenant:\n" + "tenant:" + tenant);
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getTenantURL() + tenant)
                .get()
                .build();
        String responseString = executeNetRequest(request);
        log.i("getLoginURLbyTenant\n" + responseString);

        // parse result
        try {
            // {"statusCode":200,"message":"OK","results":{"server":"https://rmtest.nextlabs.solutions/rms"}}
            JSONObject j = new JSONObject(responseString);
            if (!j.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = j.getInt("statusCode");
            if (code == 200) {
                if (j.has("results")) {
                    JSONObject results = j.getJSONObject("results");
                    if (results.has("server") && !results.isNull("server")) {
                        return results.getString("server");
                    }
                }
            } else {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, code);
            }
            // should never reach here
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public IRmUser basicLogin(String user, String password) throws RmsRestAPIException {
        //prepare put data
        StringBuilder sb;
        try {
            sb = new StringBuilder();
            sb
                    .append("email=")
                    .append(URLEncoder.encode(user, StandardCharsets.UTF_8.name()))
                    .append("&password=")
                    .append(hexifyMD5(password.getBytes(), true));
        } catch (UnsupportedEncodingException e) {
            log.e("unsupported url encoding in login-" + e.toString(), e);
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
        } catch (NoSuchAlgorithmException e) {
            log.e("failed prepare post data in login-" + e.toString(), e);
            throw new RmsRestAPIException("failed prepare post data in changePassword-", RmsRestAPIException.ExceptionDomain.NoSuchAlgorithm);
        }

        RequestBody body = RequestBody.create(
                MediaType.parse("application/x-www-form-urlencoded"),
                sb.toString());
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getLoginURL())
                .post(body)
                .build();
        String responseString = executeNetRequest(request);
        log.v("basicLogin\n" + responseString);

        try {
            // {"statusCode":200,"message":"OK","results":{"server":"https://rmtest.nextlabs.solutions/rms"}}
            JSONObject j = new JSONObject(responseString);
            if (!j.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = j.getInt("statusCode");
            if (code == 200) {
                return RmUser.buildFromJson(responseString);
            } else if (code == 401) {
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }
}
