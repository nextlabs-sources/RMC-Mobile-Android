package com.skydrm.sdk.rms.rest.repository;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.skydrm.sdk.Config;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.rest.IMyDriveService;
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


public class MyDrive extends RestAPI.RestServiceBase implements IMyDriveService {

    public MyDrive(IRmUser user, OkHttpClient httpClient, Config config, DevLog log) {
        super(user, httpClient, config, log);
    }

    @Override
    public JSONObject myDriveList(String path) throws RmsRestAPIException {
        // path rectify:
        //  - ignore root,
        //  - for others , folder requires must be end with /
        if (path.length() > 1 && !path.endsWith("/")) {
            path = path + "/";
        }
        JSONObject postJSON = new JSONObject();
        try {
             /*
            {
                 "parameters":{
                    "pathId":"/one/"
                 }
            }
         */
            JSONObject parameters = new JSONObject();
            parameters.put("pathId", path);
            // parameters.put("recursive", recursive);
            postJSON.put("parameters", parameters);
        } catch (JSONException e) {
            log.i("failed prepare post data in myDriveList-" + e.toString());
            throw new RmsRestAPIException("failed prepare post data in myDriveList-", RmsRestAPIException.ExceptionDomain.Common);
        }
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                postJSON.toString());
        Request.Builder builder = new Request.Builder();

        setCommonParas(builder);
        Request request = builder
                .url(config.getMyDriveListURL())
                .post(body)
                .build();

