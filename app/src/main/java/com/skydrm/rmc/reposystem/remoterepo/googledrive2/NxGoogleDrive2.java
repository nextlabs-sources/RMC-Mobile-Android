package com.skydrm.rmc.reposystem.remoterepo.googledrive2;

import android.os.AsyncTask;
import android.util.Base64;
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
import com.skydrm.sdk.exception.RmsRestAPIException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

import static com.skydrm.rmc.ExecutorPools.Select_Type.NETWORK_TASK;


public class NxGoogleDrive2 implements IRemoteRepo {
    private static DevLog log = new DevLog(NxGoogleDrive2.class.getSimpleName());
    String refreshToken;
    TokenMgr tokenMgr;
    private OkHttpClient client;


    public NxGoogleDrive2(String refreshToken) {
        // must hold
        this.refreshToken = refreshToken;
        tokenMgr = new TokenMgr();

        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public void updateToken(String accessToken) {

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

        // make sure Access Token can be used properly
        String accessToken;
        if (tokenMgr.isValidAccessToken()) {
            accessToken = tokenMgr.getAccessToken();
        } else {
            //time-consuming task
            try {
                accessToken = tokenMgr.refresh(refreshToken);
            } catch (Exception e) {
                e.printStackTrace();
                throw new FileListException(e.getMessage());
            }
        }


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
                    }catch (JSONException e) {
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
        // make sure Access Token can be used properly
        String accessToken;
        if (tokenMgr.isValidAccessToken()) {
            accessToken = tokenMgr.getAccessToken();
        } else {
            //time-consuming task
            try {
                accessToken = tokenMgr.refresh(refreshToken);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        try {
            About r = AboutHandler.handler(accessToken, "Bearer").call();
            info.remoteTotalSpace = r.storageLimit;
            info.remoteUsedSpace = r.storageLimit - r.storageUsage;
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
                Token token = new TokenExchangeHandler().handler(authCode).call();
                // get account info
                About about = new AboutHandler().handler(token.accessToken, token.type).call();

                return new Account(about, token);

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
        public long expired_interval;

        public Token() {
            tokenFetchedTime = System.currentTimeMillis();
        }
    }

    static public class Account {
        public About about;
        public Token token;

        public Account(About about, Token token) {
            this.about = about;
            this.token = token;
        }

        public String getEmail() {
            return about.emailAddress;
        }

        public String getUserId() {
            return token.userID;
        }

        public String getRefreshToken() {
            return token.refreshToken;
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

    static public class TokenExchangeHandler {
        static public Callable<Token> handler(final String authCode) {
            return new Callable<Token>() {
                @Override
                public Token call() throws Exception {
                    OkHttpClient client = new OkHttpClient();

                    RequestBody body = new FormBody.Builder()
                            .add("grant_type", "authorization_code")
                            .add("client_id", GoogleOAuth2.sClientID)
                            .add("code", authCode)
                            .add("redirect_uri", GoogleOAuth2.sRedirect_URL + "://")
                            .build();


                    Request request = new Request.Builder()
                            .post(body)
                            .url("https://www.googleapis.com/oauth2/v4/token")
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
                        response.close();
                        throw new Exception("error");
                    }

                    Log.v("exchange result:", result);
                    Token token = new Token();
                    try {
                        JSONObject jresult = new JSONObject(result);
                        token.tokenFetchedTime = System.currentTimeMillis();
                        token.accessToken = jresult.getString("access_token");
                        token.type = jresult.getString("token_type");
                        token.expired_interval = jresult.getLong("expires_in");
                        token.refreshToken = jresult.getString("refresh_token");
                        // since we add the new scope: https://www.googleapis.com/auth/userinfo.profile
                        // here we can retrived id_token ( as jwt format)
                        // decode it second part (result is a json) and then extract "sub" filed as the UserID
                        String id_token = jresult.getString("id_token");
                        String base64edSecondPart = id_token.substring(id_token.indexOf('.') + 1, id_token.lastIndexOf('.'));
                        token.userID = new JSONObject(new String(Base64.decode(base64edSecondPart, Base64.DEFAULT))).getString("sub");
                    } catch (JSONException e) {
                        throw e;
                    }
                    return token;
                }
            };
        }
    }

    static public class TokenRefreshHandler {
        Callable<Token> handler(final String refreshToken, final String clientId, final OkHttpClient client) {
            return new Callable<Token>() {
                @Override
                public Token call() throws Exception {
                    FormBody body = new FormBody.Builder()
                            .add("refresh_token", refreshToken)
                            .add("client_id", clientId)
                            .add("grant_type", "refresh_token")
                            .build();
                    Request request = new Request.Builder()
                            .url("https://www.googleapis.com/oauth2/v4/token")
                            .post(body)
                            .build();

                    Response response;
                    String result;
                    try {
                        response = client.newCall(request).execute();
                        result = response.body().string();
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw e;
                    }
                    if (!response.isSuccessful()) {
                        response.close();
                        throw new Exception(response.message());
                    }
                    log.v("TokenRefreshHandler-" + result);
                    // parse result
                    try {
                        Token t = new Token();
                        JSONObject jresult = new JSONObject(result);
                        t.refreshToken = refreshToken;
                        t.accessToken = jresult.getString("access_token");
                        t.expired_interval = jresult.getLong("expires_in");
                        t.type = jresult.getString("token_type");
                        return t;
                    } catch (JSONException e) {
                        e.printStackTrace();
                        throw e;
                    }
                }
            };
        }
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
            // make sure Access Token can be used properly
            String accessToken;
            if (tokenMgr.isValidAccessToken()) {
                accessToken = tokenMgr.getAccessToken();
            } else {
                //time-consuming task
                try {
                    accessToken = tokenMgr.refresh(refreshToken);
                } catch (Exception e) {
                    log.e(e);
                    fileDownloadException =
                            new FileDownloadException("invalid access token", FileDownloadException.ExceptionCode.AuthenticationFailed);
                    return false;
                }
            }
            publishProgress((long) 0); // to cheat user
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
            callback.onFinishedDownload(fileDownloadException == null, localPath, fileDownloadException);
        }

    }

    private class TokenMgr {
        Token token;
        Object locks_for_token = new Object();

        public boolean isValidAccessToken() {
            synchronized (locks_for_token) {
                if (token == null) {
                    return false;
                }
                if (token.accessToken == null) {
                    return false;
                }
                if (token.tokenFetchedTime == 0) {
                    return false;
                }
                // valid time is 50 minutes , 3000s , 3000000 millis
                if (System.currentTimeMillis() - token.tokenFetchedTime > 3000_000) {
                    return false;
                }
                return true;
            }
        }

        public String getAccessToken() {
            synchronized (locks_for_token) {
                return token.accessToken;
            }
        }

        public String refresh(String refreshToken) throws Exception {
            try {
                Token newT = new TokenRefreshHandler().handler(refreshToken, GoogleOAuth2.sClientID, client).call();
                synchronized (locks_for_token) {
                    this.token = newT;
                }
                return newT.accessToken;
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }

        }

    }

}
