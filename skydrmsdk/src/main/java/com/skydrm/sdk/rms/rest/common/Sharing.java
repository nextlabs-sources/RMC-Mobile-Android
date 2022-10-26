package com.skydrm.sdk.rms.rest.common;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.skydrm.sdk.Config;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.rest.ISharingService;
import com.skydrm.sdk.rms.rest.myVault.UpdateRecipientsResult;
import com.skydrm.sdk.rms.types.SharingLocalFilePara;
import com.skydrm.sdk.rms.types.SharingRepoFileParas;
import com.skydrm.sdk.rms.types.SharingRepoFileResult;
import com.skydrm.sdk.rms.types.UpdateProjectRecipientsResult;
import com.skydrm.sdk.rms.user.IRmUser;
import com.skydrm.sdk.utils.DevLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static com.skydrm.sdk.utils.NxCommonUtils.stringTags2Json;

public class Sharing extends RestAPI.RestServiceBase implements ISharingService {

    public Sharing(IRmUser user, OkHttpClient httpClient, Config config, DevLog log) {
        super(user, httpClient, config, log);
    }

    @Override
    @Deprecated
    public boolean share(String duid, String encryptionToken, String fileName, String deviceId, String deviceType, int rights, List<String> emails, long expireMillis) throws RmsRestAPIException {
        // prepare data
        JSONObject sharingJson = new JSONObject();
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("userId", user.getUserId());
            parameters.put("deviceId", deviceId);
            parameters.put("deviceType", deviceType);
            parameters.put("ticket", user.getTicket());
            // parameters.shareDocument
            JSONObject sharedDocumentJSON = new JSONObject();
            {
                sharedDocumentJSON.put("duid", duid);
                sharedDocumentJSON.put("membershipId", user.getMembershipId());
                sharedDocumentJSON.put("permissions", rights);
                sharedDocumentJSON.put("metadata", "{}");
                sharedDocumentJSON.put("expireTime", expireMillis);
                sharedDocumentJSON.put("fileName", URLEncoder.encode(fileName, "UTF-8"));
                // recipients in parameters.shareDocument
                JSONArray jsonArray = new JSONArray();
                for (String email : emails) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("email", email);
                    jsonArray.put(jsonObject);
                }
                sharedDocumentJSON.put("recipients", jsonArray);
            }
            String filedSharedDocDic = sharedDocumentJSON.toString();
            parameters.put("sharedDocument", filedSharedDocDic);
            String strDigest = com.skydrm.sdk.nxl.NxlFileHandler.calcHmacSha256(encryptionToken, filedSharedDocDic);
            parameters.put("checksum", strDigest);
            sharingJson.put("parameters", parameters);
        } catch (Exception e) {
            throw new RmsRestAPIException("failed prepare post data in share-", RmsRestAPIException.ExceptionDomain.Common);
        }
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), sharingJson.toString());
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getShareURL())
                .post(body)
                .build();

        String responseString = executeNetRequest(request);
        log.v("result of share\n" + responseString);

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
    @Deprecated
    public String share(File nxlFile, String tags, boolean bAsAttachment, String deviceId, String deviceType, int rights, List<String> emails, long expireMillis) throws RmsRestAPIException {
        // prepare request body
        JSONObject shareJson = new JSONObject();
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("deviceId", deviceId);
            parameters.put("deviceType", deviceType);
            parameters.put("asAttachment", String.format(Locale.getDefault(), "%b", bAsAttachment));
            parameters.put("expireTime", expireMillis);
            parameters.put("membershipId", user.getMembershipId());
            parameters.put("permissions", rights);
            parameters.put("tags", tags);
            parameters.put("metadata", "{}");
            // recipients in parameters
            JSONArray jsonArray = new JSONArray();
            for (String email : emails) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("email", email);
                jsonArray.put(jsonObject);
            }
            parameters.put("recipients", jsonArray.toString()); // must be string ??  --- also is 500.
            shareJson.put("parameters", parameters);
        } catch (JSONException e) {
            log.e("failed prepare post data in share-" + e.toString(), e);
            throw new RmsRestAPIException("failed prepare in share-", RmsRestAPIException.ExceptionDomain.Common);
        }

        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("API-input", shareJson.toString())
                .addFormDataPart("file", nxlFile.getName(),
                        RequestBody.create(MediaType.parse("application/octet-stream"), nxlFile))
                .build();

        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getShareLocalFileURL())
                .post(body)
                .build();

        String responseString = executeNetRequest(request);
        log.v("share:\n" + responseString);

        try {
            JSONObject jo = new JSONObject(responseString);
            if (!jo.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = jo.getInt("statusCode");
            if (code == 200) {
                return jo.getJSONObject("results").getString("duid");
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
    @Deprecated
    public String share2(File file, String tags, boolean bAsAttachment, int rights, List<String> emails, long expireMillis) throws RmsRestAPIException {
        // prepare request body
        JSONObject shareJson = new JSONObject();
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("asAttachment", String.format(Locale.getDefault(), "%b", bAsAttachment));
            JSONObject sharedDocument = new JSONObject();
            sharedDocument.put("membershipId", user.getMembershipId());
            sharedDocument.put("permissions", rights);
            // padding tags
            if (!TextUtils.isEmpty(tags)) {
                JSONObject tagsJson = stringTags2Json(tags);
                if (tagsJson != null) {
                    sharedDocument.put("tags", tagsJson);
                }
            }

            sharedDocument.put("metadata", "{}");
            sharedDocument.put("expireTime", expireMillis);
            // recipients in parameters
            JSONArray recipientJson = new JSONArray();
            for (String email : emails) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("email", email);
                recipientJson.put(jsonObject);
            }
            sharedDocument.put("recipients", recipientJson);
            parameters.put("sharedDocument", sharedDocument);
            shareJson.put("parameters", parameters);
        } catch (JSONException e) {
            log.e("failed prepare post data in share-" + e.toString(), e);
            throw new RmsRestAPIException("failed prepare in share2-", RmsRestAPIException.ExceptionDomain.Common);
        }

        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("API-input", shareJson.toString())
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(MediaType.parse("application/octet-stream"), file))
                .build();

        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getShareLocalFileURL())
                .post(body)
                .build();

        String responseString = executeNetRequest(request);
        log.v("share2:\n" + responseString);

        try {
            JSONObject jo = new JSONObject(responseString);
            if (!jo.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = jo.getInt("statusCode");
            if (code == 200) {
                return jo.getJSONObject("results").getString("duid");
            } else if (code == 401) { // for this api 403 is "Authentication failed" fuck!!!!, also 403 --- File has been revoked  ????
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else if (code == 403) { // for this api 403 is "Authentication failed"  rms bug!
                String msg = jo.getString("message");
                if (msg.equals("Authentication failed"))
                    throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
                else if (msg.equals("File has been revoked"))
                    throw new RmsRestAPIException("File has been revoked.", RmsRestAPIException.ExceptionDomain.FileHasBeenRevoked, code);
                else
                    throw new RmsRestAPIException("Access denied.", RmsRestAPIException.ExceptionDomain.AccessDenied, code);
            } else {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public String sharingLocalFile(SharingLocalFilePara paras) throws RmsRestAPIException {
        // prepare request body
        JSONObject shareJson = new JSONObject();
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("asAttachment", String.format(Locale.getDefault(), "%b", paras.isbAsAttachment()));
            JSONObject sharedDocument = new JSONObject();
            sharedDocument.put("membershipId", user.getMembershipId());
            sharedDocument.put("permissions", paras.getPermissions());
            // padding tags
            JSONObject tagsJson = stringTags2Json(paras.getTags());
            if (tagsJson != null) {
                sharedDocument.put("tags", tagsJson);
            }
            sharedDocument.put("metadata", "{}");
            sharedDocument.put("filePathId", paras.getFilePathId());
            sharedDocument.put("filePath", paras.getFilePath());
            sharedDocument.put("expireTime", paras.getExpireMillis());
            // recipients in parameters
            JSONArray recipientJson = new JSONArray();
            for (String email : paras.getRecipients()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("email", email);
                recipientJson.put(jsonObject);
            }
            sharedDocument.put("recipients", recipientJson);
            sharedDocument.put("comment", paras.getComment());

            // add watermark & expiry --- for sharing nxl file, we don't need to pass watermark & expiry.
            if (!TextUtils.isEmpty(paras.getWatermark())) {
                sharedDocument.put("watermark", paras.getWatermark());
            }

            if (paras.getExpiry() != null) {
                sharedDocument.put("expiry", paras.getExpiry().toJsonObj());
            }

            parameters.put("sharedDocument", sharedDocument);
            shareJson.put("parameters", parameters);
        } catch (JSONException e) {
            log.e("failed prepare post data in sharingLocalFile-" + e.toString(), e);
            throw new RmsRestAPIException("failed prepare in sharingLocalFile-", RmsRestAPIException.ExceptionDomain.Common);
        }

        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("API-input", shareJson.toString())
                .addFormDataPart("file", paras.getFile().getName(),
                        RequestBody.create(MediaType.parse("application/octet-stream"), paras.getFile()))
                .build();

        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getShareLocalFileURL())
                .post(body)
                .build();

        String responseString = executeNetRequest(request);
        log.v("sharingLocalFile:\n" + responseString);

        try {
            JSONObject jo = new JSONObject(responseString);
            if (!jo.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = jo.getInt("statusCode");
            if (code == 200) {
                return jo.getJSONObject("results").getString("duid");
            } else if (code == 400) {
                throw new RmsRestAPIException("Invalid request data", RmsRestAPIException.ExceptionDomain.MalformedRequest, code);
            } else if (code == 401) {
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else if (code == 403) {
                throw new RmsRestAPIException("Access Denied", RmsRestAPIException.ExceptionDomain.AccessDenied, code);
            } else if (code == 500) {
                throw new RmsRestAPIException("Internal Server Error.", RmsRestAPIException.ExceptionDomain.InternalServerError, code);
            } else if (code == 4001) {
                throw new RmsRestAPIException("File has been revoked.", RmsRestAPIException.ExceptionDomain.FileHasBeenRevoked, code);
            } else if (code == 4002) {
                throw new RmsRestAPIException("No transaction has been performed.", RmsRestAPIException.ExceptionDomain.NoTransactionPerformed, code);
            } else if (code == 4007) {
                throw new RmsRestAPIException("Comment too long.", RmsRestAPIException.ExceptionDomain.CommentTooLong, code);
            } else {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public SharingRepoFileResult sharingRepoFile(SharingRepoFileParas paras) throws RmsRestAPIException {
        // prepare request body
        JSONObject shareJson = new JSONObject();
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("asAttachment", String.format(Locale.getDefault(), "%b", paras.isbAsAttachment()));
            JSONObject sharedDocument = new JSONObject();
            sharedDocument.put("membershipId", user.getMembershipId());
            sharedDocument.put("permissions", paras.getPermissions());
            // padding tags
            JSONObject tagsJson = stringTags2Json(paras.getTags());
            if (tagsJson != null) {
                sharedDocument.put("tags", tagsJson);
            }
            sharedDocument.put("metadata", "{}");
            sharedDocument.put("expireTime", paras.getExpireMillis());
            sharedDocument.put("fileName", paras.getFileName());
            sharedDocument.put("repositoryId", paras.getRepositoryId());
            sharedDocument.put("filePathId", paras.getFilePathId());
            sharedDocument.put("filePath", paras.getFilePath());

            // recipients in parameters
            JSONArray recipientJson = new JSONArray();
            for (String email : paras.getRecipients()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("email", email);
                recipientJson.put(jsonObject);
            }
            sharedDocument.put("recipients", recipientJson);
            sharedDocument.put("comment", paras.getComment());

            // add watermark & expiry  --- for sharing nxl file, we don't need to pass watermark & expiry.
            if (!TextUtils.isEmpty(paras.getWatermark())) {
                sharedDocument.put("watermark", paras.getWatermark());
            }

            if (paras.getExpiry() != null) {
                sharedDocument.put("expiry", paras.getExpiry().toJsonObj());
            }

            parameters.put("sharedDocument", sharedDocument);
            shareJson.put("parameters", parameters);
        } catch (JSONException e) {
            log.e("failed prepare post data in SharingRepoFileResult-" + e.toString(), e);
            throw new RmsRestAPIException("failed prepare in SharingRepoFileResult-", RmsRestAPIException.ExceptionDomain.Common);
        }

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                shareJson.toString());
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getShareRepoFileURL())
                .post(body)
                .build();

        String responseString = executeNetRequest(request);
        log.i("remoteViewRepoFile:\n" + responseString);

        // parse response
        try {
            JSONObject jo = new JSONObject(responseString);
            if (!jo.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = jo.getInt("statusCode");
            if (code == 200) {
                return new Gson().fromJson(responseString, SharingRepoFileResult.class);
            } else if (code == 400) {
                throw new RmsRestAPIException("Invalid request data", RmsRestAPIException.ExceptionDomain.MalformedRequest, code);
            } else if (code == 401) {
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else if (code == 403) {
                throw new RmsRestAPIException("Access Denied", RmsRestAPIException.ExceptionDomain.AccessDenied, code);
            } else if (code == 500) {
                throw new RmsRestAPIException("Internal Server Error.", RmsRestAPIException.ExceptionDomain.InternalServerError, code);
            } else if (code == 4001) {
                throw new RmsRestAPIException("File has been revoked.", RmsRestAPIException.ExceptionDomain.FileHasBeenRevoked, code);
            } else if (code == 4002) {
                throw new RmsRestAPIException("No transaction has been performed.", RmsRestAPIException.ExceptionDomain.NoTransactionPerformed, code);
            } else if (code == 4003) {
                throw new RmsRestAPIException("The file you are sharing is expired.", RmsRestAPIException.ExceptionDomain.FILE_EXPIRED, code);
            } else if (code == 4007) {
                throw new RmsRestAPIException("Comment too long.", RmsRestAPIException.ExceptionDomain.CommentTooLong, code);
            } else {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public UpdateRecipientsResult updateRecipients(String duid, @Nullable List<String> newRecipientList,
                                                   @Nullable List<String> removeRecipientList,
                                                   @Nullable String comments) throws RmsRestAPIException {
        // generate request json
        JSONObject postJson = new JSONObject();
        JSONObject parameters = new JSONObject();
        try {
            if (newRecipientList != null && newRecipientList.size() > 0) {
                JSONArray newRecipients = new JSONArray();
                for (String newRecipient : newRecipientList) {
                    JSONObject oneOjb = new JSONObject();
                    oneOjb.put("email", newRecipient);
                    newRecipients.put(oneOjb);
                }
                parameters.put("newRecipients", newRecipients);
            }

            if (removeRecipientList != null && removeRecipientList.size() > 0) {
                JSONArray removedRecipients = new JSONArray();
                for (String removedRecipient : removeRecipientList) {
                    JSONObject oneOjb = new JSONObject();
                    oneOjb.put("email", removedRecipient);
                    removedRecipients.put(oneOjb);
                }
                parameters.put("removedRecipients", removedRecipients);
            }
            parameters.put("comment", comments);
            postJson.put("parameters", parameters);
        } catch (JSONException e) {
            log.e("failed prepare post data in updateRecipients-" + e.toString(), e);
            throw new RmsRestAPIException("failed prepare post data in updateRecipients-", RmsRestAPIException.ExceptionDomain.Common);
        }

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                postJson.toString());
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getUpdateRecipientURL().replace("{duid}", duid))
                .post(body)
                .build();

        String responseString = executeNetRequest(request);
        log.v("updateRecipients:\n" + responseString);

        try {
            JSONObject jo = new JSONObject(responseString);
            if (!jo.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = jo.getInt("statusCode");
            if (code == 200) {
                return new Gson().fromJson(responseString, UpdateRecipientsResult.class);
            } else if (code == 400) {
                throw new RmsRestAPIException("Malformed request", RmsRestAPIException.ExceptionDomain.MalformedRequest, code);
            } else if (code == 401) {
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else if (code == 403) {
                throw new RmsRestAPIException("Access denied", RmsRestAPIException.ExceptionDomain.AccessDenied, code);
            } else if (code == 4001) {
                throw new RmsRestAPIException("File has been revoked.", RmsRestAPIException.ExceptionDomain.FileHasBeenRevoked, code);
            } else if (code == 4002) {
                throw new RmsRestAPIException("No transaction has been performed", RmsRestAPIException.ExceptionDomain.NoTransactionPerformed, code);
            } else if (code == 4003) {
                throw new RmsRestAPIException("The file you are sharing is expired.", RmsRestAPIException.ExceptionDomain.FILE_EXPIRED, code);
            } else if (code == 500) {
                throw new RmsRestAPIException("Internal Server Error", RmsRestAPIException.ExceptionDomain.InternalServerError, code);
            } else {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public UpdateProjectRecipientsResult updateProjectRecipients(String duid,
                                                                 List<Integer> newRecipients,
                                                                 List<Integer> removedRecipients,
                                                                 String comments) throws RmsRestAPIException {
        JSONObject postObj = new JSONObject();
        JSONObject parametersObj = new JSONObject();
        try {
            if (newRecipients != null && !newRecipients.isEmpty()) {
                JSONArray newRecipientsArr = new JSONArray();
                for (Integer id : newRecipients) {
                    JSONObject recipientObj = new JSONObject();
                    recipientObj.put("projectId", id);
                    newRecipientsArr.put(recipientObj);
                }
                parametersObj.put("newRecipients", newRecipientsArr);
            }

            if (removedRecipients != null && !removedRecipients.isEmpty()) {
                JSONArray removedRecipientsArr = new JSONArray();
                for (Integer id : removedRecipients) {
                    JSONObject recipientObj = new JSONObject();
                    recipientObj.put("projectId", id);
                    removedRecipientsArr.put(recipientObj);
                }
                parametersObj.put("removedRecipients", removedRecipientsArr);
            }
            //parametersObj.put("comment", comments);
            postObj.put("parameters", parametersObj);
        } catch (JSONException e) {
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
        }

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                postObj.toString());
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getUpdateRecipientURL().replace("{duid}", duid))
                .post(body)
                .build();

        String response = paramCheck(executeNetRequest(request));
        log.v("updateProjectRecipients:\n" + response);

        try {
            JSONObject responseObj = new JSONObject(response);
            int code = responseObj.optInt("statusCode");
            String message = responseObj.optString("message");
            if (code == 200) {
                return new Gson().fromJson(response, UpdateProjectRecipientsResult.class);
            } else if (code == 400) {
                throw new RmsRestAPIException("Malformed request", RmsRestAPIException.ExceptionDomain.MalformedRequest, code);
            } else if (code == 401) {
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else if (code == 403) {
                throw new RmsRestAPIException("Access denied", RmsRestAPIException.ExceptionDomain.AccessDenied, code);
            } else if (code == 4001) {
                throw new RmsRestAPIException("File has been revoked.", RmsRestAPIException.ExceptionDomain.FileHasBeenRevoked, code);
            } else if (code == 4002) {
                throw new RmsRestAPIException("No transaction has been performed", RmsRestAPIException.ExceptionDomain.NoTransactionPerformed, code);
            } else if (code == 4003) {
                throw new RmsRestAPIException("The file you are sharing is expired.", RmsRestAPIException.ExceptionDomain.FILE_EXPIRED, code);
            } else if (code == 500) {
                throw new RmsRestAPIException("Internal Server Error", RmsRestAPIException.ExceptionDomain.InternalServerError, code);
            } else {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public boolean revokingDocument(String duid) throws RmsRestAPIException {
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getRevokingDocumentURL().replace("{duid}", duid))
                .delete()
                .build();

        String response = paramCheck(executeNetRequest(request));
        log.v("revokingDocument:\n" + response);

        try {
            JSONObject j = new JSONObject(response);
            int code = j.optInt("statusCode");
            if (code == 200) {
                return true;
            } else if (code == 304) {
                throw new RmsRestAPIException("File has been revoked.", RmsRestAPIException.ExceptionDomain.FileHasBeenRevoked, code);
            } else if (code == 400) {
                throw new RmsRestAPIException("Unknown platform.", RmsRestAPIException.ExceptionDomain.MalformedRequest, code);
            } else if (code == 401) {
                throw new RmsRestAPIException("Authentication failed.", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else if (code == 403) {
                throw new RmsRestAPIException("Access denied.", RmsRestAPIException.ExceptionDomain.AccessDenied, code);
            } else if (code == 404) {
                throw new RmsRestAPIException("Unable to find file metadata for given DUID.", RmsRestAPIException.ExceptionDomain.NotFound, code);
            } else if (code == 500) {
                throw new RmsRestAPIException("Internal Server Error.", RmsRestAPIException.ExceptionDomain.InternalServerError, code);
            } else {
                throw new RmsRestAPIException("Failed parse response.", RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
        }
    }

}
