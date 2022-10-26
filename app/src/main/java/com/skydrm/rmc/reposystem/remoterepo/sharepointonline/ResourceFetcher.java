package com.skydrm.rmc.reposystem.remoterepo.sharepointonline;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.reposystem.IRemoteRepo;
import com.skydrm.rmc.reposystem.RemoteRepoInfo;
import com.skydrm.rmc.reposystem.exception.FileDownloadException;
import com.skydrm.rmc.reposystem.localrepo.helper.Helper;
import com.skydrm.rmc.reposystem.remoterepo.ICancelable;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.reposystem.types.NXFolder;
import com.skydrm.rmc.reposystem.types.NXSite;
import com.skydrm.rmc.reposystem.types.NxFileBase;
import com.skydrm.sdk.exception.RmsRestAPIException;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

public class ResourceFetcher {
    public static final String TAG = "ResourceFetcher";
    private static final DevLog log = new DevLog(TAG);
    private OkHttpClient mClient;
    private IResource mResourceInvoker;
    private Config mConfig;
    private String mServerSite;
    private BoundService mBoundService;

    private ResourceFetcher(Builder builder) {
        this.mBoundService = builder.mBoundService;
        this.mClient = builder.mClient.createClient();
        this.mResourceInvoker = builder.mResourceInvoker;
        this.mServerSite = amendServerUrl(builder.mSiteUrl);
        this.mConfig = new Config();
    }

    public NxFileBase loadFile(NXFolder file) {
        String cloudPath = file.getCloudPath();
        if (cloudPath.equalsIgnoreCase("/")) {
            return loadRoot();
        } else if (file.isSite()) {
            return loadChildSiteAndLists(file);
        } else {
            return loadChildFoldersAndLists(file);
        }
    }

//    public NxFileBase loadSPFile(NXFolder file) {
//        String cloudPath = file.getCloudPath();
//        if (cloudPath.equalsIgnoreCase("/")) {
//            return loadRoot();
//        }
//        return null;
//    }
//
//    private NxFileBase loadSPRoot() {
//        String rootFolders = getResources(mConfig.getRootFolderUrl(amendServerUrl(mServerSite)));
//        String rootSites = getResources(mConfig.getRootSitesUrl(amendServerUrl(mServerSite)));
//    }

    private NxFileBase loadRoot() {
        String rootFolders = getResources(mConfig.getRootFolderUrl(amendServerUrl(mServerSite)));
        String rootSites = getResources(mConfig.getRootSitesUrl(amendServerUrl(mServerSite)));
        // new a root
        NxFileBase rt = new NXSite();
        ResourceParser.fillParams(rt, "/", "/", 0, "root", "", "");
        if (!TextUtils.isEmpty(rootSites)) {
            ResourceParser.parseRoots(rt, null, rootSites, true);
        }
        if (!TextUtils.isEmpty(rootFolders)) {
            ResourceParser.parseRoots(rt, null, rootFolders, false);
        }
        rt.updateRefreshTimeWisely();
        return rt;
    }

    private NxFileBase loadChildSiteAndLists(NXFolder site) {
        String childSites = getResources(mConfig.getChildSiteUrl(site.getCloudPath()));
        String fileLists = getResources(mConfig.getFileLists(site.getCloudPath()));
        log.d("loadChildSiteAndLists:&& " + childSites);
        log.d("loadChildSiteAndLists:** " + fileLists);

        NxFileBase rt = ResourceParser.getBase(site);
        if (!TextUtils.isEmpty(childSites)) {
            ResourceParser.parseRoots(rt, site, childSites, true);
        }
        if (!TextUtils.isEmpty(fileLists)) {
            ResourceParser.parseRoots(rt, site, fileLists, false);
        }
        rt.updateRefreshTimeWisely();
        return rt;
    }

