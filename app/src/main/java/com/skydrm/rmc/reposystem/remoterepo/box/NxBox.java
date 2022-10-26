package com.skydrm.rmc.reposystem.remoterepo.box;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.google.api.client.util.DateTime;
import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.reposystem.IRemoteRepo;
import com.skydrm.rmc.reposystem.RemoteRepoInfo;
import com.skydrm.rmc.reposystem.exception.FileDownloadException;
import com.skydrm.rmc.reposystem.exception.FileListException;
import com.skydrm.rmc.reposystem.exception.FolderCreateException;
import com.skydrm.rmc.reposystem.localrepo.helper.Helper;
import com.skydrm.rmc.reposystem.remoterepo.ICancelable;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.reposystem.types.NXDocument;
import com.skydrm.rmc.reposystem.types.NXFolder;
import com.skydrm.rmc.reposystem.types.NxFileBase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

import static com.skydrm.rmc.ExecutorPools.Select_Type.NETWORK_TASK;

/**
 * Created by oye on 12/13/2017.
 */

public class NxBox implements IRemoteRepo {
    static private final DevLog log = new DevLog(NxBox.class.getSimpleName());
    String accessToken;
    private OkHttpClient client;

    public NxBox(String accessToken) {
        this.accessToken = accessToken;
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public void updateToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public INxFile listFiles(NXFolder file) throws FileListException {
        try {
            NXFolder par = new NXFolder(file);
            if (par.getLocalPath().equals("/")) {
                // as Box api defined,
                par.setCloudPath("0");
            }
            String folderId = par.getCloudPath();
            if (folderId == null) {
                return null;
            }


            // call api to get file list
            Request request = new Request.Builder()
                    .url("https://api.box.com" + "/2.0/folders/" + folderId + "/items?fields=modified_at,name,size&limit=1000")
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .build();

            Response response = null;
            String result = null;
            try {
                response = client.newCall(request).execute();
                result = response.body().string();
                log.w(result);
            } catch (IOException e) {
                e.printStackTrace();
                throw e;
            }
            if (!response.isSuccessful()) {
                throw new Exception("error");
            }

            log.d("listFiles result:" + result);
            try {
                JSONObject jresult = new JSONObject(result);
                if (jresult.has("error")) {
                    return null;
                }

                // parset file list
                JSONArray array = jresult.getJSONArray("entries");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject oneObj = array.getJSONObject(i);
                    NxFileBase oneFile = null;
                    // parse into nxl file
                    String fileId = oneObj.getString("id");
                    String name = oneObj.getString("name");
                    String path = par.getLocalPath().equals("/") ? "/" + name : par.getLocalPath() + "/" + name;
                    long updatetime = System.currentTimeMillis();
                    // --- try to parse time string
                    try {
                        // "2017-12-10T18:47:58-08:00"   this is RFC3339
                        String timestr = oneObj.getString("modified_at");
                        updatetime = DateTime.parseRfc3339(timestr).value;
                    } catch (Exception e) {
                        log.e(e);
                    }
                    long size = 0;
                    String type = oneObj.getString("type");
                    if (TextUtils.equals("folder", type)) {
                        oneFile = new NXFolder();

                    } else if (TextUtils.equals("file", type)) {
                        oneFile = new NXDocument();
                        size = oneObj.getInt("size");

                    } else {
                        //neither "folder" nor "file", should never reach here
                        throw new JSONException("should never reach here");
                    }

                    // build nxfile
                    oneFile.setName(name);
                    oneFile.setLocalPath(path);
                    oneFile.setDisplayPath(path);
                    oneFile.setmCloudPathID(fileId);
                    oneFile.setCloudPath(fileId);
                    oneFile.setSize(size);
                    oneFile.setLastModifiedTimeLong(updatetime);

                    par.addChild(oneFile);

                }

                par.updateRefreshTimeWisely();
                return par;
            } catch (JSONException e) {
                throw e;
            }

        } catch (Exception e) {
            log.e(e);
        }
        return null;
    }


    private class DownLoadFileTask extends AsyncTask<Void, Long, Boolean> implements ICancelable {
        private IDownLoadCallback callback;
        private INxFile document;
        private FileDownloadException fileDownloadException;
        private String localPath;
        private int start = -1;
        private int length = -1;
        private boolean bPartialDownload = false;

        public DownLoadFileTask(INxFile document, String localFilePath, boolean bPartialDownload, IDownLoadCallback callback) {
            this.document = document;
            this.localPath = localFilePath;
            this.bPartialDownload = bPartialDownload;
            this.callback = callback;
        }

        public DownLoadFileTask(INxFile document, String localFilePath, int start, int length, boolean bPartialDownload, IDownLoadCallback callback) {
            this.document = document;
            this.localPath = localFilePath;
            this.start = start;
            this.length = length;
            this.bPartialDownload = bPartialDownload;
            this.callback = callback;
        }

