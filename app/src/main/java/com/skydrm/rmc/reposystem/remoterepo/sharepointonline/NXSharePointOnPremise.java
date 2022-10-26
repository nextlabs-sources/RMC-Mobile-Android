package com.skydrm.rmc.reposystem.remoterepo.sharepointonline;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.reposystem.IRemoteRepo;
import com.skydrm.rmc.reposystem.RemoteRepoInfo;
import com.skydrm.rmc.reposystem.exception.FileListException;
import com.skydrm.rmc.reposystem.exception.FolderCreateException;
import com.skydrm.rmc.reposystem.remoterepo.googledrive2.NxGoogleDrive3;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.reposystem.types.NXFolder;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jcifs.ntlmssp.NtlmFlags;
import jcifs.ntlmssp.Type1Message;
import jcifs.ntlmssp.Type2Message;
import jcifs.ntlmssp.Type3Message;
import jcifs.util.Base64;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class NXSharePointOnPremise implements IHttpClient, IResource, IRemoteRepo {
    public static final String TAG = "SharePointOnPremise";
    private ResourceFetcher mFetcher;
    private static String mUsername;
    private static String mPassword;

    public NXSharePointOnPremise(String serverSite, String password) {
        try {
            mUsername = SkyDRMApp.getInstance().getSession().getUserEmail();
            mPassword = password;
            //Log.d(TAG, "domain:" + serverSite + ",username: " + mUsername + ",password:" + mPassword);
        } catch (InvalidRMClientException e) {
            e.printStackTrace();
        }
        mFetcher = new ResourceFetcher.Builder()
                .setHttpClient(this)
                .setResourceInvoker(this)
                .setSiteUrl(serverSite)
                .build();
    }

    public NXSharePointOnPremise(String serverSite, String username, String password) {
        mUsername = username;
        mPassword = password;
        mFetcher = new ResourceFetcher.Builder()
                .setHttpClient(this)
                .setResourceInvoker(this)
                .setSiteUrl(serverSite)
                .build();
    }

    @Override
    public OkHttpClient createClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                //.addInterceptor(new BasicAuthInterceptor(mUsername, mPassword))
                .authenticator(new NTLMAuthenticator(mUsername, mPassword))
                .build();
    }

    @Override
    public String getResources(OkHttpClient client, String url) throws Exception {
        // path amend
        if (url.contains(" ")) {
            url = url.replaceAll(" ", "%20");
        }
        Request request = new Request.Builder()
                .url(url)
                .addHeader("accept", "application/json;odata=verbose")
                .addHeader("ContentType", "application/json;odata=verbose;charset=utf-8")
                .get()
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new Exception(response.message());
            }
            String results = response.body().string();
            Log.d(TAG, "getResources: " + results);
            return results;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    //    class BasicAuthInterceptor implements Interceptor {
