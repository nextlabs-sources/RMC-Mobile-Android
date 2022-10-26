package com.skydrm.rmc.reposystem.remoterepo.onedrive2;

import android.os.AsyncTask;

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
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

import static com.skydrm.rmc.ExecutorPools.Select_Type.NETWORK_TASK;


public class NxOneDrive3 implements IRemoteRepo {
    private static DevLog log = new DevLog(NxOneDrive3.class.getSimpleName());

    String accessToken;
    private OkHttpClient client;

    public NxOneDrive3(String accessToken) {
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
                par.setCloudPath("root");
            }
            String folderId = par.getCloudPath();
            if (folderId == null) {
                return null;
            }
            if (folderId.equalsIgnoreCase("root")) {
                // for root
                // does't need to amend
            } else {
                // for other folder except root
                folderId = "/items/" + folderId;
            }


            // call api to get file list
            Request request = new Request.Builder()
                    .url("https://api.onedrive.com/v1.0/drive/" + folderId + "/children")
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

            log.v("listFiles result:" + result);
            try {
                JSONObject jresult = new JSONObject(result);
                if (jresult.has("error")) {
                    return null;
                }
                // parse file list result
                JSONArray data = jresult.getJSONArray("value");
                for (int i = 0; i < data.length(); i++) {
                    JSONObject oneObj = data.optJSONObject(i);
                    NxFileBase onefile = null;
                    // parse nxfile
                    String fileId = oneObj.optString("id");
                    String name = oneObj.getString("name");
                    String path = par.getLocalPath().equals("/") ? "/" + name : par.getLocalPath() + "/" + name;
                    long updatetime = System.currentTimeMillis();
                    // --- try to parse time string
                    try {
                        // "2017-11-11T08:46:07.183Z"   this is RFC3339
                        // sometimes is : 2017-07-27T13:23:41.8Z    can not parset this
                        String timestr = oneObj.getString("lastModifiedDateTime");
                        // add by oms , make timestr's last value is XXXZ
                        if (timestr.length() - timestr.lastIndexOf('.') < 5) {
                            timestr = timestr.substring(0, timestr.lastIndexOf('.'));
                            timestr += ".000Z";
                        }
                        updatetime = DateTime.parseRfc3339(timestr).value;
                    } catch (Exception e) {
                        log.e(e);
                    }

                    long size = 0;
                    if (oneObj.has("folder")) {
                        onefile = new NXFolder();
                    } else {
                        onefile = new NXDocument();
                        size = oneObj.getLong("size");
                    }

                    // fill param
                    onefile.setName(name);
                    onefile.setLocalPath(path);
                    onefile.setDisplayPath(path);
                    onefile.setmCloudPathID(fileId);
                    onefile.setCloudPath(fileId);
                    onefile.setSize(size);
                    onefile.setLastModifiedTimeLong(updatetime);

                    par.addChild(onefile);
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

            Request.Builder builder = new Request.Builder();
            if (bPartialDownload) {
                builder.addHeader("Range", "bytes=" + start + "-" + length);
            }
            // call api to get file list
            Request request = builder
                    .url("https://api.onedrive.com/v1.0/drive/items/" + document.getCloudFileID() + "/content")
                    .addHeader("Authorization", "bearer " + accessToken)
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
        // we should not modify 3rd parity repo

    }

    @Override
    public void createFolder(INxFile parentFolder, String subFolderName) throws FolderCreateException {

    }

    @Override
    public boolean getInfo(RemoteRepoInfo info) {

        try {
            StorageQuata quata = QuataHandler.handler(accessToken, client).call();
            info.remoteTotalSpace = quata.total;
            info.remoteUsedSpace = quata.total - quata.available;
            info.displayName = quata.name;
            info.email = quata.email;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    static public class StorageQuata {
        long total;
        long available;
        public String userId;
        public String name;
        public String email;
    }


    static public class QuataHandler {
        static public Callable<StorageQuata> handler(final String accessToken, final OkHttpClient client) throws Exception {
            return new Callable<StorageQuata>() {
                @Override
                public StorageQuata call() throws Exception {
                    Request request = new Request.Builder()
                            .url("https://api.onedrive.com/v1.0/drives/me")
                            .addHeader("Authorization", "bearer " + accessToken)
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

                    log.v("UserInfoHandler result:" + result);
                    try {
                        StorageQuata quata = new StorageQuata();
                        JSONObject jresult = new JSONObject(result);
                        JSONObject owner = jresult.getJSONObject("owner").getJSONObject("user");
                        quata.name = owner.getString("displayName");
                        quata.userId = owner.getString("id");
                        quata.email = "";
                        jresult = jresult.getJSONObject("quota");
                        quata.total = jresult.getLong("total");
                        quata.available = jresult.getLong("remaining");
                        return quata;
                    } catch (JSONException e) {
                        throw e;
                    }
                }
            };
        }
    }


}