    private NxFileBase loadChildFoldersAndLists(NXFolder folder) {
        String folders = getResources(mConfig.getFoldersUrl(folder.getCloudPath()));
        String files = getResources(mConfig.getFilesUrl(folder.getCloudPath()));

        NxFileBase rt = ResourceParser.getBase(folder);
        if (!TextUtils.isEmpty(folders)) {
            ResourceParser.parseChildFolders(rt, folder, folders);
        }
        if (!TextUtils.isEmpty(files)) {
            ResourceParser.parseChildFiles(rt, folder, files);
        }
        rt.updateRefreshTimeWisely();
        log.d("loadChildFoldersAndLists:---- " + folders);
        log.d("loadChildFoldersAndLists:==== " + files);
        return rt;
    }

    public boolean loadRepositoryInfo(RemoteRepoInfo info) {
        String usrInfo = getResources(mConfig.getCurrentUsrInfoUrl(amendServerUrl(mServerSite)));
        String usrId = ResourceParser.parseUsrId(usrInfo);
        if (!TextUtils.isEmpty(usrId)) {
            String detailUsrInfo = getResources(mConfig.getCurrentUsrInfoDetailUrl(amendServerUrl(mServerSite), usrId));
            Map<String, String> usrDetailInfoMap = ResourceParser.parseUsrDetailInfo(detailUsrInfo);
            if (usrDetailInfoMap != null) {
                info.displayName = usrDetailInfoMap.get("username");
                info.email = usrDetailInfoMap.get("email");
            }
        }
        String siteQuotaInfo = getResources(mConfig.getSiteQuotaUrl(amendServerUrl(mServerSite)));
        Map<String, String> quotaInfoMap = ResourceParser.parseQuotaInfo(siteQuotaInfo);
        if (quotaInfoMap != null) {
            info.remoteUsedSpace = Long.valueOf(quotaInfoMap.get("Storage"));
            float storagePercentageUsed = Float.parseFloat(quotaInfoMap.get("StoragePercentageUsed"));
            if (storagePercentageUsed != 0) {
                info.remoteTotalSpace = (long) (info.remoteUsedSpace / storagePercentageUsed);
            } else {
                info.remoteTotalSpace = info.remoteUsedSpace;
            }
        }
        return true;
    }

    public boolean authByTryGetUsr() throws Exception {
        return !TextUtils.isEmpty(mResourceInvoker.getResources(mClient, mConfig.getCurrentUsrInfoUrl(amendServerUrl(mServerSite))));
    }

    public void downloadFile(INxFile document, String localPath, String accessToken, IRemoteRepo.IDownLoadCallback callback) {
        DownloadFileAsyncTask task = new DownloadFileAsyncTask(document, localPath, callback);
        task.setClient(mClient);
        task.setNTLM(TextUtils.isEmpty(accessToken));
        task.setAccessToken(accessToken);
        task.run();
    }

    private String getResources(String url) {
        try {
            return mResourceInvoker.getResources(mClient, url);
        } catch (Exception e) {
            if (e instanceof RmsRestAPIException) {
                if (((RmsRestAPIException) e).getDomain().equals(RmsRestAPIException.ExceptionDomain.AuthenticationFailed)) {
                    refreshAccessToken(mBoundService);
                }
            }
            e.printStackTrace();
        }
        return "";
    }

    private void refreshAccessToken(BoundService service) {
        if (service == null) {
            log.e("refreshAccessToken: bound service is null.");
            return;
        }
        String accessToken = refreshToken(service.rmsRepoId);
        if (TextUtils.isEmpty(accessToken)) {
            log.e("refreshAccessToken: empty access token got.");
            return;
        }
        service.accountToken = accessToken;
        log.d("refreshAccessToken: new access token received:" + accessToken);
        SkyDRMApp.getInstance().getRepoSystem().updateRepo(service);
    }

