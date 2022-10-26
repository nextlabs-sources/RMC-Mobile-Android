package com.skydrm.sdk.rms;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.skydrm.sdk.Config;
import com.skydrm.sdk.Factory;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.rest.IClassificationProfileService;
import com.skydrm.sdk.rms.rest.IFavoriteService;
import com.skydrm.sdk.rms.rest.IHeartbeatService;
import com.skydrm.sdk.rms.rest.ILogService;
import com.skydrm.sdk.rms.rest.ILoginService;
import com.skydrm.sdk.rms.rest.IMembershipService;
import com.skydrm.sdk.rms.rest.IMyDriveService;
import com.skydrm.sdk.rms.rest.IMyVaultService;
import com.skydrm.sdk.rms.rest.IPolicyEvaluationService;
import com.skydrm.sdk.rms.rest.IProjectService;
import com.skydrm.sdk.rms.rest.IRemoteViewService;
import com.skydrm.sdk.rms.rest.IRepositoryService;
import com.skydrm.sdk.rms.rest.ISharedWithMeService;
import com.skydrm.sdk.rms.rest.ISharedWithSpaceService;
import com.skydrm.sdk.rms.rest.ISharingService;
import com.skydrm.sdk.rms.rest.ITenantService;
import com.skydrm.sdk.rms.rest.ITokenService;
import com.skydrm.sdk.rms.rest.IUserService;
import com.skydrm.sdk.rms.rest.IWorkSpaceService;
import com.skydrm.sdk.rms.rest.centralpolicy.ClassificationProfileService;
import com.skydrm.sdk.rms.rest.centralpolicy.PolicyEvaluationService;
import com.skydrm.sdk.rms.rest.common.Favorite;
import com.skydrm.sdk.rms.rest.common.Sharing;
import com.skydrm.sdk.rms.rest.common.Token;
import com.skydrm.sdk.rms.rest.myVault.MyVaultService;
import com.skydrm.sdk.rms.rest.project.ProjectMetaDataResult;
import com.skydrm.sdk.rms.rest.project.ProjectService;
import com.skydrm.sdk.rms.rest.project.file.ProjectDownloadHeader;
import com.skydrm.sdk.rms.rest.repository.MyDrive;
import com.skydrm.sdk.rms.rest.repository.Repository;
import com.skydrm.sdk.rms.rest.sharedwithspace.SharedWithSpaceService;
import com.skydrm.sdk.rms.rest.sharewithme.SharedWithMeService;
import com.skydrm.sdk.rms.rest.tenant.TenantService;
import com.skydrm.sdk.rms.rest.user.Heartbeat;
import com.skydrm.sdk.rms.rest.user.Login;
import com.skydrm.sdk.rms.rest.user.Membership;
import com.skydrm.sdk.rms.rest.user.User;
import com.skydrm.sdk.rms.rest.viewer.RemoteView;
import com.skydrm.sdk.rms.rest.workspace.WorkSpaceService;
import com.skydrm.sdk.rms.user.IRmUser;
import com.skydrm.sdk.utils.ConvertProgress;
import com.skydrm.sdk.utils.DevLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

public class RestAPI {
    private static final String TAG = "RESTAPI";
    static private DevLog log = new DevLog(RestAPI.class.getSimpleName());
    private OkHttpClient httpClient;
    private Config config;
    private String currentVersion;
    private Call mCall;
    private IRemoteViewService remoteViewService;

    public RestAPI(@NonNull Config config) {
        this(config, null);
    }

    public RestAPI(@NonNull Config config, @Nullable OkHttpClient httpClient) {
        this.config = config;
        if (httpClient == null) {
            this.httpClient = new OkHttpClient.Builder()
                    .readTimeout(Config.READ_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(Config.WRITE_TIMEOUT, TimeUnit.SECONDS)
                    .connectTimeout(Config.CONNECT_TIMEOUT, TimeUnit.SECONDS)
                    .build();

            try {
                if (Factory.ignoreSSLCert) {
                    this.httpClient = buildWithIgnoreSSL();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            this.httpClient = httpClient;
        }
    }

    public Config getConfig() {
        return config;
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }
    // for debug use, google play deny this

    static public OkHttpClient buildWithIgnoreSSL() throws Exception {
        //for debug only, ignore ssl certificate check
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null,
                new TrustManager[]{new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        // Not Impl
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        // Not Impl
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }},
                new java.security.SecureRandom());
        OkHttpClient client = new OkHttpClient.Builder()
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                })
                .sslSocketFactory(sc.getSocketFactory(),
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                            }

