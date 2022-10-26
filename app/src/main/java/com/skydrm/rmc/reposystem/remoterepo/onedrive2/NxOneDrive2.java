package com.skydrm.rmc.reposystem.remoterepo.onedrive2;

import android.os.AsyncTask;

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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
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


public class NxOneDrive2 implements IRemoteRepo {
    private static DevLog log = new DevLog(NxOneDrive2.class.getSimpleName());

    String refreshToken;
    TokenMgr tokenMgr;
    private OkHttpClient client;

    public NxOneDrive2(String refreshToken) {
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
        try {
            NXFolder par = new NXFolder(file);
            if (par.getLocalPath().equals("/")) {
                par.setCloudPath("me/skydrive");
            }
            String folderId = par.getCloudPath();
            if (folderId == null) {
                return null;
            }

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
                    return null;
                }
            }

            // call api to get file list
            Request request = new Request.Builder()
                    .url("https://apis.live.net/v5.0/" + folderId + "/files")
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

            log.v("listFiles result:" + result);
            try {
                JSONObject jresult = new JSONObject(result);
                if (jresult.has("error")) {
                    return null;
                }
                // parse file list result
                JSONArray data = jresult.getJSONArray("data");
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
                        // 2016-03-14T08:43:31+0000
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz", Locale.US);
                        updatetime = df.parse(oneObj.getString("updated_time")).getTime();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    long size = 0;
                    if (fileId.startsWith("folder")) {
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
        private IRemoteRepo.IDownLoadCallback callback;
        private INxFile document;
        private FileDownloadException fileDownloadException;
        private String localPath;
        private int start = -1;
        private int length = -1;
        private boolean bPartialDownload = false;

        public DownLoadFileTask(INxFile document, String localFilePath, boolean bPartialDownload, IRemoteRepo.IDownLoadCallback callback) {
            this.document = document;
            this.localPath = localFilePath;
            this.bPartialDownload = bPartialDownload;
            this.callback = callback;
        }

        public DownLoadFileTask(INxFile document, String localFilePath, int start, int length, boolean bPartialDownload, IRemoteRepo.IDownLoadCallback callback) {
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
            String accessToken;
            if (tokenMgr.isValidAccessToken()) {
                accessToken = tokenMgr.getAccessToken();
            } else {
                //time-consuming task
                try {
                    accessToken = tokenMgr.refresh(refreshToken);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            Request.Builder builder = new Request.Builder();
            if (bPartialDownload) {
                builder.addHeader("Range", "bytes=" + start + "-" + length);
            }
            // call api to get file list
            Request request = builder
                    .url("https://apis.live.net/v5.0/" + document.getCloudFileID() + "/content")
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
            StorageQuata quata = QuataHandler.handler(accessToken, client).call();
            info.remoteTotalSpace = quata.total;
            info.remoteUsedSpace = quata.total-quata.available;
            UserInfo userInfo = UserInfoHandler.handler(accessToken).call();
            info.displayName = userInfo.name;
            info.email = userInfo.email;
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
                Account account = new Account();
                account.token = TokenExchangeHandler.handler(authCode).call();
                account.userInfo = UserInfoHandler.handler(account.token.accessToken).call();
                return account;
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

    static public class StorageQuata {
        long total;
        long available;
    }

    static public class Account {
        public Token token;
        public UserInfo userInfo;
    }

    static public class UserInfo {
        public String userId;
        public String name;
        public String email;
    }

    static public class Token {
        public String accessToken;
        public String refreshToken;
        public String type;
        public long tokenFetchedTime;
        public long expired_interval;

        public Token() {
            tokenFetchedTime = System.currentTimeMillis();
        }
    }


    static public class TokenExchangeHandler {
        static public Callable<Token> handler(final String authCode) {
            return new Callable<Token>() {
                @Override
                public Token call() throws Exception {
                    OkHttpClient client = new OkHttpClient();

                    RequestBody body = new FormBody.Builder()
                            .add("grant_type", "authorization_code")
                            .add("client_id", OAuth2Activity.getClientId())
                            .add("code", authCode)
                            .add("redirect_uri", OAuth2Activity.getRedirectUri())
                            .build();

                    Request request = new Request.Builder()
                            .post(body)
                            .url("https://login.live.com/oauth20_token.srf")
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

                    log.v("exchange result:" + result);
                    Token token = new Token();

                    try {
                        JSONObject jresult = new JSONObject(result);
                        token.tokenFetchedTime = System.currentTimeMillis();
                        token.accessToken = jresult.getString("access_token");
                        token.refreshToken = jresult.getString("refresh_token");
                        token.expired_interval = jresult.getLong("expires_in");
                        token.type = jresult.getString("token_type");
                    } catch (JSONException e) {
                        throw e;
                    }
                    return token;
                }
            };
        }
    }

    static public class UserInfoHandler {
        static public Callable<UserInfo> handler(final String accessToken) throws Exception {
            return new Callable<UserInfo>() {
                @Override
                public UserInfo call() throws Exception {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("https://apis.live.net/v5.0/me")
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
                        UserInfo userInfo = new UserInfo();
                        JSONObject jresult = new JSONObject(result);
                        userInfo.userId = jresult.getString("id");
                        userInfo.name = jresult.getString("name");
                        JSONObject jEmails = jresult.getJSONObject("emails");
                        userInfo.email = jEmails.getString("account");
                        return userInfo;
                    } catch (JSONException e) {
                        throw e;
                    }
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
                            .add("scope", OAuth2Activity.getScope())
                            .add("grant_type", "refresh_token")
                            .build();
                    Request request = new Request.Builder()
                            .url("https://login.live.com/oauth20_token.srf")
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

    static public class QuataHandler {
        static public Callable<StorageQuata> handler(final String accessToken, final OkHttpClient client) throws Exception {
            return new Callable<StorageQuata>() {
                @Override
                public StorageQuata call() throws Exception {
                    Request request = new Request.Builder()
                            .url("https://apis.live.net/v5.0/me/skydrive/quota")
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
                        quata.total = jresult.getLong("quota");
                        quata.available = jresult.getLong("available");
                        return quata;
                    } catch (JSONException e) {
                        throw e;
                    }
                }
            };
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
                Token newT = new TokenRefreshHandler().handler(refreshToken, OAuth2Activity.getClientId(), client).call();
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