//        private String credentials;
//
//        BasicAuthInterceptor(String username, String password) {
//            this.credentials = Credentials.basic(username, password);
//        }
//
//        @Override
//        public Response intercept(Chain chain) throws IOException {
//            Request request = chain.request();
//            Request authenticatedRequest = request.newBuilder()
//                    .header("Authorization", credentials).build();
//            return chain.proceed(authenticatedRequest);
//        }
//    }
    public boolean tryAuth() throws Exception {
        return mFetcher.authByTryGetUsr();
    }

    @Override
    public void updateToken(String accessToken) {
        throw new UnsupportedOperationException("Does not support update token operation.");
    }

    @Override
    public INxFile listFiles(NXFolder file) throws FileListException {
        if (file == null) {
            return null;
        }
        return mFetcher.loadFile(file);
    }

    @Override
    public void downloadFile(INxFile document, String localPath, IDownLoadCallback callback) {
        mFetcher.downloadFile(document, localPath, "", callback);
    }

    @Override
    public void downloadFilePartial(INxFile document, String localPath, int start, int length, IDownLoadCallback callback) {
        throw new UnsupportedOperationException("Does not support download file partial.");
    }

    @Override
    public void uploadFile(INxFile parentFolder, String fileName, File localFile, IUploadFileCallback callback) {
        throw new UnsupportedOperationException("Does not support upload file.");
    }

    @Override
    public void updateFile(INxFile parentFolder, INxFile updateFile, File localFile, IUploadFileCallback callback) {
        throw new UnsupportedOperationException("Does not support update file.");
    }

    @Override
    public void deleteFile(INxFile file) {
        throw new UnsupportedOperationException("Does not support delete file.");
    }

    @Override
    public void createFolder(INxFile parentFolder, String subFolderName) throws FolderCreateException {
        throw new UnsupportedOperationException("Does not support create folder.");
    }

    @Override
    public boolean getInfo(RemoteRepoInfo info) {
        return mFetcher.loadRepositoryInfo(info);
    }

    class NTLMAuthenticator implements Authenticator {
        private static final int TYPE_1_FLAGS =
                NtlmFlags.NTLMSSP_NEGOTIATE_56 |
                        NtlmFlags.NTLMSSP_NEGOTIATE_128 |
                        NtlmFlags.NTLMSSP_NEGOTIATE_NTLM2 |
                        NtlmFlags.NTLMSSP_NEGOTIATE_ALWAYS_SIGN |
                        NtlmFlags.NTLMSSP_REQUEST_TARGET;

        private String mDomain;
        private String mUsername;
        private String mPassword;
        private String mWorkstation;

        NTLMAuthenticator(String username, String password) {
            this("", username, password, "");
        }

        NTLMAuthenticator(String domain, String login, String password, String workstation) {
            mUsername = login;
            mPassword = password;
            mDomain = domain;
            mWorkstation = workstation;
        }

        @Override
        public Request authenticate(Route route, Response response) throws IOException {
            List<String> authHeaders = response.headers("WWW-Authenticate");
            if (authHeaders != null) {
                boolean negociate = false;
                boolean ntlm = false;
                String ntlmValue = null;
                for (String authHeader : authHeaders) {
                    if (authHeader.equalsIgnoreCase("Negotiate")) {
                        negociate = true;
                    }
                    if (authHeader.equalsIgnoreCase("NTLM")) {
                        ntlm = true;
                    }
                    if (authHeader.startsWith("NTLM ")) {
                        ntlmValue = authHeader.substring(5);
                    }
                }

                if (negociate && ntlm) {
                    String type1Msg = generateType1Msg(mDomain, mWorkstation);
                    String header = "NTLM " + type1Msg;
                    return response.request().newBuilder().header("Authorization", header).build();
                } else if (ntlmValue != null) {
                    String type3Msg = generateType3Msg(mUsername, mPassword, mDomain, mWorkstation, ntlmValue);
                    String ntlmHeader = "NTLM " + type3Msg;
                    return response.request().newBuilder().header("Authorization", ntlmHeader).build();
                }
            }

            if (responseCount(response) <= 3) {
                String credential = Credentials.basic(mUsername, mPassword);
                return response.request().newBuilder().header("Authorization", credential).build();
            }
            return null;
        }

        private String generateType1Msg(@NonNull String domain, @NonNull String workstation) {
            final Type1Message type1Message = new Type1Message(TYPE_1_FLAGS, domain, workstation);
            byte[] source = type1Message.toByteArray();
            return Base64.encode(source);
        }

        private String generateType3Msg(final String login, final String password, final String domain, final String workstation, final String challenge) {
            Type2Message type2Message;
            try {
                byte[] decoded = Base64.decode(challenge);
                type2Message = new Type2Message(decoded);
            } catch (final IOException exception) {
                exception.printStackTrace();
                return null;
            }
            final int type2Flags = type2Message.getFlags();
            final int type3Flags = type2Flags
                    & (0xffffffff ^ (NtlmFlags.NTLMSSP_TARGET_TYPE_DOMAIN | NtlmFlags.NTLMSSP_TARGET_TYPE_SERVER));
            final Type3Message type3Message = new Type3Message(type2Message, password, domain,
                    login, workstation, type3Flags);
            return Base64.encode(type3Message.toByteArray());
        }

        private int responseCount(Response response) {
            int result = 1;
            while ((response = response.priorResponse()) != null) {
                result++;
            }
            return result;
        }
    }

    public static class Account {
        public String domain;
        public String username;
        public String password;
        public String nickName;

        public Account(String domain, String username, String password, String nickName) {
            this.domain = domain;
            this.username = username;
            this.password = password;
            this.nickName = nickName;
        }
    }

    public static class GetAccountAsyncTask extends AsyncTask<Void, Void, Account> {

        private IGetAccountAsyncTask mCallBack = null;

        public void setCallBack(IGetAccountAsyncTask callBack) {
            this.mCallBack = callBack;
        }

        @Override
        protected Account doInBackground(Void... params) {
            SharePointAuthManager.Account account = SharePointAuthManager.getAccount();
            mUsername = account.username;
            mPassword = account.password;
            return new Account(account.domain, account.username, account.password, account.nickName);
        }

        @Override
        protected void onPostExecute(Account account) {
            if (mCallBack != null) {
                mCallBack.onFinishGet(account);
            }
        }

        public interface IGetAccountAsyncTask {
            void onFinishGet(Account account);
        }
    }
}