                            @Override
                            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                            }

                            @Override
                            public X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[0];
                            }
                        })
                .readTimeout(Config.READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(Config.WRITE_TIMEOUT, TimeUnit.SECONDS)
                .connectTimeout(Config.CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .build();
        return client;
    }

    // set the common paras for request header
    static public void setCommonParas(Request.Builder builder) throws RmsRestAPIException {
        try {

            builder.addHeader("client_id", Factory.getClientId())
                    .addHeader("clientId", Factory.getClientId())
                    .addHeader("platformId", Factory.getDeviceType())
                    .addHeader("deviceId", URLEncoder.encode(Factory.getDeviceName(), StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
        }
    }

    public ILoginService getLoginService() {
        return new Login(null, httpClient, config, log);
    }

    public ITokenService getTokenService(IRmUser user) {
        return new Token(user, httpClient, config, log);
    }

    public IMembershipService getMembershipService(IRmUser user) {
        return new Membership(user, httpClient, config, log);
    }

    public IHeartbeatService getHeartbeatService(IRmUser user) {
        return new Heartbeat(user, httpClient, config, log);
    }

    public IFavoriteService getFavoriteService(IRmUser user) {
        return new Favorite(user, httpClient, config, log);
    }

    public IRepositoryService getRepositoryService(IRmUser user) {
        return new Repository(user, httpClient, config, log);
    }

    public IMyDriveService getMyDriveService(IRmUser user) {
        return new MyDrive(user, httpClient, config, log);
    }

    public IUserService getUserService(IRmUser user) {
        return new User(user, httpClient, config, log);
    }

    public ISharingService getSharingService(IRmUser user) {
        return new Sharing(user, httpClient, config, log);
    }

    public ILogService getLogService(IRmUser user) {
        return new com.skydrm.sdk.rms.rest.common.Log(user, httpClient, config, log);
    }

    public ISharedWithMeService getSharedWithMeService(IRmUser user) {
        return new SharedWithMeService(user, httpClient, mCall, config, log);
    }

    public IRemoteViewService getRemoteViewService(IRmUser user) {
        if (remoteViewService == null) {
            // the httpClient with special timeout for remote viewer.
            this.httpClient = new OkHttpClient.Builder()
                    .readTimeout(Config.REMOTE_VIEW_READ_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(Config.REMOTE_VIEW_WRITE_TIMEOUT, TimeUnit.SECONDS)
                    .connectTimeout(Config.REMOTE_VIEW_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                    .build();
            try {
                if (Factory.ignoreSSLCert) {
                    this.httpClient = buildWithIgnoreSSL();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            remoteViewService = new RemoteView(user, httpClient, config, log);
        }
        return remoteViewService;
    }

    public IClassificationProfileService getClassificationProfileService(IRmUser rmUser) {
        return new ClassificationProfileService(rmUser, httpClient, config, log);
    }

    public IPolicyEvaluationService getPolicyEvaluationService(IRmUser rmUser) {
        return new PolicyEvaluationService(rmUser, httpClient, config, log);
    }

    public IMyVaultService getMyVaultService(IRmUser rmUser) {
        return new MyVaultService(rmUser, httpClient, config, log);
    }

    public IProjectService getProjectService(IRmUser rmUser) {
        return new ProjectService(rmUser, httpClient, config, log);
    }

    public ITenantService getTenantService(IRmUser user) {
        return new TenantService(user, httpClient, config, log);
    }

    public IWorkSpaceService getWorkSpaceService(IRmUser user) {
        return new WorkSpaceService(user, httpClient, config, log);
    }

    public ISharedWithSpaceService getSharedWithSpaceService(IRmUser user) {
        return new SharedWithSpaceService(user, httpClient, config, log);
    }

    /**
     * Execute the network request
     *
     * @param request request body
     * @return response body string
     * @throws RmsRestAPIException rms request Exception
     */
    private String executeNetRequest(Request request) throws RmsRestAPIException {
        String responseString;
        try {
            Response response = httpClient.newCall(request).execute();
            responseString = response.body().string();
        } catch (IOException e) {
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.NetWorkIOFailed);
        }

        return responseString;
    }

    /**
     * This method checks whether there is a new version
     *
     * @param context The context
     * @return
     * @throws Exception
     */
    public boolean checkAppVersionUpdate(Context context) throws Exception {
        boolean result = false;
        String currentVersion = null;
        String packageName = context.getPackageName();
        String url = config.getMyCheckUpDateAppUrl() + packageName;

        PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
        String oldVersionName = info.versionName;
        Log.d("checkUpdateApp", "oldVersionName: " + oldVersionName);

        int oldVersionCode = info.versionCode;

        Log.d("checkUpdateApp", "oldVersionCode: " + oldVersionCode);

        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(url).build();
        Response response = httpClient.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new RmsRestAPIException("network failed" + response.code() + response.message());
        }
        Reader reader = response.body().charStream();
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line;
        String content = "";
        Pattern p = Pattern.compile("\"softwareVersion\"\\W*([\\d\\.]+)");
        while ((line = bufferedReader.readLine()) != null) {
            Matcher matcher = p.matcher(line);
            if (matcher.find()) {
                Log.d("checkUpdateApp", "currentVersion.:" + matcher.group(1));
                currentVersion = matcher.group(1);
                Log.d("checkUpdateApp", "currentVersion: " + currentVersion);
            }
            content += line;
        }
        if (null != currentVersion && currentVersion.compareTo(oldVersionName) > 0) {
            result = true;
        }
        Log.d("checkUpdateApp", "content " + content);
        if (bufferedReader != null) {
            bufferedReader.close();
        }
        return result;
    }

    /**
     * The API used to get one project metaData
     *
     * @param user
     * @param projectId: the project id
     */
    @Deprecated
    public ProjectMetaDataResult getProjectMetaData(IRmUser user, int projectId) throws RmsRestAPIException {
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getProjectMetaDataRUL().replace("{projectId}", Integer.toString(projectId)))
                .addHeader("userId", user.getUserIdStr())
                .addHeader("ticket", user.getTicket())
                .get()
                .build();

        String responseString = executeNetRequest(request);
        debugLog("getProjectMetaData:\n" + responseString);

        try {
            JSONObject jo = new JSONObject(responseString);
            if (!jo.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = jo.getInt("statusCode");
            if (code == 200) {
                return new Gson().fromJson(responseString, ProjectMetaDataResult.class);
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
     * this API used to download project file.
     * note: support partial download you can request only a few bytes from the file, such as you can only get the header to read tags and rights etc by
     * using the parameter start and length.
     *
     * @param user
     * @param projectId
     * @param pathId:    file path ---- current directory + fileName, like:  /test/for_test-2017-02-09-07-44-48.xlsx.nxl/"
     * @param localPath: used to store local -- abs path.
     * @param listener:  download listener
     * @param args:      variable length parameters, include start and length -- args[0] is start parameter and args[1] is length parameter.
     */
    @Deprecated
    public ProjectDownloadHeader projectDownloadFile(IRmUser user, int projectId, String
            pathId, String localPath, int type, DownloadListener listener, int... args) throws RmsRestAPIException {
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
            debugLog("failed prepare post data in projectDownloadFile-" + e.toString());
            throw new RmsRestAPIException("failed prepare post data in projectDownloadFile-", e);
        }

        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json"),
                postJson.toString());

        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getProjectDownloadFileURL().replace("{projectId}", Integer.toString(projectId)))
                .addHeader("userId", user.getUserIdStr())
                .addHeader("ticket", user.getTicket())
                .post(requestBody)
                .build();

        Response response;
        try {
            mCall = httpClient.newCall(request);
            response = mCall.execute();
        } catch (IOException e) {
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.NetWorkIOFailed);
        }

        if (!response.isSuccessful()) {
            if (response.code() == 401) {
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, response.code());
            } else if (response.code() == 403) {
                throw new RmsRestAPIException("Access denied", RmsRestAPIException.ExceptionDomain.AccessDenied, response.code());
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
            File file = new File(localPath);
            if (!file.exists()) {
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
            return projectDownloadHeader;

        } catch (FileNotFoundException e) {
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.FileNotFound);
        } catch (IOException e) {
            if (mCall.isCanceled()) { // user cancel.
                listener.cancel();
                return null;
            } else {
                throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.NetWorkIOFailed);
            }
        }
    }


    /**
     * this API used to convert part CAD into .hsf format by rms
     *
     * @param user
     * @param file
     * @param convertListener
     * @return byte[]: converted binary data
     */
    public byte[] convertCAD(IRmUser user, final File file,
                             final IConvertListener convertListener) throws RmsRestAPIException {
        String encodeFileName;
        try {
            encodeFileName = URLEncoder.encode(file.getName(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.e("unsupported url encoding in convertCAD-" + e.toString(), e);
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
        }

        RequestBody body = RequestBody.create(
                MediaType.parse("application/octet-stream"), file);
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getConvertCADURL().replace("{filename}", encodeFileName))
                .addHeader("userId", user.getUserIdStr())
                .addHeader("ticket", user.getTicket())
                .post(body)
                .build();

        // simulate the progress of conversion
        ConvertProgress convertThread = new ConvertProgress(file, convertListener);
        convertThread.start();

        Response response;
        try {
            mCall = httpClient.newCall(request);
            response = mCall.execute();
//            if (!response.isSuccessful()) {
//                throw new RmsRestAPIException("network failed" + response.code() + response.message());
//            }
        } catch (IOException e) {
            if (mCall.isCanceled()) { // user cancel.
                convertThread.cancel();
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

        try {
            byte[] convertedBinaries = response.body().bytes();
            if (convertedBinaries.length > 0) {
                return convertedBinaries;
            }
        } catch (IOException e) {
            throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.FileIOFailed);
        }

        return null;

    }

    /**
     * cancel request.
     */
    public void cancel() {
        if (mCall != null) {
            mCall.cancel();
        }
    }

    private void debugLog(String msg) {
        if (Factory.DEBUG && msg != null && !msg.isEmpty()) {
            Log.d(TAG, msg);
        }
    }

    public interface DownloadListener {
        void current(int i);

        void cancel();
    }

    public interface Listener {
        void progress(int current, int total);

        void currentState(String state);
    }

    /**
     * Represents any functionality related to Convert CAD & Office and PDF.
     * Representational State Transfer (REST) API.
     */
    public interface IConvertListener {

        /**
         * Updates the progression of the convert.
         *
         * @param current: The bytes remaining to convert.
         * @param total:   The total bytes convert.
         */
        void onConvertProgress(int current, int total);
    }

    public interface IRequestCallBack<T> {
        void onSuccess(T result);

        void onFailed(int statusCode, String errorMsg);
    }

    /* end ---RMS Defined Services---*/

    /*Force each sort of Rest Service Extends with this */
    static public abstract class RestServiceBase {
        protected IRmUser user;
        protected OkHttpClient httpClient;
        protected Config config;
        protected DevLog log;

        public RestServiceBase(IRmUser user, OkHttpClient httpClient, Config config, DevLog log) {
            this.user = user;
            this.httpClient = httpClient;
            this.config = config;
            this.log = log;
        }

        // set the common paras for request header
        protected void setCommonParas(Request.Builder builder) throws RmsRestAPIException {
            try {
                // safe code for userid and ticket
                if (user != null) {
                    builder.addHeader("userId", user.getUserIdStr());
                    String ticket = user.getTicket();
                    if (ticket != null && !ticket.isEmpty()) {
                        builder.addHeader("ticket", user.getTicket());
                    }
                }
                builder.addHeader("client_id", Factory.getClientId())
                        .addHeader("clientId", Factory.getClientId())
                        .addHeader("platformId", Factory.getDeviceType())
                        .addHeader("deviceId", URLEncoder.encode(Factory.getDeviceName(), StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
            }
        }

        /**
         * Execute the network request
         *
         * @param request request body
         * @return response body string
         * @throws RmsRestAPIException rms request Exception
         */
        protected String executeNetRequest(Request request) throws RmsRestAPIException {
            String responseString;
            try {
                Response response = httpClient.newCall(request).execute();
                responseString = response.body().string();
            } catch (IOException e) {
                throw new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.NetWorkIOFailed);
            }

            return responseString;
        }

        protected <T> T paramCheck(T params) throws RmsRestAPIException {
            if (params == null) {
                throw new RmsRestAPIException("fatal error:The params must not be null.",
                        RmsRestAPIException.ExceptionDomain.Common);
            }
            return params;
        }

        protected String checkResponse(String params) throws RmsRestAPIException {
            if (params == null || params.isEmpty()) {
                throw new RmsRestAPIException("Failed to get response from rms.",
                        RmsRestAPIException.ExceptionDomain.Common);
            }
            return params;
        }
    }
}