package com.skydrm.sdk.rms.rest.user;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.skydrm.sdk.Config;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.rest.IUserService;
import com.skydrm.sdk.rms.user.IRmUser;
import com.skydrm.sdk.utils.DevLog;
import com.skydrm.sdk.utils.NxCommonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class User extends RestAPI.RestServiceBase implements IUserService {

    public User(IRmUser user, OkHttpClient httpClient, Config config, DevLog log) {
        super(user, httpClient, config, log);
    }

    @Override
    public JSONObject getCaptcha() throws Exception {
        JSONObject jo = null;
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder.url(config.getCaptchaURL()).get().build();
        Response response = httpClient.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new RmsRestAPIException("network failed" + response.code() + response.message());
        }
        String responseString = response.body().string();
        // debugLog("result of getCaptcha\n" + responseString);
        Log.d("getCaptcha", "getCaptcha: =" + responseString);
        jo = new JSONObject(responseString);
        if (jo.has("statusCode") && !jo.isNull("statusCode") && 200 == jo.getInt("statusCode")) {
            // return Repo.buildFromJson(responseString);
            return jo;

        } else {
            log.v("getCaptcha failed!");
            return null;
        }
    }

    @Override
    public JSONObject sendCaptcha(String email, String nonce, String captcha) throws RmsRestAPIException {
        JSONObject jo = null;
        FormBody body = new FormBody.Builder()
                .add("email", email)
                .add("nonce", nonce)
                .add("captcha", captcha)
                .build();

        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getSendCaptchaURl())
                .post(body)
                .build();

        String responseString = executeNetRequest(request);
        log.v("sendCaptcha:\n" + responseString);
        try {
            jo = new JSONObject(responseString);
            if (!jo.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = jo.getInt("statusCode");
            if (code == 200) {
                return jo;
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
    public void changePassword(String oldPassword, String newPassword) throws RmsRestAPIException {
        JSONObject postJson = new JSONObject();
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("oldPassword", NxCommonUtils.hexifyMD5(oldPassword.getBytes(), false));
            parameters.put("newPassword", NxCommonUtils.hexifyMD5(newPassword.getBytes(), false));
            postJson.put("parameters", parameters);
        } catch (JSONException e) {
            log.e("failed prepare post data in changePassword-" + e.toString());
            throw new RmsRestAPIException("failed prepare post data in changePassword-", RmsRestAPIException.ExceptionDomain.Common);
        } catch (NoSuchAlgorithmException e) {
            log.e("failed prepare post data in changePassword-" + e.toString());
            throw new RmsRestAPIException("failed prepare post data in changePassword-", RmsRestAPIException.ExceptionDomain.NoSuchAlgorithm);
        }

        RequestBody body = RequestBody.create(MediaType.parse("application/json")
                , postJson.toString());

        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getChangePasswordURL())
                .post(body)
                .build();

        String responseString = executeNetRequest(request);
        log.v("changePassword result:\n" + responseString);

        if (TextUtils.isEmpty(responseString)) {
            throw new RmsRestAPIException("error ,no response from rms");
        }

        try {
            JSONObject result = new JSONObject(responseString);
            if (!result.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = result.getInt("statusCode");
            if (code == 200) {
                return;
            } else if (code == 401) {
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else if (code == 4001) {
                throw new RmsRestAPIException("Incorrect password", RmsRestAPIException.ExceptionDomain.InvalidPassword, code);
            } else if (code == 4002) {
                throw new RmsRestAPIException(result.getString("message"), RmsRestAPIException.ExceptionDomain.TooManyAttemps, code);
            } else {
                throw new RmsRestAPIException(result.getString("message"), RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public JSONObject retrieveUserProfile() throws RmsRestAPIException {
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getProfileRetrieveUrl())
                .get()
                .build();

        String responseString = executeNetRequest(request);
        log.v("retrieveUserProfile:\n" + responseString);

        try {
            //parse response
            JSONObject j = new JSONObject(responseString);
            if (!j.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = j.getInt("statusCode");
            if (code == 200) {
                return j;
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
    public void updateUserProfile(byte[] byteFile, RestAPI.IRequestCallBack<String> callBack) throws RmsRestAPIException {
        JSONObject postJSON = new JSONObject();
        JSONObject parameters = new JSONObject();
        try {
            JSONObject preferences = new JSONObject();
//            parameters.put("userId", user.getUserIdStr());
//            parameters.put("ticket", user.getTicket());
            parameters.put("displayName", user.getName());
            preferences.put("profile_picture", "data:image/jpeg;base64," + Base64.encodeToString(byteFile, Base64.DEFAULT));
            parameters.put("preferences", preferences);
            postJSON.put("parameters", parameters);
            log.v("updateUserProfile: send\n" + postJSON.toString());
        } catch (JSONException e) {
            log.e("failed to prepare post data in update user profile");
            throw new RmsRestAPIException("failed prepare in updateUserProfile-", RmsRestAPIException.ExceptionDomain.Common);
        }
        RequestBody boby = RequestBody.create(MediaType.parse("application/json"),
                postJSON.toString());
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getProfileUpdateURL())
                .post(boby)
                .build();

        String responseString = executeNetRequest(request);
        log.v("updateUserProfile:\n" + responseString);

        try {
            //parse response
            JSONObject j = new JSONObject(responseString);
            if (!j.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = j.getInt("statusCode");
            if (code == 200) {
                callBack.onSuccess(j.getString("message"));
            } else if (code == 401) {
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else {
                callBack.onFailed(j.getInt("statusCode"), j.getString("message"));
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public void updateUserDisplayName(String displayName, RestAPI.IRequestCallBack<String> callBack) throws RmsRestAPIException {
        JSONObject postJSON = new JSONObject();
        JSONObject parameters = new JSONObject();
        try {
            JSONObject preferences = new JSONObject();
            parameters.put("displayName", displayName);
            preferences.put("profile_picture", "data:image/jpeg;base64,");
            postJSON.put("parameters", parameters);
        } catch (JSONException e) {
            log.e("failed to prepare post data in update user profile");
            e.printStackTrace();
        }
        RequestBody boby = RequestBody.create(MediaType.parse("application/json"),
                postJSON.toString());
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getProfileUpdateURL())
                .post(boby)
                .build();

        String responseString = executeNetRequest(request);
        log.v("updateUserDisplayName:\n" + responseString);

        try {
            //parse response
            JSONObject j = new JSONObject(responseString);
            if (!j.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = j.getInt("statusCode");
            if (code == 200) {
                callBack.onSuccess(j.getString("message"));
            } else if (code == 401) {
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    /**
     * @param watermark need a String format like this:$(User)\n$(Date) $(Time) ,pass null represents update expiry only
     * @param expiry
     * @throws RmsRestAPIException
     */
    @Override
    public void updateUserPreference(String watermark, IExpiry expiry, RestAPI.IRequestCallBack<String> callBack) throws RmsRestAPIException {
        JSONObject postJson = new JSONObject();
        JSONObject parameters = new JSONObject();
        try {
            if (!TextUtils.isEmpty(watermark)) {
                parameters.put("watermark", watermark);
            }
            if (expiry != null) {
                JSONObject expiryObj = new JSONObject();
                int operation = expiry.getOption();
                expiryObj.put("option", operation);
                switch (operation) {
                    case 1:
                        expiryObj.put("relativeDay", relative(expiry));
                        break;
                    case 2:
                        IAbsolute absolute = (IAbsolute) expiry;
                        expiryObj.put("endDate", absolute.endDate());
                        break;
                    case 3:
                        IRange range = (IRange) expiry;
                        expiryObj.put("startDate", range.startDate());
                        expiryObj.put("endDate", range.endDate());
                        break;
                    default:
                        break;
                }
                parameters.put("expiry", expiryObj);
            } else {
                throw new RmsRestAPIException("interface IExpiry needs to be implemented to finish this operation.");
            }
            postJson.put("parameters", parameters);
        } catch (JSONException e) {
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
        }
        RequestBody body = RequestBody.create(MediaType.parse("application/json")
                , postJson.toString());

        log.d("result=\n" + postJson.toString());
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getUpdateUserPreferenceURL())
                .put(body)
                .build();

        try {
            String result = executeNetRequest(request);
            log.d("result=\n" + result);
            JSONObject resultObj = new JSONObject(result);
            int statusCode = resultObj.getInt("statusCode");
            String message = resultObj.getString("message");
            switch (statusCode) {
                case 200:
                    callBack.onSuccess(result);
                    break;
                case 400:
                    callBack.onFailed(statusCode, message);
                    throw new RmsRestAPIException("Missing request.", RmsRestAPIException.ExceptionDomain.Common, statusCode);
                case 401:
                    callBack.onFailed(statusCode, message);
                    throw new RmsRestAPIException("Authentication failed/Missing login parameters.", RmsRestAPIException.ExceptionDomain.Common, statusCode);
                case 4001:
                    callBack.onFailed(statusCode, message);
                    throw new RmsRestAPIException("Watermark too long.(255)", RmsRestAPIException.ExceptionDomain.Common, statusCode);
                case 4002:
                    callBack.onFailed(statusCode, message);
                    throw new RmsRestAPIException("Invalid parameters.", RmsRestAPIException.ExceptionDomain.Common, statusCode);
                case 500:
                    callBack.onFailed(statusCode, message);
                    throw new RmsRestAPIException("Internal Server Error.", RmsRestAPIException.ExceptionDomain.Common, statusCode);
                default:
                    callBack.onFailed(statusCode, message);
                    throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.Common, statusCode);
            }
        } catch (JSONException | RmsRestAPIException e) {
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public void retrieveUserPreference(RestAPI.IRequestCallBack<String> callBack) throws RmsRestAPIException {
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getUpdateUserPreferenceURL())
                .get()
                .build();
        try {
            String result = executeNetRequest(request);
            JSONObject jsonObject = new JSONObject(result);
            int statusCode = 0;
            String message = "";
            if (jsonObject.has("statusCode"))
                statusCode = jsonObject.getInt("statusCode");
            if (jsonObject.has("message")) {
                message = jsonObject.getString("message");
            }
            switch (statusCode) {
                case 200:
                    callBack.onSuccess(result);
                    break;
                case 400:
                    callBack.onFailed(statusCode, message);
                    throw new RmsRestAPIException("Missing request.", RmsRestAPIException.ExceptionDomain.Common, statusCode);
                case 401:
                    callBack.onFailed(statusCode, message);
                    throw new RmsRestAPIException("Authentication failed/Missing login parameters.", RmsRestAPIException.ExceptionDomain.Common, statusCode);
                case 4001:
                    callBack.onFailed(statusCode, message);
                    throw new RmsRestAPIException("Watermark too long.(255)", RmsRestAPIException.ExceptionDomain.Common, statusCode);
                case 4002:
                    callBack.onFailed(statusCode, message);
                    throw new RmsRestAPIException("Invalid parameters.", RmsRestAPIException.ExceptionDomain.Common, statusCode);
                case 500:
                    callBack.onFailed(statusCode, message);
                    throw new RmsRestAPIException("Internal Server Error.", RmsRestAPIException.ExceptionDomain.Common, statusCode);
                default:
                    throw new RmsRestAPIException("Unknown error while execting retrieve user preference request.", RmsRestAPIException.ExceptionDomain.Common);
            }
        } catch (Exception e) {
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    /**
     * this is for never expired option
     */
    public interface IExpiry {
        int getOption();
    }

    /**
     * this is for relative expiry date
     */
    public interface IRelative extends IExpiry {
        int getYear();

        int getMonth();

        int getWeek();

        int getDay();
    }

    /**
     * this is for absolute expiry date
     */
    public interface IAbsolute extends IExpiry {
        long endDate();
    }

    /**
     * this is for range  expiry date
     */
    public interface IRange extends IExpiry {
        long startDate();

        long endDate();
    }

    private JSONObject relative(IExpiry expiry) throws RmsRestAPIException {
        try {
            IRelative relative = (IRelative) expiry;
            JSONObject relativeObj = new JSONObject();
            if (relative.getYear() == 0 && relative.getMonth() == 0
                    && relative.getDay() == 0 && relative.getWeek() == 0) {
                throw new RmsRestAPIException("Please specify relative digital rights validity period",
                        RmsRestAPIException.ExceptionDomain.Common);
            }
            relativeObj.put("year", relative.getYear());
            relativeObj.put("month", relative.getMonth());
            relativeObj.put("week", relative.getWeek());
            relativeObj.put("day", relative.getDay());
            return relativeObj;
        } catch (JSONException | ClassCastException e) {
            e.printStackTrace();
        }
        return null;
    }
}
