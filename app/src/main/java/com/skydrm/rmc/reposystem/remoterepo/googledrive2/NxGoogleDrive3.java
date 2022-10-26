package com.skydrm.rmc.reposystem.remoterepo.googledrive2;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.util.DateTime;
import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.engine.Render.RenderHelper;
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


public class NxGoogleDrive3 implements IRemoteRepo {
    private static DevLog log = new DevLog(NxGoogleDrive3.class.getSimpleName());
    String accessToken;
    private OkHttpClient client;


    public NxGoogleDrive3(String accessToken) {
        // must hold
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
        // sanity check
        if (!file.isFolder()) {
            log.e("only accept folder");
            throw new FileListException("only accept folder", FileListException.ExceptionCode.ParamInvalid);
        }
        String path = file.getCloudPath();
        if (path == null || path.isEmpty()) {
            log.e("invalid path");
            throw new FileListException("invalid path", FileListException.ExceptionCode.ParamInvalid);
        }

        log.i("list folder:" + file.getCloudPath());

        // config param q first to split Root list and subFolder list
        String parma_q;
        boolean isRoot;
        if ("/".equals(file.getCloudPath())) {
            parma_q = "q=" + "'root' in parents and trashed != true";
            isRoot = true;
        } else {
            parma_q = "q=" + "'" + file.getCloudPath() + "'" + " in parents and trashed != true";
            isRoot = false;
        }
        String fields_param = "fields=incompleteSearch,nextPageToken,files(id,name,mimeType,modifiedTime,size)";
        String pageSize_param = "pageSize=1000";

        // send request to Google
        // GET https://www.googleapis.com/drive/v3/about
        Request request = new Request.Builder()
                .url("https://www.googleapis.com/drive/v3/files?" + parma_q + "&" + fields_param + "&" + pageSize_param)
                .get()
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        Response response;

        try {
            response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                response.close();
                throw new FileListException("response error" + response.message());
            }
            String result = response.body().string();
            log.v("list:" + result);
            // parse result
            NxFileBase rt = new NXFolder(file);
            JSONObject resultJson = new JSONObject(result);
            boolean isListFinished;
            try {
                isListFinished = resultJson.getBoolean("incompleteSearch");
            } catch (JSONException e) {
                isListFinished = true;
                e.printStackTrace();
            }
            JSONArray array = resultJson.getJSONArray("files");
            for (int i = 0; i < array.length(); i++) {
                JSONObject f = array.getJSONObject(i);
                // begin to parse;
                NxFileBase nxFile;
                String fId = f.getString("id");
                String fName = f.getString("name");
                String localpath;
                String displayPath;
                long fSize = 0;
                String fMimeType;
                if ("application/vnd.google-apps.folder".equals(f.getString("mimeType"))) {
                    // for folder
                    nxFile = new NXFolder();
                    if (isRoot) {
                        localpath = "/" + fId;
                        displayPath = "/" + fName;
                    } else {
                        localpath = file.getLocalPath() + "/" + fId;
                        displayPath = file.getDisplayPath() + "/" + fName;
                    }
                    fMimeType = "application/vnd.google-apps.folder";
                } else {
                    // for file
                    nxFile = new NXDocument();
                    if (isRoot) {
                        localpath = "/" + fId + "/" + fName;
                        displayPath = "/" + fName;
                    } else {
                        localpath = file.getLocalPath() + "/" + fId + "/" + fName;
                        displayPath = file.getDisplayPath() + "/" + fName;
                    }

                    // for GoogleDoc, no the size field
                    try {
                        fSize = f.getLong("size");
                    } catch (JSONException e) {
                        fSize = 0;
                    }

                    try {
                        fMimeType = f.getString("mimeType");
                    } catch (JSONException e) {
                        fMimeType = "";
                    }

                }
                {
                    try {
                        // parset lastmodifyed time
                        String timestr = f.getString("modifiedTime"); //2017-05-26T12:59:38.128Z    RFC3339
                        // convert into long, use com.google.api.client.util.DateTime
                        nxFile.setLastModifiedTimeLong(DateTime.parseRfc3339(timestr).value);
                    } catch (JSONException e) {
                        nxFile.setLastModifiedTimeLong(System.currentTimeMillis());
                    }
                }

                //  for Google exported file, we append corresponding postfix, GDoc - .docx, GSheet - .xlsx, GSlide - .pptx, GDraw - .png
                localpath = RenderHelper.isGoogleFile(fMimeType) ? RenderHelper.appendGoogleFileExportPostfix(fMimeType, localpath) : localpath;

                nxFile.setName(fName);
                nxFile.setLocalPath(localpath);
                nxFile.setDisplayPath(displayPath);
                nxFile.setCloudPath(fId);
                nxFile.setmCloudPathID(fId);
                nxFile.setSize(fSize);
                nxFile.setUserDefinedStr(fMimeType);
                rt.addChild(nxFile);
            }
            return rt;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            throw new FileListException(e.getMessage());
        }
    }

    @Override
    public void downloadFile(final INxFile document, final String localPath, final IDownLoadCallback callback) {
        new Task(document, localPath, false, callback).executeOnExecutor(ExecutorPools.SelectSmartly(NETWORK_TASK));
    }

    @Override
    public void downloadFilePartial(INxFile document, String localPath, int start, int length, IDownLoadCallback callback) {
        new Task(document, localPath, start, length, true, callback).executeOnExecutor(ExecutorPools.SelectSmartly(NETWORK_TASK));
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
            About r = AboutHandler.handler(accessToken, "Bearer").call();
            info.remoteTotalSpace = r.storageLimit;
            info.remoteUsedSpace = r.storageUsage;
            info.displayName = r.displayName;
            info.email = r.emailAddress;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static class GetAccountAsyncTask extends AsyncTask<Void, Void, Account> {
        String authCode;
        IGetAccountAsyncTask callback;

        public GetAccountAsyncTask(String authCode, IGetAccountAsyncTask callback) {
            this.authCode = authCode;
            this.callback = callback;
        }


        @Override
        protected Account doInBackground(Void... params) {
            try {
                // get token first
//                Token token = new TokenExchangeHandler().handler(authCode).call();
                // get account info
                About about = new AboutHandler().handler(authCode, "Bearer").call();

                return new Account(about);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Account account) {
            callback.onFinishGet(account);
        }

        public interface IGetAccountAsyncTask {
            void onFinishGet(Account account);
        }
    }

    static public class Token {
        public String userID;
        public String accessToken;
        public String refreshToken;
        public String type;
        public long tokenFetchedTime;

        public Token() {
            tokenFetchedTime = System.currentTimeMillis();
        }
    }

    static public class Account {
        public About about;

        public Account(About about) {
            this.about = about;
        }

        public String getEmail() {
            return about.emailAddress;
        }

        public String getUserId() {
            return "null";
        }

        public String getRefreshToken() {
            return "null";
        }
    }

    static public class About {
        public String userId;
        public String displayName;
        public String emailAddress;
        public long storageLimit;
        public long storageUsage;
        public long storageUsageInDrive;
        public long storageUsageInTrash;
    }

    static public class AboutHandler {
        static public Callable<About> handler(final String accessToken, final String type) {
            return new Callable<About>() {
                @Override
                public About call() throws Exception {
                    // GET https://www.googleapis.com/drive/v3/about
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("https://www.googleapis.com/drive/v3/about?fields=kind,user,storageQuota")
                            .get()
                            .addHeader("Authorization", type + " " + accessToken)
                            .build();

                    Response response = null;
                    try {
                        response = client.newCall(request).execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw e;
                    }
                    if (!response.isSuccessful()) {
                        response.close();
                        throw new Exception("error");
                    }
                    String result;
                    try {
                        result = response.body().string();
                        Log.v("OnAbout result", result);
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw e;
                    }
                    About about = new About();
                    // parge result
                    JSONObject resultJson = new JSONObject(result);
                    JSONObject userJson = resultJson.getJSONObject("user");

                    about.displayName = userJson.getString("displayName");
                    about.emailAddress = userJson.getString("emailAddress");

                    JSONObject storageQuato = resultJson.getJSONObject("storageQuota");

                    about.storageLimit = storageQuato.getLong("limit");
                    about.storageUsage = storageQuato.getLong("usage");
                    about.storageUsageInDrive = storageQuato.getLong("usageInDrive");
                    about.storageUsageInTrash = storageQuato.getLong("usageInDriveTrash");
                    return about;
                }
            };
        }
    }

    class Task extends AsyncTask<Void, Long, Boolean> implements ICancelable {

        private INxFile document;
        private String localPath;
        private int start = -1;
        private int length = -1;
        private boolean bPartialDownload = false;
        private IDownLoadCallback callback;

        private FileDownloadException fileDownloadException = null;

        Task(final INxFile document, final String localPath, boolean bPartialDownload, final IDownLoadCallback callback) {
            this.document = document;
            this.localPath = localPath;
            this.bPartialDownload = bPartialDownload;
            this.callback = callback;
        }

        Task(final INxFile document, final String localPath, final int start, final int length, boolean bPartialDownload, final IDownLoadCallback callback) {
            this.document = document;
            this.localPath = localPath;
            this.start = start;
            this.length = length;
            this.bPartialDownload = bPartialDownload;
            this.callback = callback;
        }

        @Override
        public void cancel() {

        }

        @Override
        protected void onPreExecute() {
            Helper.makeSureDocExist(new File(localPath));
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // sanity check
            if (document == null) {
                fileDownloadException =
                        new FileDownloadException("document is null", FileDownloadException.ExceptionCode.ParamInvalid);
                return false;
            }
            if (localPath == null || localPath.isEmpty()) {
                fileDownloadException =
                        new FileDownloadException("localPath is null", FileDownloadException.ExceptionCode.ParamInvalid);
                return false;
            }
            publishProgress((long) 1); // to cheat user

            // SDK path:    GET https://www.googleapis.com/drive/v3/files/fileId

            String path = null;
            String mimeType = null;
            if (RenderHelper.isGoogleFile(document) && (mimeType = RenderHelper.getGoogleExportedFormat(document)) != null) {
                path = "https://www.googleapis.com/drive/v3/files/" + document.getCloudPath() + "/export?mimeType=" + mimeType;
            } else {
                path = "https://www.googleapis.com/drive/v3/files/" + document.getCloudPath() + "?alt=media";
            }

            Request.Builder builder = new Request.Builder();
            if (bPartialDownload) {
                builder.addHeader("Range", "bytes=" + start + "-" + length);
            }

            Request request = builder.get()
                    .url(path)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .build();

            Response response;
            try {
                response = client.newCall(request).execute();

                if (!response.isSuccessful()) { // error
                    String responseStr = response.body().string();

                    try {
                        JSONObject jo = new JSONObject(responseStr);
                        JSONObject joError = jo.getJSONObject("error");

                        int code = joError.getInt("code");
                        if (code == 403) {
                            fileDownloadException = new FileDownloadException("Access denied", FileDownloadException.ExceptionCode.ExportedFileTooLarge);
                        } else if (code == 401) {
                            fileDownloadException = new FileDownloadException("Invalid Credentials", FileDownloadException.ExceptionCode.Common);
                        }
                    } catch (JSONException e) {
                        fileDownloadException = new FileDownloadException("failed parse response", FileDownloadException.ExceptionCode.Common);
                    }

                    log.v(response.body().string());
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
                response.close();
                fileDownloadException = new FileDownloadException("download error", FileDownloadException.ExceptionCode.NetWorkIOFailed);
                return false;
            }
            return true;
        }

        @Override
        protected void onProgressUpdate(Long... values) {
            callback.progressing(values[0]);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                callback.onFinishedDownload(true, localPath, fileDownloadException);
            } else {
                Helper.deleteFile(new File(localPath));
                callback.onFinishedDownload(false, localPath, fileDownloadException);
            }
        }

    }


}
