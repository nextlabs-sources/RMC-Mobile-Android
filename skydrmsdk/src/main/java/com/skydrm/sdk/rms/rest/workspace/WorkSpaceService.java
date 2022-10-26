package com.skydrm.sdk.rms.rest.workspace;

import com.google.gson.Gson;
import com.skydrm.sdk.Config;
import com.skydrm.sdk.INxlRights;
import com.skydrm.sdk.INxlTags;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.rest.IWorkSpaceService;
import com.skydrm.sdk.rms.user.IRmUser;
import com.skydrm.sdk.ui.uploadprogress.ProgressHelper;
import com.skydrm.sdk.ui.uploadprogress.ProgressRequestListener;
import com.skydrm.sdk.utils.DevLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

public class WorkSpaceService extends RestAPI.RestServiceBase implements IWorkSpaceService {

    public WorkSpaceService(IRmUser user, OkHttpClient httpClient, Config config, DevLog log) {
        super(user, httpClient, config, log);
    }

    @Override
    public ListFileResult listFile(ListFileParam param) throws RmsRestAPIException {
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);

        Request request = builder
                .url(buildListFileURL(config.getWorkSpaceListFileURL(), paramCheck(param)))
                .get()
                .build();

        String response = checkResponse(executeNetRequest(request));
        log.v("RESTFUL_listFile:\n" + response);
        JSONObject responseObj;
        try {
            responseObj = new JSONObject(response);
        } catch (JSONException e) {
            throw new RmsRestAPIException(e.getMessage(),
                    RmsRestAPIException.ExceptionDomain.Common);
        }

