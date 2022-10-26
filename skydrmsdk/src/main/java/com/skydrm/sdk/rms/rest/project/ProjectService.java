package com.skydrm.sdk.rms.rest.project;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.skydrm.sdk.Config;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.rest.IProjectService;
import com.skydrm.sdk.rms.rest.project.file.ListFileParam;
import com.skydrm.sdk.rms.rest.project.file.ListFileResult;
import com.skydrm.sdk.rms.rest.project.file.ProjectDownloadHeader;
import com.skydrm.sdk.rms.rest.project.file.UploadFileResult;
import com.skydrm.sdk.rms.rest.project.file.UploadForNXLFileParam;
import com.skydrm.sdk.rms.rest.project.member.ListMemberParam;
import com.skydrm.sdk.rms.rest.project.member.ListMemberResult;
import com.skydrm.sdk.rms.rest.project.member.MemberDetailResult;
import com.skydrm.sdk.rms.rest.project.member.PendingInvitationResult;
import com.skydrm.sdk.rms.rest.project.member.ProjectInvitationResult;
import com.skydrm.sdk.rms.rest.project.member.ProjectPendingInvitationsParas;
import com.skydrm.sdk.rms.user.IRmUser;
import com.skydrm.sdk.ui.uploadprogress.ProgressHelper;
import com.skydrm.sdk.ui.uploadprogress.ProgressRequestListener;
import com.skydrm.sdk.utils.DevLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/**
 * Created by hhu on 5/3/2018.
 */

public class ProjectService extends RestAPI.RestServiceBase implements IProjectService {

    public ProjectService(IRmUser user, OkHttpClient httpClient, Config config, DevLog log) {
        super(user, httpClient, config, log);
    }

