package com.skydrm.rmc.reposystem.remoterepo.sharepointonline;

import android.text.TextUtils;
import android.util.Log;

import com.skydrm.rmc.reposystem.IRemoteRepo;
import com.skydrm.rmc.reposystem.RemoteRepoInfo;
import com.skydrm.rmc.reposystem.exception.FileListException;
import com.skydrm.rmc.reposystem.exception.FolderCreateException;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.reposystem.types.NXFolder;
import com.skydrm.sdk.exception.RmsRestAPIException;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NXSharePointOnline2 implements IHttpClient, IResource, IRemoteRepo {
    public static final String TAG = "SharePointOnline";
    private ResourceFetcher mFetcher;
    private String mAccessToken;

    public NXSharePointOnline2(BoundService boundService) {
        this.mAccessToken = boundService.accountToken;
        mFetcher = new ResourceFetcher.Builder()
                .setHttpClient(this)
                .setResourceInvoker(this)
                .setSiteUrl(boundService.account)
                .setBoundService(boundService)
                .build();
    }

    @Override
    public OkHttpClient createClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
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
                .addHeader("Authorization ", "Bearer " + mAccessToken)
                .addHeader("accept", "application/json;odata=verbose")
                .get()
                .build();
        try {
            Response response = client.newCall(request).execute();
            String results = response.body().string();
            if (!response.isSuccessful()) {
                JSONObject resultsObj = new JSONObject(results);
                String error_description = resultsObj.optString("error_description");
                if (TextUtils.equals(error_description, "Invalid JWT token. The token is expired.")) {
                    throw new RmsRestAPIException(error_description, RmsRestAPIException.ExceptionDomain.AuthenticationFailed);
                } else {
                    throw new IllegalStateException(TextUtils.isEmpty(error_description) ? "Failed to get resources." : error_description);
                }
            }
            Log.d(TAG, "getResources: " + results);
            return results;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void updateToken(String accessToken) {
        this.mAccessToken = accessToken;
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
        mFetcher.downloadFile(document, localPath, mAccessToken, callback);
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
}