        @Override
        public void cancel() {

        }

        @Override
        protected void onProgressUpdate(Long... values) {
            callback.progressing(values[0]);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            callback.onFinishedDownload(fileDownloadException == null, localPath, fileDownloadException);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Helper.makeSureDocExist(new File(localPath));
            // make sure Access Token can be used properly

            Request.Builder builder = new Request.Builder();
            if (bPartialDownload) {
                builder.addHeader("Range", "bytes=" + start + "-" + length);
            }
            // call api to get file list
            Request request = builder
                    .url("https://api.box.com/2.0/files/" + document.getCloudFileID() + "/content")
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .build();
            Response response;
            try {
                response = client.newCall(request).execute();

                if (!response.isSuccessful()) {
                    log.v(response.body().string());
                    fileDownloadException =
                            new FileDownloadException("invalid response", FileDownloadException.ExceptionCode.Common);
                    return false;
                }
            } catch (IOException e) {
                log.e(e);
                fileDownloadException =
                        new FileDownloadException("network io failed", FileDownloadException.ExceptionCode.NetWorkIOFailed);
                return false;
            }
            // downlaod the stream
            //Download need to notify caller the progressing
            try {
                final int DOWNLOAD_CHUNK_SIZE = 2048;
                BufferedSource source = response.body().source();
                File file = new File(localPath);
                BufferedSink sink = Okio.buffer(Okio.sink(file));
                long contentLength = response.body().contentLength();
                // contentLength may be -1;
                if (contentLength == -1) {
                    contentLength = document.getSize();
                    if (contentLength == -1 || contentLength == 0) {
                        contentLength = 10_000;  // to cheat user
                    }
                }

                long totalRead = 0;
                long hasRead;
                while ((hasRead = source.read(sink.buffer(), DOWNLOAD_CHUNK_SIZE)) != -1) {
                    totalRead += hasRead;
                    long progress = (long) (totalRead * 100.0 / contentLength);
                    if (progress > 100) {
                        publishProgress((long) 99);
                    } else {
                        publishProgress(progress);
                    }
                }
                sink.writeAll(source);
                sink.flush();
                sink.close();
            } catch (Exception e) {
                log.e(e);
                fileDownloadException = new FileDownloadException("download error", FileDownloadException.ExceptionCode.NetWorkIOFailed);
                return false;
            }
            return true;
        }
    }


    @Override
    public void downloadFile(INxFile document, String localPath, IDownLoadCallback callback) {
        new DownLoadFileTask(document, localPath, false, callback).executeOnExecutor(ExecutorPools.SelectSmartly(NETWORK_TASK));
    }

    @Override
    public void downloadFilePartial(INxFile document, String localPath, int start, int length, IDownLoadCallback callback) {
        new DownLoadFileTask(document, localPath, start, length, true, callback).executeOnExecutor(ExecutorPools.SelectSmartly(NETWORK_TASK));

    }

    @Override
    public void uploadFile(INxFile parentFolder, String fileName, File localFile, IUploadFileCallback callback) {

    }

    @Override
    public void updateFile(INxFile parentFolder, INxFile updateFile, File localFile, IUploadFileCallback callback) {

    }

    @Override
    public void deleteFile(INxFile file) {

    }

    @Override
    public void createFolder(INxFile parentFolder, String subFolderName) throws FolderCreateException {

    }

    @Override
    public boolean getInfo(RemoteRepoInfo info) {
        try {
            //https://api.box.com/2.0/me
            Request request = new Request.Builder()
                    .url("https://api.box.com/2.0/users/me")
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .build();
            Response response = null;
            String result = null;
            try {
                response = client.newCall(request).execute();
                result = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
                throw e;
            }
            if (!response.isSuccessful()) {
                throw new Exception("error");
            }
            log.d("BoxAPI(/users/me) result:" + result);
            /*
            api: https://api.box.com/2.0/users/me
            result:
                    {
                      "type": "user",
                      "id": "2986117449",
                      "name": "osmond ye",
                      "login": "osmond.ye@gmail.com",
                      "created_at": "2017-12-07T22:26:59-08:00",
                      "modified_at": "2018-01-09T09:36:51-08:00",
                      "language": "en",
                      "timezone": "America/Los_Angeles",
                      "space_amount": 10737418240,
                      "space_used": 3163959,
                      "max_upload_size": 2147483648,
                      "status": "active",
                      "job_title": "",
                      "phone": "15558139286",
                      "address": "",
                      "avatar_url": "https://app.box.com/api/avatar/large/2986117449"
                    }
             */
            JSONObject jresult = new JSONObject(result);
            info.displayName = jresult.getString("name");
            info.email = jresult.getString("login");
            info.remoteTotalSpace = jresult.getLong("space_amount");
            info.remoteUsedSpace = jresult.getLong("space_used");
            return true;
        } catch (Exception e) {
            log.e(e);
        }
        return false;
    }
}
