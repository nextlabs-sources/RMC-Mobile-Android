package com.skydrm.sdk.rms.rest.common;

import com.skydrm.sdk.Config;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.exception.TokenAccessDenyException;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.rest.ITokenService;
import com.skydrm.sdk.rms.user.IRmUser;
import com.skydrm.sdk.utils.DevLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static com.skydrm.sdk.utils.NxCommonUtils.bytesToHexString;


public class Token extends RestAPI.RestServiceBase implements ITokenService {
    public Token(IRmUser user, OkHttpClient httpClient, Config config, DevLog log) {
        super(user, httpClient, config, log);
    }

    @Override
    public Map<String, String> getEncryptionToken(byte[] dhAgreementKey) throws Exception {
        return getEncryptionToken(dhAgreementKey, 100);
    }

    @Override
    public Map<String, String> getEncryptionToken(byte[] dhAgreementKey, int count) throws RmsRestAPIException {
        // prepare put data
        String hexifiedAgreementKey = bytesToHexString(dhAgreementKey);
        JSONObject encryptionTokenJson = new JSONObject();
        try {
            JSONObject parameters = new JSONObject();
            parameters.put("userId", user.getUserId());
            parameters.put("ticket", user.getTicket());
            parameters.put("membership", user.getMembershipId());
            parameters.put("agreement", hexifiedAgreementKey);
            parameters.put("count", count);
            encryptionTokenJson.put("parameters", parameters);
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed prepare in getEncryptionToken-", RmsRestAPIException.ExceptionDomain.Common);
        }

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), encryptionTokenJson.toString());

        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getTokenURL())
                .put(body)
                .build();

        String responseString = executeNetRequest(request);
        log.v("getEncryptionToken:\n" + responseString);

        Map<String, String> rt = new HashMap<>();
        // parse result
        try {
            JSONObject jo = new JSONObject(responseString);
            if (!jo.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = jo.getInt("statusCode");
            if (code == 200) {
                if (jo.has("results") && !jo.isNull("results")) {
                    JSONObject results = jo.getJSONObject("results");
                    if (results.has("tokens") && !results.isNull("tokens")) {
                        JSONObject tokensJSON = results.getJSONObject("tokens");
                        Iterator iterator = tokensJSON.keys();
                        while (iterator.hasNext()) {
                            String duid = (String) iterator.next();
                            rt.put(duid, tokensJSON.getJSONObject(duid).toString());
                        }
                    } else {
                        throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
                    }
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
        return rt;
    }

    @Override
    public Map<String, String> getEncryptionToken(byte[] dhAgreementKey, String projectMembershipId, int count) throws RmsRestAPIException {
//        {
//            "parameters":{
//                    "userId":"3",
//                    "ticket":"DDDDF9F2636532519D3E5BF3C6B51C46",
//                    "membership":"m3@nextlabs.com",
//                    "agreement":"2490190aab76f9415d75...",
//                    "count":"50",
//                    "prefetch":true
//        }
//        }
        // prepare put data
        String hexifiedAgreementKey = bytesToHexString(dhAgreementKey);
        JSONObject encryptionTokenJson = new JSONObject();
        try {
            JSONObject parameters = new JSONObject();
            parameters.put("userId", user.getUserId());
            parameters.put("ticket", user.getTicket());
            parameters.put("membership", projectMembershipId);
            parameters.put("agreement", hexifiedAgreementKey);
            parameters.put("prefetch", true);
            parameters.put("count", count);
            encryptionTokenJson.put("parameters", parameters);
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed prepare in getEncryptionToken-", RmsRestAPIException.ExceptionDomain.Common);
        }

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), encryptionTokenJson.toString());

        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getTokenURL())
                .put(body)
                .build();

        String responseString = executeNetRequest(request);
        log.v("getEncryptionToken:\n" + responseString);

        Map<String, String> rt = new HashMap<>();
        // parse result
        try {
            JSONObject jo = new JSONObject(responseString);
            if (!jo.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = jo.getInt("statusCode");
            if (code == 200) {
                if (jo.has("results") && !jo.isNull("results")) {
                    JSONObject results = jo.getJSONObject("results");
                    if (results.has("tokens") && !results.isNull("tokens")) {
                        JSONObject tokensJSON = results.getJSONObject("tokens");
                        Iterator iterator = tokensJSON.keys();
//                        "6840F701C5A19215CC5892B7CD82803F": {
//                                    "otp": "914E4953059B5B9162F2B16D87DE20A2",
//                                    "token": "086D797D9E9D319304EF10AD9B11B26F1E23DCB7ACAB8DADA02173D3236B8672"
//                        }
                        while (iterator.hasNext()) {
                            String duid = (String) iterator.next();
                            rt.put(duid, tokensJSON.getString(duid));
                        }
                    } else {
                        throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
                    }
                } else {
                    throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
                }
            } else if (code == 401) {
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else if (code == 403) {
                throw new RmsRestAPIException("Access denied", RmsRestAPIException.ExceptionDomain.AccessDenied, code);
            } else if (code == 404) {
                throw new RmsRestAPIException("Not found", RmsRestAPIException.ExceptionDomain.NotFound, code);
            } else {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
        return rt;
    }

    @Override
    public String getDecryptionToken(String tenant, INxlFileFingerPrint nxlFingerPrint)
            throws RmsRestAPIException, TokenAccessDenyException {
        // prepare post data
        JSONObject encryptionTokenJson = new JSONObject();
        try {
            JSONObject parameters = new JSONObject();
            parameters.put("userId", user.getUserId());
            parameters.put("ticket", user.getTicket());
            parameters.put("tenant", tenant);
            parameters.put("owner", nxlFingerPrint.getOwnerID());
            parameters.put("agreement", nxlFingerPrint.getRootAgreementKey());
            parameters.put("duid", nxlFingerPrint.getDUID());
            parameters.put("ml", nxlFingerPrint.getMaintenanceLevel());
            if (nxlFingerPrint.hasRights()) {
                //adhoc
                parameters.put("filePolicy", nxlFingerPrint.getADHocSectionRaw());
                parameters.put("fileTags", "");
                parameters.put("protectionType", 0);
            } else {
                //central policy
                parameters.put("filePolicy", "");
                parameters.put("fileTags", nxlFingerPrint.getCentralSectionRaw());
                parameters.put("protectionType", 1);
            }
            encryptionTokenJson.put("parameters", parameters);
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed prepare in getDecryptionToken-", RmsRestAPIException.ExceptionDomain.Common);
        }

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), encryptionTokenJson.toString());
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getTokenURL())
                .post(body)
                .build();

        String responseString = executeNetRequest(request);
        log.v("getDecryptionToken:\n" + responseString);
        // parse result
        try {
            JSONObject jo = new JSONObject(responseString);
            if (!jo.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = jo.optInt("statusCode");
            String msg = jo.optString("message");
            if (code == 200) {
                if (jo.has("results") && !jo.isNull("results")) {
                    JSONObject results = jo.getJSONObject("results");
                    if (results.has("token") && !results.isNull("token")) {
                        return results.getString("token");
                    } else {
                        throw new RmsRestAPIException("key tokens not matched");
                    }
                } else {
                    throw new RmsRestAPIException("key results not matched");
                }
            } else if (code == 401) {
                throw new RmsRestAPIException(msg, RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else if (code == 403 || code == 404) {
                throw new TokenAccessDenyException(msg);
            } else if (code == 4000) {
                throw new RmsRestAPIException(msg, RmsRestAPIException.ExceptionDomain.AuthenticationFailed);
            } else {
                throw new RmsRestAPIException(msg, RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public String getDecryptionToken(String tenant, INxlFileFingerPrint fingerPrint,
                                     int sharedSpaceType, int sharedSpaceId,
                                     String sharedSpaceUserMembership)
            throws RmsRestAPIException, TokenAccessDenyException {
        INxlFileFingerPrint fp = paramCheck(fingerPrint);
        // prepare post data
        JSONObject encryptionTokenJson = new JSONObject();
        try {
            JSONObject parameters = new JSONObject();
            parameters.put("userId", user.getUserId());
            parameters.put("ticket", user.getTicket());
            parameters.put("tenant", tenant);
            parameters.put("owner", fp.getOwnerID());
            parameters.put("agreement", fp.getRootAgreementKey());
            parameters.put("duid", fp.getDUID());
            parameters.put("ml", fp.getMaintenanceLevel());
            parameters.put("sharedSpaceType", sharedSpaceType);
            parameters.put("sharedSpaceId", sharedSpaceId);
            parameters.put("sharedSpaceUserMembership", sharedSpaceUserMembership);
            if (fp.hasRights()) {
                //adhoc
                parameters.put("filePolicy", fp.getADHocSectionRaw());
                parameters.put("fileTags", "");
                parameters.put("protectionType", 0);
            } else {
                //central policy
                parameters.put("filePolicy", "");
                parameters.put("fileTags", fp.getCentralSectionRaw());
                parameters.put("protectionType", 1);
            }
            encryptionTokenJson.put("parameters", parameters);
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed prepare in getDecryptionToken-", RmsRestAPIException.ExceptionDomain.Common);
        }

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), encryptionTokenJson.toString());
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getTokenURL())
                .post(body)
                .build();

        String response = executeNetRequest(request);
        log.v("getDecryptionToken:\n" + response);
        // parse result
        try {
            JSONObject responseObj = new JSONObject(response);
            int code = responseObj.optInt("statusCode");
            String msg = responseObj.optString("message");
            if (code == 200) {
                JSONObject resultsObj = responseObj.optJSONObject("results");
                if (resultsObj != null) {
                    return resultsObj.optString("token");
                }
                throw new RmsRestAPIException("key results not matched");
            } else if (code == 401) {
                throw new RmsRestAPIException(msg, RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else if (code == 403 || code == 404) {
                throw new TokenAccessDenyException(msg);
            } else if (code == 4000) {
                throw new RmsRestAPIException(msg, RmsRestAPIException.ExceptionDomain.AuthenticationFailed);
            } else {
                throw new RmsRestAPIException(msg, RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public String updateNXLMetadata(String duid, String otp, String sectionRaw, int protectionType, int ml) throws RmsRestAPIException {
        JSONObject requestObj = new JSONObject();
        try {
            JSONObject parameters = new JSONObject();
            parameters.put("otp", otp);
            parameters.put("ml", ml);
            if (protectionType == 0) { //filePolicy
                parameters.put("protectionType", 0);
                parameters.put("filePolicy", sectionRaw);
                parameters.put("fileTags", "{}");
            } else { //fileTags
                parameters.put("protectionType", 1);
                parameters.put("filePolicy", "{}");
                parameters.put("fileTags", sectionRaw);
            }
            requestObj.put("parameters", parameters);
        } catch (JSONException e) {
            throw new RmsRestAPIException("JSONException occurred when invoke updateNXLMetadata.", e);
        }

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                requestObj.toString());

        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request r = builder
                .url(config.getTokenURL().concat("/").concat(duid))
                .put(body)
                .build();

        String response = executeNetRequest(r);
        try {
            JSONObject responseObj = new JSONObject(response);
            int statusCode = responseObj.optInt("statusCode");
            if (statusCode == 200) {
                JSONObject resultsObj = responseObj.optJSONObject("results");
                return resultsObj == null ? responseObj.toString() : resultsObj.toString();
            } else {
                String message = responseObj.optString("message", "Try update nxl metadata failed.");
                if (statusCode == 400) {
                    throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.MalformedRequest);
                } else if (statusCode == 403) {
                    throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.AccessDenied);
                } else if (statusCode == 404) {
                    throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.NotFound);
                } else if (statusCode == 4000) {
                    throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.UNVERIFIED_METADATA);
                } else {
                    throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.Common);
                }
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("Failed parse response json string when invoke updateNXLMetadata request.", e);
        }
    }
}