    @Override
    public CreateProjectResult createProject(String name, String description,
                                             List<String> emails, String invitationMsg)
            throws RmsRestAPIException {
        JSONObject requestObj = new JSONObject();
        JSONObject parameters = new JSONObject();
        try {
            JSONArray emailArr = new JSONArray();
            if (emails != null && emails.size() != 0) {
                for (String email : emails) {
                    emailArr.put(email);
                }
            }
            parameters.put("projectName", name);
            parameters.put("projectDescription", description);
            if (!TextUtils.isEmpty(invitationMsg)) {
                parameters.put("invitationMsg", invitationMsg);
            }
            parameters.put("emails", emailArr);
            requestObj.put("parameters", parameters);
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed prepare post data in createProject-", RmsRestAPIException.ExceptionDomain.Common);
        }

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), requestObj.toString());
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getCreateProjectURL())
                .put(requestBody)
                .build();

        String responseString = executeNetRequest(request);
        log.i("RESTFUL_createProject:\n" + responseString);

        try {
            JSONObject responseObj = new JSONObject(responseString);
            int code = responseObj.optInt("statusCode");
            String message = responseObj.optString("message");
            if (code == 200) {
                return new Gson().fromJson(responseString, CreateProjectResult.class);
            } else if (code == 400) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.MalformedRequest, code);
            } else if (code == 401) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else if (code == 403) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.AccessDenied, code);
            } else if (code == 4001) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.InvalidProjectName, code);
            } else if (code == 4002) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.InvalidProjectDescription, code);
            } else if (code == 4003) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.InvalidProjectName, code);
            } else if (code == 5005) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.ProjectNameAlreadyExist, code);
            } else if (code == 500) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.InternalServerError, code);
            } else {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public ListProjectItemResult listProject(int page, int size, String orderBy, boolean ownedByMe)
            throws RmsRestAPIException {
        String url = config.getListProjectsURL();
        String urlParams = "?page=" +
                page +
                "&size=" +
                size +
                "&orderBy=" +
                orderBy +
                "&ownedByMe=" +
                ownedByMe;
        url += urlParams;

        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(url)
                .get()
                .build();

        String responseString = executeNetRequest(request);
        log.i("RESTFUL_listProject:\n" + responseString);

        try {
            JSONObject jo = new JSONObject(responseString);
            if (!jo.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = jo.getInt("statusCode");
            if (code == 200) {
                return new Gson().fromJson(responseString, ListProjectItemResult.class);
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
    public UpdateProjectResult updateProject(int projectId, String projectName,
                                             String projectDescription, String invitationMsg)
            throws RmsRestAPIException {
        JSONObject requestJson = new JSONObject();
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("projectName", projectName);
            parameters.put("projectDescription", projectDescription);
            parameters.put("invitationMsg", invitationMsg);
            requestJson.put("parameters", parameters);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), requestJson.toString());

        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getUpdateProjectURL().replace("{projectId}", Integer.toString(projectId)))
                .post(requestBody)
                .build();

        String responseString = executeNetRequest(request);
        log.i("RESTFUL_updateProject:\n" + responseString);

        try {
            JSONObject responseObj = new JSONObject(responseString);
            int code = responseObj.optInt("statusCode");
            String message = responseObj.optString("message");
            if (code == 200) {
                return new Gson().fromJson(responseString, UpdateProjectResult.class);
            } else if (code == 400) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.MalformedRequest, code);
            } else if (code == 401) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else if (code == 403) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.AccessDenied, code);
            } else if (code == 404) {
                throw new RmsRestAPIException("Invalid project", RmsRestAPIException.ExceptionDomain.NotFound, code);
            } else if (code == 4001) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.InvalidProjectName, code);
            } else if (code == 4002) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.InvalidProjectDescription, code);
            } else if (code == 4003) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.InvalidProjectName, code);
            } else if (code == 5005) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.ProjectNameAlreadyExist, code);
            } else if (code == 500) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.InternalServerError, code);
            } else {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public GetProjectMetadataResult getProjectMetadata(int projectId)
            throws RmsRestAPIException {
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getProjectMetaDataRUL().replace("{projectId}", Integer.toString(projectId)))
                .get()
                .build();

        String responseString = executeNetRequest(request);
        log.i("RESTFUL_getProjectMetaData:\n" + responseString);

        try {
            JSONObject responseObj = new JSONObject(responseString);
            int code = responseObj.optInt("statusCode");
            String message = responseObj.optString("message");
            if (code == 200) {
                return new Gson().fromJson(responseString, GetProjectMetadataResult.class);
            } else if (code == 400) {
                if (message != null && message.contains("Invalid project")) {
                    throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.NotFound, code);
                } else {
                    throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.MalformedRequest, code);
                }
            } else if (code == 401) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else if (code == 404) {
                throw new RmsRestAPIException("Invalid project", RmsRestAPIException.ExceptionDomain.NotFound, code);
            } else {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public String getProjectMembershipId(int projectId)
            throws RmsRestAPIException {
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getProjectGetMembershipURL().replace("{projectId}", Integer.toString(projectId)))
                .get()
                .build();
        String responseString = executeNetRequest(request);
        log.i("RESTFUL_projectGetMembership:\n" + responseString);
        // parse response
        try {
            JSONObject responseObj = new JSONObject(responseString);
            int code = responseObj.optInt("statusCode");
            String message = responseObj.optString("message");
            if (code == 200) {
                return responseString;
            } else if (code == 401) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else if (code == 404) {
                throw new RmsRestAPIException("Invalid project", RmsRestAPIException.ExceptionDomain.NotFound, code);
            } else {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public UploadFileResult uploadNXLFile(int projectId, UploadForNXLFileParam param, File file,
                                          ProgressRequestListener progressRequestListener)
            throws RmsRestAPIException {
        String jsonRequest = new Gson().toJson(param);

        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("API-input", jsonRequest)
                .addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse("application/octet-stream"), file))
                .addFormDataPart("type", Integer.toString(0))
                .build();

        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getProjectUploadNXLFileURL().replace("{projectId}", Integer.toString(projectId)))
                .post(ProgressHelper.addProgressRequestListener(body, progressRequestListener))
                .build();

        String responseString = executeNetRequest(request);
        log.d("RESTFUL_uploadNXLFile:\n" + responseString);
        try {
            JSONObject responseObj = new JSONObject(responseString);
            if (!responseObj.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = responseObj.optInt("statusCode");
            String message = responseObj.optString("message");
            if (code == 200) {
                return new Gson().fromJson(responseString, UploadFileResult.class);
            } else if (code == 400) {
                if (message != null && message.contains("Invalid project")) {
                    throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.NotFound, code);
                } else {
                    throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.MalformedRequest, code);
                }
            } else if (code == 401) {
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else if (code == 404) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.FileNotFound, code);
            } else if (code == 4006) {
                throw new RmsRestAPIException("File already exists", RmsRestAPIException.ExceptionDomain.ProjectFileAlreadyExists, code);
            } else {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public ListFileResult listFile(int projectId, ListFileParam paras)
            throws RmsRestAPIException {
        String urlPath = config.getProjectFileListingURL();
        urlPath = urlPath.replace("{projectId}", Integer.toString(projectId));
        StringBuilder stringBuilder = new StringBuilder();
        if (paras.getmPage() != -1 && paras.getmSize() != -1) {
            stringBuilder.append("page=");
            stringBuilder.append(paras.getmPage());
            stringBuilder.append("&size=");
            stringBuilder.append(paras.getmSize());
        }
        if (stringBuilder.length() == 0) {
            stringBuilder.append("orderBy=");
            stringBuilder.append(paras.getmOrderBy());
        } else {
            stringBuilder.append("&orderBy=");
            stringBuilder.append(paras.getmOrderBy());
        }
        if (!TextUtils.isEmpty(paras.getmPathId())) {
            if (stringBuilder.length() == 0) {
                stringBuilder.append("pathId=");
                stringBuilder.append(paras.getmPathId());
            } else { // have para before it, need add "&"
                stringBuilder.append("&pathId=");
                stringBuilder.append(paras.getmPathId());
            }
        }
        if (!TextUtils.isEmpty(paras.getmP()) && !TextUtils.isEmpty(paras.getmSearchString())) {
            if (stringBuilder.length() == 0) {
                stringBuilder.append("q=");
                stringBuilder.append(paras.getmP());
                stringBuilder.append("searchString=");
                stringBuilder.append(paras.getmSearchString());
            } else {
                stringBuilder.append("&q=");
                stringBuilder.append(paras.getmP());
                stringBuilder.append("&searchString=");
                stringBuilder.append(paras.getmSearchString());
            }
        }
        if (!TextUtils.isEmpty(paras.getFilter())) {
            stringBuilder.append("&filter=");
            stringBuilder.append(paras.getFilter());
        }

        urlPath += stringBuilder.toString();
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(urlPath)
                .get()
                .build();

        String responseResult;
        Call call;
        try {
            call = httpClient.newCall(request);
            Response response = call.execute();
            responseResult = response.body().string();
        } catch (IOException e) {
            if (e instanceof InterruptedIOException) {
                throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.ThreadInterrupted);
            } else {
                throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.NetWorkIOFailed);
            }
        }

        log.i("RESTFUL_listFile:\n" + responseResult);

        try {
            JSONObject responseObj = new JSONObject(responseResult);
            int code = responseObj.optInt("statusCode");
            String message = responseObj.optString("message");
            if (code == 200) {
                return new Gson().fromJson(responseResult, ListFileResult.class);
            } else if (code == 400) {
                throw new RmsRestAPIException("Invalid project", RmsRestAPIException.ExceptionDomain.NotFound, code);
            } else if (code == 401) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else if (code == 404) {
                throw new RmsRestAPIException("Invalid file.", RmsRestAPIException.ExceptionDomain.FileNotFound, code);
            } else {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public ListMemberResult listMember(int projectId, ListMemberParam params)
            throws RmsRestAPIException {
        String urlPath = config.getProjectListMembersURL();
        urlPath = urlPath.replace("{projectId}", Integer.toString(projectId));

        StringBuilder stringBuilder = new StringBuilder();
        if (params.getmPage() != -1 && params.getmSize() != -1) {
            stringBuilder.append("page=");
            stringBuilder.append(params.getmPage());
            stringBuilder.append("&size=");
            stringBuilder.append(params.getmSize());
        }

        if (stringBuilder.length() == 0) {
            stringBuilder.append("orderBy=");
            stringBuilder.append(params.getmOrderBy());
        } else {
            stringBuilder.append("&orderBy=");
            stringBuilder.append(params.getmOrderBy());
        }

        if (stringBuilder.length() == 0) {
            stringBuilder.append("picture=");
            stringBuilder.append(params.isbIsRequestProfilePicture());
        } else {
            stringBuilder.append("&picture=");
            stringBuilder.append(params.isbIsRequestProfilePicture());
        }

        if (!TextUtils.isEmpty(params.getmSearchField())) {
            if (stringBuilder.length() == 0) {
                stringBuilder.append("q=");
                stringBuilder.append(params.getmSearchField());
            } else { // have para before it, need add "&"
                stringBuilder.append("&q=");
                stringBuilder.append(params.getmSearchField());
            }
        }

        if (!TextUtils.isEmpty(params.getmSearchString())) {
            if (stringBuilder.length() == 0) {
                stringBuilder.append("searchString=");
                stringBuilder.append(params.getmSearchString());
            } else { // have para before it, need add "&"
                stringBuilder.append("&searchString=");
                stringBuilder.append(params.getmSearchString());
            }
        }

        urlPath += stringBuilder.toString();
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(urlPath)
                .get()
                .build();

        String responseRawJson;
        try {
            Response response = httpClient.newCall(request).execute();
            responseRawJson = response.body().string();
        } catch (IOException e) {
            if (e instanceof InterruptedIOException) {
                throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.ThreadInterrupted);
            } else {
                throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.NetWorkIOFailed);
            }
        }
        log.i("RESTFUL_projectListMembers:\n" + responseRawJson);
        try {
            JSONObject responseObj = new JSONObject(responseRawJson);
            int statusCode = responseObj.optInt("statusCode");
            String message = responseObj.optString("message");
            if (statusCode == 200) {
                return new Gson().fromJson(responseRawJson, ListMemberResult.class);
            } else if (statusCode == 400) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.InvalidProject, statusCode);
            } else if (statusCode == 401) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.AuthenticationFailed, statusCode);
            } else {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.Common, statusCode);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public String removeMember(int projectId, int memberId)
            throws RmsRestAPIException {
        // prepare data
        JSONObject postJSON = new JSONObject();
        try {
            JSONObject parameters = new JSONObject();
            parameters.put("memberId", memberId);
            postJSON.put("parameters", parameters);
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed prepare post data in projectRemoveMember-", e);
        }
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                postJSON.toString());
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getProjectRemoveMemberURL().replace("{projectId}", Integer.toString(projectId)))
                .post(body)
                .build();

        String responseString = executeNetRequest(request);
        log.i("RESTFUL_projectRemoveMember:\n" + responseString);

        // parse response
        try {
            JSONObject responseObj = new JSONObject(responseString);
            int code = responseObj.optInt("statusCode");
            String message = responseObj.optString("message");
            if (code >= 200 && code < 300) {  // note: here is 204(not 200) means succeed!
                return responseString;
            } else if (code == 400) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.MalformedRequest, code);
            } else if (code == 401) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else if (code == 404) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.NotFound, code);
            } else if (code == 5001) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.OnlyOwnerRemoveMember, code);
            } else if (code == 5002) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.OwnerCannotBeRemoved, code);
            } else if (code == 500) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.InternalServerError, code);
            } else {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public MemberDetailResult getMemberDetail(int projectId, int memberId)
            throws RmsRestAPIException {
        String url = config.getProjectMemberDetailsURL();
        url = url.replace("{projectId}", Integer.toString(projectId));
        url = url.replace("{memberId}", Integer.toString(memberId));
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(url)
                .get()
                .build();

        String responseString = executeNetRequest(request);
        log.i("RESTFUL_projectMemberDetails:\n" + responseString);
        // parse response
        try {
            JSONObject responseObj = new JSONObject(responseString);

            int code = responseObj.optInt("statusCode");
            String message = responseObj.optString("message");
            if (code == 200) {
                return new Gson().fromJson(responseString, MemberDetailResult.class);
            } else if (code == 401) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else if (code == 404) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.NotFound, code);
            } else {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public ListPendingInvitationResult listPendingInvitationForUser()
            throws RmsRestAPIException {
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getListPendingInvitationsForAUser())
                .get()
                .build();

        String responseString = executeNetRequest(request);
        log.i("RESTFUL_listUserPendingInvitations:\n" + responseString);

        // parse response
        try {
            JSONObject responseObj = new JSONObject(responseString);
            int code = responseObj.optInt("statusCode");
            String message = responseObj.optString("message");
            if (code == 200) {
                return new Gson().fromJson(responseString, ListPendingInvitationResult.class);
            } else if (code == 401) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public String acceptInvitation(int invitationId, String code)
            throws RmsRestAPIException {
        String url = config.getProjectAcceptInvitationURL();
        url = url.replace("{invitation_id}", Integer.toString(invitationId));
        url = url.replace("{code}", code);
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(url)
                .get()
                .build();

        String responseString = executeNetRequest(request);
        log.i("RESTFUL_projectAcceptInvitation:\n" + responseString);

        // parse response
        try {
            JSONObject responseObj = new JSONObject(responseString);

            int statusCode = responseObj.optInt("statusCode");
            String message = responseObj.optString("message");
            if (statusCode == 200) {
                return responseString;
            } else if (statusCode == 401) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.AuthenticationFailed, statusCode);
            } else if (statusCode == 404) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.NotFound, statusCode);
            } else if (statusCode == 4001) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.InvitationExpired, statusCode);
            } else if (statusCode == 4002) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.InvitationAlreadyDeclined, statusCode);
            } else if (statusCode == 4003) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.EmailNotMatched, statusCode);
            } else if (statusCode == 4005) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.InvitationAlreadyAccepted, statusCode);
            } else if (statusCode == 4006) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.InvitationAlreadyRevoked, statusCode);
            } else if (statusCode == 500) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.InternalServerError, statusCode);
            } else {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.Common, statusCode);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public boolean denyInvitation(int invitationId, String code, @Nullable String reason)
            throws RmsRestAPIException {
        // prepare post data
        StringBuilder sb;
        try {
            sb = new StringBuilder()
                    .append("id=")
                    .append(URLEncoder.encode(Integer.toString(invitationId), StandardCharsets.UTF_8.name()))
                    .append("&code=")
                    .append(URLEncoder.encode(code, StandardCharsets.UTF_8.name()))
                    .append("&declineReason=")
                    .append(URLEncoder.encode(reason, StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
        }

        RequestBody body = RequestBody.create(
                MediaType.parse("application/x-www-form-urlencoded"),
                sb.toString());
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getProjectDeclineInvitationURL())
                .post(body)
                .build();

        String responseString = executeNetRequest(request);
        log.i("RESTFUL_projectDeclineInvitation:\n" + responseString);

        try {
            JSONObject j = new JSONObject(responseString);
            if (!j.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int statusCode = j.getInt("statusCode");
            if (statusCode == 200) {
                return true;
            } else if (statusCode == 400) {
                throw new RmsRestAPIException("Invalid request parameters", RmsRestAPIException.ExceptionDomain.MalformedRequest, statusCode);
            } else if (statusCode == 401) {
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, statusCode);
            } else if (statusCode == 404) {
                throw new RmsRestAPIException("Invitation not found", RmsRestAPIException.ExceptionDomain.NotFound, statusCode);
            } else if (statusCode == 4001) {
                throw new RmsRestAPIException("Invitation expired", RmsRestAPIException.ExceptionDomain.InvitationExpired, statusCode);
            } else if (statusCode == 4002) {
                throw new RmsRestAPIException("Invitation already declined", RmsRestAPIException.ExceptionDomain.InvitationAlreadyDeclined, statusCode);
            } else if (statusCode == 4005) {
                throw new RmsRestAPIException("Invitation already accepted", RmsRestAPIException.ExceptionDomain.InvitationAlreadyAccepted, statusCode);
            } else if (statusCode == 4006) {
                throw new RmsRestAPIException("Invitation already revoked.", RmsRestAPIException.ExceptionDomain.InvitationAlreadyRevoked, statusCode);
            } else if (statusCode == 4007) {
                throw new RmsRestAPIException("Decline reason too long", RmsRestAPIException.ExceptionDomain.DeclineReasonTooLong, statusCode);
            } else if (statusCode == 500) {
                throw new RmsRestAPIException("Internal Server Error", RmsRestAPIException.ExceptionDomain.InternalServerError, statusCode);
            } else {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, statusCode);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public ProjectDownloadHeader downloadFile(int projectId, String pathId, File f, int type,
                                              RestAPI.DownloadListener listener, int... args)
            throws RmsRestAPIException {
        if (f == null || !f.canWrite()) {
            throw new RmsRestAPIException("invalid f param", RmsRestAPIException.ExceptionDomain.Common);
        }
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // generate request json
        JSONObject postJson = new JSONObject();
        JSONObject parameters = new JSONObject();
        try {
            for (int i = 0; i < args.length; ++i) {
                if (i == 0) {
                    parameters.put("start", args[0]);
                } else if (i == 1) {
                    parameters.put("length", args[1]);
                }
            }
            parameters.put("pathId", pathId);
            parameters.put("type", type);
            postJson.put("parameters", parameters);
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed prepare post data in projectDownloadFile-", e);
        }

        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json"),
                postJson.toString());

        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getProjectDownloadFileURL().replace("{projectId}", Integer.toString(projectId)))
                .post(requestBody)
                .build();

        Response response;
        try {
            response = httpClient.newCall(request).execute();
        } catch (IOException e) {
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.NetWorkIOFailed);
        }

        if (!response.isSuccessful()) {
            if (response.code() == 401) {
                throw new RmsRestAPIException("Authentication failed.", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, response.code());
            } else if (response.code() == 403) {
                throw new RmsRestAPIException("Access denied.", RmsRestAPIException.ExceptionDomain.AccessDenied, response.code());
            } else if (response.code() == 404) {
                throw new RmsRestAPIException("Invalid file.", RmsRestAPIException.ExceptionDomain.FileNotFound, response.code());
            } else if (response.code() == 500) {
                throw new RmsRestAPIException("Internal server error.", RmsRestAPIException.ExceptionDomain.InternalServerError, response.code());
            } else {
                throw new RmsRestAPIException(response.message(), RmsRestAPIException.ExceptionDomain.Common, response.code());
            }
        }

        String x_rms_last_modified = response.header("x-rms-last-modified");
        String content_Disposition = response.header("Content-Disposition");
        String x_rms_file_size = response.header("x-rms-file-size");

        ProjectDownloadHeader projectDownloadHeader = new ProjectDownloadHeader();
        projectDownloadHeader.setX_rms_last_modified(x_rms_last_modified);
        projectDownloadHeader.setContent_Disposition(content_Disposition);
        projectDownloadHeader.setX_rms_file_size(x_rms_file_size);

        try {
            final int DOWNLOAD_CHUNK_SIZE = 2048;
            BufferedSource source = response.body().source();

            BufferedSink sink = Okio.buffer(Okio.sink(f));
            long contentLength = Long.parseLong(x_rms_file_size);

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
            return projectDownloadHeader;
        } catch (FileNotFoundException e) {
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.FileNotFound);
        } catch (IOException e) {
            f.delete();
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.NetWorkIOFailed);
        }
    }

    @Override
    public boolean deleteFile(int projectId, String pathId)
            throws RmsRestAPIException {
        // prepare data
        JSONObject postJSON = new JSONObject();
        try {
            JSONObject parameters = new JSONObject();
            parameters.put("pathId", pathId);
            postJSON.put("parameters", parameters);
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed prepare post data in projectDeleteFileFolder-", e);
        }
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                postJSON.toString());
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getProjectDeleteFileOrFolderURL().replace("{projectId}", Integer.toString(projectId)))
                .post(body)
                .build();

        String responseString = executeNetRequest(request);
        log.i("RESTFUL_projectDeleteFileFolder:\n" + responseString);

        // parse response
        try {
            JSONObject responseObj = new JSONObject(responseString);

            int code = responseObj.optInt("statusCode");
            String message = responseObj.optString("message");
            if (code == 200) {
                return true;
            } else if (code == 401) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else if (code == 404) {
                throw new RmsRestAPIException("Invalid file.", RmsRestAPIException.ExceptionDomain.FileNotFound, code);
            } else {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public ProjectInvitationResult inviteMember(int projectId, List<String> emails, String invitationMsg)
            throws RmsRestAPIException {
        // prepare data
        JSONObject postJSON = new JSONObject();
        try {
            JSONObject parameters = new JSONObject();
            if (emails.size() > 0) {
                if (emails.size() == 1) {
                    JSONArray emailJsonArray = new JSONArray();
                    emailJsonArray.put(emails.get(0));
                    parameters.put("emails", emailJsonArray);
                } else {
                    JSONArray emailJsonArray = new JSONArray();
                    for (String oneValue : emails) {
                        emailJsonArray.put(oneValue);
                    }
                    parameters.put("emails", emailJsonArray);
                }
            }
            if (!TextUtils.isEmpty(invitationMsg)) {
                parameters.put("invitationMsg", invitationMsg);
            }
            postJSON.put("parameters", parameters);
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed prepare post data in projectInvitation-", e);
        }
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                postJSON.toString());
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getProjectInvitationURL().replace("{projectId}", Integer.toString(projectId)))
                .post(body)
                .build();
        String responseString = executeNetRequest(request);
        log.i("RESTFUL_projectInvitation:\n" + responseString);

        // parse response
        try {
            JSONObject responseObj = new JSONObject(responseString);
            int code = responseObj.optInt("statusCode");
            String message = responseObj.optString("message");
            if (code == 200) {
                return new Gson().fromJson(responseString, ProjectInvitationResult.class);
            } else if (code == 400) {
                if (message != null && message.contains("Invalid project")) {
                    throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.NotFound, code);
                } else {
                    throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.MalformedRequest, code);
                }
            } else if (code == 401) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public String createFolder(int projectId, String parentPathId, String name, boolean bIsAutoRename)
            throws RmsRestAPIException {
        // prepare data
        JSONObject postJSON = new JSONObject();
        try {
            JSONObject parameters = new JSONObject();
            parameters.put("parentPathId", parentPathId);
            parameters.put("name", name);
            parameters.put("autorename", String.format(Locale.getDefault(), "%b", bIsAutoRename));
            postJSON.put("parameters", parameters);
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed prepare post data in projectCreateFolder-", e);
        }
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                postJSON.toString());
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getProjectCreateFolderURL().replace("{projectId}", Integer.toString(projectId)))
                .post(body)
                .build();

        String responseString = executeNetRequest(request);
        log.i("RESTFUL_projectCreateFolder:\n" + responseString);

        // parse response
        try {
            JSONObject responseObj = new JSONObject(responseString);
            int code = responseObj.optInt("statusCode");
            String message = responseObj.optString("message");
            if (code == 200) {
                if (responseObj.has("results")) {
                    JSONObject results = responseObj.getJSONObject("results").getJSONObject("entry");
                    return results.getString("name");
                } else {
                    throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, code);
                }
            } else if (code == 400) {
                if (message != null && message.contains("Invalid project")) {
                    throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.NotFound, code);
                } else {
                    throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.MalformedRequest, code);
                }
            } else if (code == 401) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else if (code == 404) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.FileNotFound, code);
            } else if (code == 4001) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.InvalidFolderName, code);
            } else if (code == 4002) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.FileAlreadyExists, code);
            } else if (code == 500) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.InternalServerError, code);
            } else {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public PendingInvitationResult listPendingInvitations(int projectId, ProjectPendingInvitationsParas requestParas)
            throws RmsRestAPIException {
        String urlPath = config.getListPendingInvitationsForAProject();
        urlPath = urlPath.replace("{projectId}", Integer.toString(projectId));

        StringBuilder stringBuilder = new StringBuilder();
        if (requestParas.getmPage() != -1 && requestParas.getmSize() != -1) {
            stringBuilder.append("page=");
            stringBuilder.append(requestParas.getmPage());
            stringBuilder.append("&size=");
            stringBuilder.append(requestParas.getmSize());
        }

        if (stringBuilder.length() == 0) {
            stringBuilder.append("orderBy=");
            stringBuilder.append(requestParas.getmOrderBy());
        } else {
            stringBuilder.append("&orderBy=");
            stringBuilder.append(requestParas.getmOrderBy());
        }

        if (!TextUtils.isEmpty(requestParas.getmSearchField())) {
            if (stringBuilder.length() == 0) {
                stringBuilder.append("q=");
                stringBuilder.append(requestParas.getmSearchField());
            } else { // have para before it, need add "&"
                stringBuilder.append("&q=");
                stringBuilder.append(requestParas.getmSearchField());
            }
        }

        if (!TextUtils.isEmpty(requestParas.getmSearchString())) {
            if (stringBuilder.length() == 0) {
                stringBuilder.append("searchString=");
                stringBuilder.append(requestParas.getmSearchString());
            } else { // have para before it, need add "&"
                stringBuilder.append("&searchString=");
                stringBuilder.append(requestParas.getmSearchString());
            }
        }

        urlPath += stringBuilder.toString();

        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(urlPath)
                .get()
                .build();

        String responseString = executeNetRequest(request);
        log.i("RESTFUL_listProjectPendingInvitations:\n" + responseString);

        try {
            JSONObject responseObj = new JSONObject(responseString);
            int code = responseObj.optInt("statusCode");
            if (code == 200) {
                return new Gson().fromJson(responseString, PendingInvitationResult.class);
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
    public String resendInvitation(int invitationId)
            throws RmsRestAPIException {
        // prepare data
        JSONObject postJSON = new JSONObject();
        try {
            JSONObject parameters = new JSONObject();
            parameters.put("invitationId", invitationId);
            postJSON.put("parameters", parameters);
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed prepare post data in projectResendInvitation-", e);
        }
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                postJSON.toString());
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getProjectSendInvitationReminder())
                .post(body)
                .build();

        String responseString = executeNetRequest(request);
        log.i("RESTFUL_projectResendInvitation:\n" + responseString);

        // parse response
        try {
            JSONObject responseObj = new JSONObject(responseString);
            int code = responseObj.optInt("statusCode");
            String msg = responseObj.optString("message");
            if (code == 200) {
                return responseString;
            } else if (code == 401) {
                throw new RmsRestAPIException(msg, RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else if (code == 404) {
                throw new RmsRestAPIException(msg, RmsRestAPIException.ExceptionDomain.NotFound, code);
            } else if (code == 4001) {
                throw new RmsRestAPIException(msg, RmsRestAPIException.ExceptionDomain.InvitationExpired, code);
            } else if (code == 4002) {
                throw new RmsRestAPIException(msg, RmsRestAPIException.ExceptionDomain.InvitationAlreadyDeclined, code);
            } else if (code == 4005) {
                throw new RmsRestAPIException(msg, RmsRestAPIException.ExceptionDomain.InvitationAlreadyAccepted, code);
            } else if (code == 4006) {
                throw new RmsRestAPIException(msg, RmsRestAPIException.ExceptionDomain.InvitationAlreadyRevoked, code);
            } else if (code == 500) {
                throw new RmsRestAPIException(msg, RmsRestAPIException.ExceptionDomain.InternalServerError, code);
            } else {
                throw new RmsRestAPIException(msg, RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public String revokeInvitation(int invitationId)
            throws RmsRestAPIException {
        // prepare data
        JSONObject postJSON = new JSONObject();
        try {
            JSONObject parameters = new JSONObject();
            parameters.put("invitationId", invitationId);
            postJSON.put("parameters", parameters);
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed prepare post data in projectRevokeInvitation-", e);
        }
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                postJSON.toString());
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getProjectRevokeInvitationURL())
                .post(body)
                .build();

        String responseString = executeNetRequest(request);
        log.i("RESTFUL_projectRevokeInvitation:\n" + responseString);

        // parse response
        try {
            JSONObject responseObj = new JSONObject(responseString);
            int code = responseObj.optInt("statusCode");
            String msg = responseObj.optString("message");
            if (code == 200) {
                return responseString;
            } else if (code == 401) {
                throw new RmsRestAPIException(msg, RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else if (code == 404) {
                throw new RmsRestAPIException(msg, RmsRestAPIException.ExceptionDomain.NotFound, code);
            } else if (code == 4001) {
                throw new RmsRestAPIException(msg, RmsRestAPIException.ExceptionDomain.InvitationExpired, code);
            } else if (code == 4002) {
                throw new RmsRestAPIException(msg, RmsRestAPIException.ExceptionDomain.InvitationAlreadyDeclined, code);
            } else if (code == 4005) {
                throw new RmsRestAPIException(msg, RmsRestAPIException.ExceptionDomain.InvitationAlreadyAccepted, code);
            } else if (code == 4006) {
                throw new RmsRestAPIException(msg, RmsRestAPIException.ExceptionDomain.InvitationAlreadyRevoked, code);
            } else if (code == 500) {
                throw new RmsRestAPIException(msg, RmsRestAPIException.ExceptionDomain.InternalServerError, code);
            } else {
                throw new RmsRestAPIException(msg, RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public FileMetadata getFileMetadata(int projectId, String filePathId)
            throws RmsRestAPIException {
        if (filePathId == null || filePathId.isEmpty()) {
            throw new RmsRestAPIException("Params filePathId must not be null",
                    RmsRestAPIException.ExceptionDomain.Common);
        }

        JSONObject postObj = new JSONObject();
        try {
            JSONObject parameterObj = new JSONObject();
            parameterObj.put("pathId", filePathId);
            postObj.put("parameters", parameterObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .post(RequestBody.create(MediaType.parse("application/json"), postObj.toString()))
                .url(config.getProjectGetFileMetaDataURL(projectId))
                .build();
        String responseStr = checkResponse(executeNetRequest(request));
        log.i("RESTFUL_getFileMetadata:\n" + responseStr);

        try {
            JSONObject responseObj = new JSONObject(responseStr);
            int statusCode = responseObj.optInt("statusCode");
            String message = responseObj.optString("message");
            if (statusCode == 200) {
                return new Gson().fromJson(responseStr, FileMetadata.class);
            } else if (statusCode == 401) {
                throw new RmsRestAPIException(message,
                        RmsRestAPIException.ExceptionDomain.AuthenticationFailed, statusCode);
            } else if (statusCode == 404) {
                throw new RmsRestAPIException("Invalid file.",
                        RmsRestAPIException.ExceptionDomain.FileNotFound, statusCode);
            } else if (statusCode == 4001) {
                throw new RmsRestAPIException(message,
                        RmsRestAPIException.ExceptionDomain.InvitationExpired, statusCode);
            } else if (statusCode == 4002) {
                throw new RmsRestAPIException(message,
                        RmsRestAPIException.ExceptionDomain.InvitationAlreadyDeclined, statusCode);
            } else if (statusCode == 4005) {
                throw new RmsRestAPIException(message,
                        RmsRestAPIException.ExceptionDomain.InvitationAlreadyAccepted, statusCode);
            } else if (statusCode == 4006) {
                throw new RmsRestAPIException(message,
                        RmsRestAPIException.ExceptionDomain.InvitationAlreadyRevoked, statusCode);
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
    public ReclassifyResult reClassify(int projectId, @NonNull String fileName,
                                       @NonNull String parentPathId, @Nullable String fileTags)
            throws RmsRestAPIException {
        String fName = paramCheck(fileName);
        String pathId = paramCheck(parentPathId);

        JSONObject requestObj = new JSONObject();
        try {
            JSONObject parameterObj = new JSONObject();
            parameterObj.put("fileName", fName);
            parameterObj.put("parentPathId", pathId);
            parameterObj.put("fileTags", fileTags);
            requestObj.put("parameters", parameterObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);

        Request request = builder
                .url(config.getProjectReClassifyFileURL(projectId))
                .put(RequestBody.create(MediaType.parse("application/json"),
                        requestObj.toString()))
                .build();

        String responseStr = checkResponse(executeNetRequest(request));
        log.v("RESTFUL_reClassify:\n" + responseStr);

        try {
            JSONObject responseObj = new JSONObject(responseStr);
            int statusCode = responseObj.optInt("statusCode");
            String message = responseObj.optString("message");

            if (statusCode == 200) {
                return new Gson().fromJson(responseStr, ReclassifyResult.class);
            } else if (statusCode == 400) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.MalformedRequest);
            } else if (statusCode == 403) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.AccessDenied);
            } else if (statusCode == 404) {
                throw new RmsRestAPIException("Invalid file.", RmsRestAPIException.ExceptionDomain.FileNotFound);
            } else if (statusCode == 500) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.InternalServerError);
            } else {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.Common);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException(e.getMessage(), e);
        }
    }

    @Override
    public ShareFileResult shareFile(int projectId, String membershipId,
                                     String fileName, String filePathId, String filePath,
                                     List<Integer> recipients, String comments) throws RmsRestAPIException {
        //Sanity check first.
        if (recipients == null || recipients.isEmpty()) {
            throw new RmsRestAPIException("Recipients must not be null to perform sharing action.",
                    RmsRestAPIException.ExceptionDomain.Common);
        }

        JSONObject postObj = new JSONObject();
        try {
            JSONObject paramObj = new JSONObject();
            paramObj.put("asAttachment", false);
            JSONObject sharedDocObj = new JSONObject();
            sharedDocObj.put("membershipId", membershipId);
            sharedDocObj.put("metadata", "{}");
            sharedDocObj.put("fromSpace", 1);
            sharedDocObj.put("projectId", projectId);
            sharedDocObj.put("fileName", fileName);
            sharedDocObj.put("filePathId", filePathId);
            sharedDocObj.put("filePath", filePath);

            JSONArray recipientArr = new JSONArray();
            for (Integer id : recipients) {
                JSONObject recipientsObj = new JSONObject();
                recipientsObj.put("projectId", id);
                recipientArr.put(recipientsObj);
            }
            sharedDocObj.put("recipients", recipientArr);
            sharedDocObj.put("comment", comments);

            paramObj.put("sharedDocument", sharedDocObj);
            postObj.put("parameters", paramObj);
        } catch (JSONException e) {
            throw new RmsRestAPIException(e.getMessage(), e);
        }

        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getShareRepoFileURL())
                .post(RequestBody.create(MediaType.parse("application/json"), postObj.toString()))
                .build();

        String responseStr = checkResponse(executeNetRequest(request));
        try {
            JSONObject responseObj = new JSONObject(responseStr);
            int statusCode = responseObj.optInt("statusCode");
            String message = responseObj.optString("message");
            if (statusCode == 200) {
                return new Gson().fromJson(responseStr, ShareFileResult.class);
            } else if (statusCode == 400) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.MalformedRequest);
            } else if (statusCode == 403) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.AccessDenied);
            } else if (statusCode == 404) {
                throw new RmsRestAPIException("Invalid file.", RmsRestAPIException.ExceptionDomain.FileNotFound);
            } else if (statusCode == 500) {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.InternalServerError);
            } else {
                throw new RmsRestAPIException(message, RmsRestAPIException.ExceptionDomain.Common);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException(e.getMessage(), e);
        }
    }

    @Override
    public ReShareResult reShare(String transactionId, String transactionCode, int spaceId,
                                 List<Integer> recipients, String comments) throws RmsRestAPIException {
        if (recipients == null || recipients.isEmpty()) {
            throw new RmsRestAPIException("An recipients is required.", RmsRestAPIException.ExceptionDomain.Common);
        }
        JSONObject postObj = new JSONObject();
        try {
            JSONObject parameterObj = new JSONObject();
            parameterObj.put("transactionId", transactionId);
            parameterObj.put("transactionCode", transactionCode);
            parameterObj.put("spaceId", spaceId);
            JSONArray recipientsArr = new JSONArray();
            for (Integer id : recipients) {
                JSONObject recipientObj = new JSONObject();
                recipientObj.put("projectId", id);
                recipientsArr.put(recipientObj);
            }
            parameterObj.put("recipients", recipientsArr);
            postObj.put("parameters", parameterObj);
        } catch (JSONException e) {
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
        }

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), postObj.toString());
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder.url(config.getSharedWithMeReshareURL())
                .post(body)
                .build();

        String response = paramCheck(executeNetRequest(request));
        log.i("RESTFUL_preShareFile\n" + response);
        try {
            JSONObject responseObj = new JSONObject(response);
            int statusCode = responseObj.optInt("statusCode");
            String message = responseObj.optString("message");
            if (statusCode == 200) {
                return new Gson().fromJson(response, ReShareResult.class);
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
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public AllProjectsResult listAllProjects() throws RmsRestAPIException {
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getListAllProjectsURL())
                .get()
                .build();
        String response = paramCheck(executeNetRequest(request));

        try {
            JSONObject responseObj = new JSONObject(response);
            int statusCode = responseObj.optInt("statusCode");
            String message = responseObj.optString("message");
            if (statusCode == 200) {
                return new Gson().fromJson(response, AllProjectsResult.class);
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
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
        }
    }
}
