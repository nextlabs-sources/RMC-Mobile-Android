package com.skydrm.sdk.rms.rest.user;

import com.skydrm.sdk.Config;
import com.skydrm.sdk.Factory;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.policy.Expiry;
import com.skydrm.sdk.policy.Watermark;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.rest.IHeartbeatService;
import com.skydrm.sdk.rms.types.HeartbeatResponse;
import com.skydrm.sdk.rms.user.IRmUser;
import com.skydrm.sdk.utils.DevLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;


public class Heartbeat extends RestAPI.RestServiceBase implements IHeartbeatService {

    public Heartbeat(IRmUser user, OkHttpClient httpClient, Config config, DevLog log) {
        super(user, httpClient, config, log);
    }

    @Override
    public HeartbeatResponse heartbeat() throws RmsRestAPIException {
        // prepare data
        JSONObject postJSON = new JSONObject();
        try {
            JSONObject parameters = new JSONObject();
            parameters.put("platformId", Factory.getDeviceType());
            postJSON.put("parameters", parameters);
        } catch (JSONException e) {
            log.e("failed prepare post data in heartbeat-" + e.toString(), e);
            throw new RmsRestAPIException("failed prepare in heartbeat-", RmsRestAPIException.ExceptionDomain.Common);
        }
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                postJSON.toString());
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getHeartbeatV2URL())
                .post(body)
                .build();

        String responseString = executeNetRequest(request);
        log.i("heartbeat:\n" + responseString);

        // parse response
        try {
            JSONObject j = new JSONObject(responseString);
            try {
                // insert an obligation here, every time getting result, it need to update Expiry's stdTime according to server time
                if (!j.has("serverTime")){
                    throw new Exception("failed when parsing server time in heartbeat");
                }
                Expiry.sStdCurMills = j.getLong("serverTime");
                log.d("update Expiry time:"+ new Date(Expiry.sStdCurMills));
            }catch (Exception e){
                Expiry.sStdCurMills = System.currentTimeMillis();
            }

            if (!j.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = j.getInt("statusCode");
            if (code == 200) {
                if (j.has("results")) {
                    HeartbeatResponse heartbeatResponse = new HeartbeatResponse();
                    Watermark watermark = new Watermark(responseString);
                    if (watermark.isbIsBuildSucceed()) {
                        heartbeatResponse.setWatermark(watermark);
                    } else {
                        log.e("heartbeat: parse watermark failed!!!");
                    }
                    return heartbeatResponse;
                } else {
                    throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
                }
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