    private String refreshToken(String repoId) {
        // get token first
        try {
            return SkyDRMApp.getInstance().getSession().getRmsRestAPI()
                    .getRepositoryService(SkyDRMApp.getInstance().getSession().getRmUser())
                    .getAccessTokenByRepoID(repoId);
        } catch (RmsRestAPIException e) {
            e.printStackTrace();
        } catch (SessionInvalidException e) {
            e.printStackTrace();
        } catch (InvalidRMClientException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String amendServerUrl(String url) {
        if (url.endsWith("/")) {
            return url;
        } else {
            return url + "/";
        }
    }

    public static class Builder {
        private IHttpClient mClient;
        private IResource mResourceInvoker;
        private String mSiteUrl;
        private BoundService mBoundService;

        public Builder setHttpClient(IHttpClient client) {
            this.mClient = client;
            return this;
        }

        public Builder setResourceInvoker(IResource invoker) {
            this.mResourceInvoker = invoker;
            return this;
        }

        public Builder setSiteUrl(String siteUrl) {
            this.mSiteUrl = siteUrl;
            return this;
        }

        public Builder setBoundService(BoundService boundService) {
            this.mBoundService = boundService;
            return this;
        }

        public ResourceFetcher build() {
            return new ResourceFetcher(this);
        }
    }

    static class DownloadFileAsyncTask extends AsyncTask<Void, Long, Boolean> implements Runnable, ICancelable {
        private INxFile mDocument;
        private String mLocalPath;
        private IRemoteRepo.IDownLoadCallback mDownloadCallback;
        private OkHttpClient mClient;
        private String mAccessToken;
        private FileDownloadException mFileDownloadException;
        private boolean NTLM;

        DownloadFileAsyncTask(INxFile document, String localPath, IRemoteRepo.IDownLoadCallback callback) {
            this.mDocument = document;
            this.mLocalPath = localPath;
            this.mDownloadCallback = callback;
        }

        public void setClient(OkHttpClient client) {
            this.mClient = client;
        }

        public void setAccessToken(String accessToken) {
            this.mAccessToken = accessToken;
        }

        public void setNTLM(boolean NTLM) {
            this.NTLM = NTLM;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mDownloadCallback != null) {
                mDownloadCallback.cancelHandler(this);
            }
        }

        @Override
        protected void onProgressUpdate(Long... values) {
            super.onProgressUpdate(values);
            if (mDownloadCallback != null) {
                mDownloadCallback.progressing(values[0]);
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return download(mDocument);
        }

        private boolean download(INxFile file) {
            if (mClient == null) {
                return false;
            }
            Helper.makeSureDocExist(new File(mLocalPath));
            String downloadUrl = file.getCloudPath() + "/$value";
            if (downloadUrl.contains(" ")) {
                downloadUrl = downloadUrl.replaceAll(" ", "%20");
            }
            Request request;
            if (NTLM) {
                request = new Request.Builder()
                        .url(downloadUrl)
                        .build();
            } else {
                request = new Request.Builder()
                        .url(downloadUrl)
                        .addHeader("Authorization", "Bearer " + mAccessToken)
                        .build();
            }

            try {
                Response response = mClient.newCall(request).execute();
                File cacheFile = new File(mLocalPath);

                long length = response.body().contentLength();
                if (length == -1) {
                    length = mDocument.getSize();
                }
                long totalReads = 0;
                long readBytes;
                BufferedSource source = response.body().source();
                BufferedSink bf = Okio.buffer(Okio.sink(cacheFile));
                final int DOWNLOAD_CHUNK_SIZE = 2048;
                while ((readBytes = source.read(bf.buffer(), DOWNLOAD_CHUNK_SIZE)) != -1) {
                    totalReads += readBytes;
                    long progress = totalReads * 100 / length;
                    if (progress > 100) {
                        progress = 100;
                    }
                    publishProgress(progress);
                }
                bf.writeAll(source);
                bf.flush();
                bf.close();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                mFileDownloadException = new FileDownloadException("download error",
                        FileDownloadException.ExceptionCode.NetWorkIOFailed);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                ((NxFileBase) mDocument).setCached(true);
            } else {
                ((NxFileBase) mDocument).setCached(false);
            }
            if (mDownloadCallback != null) {
                mDownloadCallback.onFinishedDownload(mFileDownloadException == null, mLocalPath, mFileDownloadException);
            }
        }

        @Override
        public void run() {
            executeOnExecutor(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.NETWORK_TASK));
        }

        @Override
        public void cancel() {
            ((NxFileBase) mDocument).setCached(false);
            if (!isCancelled()) {
                cancel(true);
            }
        }
    }
}