        String responseString = executeNetRequest(request);
        log.i("myDriveList-path:\t" + path + "\n" + responseString);
        // parse response
        try {
            JSONObject j = new JSONObject(responseString);
            if (!j.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = j.getInt("statusCode");
            if (code == 200) {
                if (j.has("results")) {
                    return j.getJSONObject("results");
                } else {
                    throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, code);
                }
            } else if (code == 400) {
                throw new RmsRestAPIException("Malformed request", RmsRestAPIException.ExceptionDomain.MalformedRequest, code);
            } else if (code == 401) {
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
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
    public boolean myDriveDelete(String path) throws RmsRestAPIException {
        // prepare data
        JSONObject postJSON = new JSONObject();
        try {
            JSONObject parameters = new JSONObject();
            parameters.put("pathId", path);
            postJSON.put("parameters", parameters);
        } catch (JSONException e) {
            log.i("failed prepare post data in myDriveDelete-" + e.toString());
            throw new RmsRestAPIException("failed prepare post data in myDriveDelete-", RmsRestAPIException.ExceptionDomain.Common);
        }
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                postJSON.toString());
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getMyDriveDeleteURL())
                .post(body)
                .build();
        String responseString = executeNetRequest(request);
        log.i("myDriveDelete-path:\n" + path + "\n" + responseString);
        try {
            JSONObject j = new JSONObject(responseString);
            if (!j.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = j.getInt("statusCode");
            if (code == 200 || code == 204) {
                return true;
            } else if (code == 400) {
                throw new RmsRestAPIException("Malformed request", RmsRestAPIException.ExceptionDomain.MalformedRequest, code);
            } else if (code == 401) {
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else if (code == 404) {
                throw new RmsRestAPIException("Parent Folder missing", RmsRestAPIException.ExceptionDomain.NotFound, code);
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
    public boolean myDriveCreateFolder(String parentPathId, String name) throws RmsRestAPIException {
        // prepare data
        JSONObject postJSON = new JSONObject();
        try {
            JSONObject parameters = new JSONObject();
            parameters.put("parentPathId", parentPathId);
            parameters.put("name", name);
            postJSON.put("parameters", parameters);
        } catch (JSONException e) {
            log.i("failed prepare post data in myDriveCreateFolder-" + e.toString());
            throw new RmsRestAPIException("failed prepare post data in myDriveCreateFolder-", RmsRestAPIException.ExceptionDomain.Common);
        }
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                postJSON.toString());
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getMyDriveCreateFolderURL())
                .post(body)
                .build();
        Response response;
        String responseString;
        try {
            response = httpClient.newCall(request).execute();
            responseString = response.body().string();
            log.i("myDriveCreateFolder\n" + responseString);
        } catch (IOException e) {
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.NetWorkIOFailed);
        }

        try {
            JSONObject j = new JSONObject(responseString);
            if (!j.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = j.getInt("statusCode");
            if (code >= 200 && code < 300) {
                return true;
            } else if (code == 400) {
                throw new RmsRestAPIException("Malformed request", RmsRestAPIException.ExceptionDomain.MalformedRequest, code);
            } else if (code == 401) {
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else if (code == 404) {
                throw new RmsRestAPIException("Parent folder missing", RmsRestAPIException.ExceptionDomain.NotFound, code);
            } else if (code == 4001) {
                throw new RmsRestAPIException("Invalid folder name", RmsRestAPIException.ExceptionDomain.InvalidFolderName, code);
            } else if (code == 4002) {
                throw new RmsRestAPIException("File already exists", RmsRestAPIException.ExceptionDomain.FileAlreadyExists, code);
            } else if (code == 6001) {
                throw new RmsRestAPIException("Drive storage exceeded", RmsRestAPIException.ExceptionDomain.DriveStorageExceeded, code);
            } else if (code == 500) {
                throw new RmsRestAPIException("Internal server error", RmsRestAPIException.ExceptionDomain.InternalServerError, code);
            } else {
                throw new RmsRestAPIException("Failed parse response", RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("Failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public void myDriveDownload(String path, long fileSize, String localPath, RestAPI.DownloadListener listener) throws RmsRestAPIException {
        if (listener == null) {
            listener = new RestAPI.DownloadListener() {
                @Override
                public void current(int i) {
                }

                @Override
                public void cancel() {
                }
            };
        }
        // prepare data
        JSONObject postJSON = new JSONObject();
        try {
            JSONObject parameters = new JSONObject();
            parameters.put("pathId", path);
            parameters.put("start", 0);
            if (fileSize != 0) {
                parameters.put("length", fileSize);
            }
            postJSON.put("parameters", parameters);
        } catch (JSONException e) {
            log.e(e);
            throw new RmsRestAPIException("failed prepare post data in myDriveDownload-", RmsRestAPIException.ExceptionDomain.Common);
        }
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                postJSON.toString());
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getMyDriveDownloadURL())
                .post(body)
                .build();

        Response response;
        try {
            response = httpClient.newCall(request).execute();
        } catch (IOException e) {
            log.e(e);
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.NetWorkIOFailed);
        }
        if (!response.isSuccessful()) {
            if (response.code() == 401) {
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, 401);
            } else {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, response.code());
            }
        }
        String contentLengthStr = response.header("Content-Length");
        //Download need to notify caller the progressing
        try {
            final int DOWNLOAD_CHUNK_SIZE = 2048;
            BufferedSource source = response.body().source();
            File file = new File(localPath);
            BufferedSink sink = Okio.buffer(Okio.sink(file));
            long contentLength = TextUtils.isEmpty(contentLengthStr) ? fileSize : Long.parseLong(contentLengthStr);
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
        } catch (FileNotFoundException e) {
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.FileNotFound);
        } catch (IOException e) {
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.FileIOFailed);
        }
    }

    @Override
    public String myDriveUpload(String parentPathId, String fileName, File file) throws RmsRestAPIException {
        // prepare data
        JSONObject postJSON = new JSONObject();
        try {
            JSONObject parameters = new JSONObject();
            parameters.put("parentPathId", parentPathId);
            parameters.put("name", fileName);
            postJSON.put("parameters", parameters);
        } catch (JSONException e) {
            log.e("failed prepare post data in myDriveUpload-" + e.toString(), e);
            throw new RmsRestAPIException("failed prepare post data in myDriveUpload-", e);
        }
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("API-input", postJSON.toString())
                .addFormDataPart("file", "file_name_not_set",
                        RequestBody.create(MediaType.parse("application/octet-stream"), file))
                .build();
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getMyDriveUploadURL())
                .post(body)
                .build();

        String responseString = executeNetRequest(request);
        log.v("myDriveUpload:\n" + responseString);


        try {
            JSONObject j = new JSONObject(responseString);
            if (!j.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = j.getInt("statusCode");
            if (code >= 200 && code < 300) {
                return responseString;
            } else if (code == 400) {
                throw new RmsRestAPIException("Malformed request", RmsRestAPIException.ExceptionDomain.MalformedRequest, code);
            } else if (code == 401) {
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else if (code == 404) {
                throw new RmsRestAPIException("Parent Folder missing", RmsRestAPIException.ExceptionDomain.NotFound, code);
            } else if (code == 4002) {
                throw new RmsRestAPIException("File already exists", RmsRestAPIException.ExceptionDomain.FileAlreadyExists, code);
            } else if (code == 6001) {
                throw new RmsRestAPIException("Drive Storage Exceeded", RmsRestAPIException.ExceptionDomain.DriveStorageExceeded, code);
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
    public String myDriveUploadProgress(String parentPathId, String fileName, File file, @NonNull ProgressRequestListener progressRequestListener) throws RmsRestAPIException {
        // prepare data
        JSONObject postJSON = new JSONObject();
        try {
            JSONObject parameters = new JSONObject();
            parameters.put("parentPathId", parentPathId);
            parameters.put("name", fileName);
            postJSON.put("parameters", parameters);
        } catch (JSONException e) {
            log.e("failed prepare post data in myDriveUpload-" + e.toString(), e);
            throw new RmsRestAPIException("failed prepare post data in myDriveUploadProgress-", RmsRestAPIException.ExceptionDomain.Common);
        }
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("API-input", postJSON.toString())
                .addFormDataPart("file", "file_name_not_set",
                        RequestBody.create(MediaType.parse("application/octet-stream"), file))
                .build();
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getMyDriveUploadURL())
                .post(ProgressHelper.addProgressRequestListener(body, progressRequestListener))
                .build();
//      httpClient = httpClient.newBuilder().writeTimeout(60000,TimeUnit.SECONDS).build();
        String responseString = executeNetRequest(request);
        log.v("myDriveUploadProgress:\n" + responseString);
        try {
            JSONObject j = new JSONObject(responseString);
            if (!j.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = j.getInt("statusCode");
            if (code >= 200 && code < 300) {
                return responseString;
            } else if (code == 400) {
                throw new RmsRestAPIException("malformed request", RmsRestAPIException.ExceptionDomain.MalformedRequest, code);
            } else if (code == 401) {
                throw new RmsRestAPIException("authentication failed.", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else if (code == 404) {
                throw new RmsRestAPIException("parent Folder missing", RmsRestAPIException.ExceptionDomain.NotFound, code);
            } else if (code == 4002) {
                throw new RmsRestAPIException("file already exists", RmsRestAPIException.ExceptionDomain.FileAlreadyExists, code);
            } else if (code == 6001) {
                throw new RmsRestAPIException("drive storage exceeded", RmsRestAPIException.ExceptionDomain.DriveStorageExceeded, code);
            } else if (code == 500) {
                throw new RmsRestAPIException("internal server error", RmsRestAPIException.ExceptionDomain.InternalServerError, code);
            } else {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    @Override
    public JSONObject myDriveStorageUsed() throws RmsRestAPIException {
        // prepare data
        JSONObject postJSON = new JSONObject();
        try {
            JSONObject parameters = new JSONObject();
            parameters.put("userId", Integer.parseInt(user.getUserIdStr()));
            parameters.put("ticket", user.getTicket());
            postJSON.put("parameters", parameters);
        } catch (JSONException e) {
            log.i("failed prepare post data in myDriveStorageUsed-" + e.toString());
            throw new RmsRestAPIException("failed prepare post data in myDriveStorageUsed-", RmsRestAPIException.ExceptionDomain.Common);
        }
        log.i("myDriveStorageUsed:" + postJSON.toString());
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                postJSON.toString());
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getMyDriveStorageUsedURL())
                .post(body)
                .build();

        String responseString = executeNetRequest(request);
        log.i("myDriveStorageUsed:\n" + responseString);

        // parse response
        try {
            JSONObject j = new JSONObject(responseString);
            if (!j.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = j.getInt("statusCode");
            if (code == 200) {
                if (j.has("results")) {
                    return j.getJSONObject("results");
                } else {
                    throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, code);
                }
            } else if (code == 400) {
                throw new RmsRestAPIException("Malformed request", RmsRestAPIException.ExceptionDomain.MalformedRequest, code);
            } else if (code == 401) {
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
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
    public String myDriveCreatePublicShare(String path) throws RmsRestAPIException {
        // prepare data
        JSONObject postJSON = new JSONObject();
        try {
            JSONObject parameters = new JSONObject();
            parameters.put("path", path);
            postJSON.put("parameters", parameters);
        } catch (JSONException e) {
            log.e("failed prepare post data in myDriveCreatePublicShare-" + e.toString(), e);
            throw new RmsRestAPIException("failed prepare post data in myDriveCreatePublicShare-", RmsRestAPIException.ExceptionDomain.Common);
        }
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                postJSON.toString());
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getMyDriveCreatePublicShareURL())
                .post(body)
                .build();

        String responseString = executeNetRequest(request);
        log.v("myDriveCreatePublicShare:\n" + responseString);

        // parse response
        try {
            JSONObject j = new JSONObject(responseString);
            if (!j.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = j.getInt("statusCode");
            if (code >= 200 && code < 300) {
                if (j.has("results")) {
                    JSONObject results = j.getJSONObject("results");
                    if (results.has("url") && !results.isNull("url")) {
                        return results.getString("url");
                    } else {
                        throw new RmsRestAPIException("invalid json struct, no results.url");
                    }
                } else {
                    throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, code);
                }
            } else if (code == 400) {
                throw new RmsRestAPIException("Malformed request", RmsRestAPIException.ExceptionDomain.MalformedRequest, code);
            } else if (code == 401) {
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else if (code == 500) {
                throw new RmsRestAPIException("Internal Server Error", RmsRestAPIException.ExceptionDomain.InternalServerError, code);
            } else {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
    }


}