        int statusCode = responseObj.optInt("statusCode");
        String message = responseObj.optString("message");
        if (statusCode == 200) {
            return new Gson().fromJson(response, ListFileResult.class);
        } else if (statusCode == 400) {
            throw new RmsRestAPIException(message,
                    RmsRestAPIException.ExceptionDomain.Missing_Request, statusCode);
        } else if (statusCode == 401) {
            throw new RmsRestAPIException("Authentication failed.",
                    RmsRestAPIException.ExceptionDomain.AuthenticationFailed, statusCode);
        } else if (statusCode == 403) {
            throw new RmsRestAPIException("Access denied.",
                    RmsRestAPIException.ExceptionDomain.AccessDenied, statusCode);
        } else if (statusCode == 404) {
            throw new RmsRestAPIException("Invalid file.",
                    RmsRestAPIException.ExceptionDomain.FileNotFound, statusCode);
        } else if (statusCode == 4005) {
            throw new RmsRestAPIException(message,
                    RmsRestAPIException.ExceptionDomain.InvalidFileName, statusCode);
        } else if (statusCode == 500) {
            throw new RmsRestAPIException(message,
                    RmsRestAPIException.ExceptionDomain.InternalServerError, statusCode);
        } else {
            throw new RmsRestAPIException(message,
                    RmsRestAPIException.ExceptionDomain.Common, statusCode);
        }

    }

    @Override
    public UploadFileResult uploadFile(String filePath, String parentPathId, int type,
                                       ProgressRequestListener listener)
            throws RmsRestAPIException {
        File f = new File(paramCheck(filePath));
        if (!f.exists() || f.isDirectory()) {
            throw new RmsRestAPIException("Invalid file status performed.",
                    RmsRestAPIException.ExceptionDomain.Common);
        }

        JSONObject postObj = new JSONObject();
        try {
            JSONObject parameters = new JSONObject();
            parameters.put("name", f.getName());
            parameters.put("parentPathId", paramCheck(parentPathId));
            parameters.put("type", type);
            postObj.put("parameters", parameters);
        } catch (JSONException e) {
            throw new RmsRestAPIException(e.getMessage(),
                    RmsRestAPIException.ExceptionDomain.Common);
        }

        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);

        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", f.getName(),
                        RequestBody.create(MediaType.parse("application/octet-stream"), f))
                .addFormDataPart("API-input", postObj.toString())
                .addFormDataPart("type", String.valueOf(type))
                .build();

        Request request = builder.url(config.getWorkSpaceUploadFileURL())
                .post(ProgressHelper.addProgressRequestListener(body, listener))
                .build();

        String response = checkResponse(executeNetRequest(request));
        JSONObject responseObj;
        try {
            responseObj = new JSONObject(response);
        } catch (JSONException e) {
            throw new RmsRestAPIException(e.getMessage(),
                    RmsRestAPIException.ExceptionDomain.Common);
        }

        int statusCode = responseObj.optInt("statusCode");
        String message = responseObj.optString("message");
        if (statusCode == 200) {
            return new Gson().fromJson(response, UploadFileResult.class);
        } else if (statusCode == 400) {
            throw new RmsRestAPIException(message,
                    RmsRestAPIException.ExceptionDomain.Missing_Request, statusCode);
        } else if (statusCode == 401) {
            throw new RmsRestAPIException("Authentication failed.",
                    RmsRestAPIException.ExceptionDomain.AuthenticationFailed, statusCode);
        } else if (statusCode == 403) {
            throw new RmsRestAPIException("Access denied.",
                    RmsRestAPIException.ExceptionDomain.AccessDenied, statusCode);
        } else if (statusCode == 500) {
            throw new RmsRestAPIException(message,
                    RmsRestAPIException.ExceptionDomain.InternalServerError, statusCode);
        } else if (statusCode == 5003) {
            throw new RmsRestAPIException(message,
                    RmsRestAPIException.ExceptionDomain.UNSUPPORTED_WORKSPACE_UPLOAD_FILE, statusCode);
        } else {
            throw new RmsRestAPIException(message,
                    RmsRestAPIException.ExceptionDomain.Common, statusCode);
        }
    }

    @Override
    public UploadFileResult uploadFile(String filePath, String parentPathId,
                                       INxlRights rights, INxlTags tags,
                                       ProgressRequestListener listener)
            throws RmsRestAPIException {
        File f = new File(paramCheck(filePath));
        if (!f.exists() || f.isDirectory()) {
            throw new RmsRestAPIException("Invalid file status performed.",
                    RmsRestAPIException.ExceptionDomain.Common);
        }

        JSONObject postObj = new JSONObject();
        try {
            JSONObject parameters = new JSONObject();
            parameters.put("name", f.getName());
            if (rights != null) {
                parameters.put("rightsJSON", rights.toList());
            }
            if (tags != null) {
                parameters.put("tags", tags.toJsonFormat());
            }
            parameters.put("parentPathId", parentPathId);
            postObj.put("parameters", parameters);
        } catch (JSONException e) {
            throw new RmsRestAPIException(e.getMessage(),
                    RmsRestAPIException.ExceptionDomain.Common);
        }

        RequestBody body = new MultipartBody
                .Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", f.getName(),
                        RequestBody.create(MediaType.parse("application/octet-stream"), f))
                .addFormDataPart("API-input", postObj.toString())
                .addFormDataPart("type", "0")
                .build();

        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);

        Request request = builder
                .post(ProgressHelper.addProgressRequestListener(body, listener))
                .url(config.getWorkSpaceUploadFileURL())
                .build();

        String response = checkResponse(executeNetRequest(request));
        JSONObject responseObj;
        try {
            responseObj = new JSONObject(response);
        } catch (JSONException e) {
            throw new RmsRestAPIException(e.getMessage(),
                    RmsRestAPIException.ExceptionDomain.Common);
        }

        int statusCode = responseObj.optInt("statusCode");
        String message = responseObj.optString("message");
        if (statusCode == 200) {
            return new Gson().fromJson(response, UploadFileResult.class);
        } else if (statusCode == 400) {
            throw new RmsRestAPIException(message,
                    RmsRestAPIException.ExceptionDomain.Missing_Request, statusCode);
        } else if (statusCode == 401) {
            throw new RmsRestAPIException("Authentication failed.",
                    RmsRestAPIException.ExceptionDomain.AuthenticationFailed, statusCode);
        } else if (statusCode == 403) {
            throw new RmsRestAPIException("Access denied.",
                    RmsRestAPIException.ExceptionDomain.AccessDenied, statusCode);
        } else if (statusCode == 500) {
            throw new RmsRestAPIException(message,
                    RmsRestAPIException.ExceptionDomain.InternalServerError, statusCode);
        } else {
            throw new RmsRestAPIException(message,
                    RmsRestAPIException.ExceptionDomain.Common, statusCode);
        }
    }

    @Override
    public CreateFolderResult createFolder(String parentPathId, String name, boolean autoRename)
            throws RmsRestAPIException {
        JSONObject postObj = new JSONObject();
        try {
            JSONObject parameters = new JSONObject();
            parameters.put("parentPathId", paramCheck(parentPathId));
            parameters.put("name", paramCheck(name));
            parameters.put("autorename", autoRename);
            postObj.put("parameters", parameters);
        } catch (JSONException e) {
            throw new RmsRestAPIException(e.getMessage(),
                    RmsRestAPIException.ExceptionDomain.Common);
        }

        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);

        Request request = builder
                .url(config.getWorkSpaceCreateFolderURL())
                .post(RequestBody.create(MediaType.parse("application/json"),
                        postObj.toString()))
                .build();

        String response = checkResponse(executeNetRequest(request));
        JSONObject responseObj;
        try {
            responseObj = new JSONObject(response);
        } catch (JSONException e) {
            throw new RmsRestAPIException(e.getMessage(),
                    RmsRestAPIException.ExceptionDomain.Common);
        }

        int statusCode = responseObj.optInt("statusCode");
        String message = responseObj.optString("message");
        if (statusCode == 200) {
            return new Gson().fromJson(response, CreateFolderResult.class);
        } else if (statusCode == 400) {
            throw new RmsRestAPIException(message,
                    RmsRestAPIException.ExceptionDomain.Missing_Request, statusCode);
        } else if (statusCode == 401) {
            throw new RmsRestAPIException("Authentication failed.",
                    RmsRestAPIException.ExceptionDomain.AuthenticationFailed, statusCode);
        } else if (statusCode == 403) {
            throw new RmsRestAPIException("Access denied.",
                    RmsRestAPIException.ExceptionDomain.AccessDenied, statusCode);
        } else if (statusCode == 4005) {
            throw new RmsRestAPIException(message,
                    RmsRestAPIException.ExceptionDomain.InvalidFileName, statusCode);
        } else if (statusCode == 500) {
            throw new RmsRestAPIException(message,
                    RmsRestAPIException.ExceptionDomain.InternalServerError, statusCode);
        } else {
            throw new RmsRestAPIException(message,
                    RmsRestAPIException.ExceptionDomain.Common, statusCode);
        }
    }

    @Override
    public DeleteItemResult deleteItem(String pathId) throws RmsRestAPIException {
        JSONObject postObj = new JSONObject();
        try {
            JSONObject parameters = new JSONObject();
            parameters.put("pathId", paramCheck(pathId));
            postObj.put("parameters", parameters);
        } catch (JSONException e) {
            throw new RmsRestAPIException(e.getMessage(),
                    RmsRestAPIException.ExceptionDomain.Common);
        }

        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);

        Request request = builder
                .url(config.getWorkSpaceDeleteItemURL())
                .post(RequestBody.create(MediaType.parse("application/json"), postObj.toString()))
                .build();

        String response = paramCheck(executeNetRequest(request));
        JSONObject responseObj;
        try {
            responseObj = new JSONObject(response);
        } catch (JSONException e) {
            throw new RmsRestAPIException(e.getMessage(),
                    RmsRestAPIException.ExceptionDomain.Common);
        }

        int statusCode = responseObj.optInt("statusCode");
        String message = responseObj.optString("message");
        if (statusCode == 200) {
            return new Gson().fromJson(response, DeleteItemResult.class);
        } else if (statusCode == 400) {
            throw new RmsRestAPIException(message,
                    RmsRestAPIException.ExceptionDomain.Missing_Request, statusCode);
        } else if (statusCode == 401) {
            throw new RmsRestAPIException("Authentication failed.",
                    RmsRestAPIException.ExceptionDomain.AuthenticationFailed, statusCode);
        } else if (statusCode == 403) {
            throw new RmsRestAPIException("Access denied.",
                    RmsRestAPIException.ExceptionDomain.AccessDenied, statusCode);
        } else if (statusCode == 404) {
            throw new RmsRestAPIException("Invalid file.",
                    RmsRestAPIException.ExceptionDomain.FileNotFound, statusCode);
        } else if (statusCode == 500) {
            throw new RmsRestAPIException(message,
                    RmsRestAPIException.ExceptionDomain.InternalServerError, statusCode);
        } else {
            throw new RmsRestAPIException(message,
                    RmsRestAPIException.ExceptionDomain.Common, statusCode);
        }
    }

    @Override
    public FileMetadata getFileMetadata(String pathId) throws RmsRestAPIException {
        JSONObject postObj = new JSONObject();
        try {
            JSONObject parameters = new JSONObject();
            parameters.put("pathId", paramCheck(pathId));
            postObj.put("parameters", parameters);
        } catch (JSONException e) {
            throw new RmsRestAPIException(e.getMessage(),
                    RmsRestAPIException.ExceptionDomain.Common);
        }

        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);

        Request request = builder
                .url(config.getWorkSpaceFileMetadataURL())
                .post(RequestBody.create(MediaType.parse("application/json"), postObj.toString()))
                .build();

        String response = checkResponse(executeNetRequest(request));
        JSONObject responseObj;
        try {
            responseObj = new JSONObject(response);
        } catch (JSONException e) {
            throw new RmsRestAPIException(e.getMessage(),
                    RmsRestAPIException.ExceptionDomain.Common);
        }

        int statusCode = responseObj.optInt("statusCode");
        String message = responseObj.optString("message");
        if (statusCode == 200) {
            return new Gson().fromJson(response, FileMetadata.class);
        } else if (statusCode == 400) {
            throw new RmsRestAPIException(message,
                    RmsRestAPIException.ExceptionDomain.Missing_Request, statusCode);
        } else if (statusCode == 401) {
            throw new RmsRestAPIException("Authentication failed.",
                    RmsRestAPIException.ExceptionDomain.AuthenticationFailed, statusCode);
        } else if (statusCode == 403) {
            throw new RmsRestAPIException("Access denied.",
                    RmsRestAPIException.ExceptionDomain.AccessDenied, statusCode);
        } else if (statusCode == 404) {
            throw new RmsRestAPIException("Invalid file.",
                    RmsRestAPIException.ExceptionDomain.FileNotFound, statusCode);
        } else if (statusCode == 500) {
            throw new RmsRestAPIException(message,
                    RmsRestAPIException.ExceptionDomain.InternalServerError, statusCode);
        } else {
            throw new RmsRestAPIException(message,
                    RmsRestAPIException.ExceptionDomain.Common, statusCode);
        }
    }

    @Override
    public FolderMetadata getFolderMetadata(FolderMetadataParam param) throws RmsRestAPIException {
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);

        Request request = builder
                .url(buildGetFolderMetadataURL(config.getWorkSpaceFolderMetadataURL(), param))
                .get()
                .build();

        String response = checkResponse(executeNetRequest(request));
        JSONObject responseObj;
        try {
            responseObj = new JSONObject(response);
        } catch (JSONException e) {
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
        }

        int statusCode = responseObj.optInt("statusCode");
        String message = responseObj.optString("message");
        if (statusCode == 200) {
            return new Gson().fromJson(response, FolderMetadata.class);
        } else if (statusCode == 400) {
            throw new RmsRestAPIException(message,
                    RmsRestAPIException.ExceptionDomain.Missing_Request, statusCode);
        } else if (statusCode == 401) {
            throw new RmsRestAPIException("Authentication failed.",
                    RmsRestAPIException.ExceptionDomain.AuthenticationFailed, statusCode);
        } else if (statusCode == 403) {
            throw new RmsRestAPIException("Access denied.",
                    RmsRestAPIException.ExceptionDomain.AccessDenied, statusCode);
        } else if (statusCode == 500) {
            throw new RmsRestAPIException(message,
                    RmsRestAPIException.ExceptionDomain.InternalServerError, statusCode);
        } else {
            throw new RmsRestAPIException(message,
                    RmsRestAPIException.ExceptionDomain.Common, statusCode);
        }
    }

    @Override
    public DownloadResult downloadFile(String localPath, String pathId, int type,
                                       RestAPI.DownloadListener listener, int... args)
            throws RmsRestAPIException {
        File f = new File(paramCheck(localPath));
        if (!f.exists() || f.isDirectory()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
            }
        }
        JSONObject postObj = new JSONObject();
        try {
            JSONObject parameters = new JSONObject();
            for (int i = 0; i < args.length; i++) {
                if (i == 0) {
                    parameters.put("start", args[0]);
                }
                if (i == 1) {
                    parameters.put("length", args[1]);
                }
            }
            parameters.put("pathId", pathId);
            parameters.put("type", type);
            postObj.put("parameters", parameters);
        } catch (JSONException e) {
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
        }

        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);

        Request request = builder.url(config.getWorkSpaceDownloadFileURL())
                .post(RequestBody.create(MediaType.parse("application/json"), postObj.toString()))
                .build();

        Response response;
        try {
            response = httpClient.newCall(request).execute();
        } catch (IOException e) {
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.NetWorkIOFailed);
        }

        if (!response.isSuccessful()) {
            if (response.code() == 401) {
                throw new RmsRestAPIException("Authentication failed.",
                        RmsRestAPIException.ExceptionDomain.AuthenticationFailed, response.code());
            } else if (response.code() == 403) {
                throw new RmsRestAPIException("Access denied.",
                        RmsRestAPIException.ExceptionDomain.AccessDenied, response.code());
            } else if (response.code() == 404) {
                throw new RmsRestAPIException("Invalid file.",
                        RmsRestAPIException.ExceptionDomain.FileNotFound, response.code());
            } else if (response.code() == 500) {
                throw new RmsRestAPIException("Internal server error.",
                        RmsRestAPIException.ExceptionDomain.InternalServerError, response.code());
            } else {
                throw new RmsRestAPIException(response.message(),
                        RmsRestAPIException.ExceptionDomain.Common, response.code());
            }
        }
        String x_rms_last_modified = response.header("x-rms-last-modified");
        String content_Disposition = response.header("Content-Disposition");
        String x_rms_file_size = response.header("x-rms-file-size");

        DownloadResult downloadResult = new DownloadResult(x_rms_last_modified,
                content_Disposition, x_rms_file_size);

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
            return downloadResult;
        } catch (FileNotFoundException e) {
            throw new RmsRestAPIException(e.getMessage(),
                    RmsRestAPIException.ExceptionDomain.FileNotFound);
        } catch (IOException e) {
            f.delete();
            throw new RmsRestAPIException(e.getMessage(),
                    RmsRestAPIException.ExceptionDomain.NetWorkIOFailed);
        }
    }

    @Override
    public ReClassifyResult reClassifyFile(String fileName, String parentPathId, String fileTags)
            throws RmsRestAPIException {
        JSONObject postObj = new JSONObject();
        try {
            JSONObject parameter = new JSONObject();
            parameter.put("fileName", paramCheck(fileName));
            parameter.put("parentPathId", paramCheck(parentPathId));
            parameter.put("fileTags", paramCheck(fileTags));
            postObj.put("parameters", parameter);
        } catch (JSONException e) {
            throw new RmsRestAPIException(e.getMessage(),
                    RmsRestAPIException.ExceptionDomain.Common);
        }

        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);

        Request request = builder.url(config.getWorkSpaceReClassifyFileURL())
                .put(RequestBody.create(MediaType.parse("application/json"), postObj.toString()))
                .build();
        String response = checkResponse(executeNetRequest(request));

        JSONObject responseObj;
        try {
            responseObj = new JSONObject(response);
        } catch (JSONException e) {
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
        }

        int statusCode = responseObj.optInt("statusCode");
        String message = responseObj.optString("message");
        if (statusCode == 200) {
            return new Gson().fromJson(response, ReClassifyResult.class);
        } else if (statusCode == 400) {
            throw new RmsRestAPIException(message,
                    RmsRestAPIException.ExceptionDomain.Missing_Request, statusCode);
        } else if (statusCode == 401) {
            throw new RmsRestAPIException("Authentication failed.",
                    RmsRestAPIException.ExceptionDomain.AuthenticationFailed, statusCode);
        } else if (statusCode == 403) {
            throw new RmsRestAPIException("Access denied.",
                    RmsRestAPIException.ExceptionDomain.AccessDenied, statusCode);
        } else if (statusCode == 404) {
            throw new RmsRestAPIException("Invalid file.",
                    RmsRestAPIException.ExceptionDomain.FileNotFound, statusCode);
        } else if (statusCode == 500) {
            throw new RmsRestAPIException(message,
                    RmsRestAPIException.ExceptionDomain.InternalServerError, statusCode);
        } else {
            throw new RmsRestAPIException(message,
                    RmsRestAPIException.ExceptionDomain.Common, statusCode);
        }

    }

    private String buildListFileURL(String base, ListFileParam param) {
        StringBuilder urlBuilder = new StringBuilder(base);
        // append page.
        int page = param.getPage();
        urlBuilder.append("page=").append(page);
        // append size.
        int size = param.getSize();
        urlBuilder.append("&size=").append(size);
        // append orderBy.
        String orderBy = param.getOrderBy();
        if (orderBy != null && !orderBy.isEmpty()) {
            urlBuilder.append("&orderBy=").append(orderBy);
        }
        // append pathId.
        String pathId = param.getPathId();
        if (pathId != null && !pathId.isEmpty()) {
            urlBuilder.append("&pathId=").append(pathId);
        }
        // append q.
        String q = param.getQ();
        if (q != null && !q.isEmpty()) {
            urlBuilder.append("&q=").append(q);
        }
        // append search string.
        String searchString = param.getSearchString();
        if (searchString != null && !searchString.isEmpty()) {
            urlBuilder.append("&searchString=").append(searchString);
        }
        return urlBuilder.toString();
    }

    private String buildGetFolderMetadataURL(String base, FolderMetadataParam param)
            throws RmsRestAPIException {
        StringBuilder urlBuilder = new StringBuilder(base);
        FolderMetadataParam tmp = paramCheck(param);
        int page = tmp.getPage();
        urlBuilder.append("page=").append(page);
        int size = tmp.getSize();
        urlBuilder.append("&size=").append(size);
        String orderBy = tmp.getOrderBy();
        urlBuilder.append("&orderBy=").append(paramCheck(orderBy));
        String pathId = tmp.getPathId();
        urlBuilder.append("&pathId=").append(paramCheck(pathId));
        long lastModified = tmp.getLastModified();
        urlBuilder.append("&lastModified=").append(paramCheck(lastModified));
        return urlBuilder.toString();
    }
}
