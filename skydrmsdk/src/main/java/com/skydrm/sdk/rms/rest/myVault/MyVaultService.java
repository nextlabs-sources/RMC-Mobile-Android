package com.skydrm.sdk.rms.rest.myVault;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.skydrm.sdk.Config;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.rest.IMyVaultService;
import com.skydrm.sdk.rms.user.IRmUser;
import com.skydrm.sdk.ui.uploadprogress.ProgressHelper;
import com.skydrm.sdk.ui.uploadprogress.ProgressRequestListener;
import com.skydrm.sdk.utils.DevLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

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
 * Created by hhu on 4/27/2018.
 */

public class MyVaultService extends RestAPI.RestServiceBase implements IMyVaultService {

    public MyVaultService(IRmUser user, OkHttpClient httpClient, Config config, DevLog log) {
        super(user, httpClient, config, log);
    }

    @Override
    public MyVaultUploadFileResult uploadFileToMyVault(MyVaultUploadFileParams params,
                                                       ProgressRequestListener listener) throws RmsRestAPIException {
        // prepare request body
        JSONObject requestJson = new JSONObject();
        JSONObject parameters = new JSONObject();
        try {
            /*
                When uploading local files to MyVault through this API endpoint
                Note that the following two parameters were omitted: srcPathId & srcRepoId
             */
            parameters.put("srcPathId", params.getSrcPathId());
            parameters.put("srcPathDisplay", params.getSrcPathDisplay());
            parameters.put("srcRepoId", params.getSrcRepoId());
            parameters.put("srcRepoName", params.getSrcRepoName());
            parameters.put("srcRepoType", params.getSrcRepoType());
            requestJson.put("parameters", parameters);
            log.d("myVaultUpload(parameters):\n" + parameters.toString());
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed prepare post data in myVaultProgressUpload-",
                    RmsRestAPIException.ExceptionDomain.Common);
        }
        File nxlFile = params.getNxlFile();
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("API-input", requestJson.toString())
                .addFormDataPart("file", nxlFile.getName(),
                        RequestBody.create(MediaType.parse("application/octet-stream"), nxlFile))
                .build();

        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getMyVaultUploadFileURL())
                .post(ProgressHelper.addProgressRequestListener(body, listener))
                .build();

        String responseString = executeNetRequest(request);
        log.d("myVaultUpload(response):\n" + responseString);

        int code;
        try {
            JSONObject jo = new JSONObject(responseString);
            if (!jo.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            code = jo.getInt("statusCode");
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }
        if (code == 200) {
            return new Gson().fromJson(responseString, MyVaultUploadFileResult.class);
        } else if (code == 400) {
            throw new RmsRestAPIException("Malformed request", RmsRestAPIException.ExceptionDomain.MalformedRequest, code);
        } else if (code == 401) {
            throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
        } else if (code == 403) {
            throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AccessDenied, code);
        } else if (code == 5001) {
            throw new RmsRestAPIException("Invalid nxl format", RmsRestAPIException.ExceptionDomain.InvalidNxlFormat, code);
        } else if (code == 5002) {
            throw new RmsRestAPIException("Invalid repository metadata", RmsRestAPIException.ExceptionDomain.InvalidRepoMetadata, code);
        } else if (code == 5003) {
            throw new RmsRestAPIException("Invalid file name", RmsRestAPIException.ExceptionDomain.InvalidFileName, code);
        } else if (code == 5004) {
            throw new RmsRestAPIException("Invalid file extension", RmsRestAPIException.ExceptionDomain.InvalidFileExtension, code);
        } else if (code == 500) {
            throw new RmsRestAPIException("Internal Server Error", RmsRestAPIException.ExceptionDomain.InternalServerError, code);
        } else {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, code);
        }
    }

    @Override
    public MyVaultFileListResult listMyVaultFile(MyVaultListFileRequestParas requestParas) throws RmsRestAPIException {
        String urlPath = config.getMyVaultListFileURL();
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

        if (stringBuilder.length() == 0) {
            stringBuilder.append("filter=");
            stringBuilder.append(requestParas.getmFilter());
        } else {
            stringBuilder.append("&filter=");
            stringBuilder.append(requestParas.getmFilter());
        }

        if (!TextUtils.isEmpty(requestParas.getmSearchText())) {
            if (stringBuilder.length() == 0) {
                stringBuilder.append("q.fileName=");
                stringBuilder.append(requestParas.getmSearchText());
            } else { // have para before it, need add "&"
                stringBuilder.append("&q.fileName=");
                stringBuilder.append(requestParas.getmSearchText());
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
        log.d("listMyVaultFile:\n" + responseString);

        try {
            JSONObject jo = new JSONObject(responseString);
            if (!jo.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = jo.getInt("statusCode");
            if (code == 200) {
                return new Gson().fromJson(responseString, MyVaultFileListResult.class);
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
     * URL: /rms/rs/myVault/v2/download
     * method: POST
     * consumes: application/json
     * type: 0 for normal download, 1 for download for viewer(same with forViewer:true in v1) and 2 for download for offline
     *
     * @param pathId    represents which remote file will be download.
     * @param localPath represents where local file will be stored.
     * @param type      0 for normal download, 1 for download for viewer(same with forViewer:true in v1) and 2 for download for offline
     * @param listener  download listener when file download action occurred.
     * @param args      which should include the params start&length[used for partial download].
     * @return {@link MyVaultDownloadHeader}
     * @throws RmsRestAPIException exception download may happen.
     */
    @Override
    public MyVaultDownloadHeader downloadMyVaultFile(String pathId, String localPath, int type, RestAPI.DownloadListener listener, int... args)
            throws RmsRestAPIException {
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
            throw new RmsRestAPIException("failed prepare post data in myVaultDownloadFile-", e);
        }

        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json"),
                postJson.toString());


        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getMyVaultDownloadFileURL())
                .post(requestBody)
                .build();

        Response response;
        Call mCall = null;
        try {
            mCall = httpClient.newCall(request);
            response = mCall.execute();
        } catch (IOException e) {
            if (mCall.isCanceled()) { // it looks like can't enter this when user cancel for this api --- please refer to projectDownload api
                listener.cancel();
                return null;
            } else {
                throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.NetWorkIOFailed);
            }
        }

        if (!response.isSuccessful()) {
            if (response.code() == 401) {
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, 401);
            } else {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, response.code());
            }
        }

        String x_rms_last_modified = response.header("x-rms-last-modified");
        String content_Disposition = response.header("Content-Disposition");
        String x_rms_file_size = response.header("x-rms-file-size");
        MyVaultDownloadHeader myVaultDownloadHeader = new MyVaultDownloadHeader();
        myVaultDownloadHeader.setX_rms_last_modified(x_rms_last_modified);
        myVaultDownloadHeader.setContent_Disposition(content_Disposition);
        myVaultDownloadHeader.setX_rms_file_size(x_rms_file_size);

        File file = new File(localPath);
        try {
            if (!file.exists() || !file.isFile()) {
                file.createNewFile();
            }

            final int DOWNLOAD_CHUNK_SIZE = 2048;
            BufferedSource source = response.body().source();

            BufferedSink sink = Okio.buffer(Okio.sink(file));
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
            return myVaultDownloadHeader;
        } catch (FileNotFoundException e) {
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.FileNotFound);
        } catch (IOException e) {
            file.delete();
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.FileIOFailed);
        }
    }

    @Override
    public MyVaultMetaDataResult getMyVaultFileMetaData(String duid, String filePathId) throws RmsRestAPIException {
        JSONObject postJson = new JSONObject();
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("pathId", filePathId);
            postJson.put("parameters", parameters);
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed prepare post data in myVaultMetaData-", e);
        }

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), postJson.toString());
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getMyVaultFileMetaDataURL().replace("{duid}", duid))
                .post(requestBody)
                .build();

        String responseString = executeNetRequest(request);
        log.d("myVaultMetaData:\n" + responseString);
        try {
            JSONObject jo = new JSONObject(responseString);
            if (!jo.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = jo.getInt("statusCode");
            if (code == 200) {
                return new Gson().fromJson(responseString, MyVaultMetaDataResult.class);
            } else if (code == 400) {
                throw new RmsRestAPIException("Malformed request", RmsRestAPIException.ExceptionDomain.MalformedRequest, code);
            } else if (code == 401) {
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else if (code == 404) {
                throw new RmsRestAPIException("Unable to find file metadata.", RmsRestAPIException.ExceptionDomain.NotFound, code);
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
    public boolean deleteMyVaultFile(String duid, String filePathId) throws RmsRestAPIException {
        JSONObject postJson = new JSONObject();
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("pathId", filePathId);
            postJson.put("parameters", parameters);
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed prepare post data in myVaultDeleteFile-", e);
        }

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), postJson.toString());
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getMyVaultDeleteFileURL().replace("{duid}", duid))
                .post(requestBody)
                .build();

        String responseString = executeNetRequest(request);
        log.d("myVaultDeleteFile:\n" + responseString);
        try {
            JSONObject jo = new JSONObject(responseString);
            if (!jo.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = jo.getInt("statusCode");
            if (code == 200) {
                return true;
            } else if (code == 304) {
                throw new RmsRestAPIException("File has been revoked.", RmsRestAPIException.ExceptionDomain.FileHasBeenRevoked, code);
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
