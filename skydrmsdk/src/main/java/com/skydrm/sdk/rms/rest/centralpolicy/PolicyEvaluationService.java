package com.skydrm.sdk.rms.rest.centralpolicy;

import android.support.annotation.NonNull;

import com.skydrm.sdk.Config;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.rest.IPolicyEvaluationService;
import com.skydrm.sdk.rms.user.IRmUser;
import com.skydrm.sdk.utils.DevLog;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by hhu on 4/10/2018.
 */

public class PolicyEvaluationService extends RestAPI.RestServiceBase implements IPolicyEvaluationService {

    public PolicyEvaluationService(IRmUser user, OkHttpClient httpClient, Config config, DevLog log) {
        super(user, httpClient, config, log);
    }

    @Override
    public String performPolicyEvaluation(@NonNull JSONObject parametersObj) throws RmsRestAPIException {
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        JSONObject postJson = new JSONObject();
        try {
            postJson.put("parameters", parametersObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        log.d("PolicyEvaluationService post json\n" + postJson.toString());
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), postJson.toString());
        Request request = builder.url(config.getPerformPolicyEvaluationURL())
                .post(requestBody)
                .build();
        String response = executeNetRequest(request);
        log.d("PolicyEvaluationService execute response\n" + response);
        try {
            JSONObject responseObj = new JSONObject(response);
            if (!responseObj.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int statusCode = responseObj.getInt("statusCode");
            if (statusCode == 200) {
                return response;
            } else if (statusCode == 404) {
                throw new RmsRestAPIException("Invalid file.", RmsRestAPIException.ExceptionDomain.FileNotFound, statusCode);
            } else if (statusCode == 5002) {
                throw new RmsRestAPIException("No policy to evaluate.", RmsRestAPIException.ExceptionDomain.NOPOLICY_TO_EVALUATE, statusCode);
            } else {
                throw new RmsRestAPIException(responseObj.getString("message"), RmsRestAPIException.ExceptionDomain.Common, statusCode);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
        }
    }
}
