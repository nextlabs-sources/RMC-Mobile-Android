package com.skydrm.rmc.reposystem.remoterepo.dropbox;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.android.Auth;
import com.dropbox.core.http.OkHttp3Requestor;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.CreateFolderErrorException;
import com.dropbox.core.v2.files.DownloadBuilder;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.UploadUploader;
import com.dropbox.core.v2.files.WriteError;
import com.dropbox.core.v2.files.WriteMode;
import com.dropbox.core.v2.users.FullAccount;
import com.dropbox.core.v2.users.SpaceUsage;
import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.reposystem.IRemoteRepo;
import com.skydrm.rmc.reposystem.RemoteRepoInfo;
import com.skydrm.rmc.reposystem.exception.FileDownloadException;
import com.skydrm.rmc.reposystem.exception.FileListException;
import com.skydrm.rmc.reposystem.exception.FileUploadException;
import com.skydrm.rmc.reposystem.exception.FolderCreateException;
import com.skydrm.rmc.reposystem.localrepo.helper.Helper;
import com.skydrm.rmc.reposystem.remoterepo.ICancelable;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.reposystem.types.NXDocument;
import com.skydrm.rmc.reposystem.types.NXFolder;
import com.skydrm.rmc.reposystem.types.NxFileBase;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import static com.skydrm.rmc.ExecutorPools.Select_Type.NETWORK_TASK;


public class NxDropBox2 implements IRemoteRepo {
    //
    // get from RMS team provided
    // -----granted key & secret are hard-coded values
    // -----if it must change the key value, change the other place's val at AndoridManifest.xml either
    //RMS debug
//    public static final String NEXTLABS_GRANTED_KEY = "ztz47zh615kmf7b";
//    public static final String NEXTLABS_GRANTED_SECRET = "4oxekpdu8v6y7xi";
    //RMS release
    public static final String NEXTLABS_GRANTED_KEY = "3y95f3gtd9hii68";
    public static final String NEXTLABS_GRANTED_SECRET = "h9e95mu086nokfg";

    static private final String NEXTLABS_CLIENT_ID = "SkyDRM-Android/Nextlabs";
    static private final DevLog log = new DevLog(NxDropBox2.class.getSimpleName());
    private DbxClientV2 sDbxClient;


    public NxDropBox2(String accessToken) {
        DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder(NEXTLABS_CLIENT_ID)
                .withHttpRequestor(new OkHttp3Requestor(OkHttp3Requestor.defaultOkHttpClient()))
                .build();

        sDbxClient = new DbxClientV2(requestConfig, accessToken);
    }

    static public void startOAuth2Authentication(Context context) {
        Auth.startOAuth2Authentication(context, NEXTLABS_GRANTED_KEY);
    }

    static public String getOAuth2Token() {
        return Auth.getOAuth2Token();
    }

    /**
     * shit! Dropbox2, you can not extract uid from FullAccount
     *
     * @return
     */
    static public String getOAuth2UID() {
        return Auth.getUid();
    }

    static public FullAccount getAccount(String accessToken) {
        try {
            return new NxDropBox2(accessToken).sDbxClient.users().getCurrentAccount();
        } catch (DbxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public DbxClientV2 getsDbxClient() {
        return sDbxClient;
    }

    @Override
    public void updateToken(String accessToken) {
//        sDbxClient.
    }

    @Override
    public INxFile listFiles(NXFolder file) throws FileListException {
        // sanity check
        if (file == null || file.getCloudPath() == null || file.getCloudPath().isEmpty()) {
            log.e("file is null");
            throw new FileListException("file is null", FileListException.ExceptionCode.ParamInvalid);
        }
        String path = file.getCloudPath();
        try {
            ListFolderResult result;
            if (TextUtils.equals(path, "/")) {
                // for list root
                result = sDbxClient.files().listFolder("");
            } else {
                result = sDbxClient.files().listFolder(path);
            }
            log.v("list " + path + ": " + result.toStringMultiline());
            fillFileParmas(file, result.getEntries());
            return file;
        } catch (Exception e) {
            log.e(e);
            throw new FileListException(e.getMessage());
        }
    }

    private void fillFileParmas(NXFolder file, List<Metadata> metas) {
        for (Metadata m : metas) {
            NxFileBase child;
            if (m instanceof FileMetadata) {
                child = new NXDocument();
                child.setSize(((FileMetadata) m).getSize());
                child.setLastModifiedTimeLong(((FileMetadata) m).getServerModified().getTime());
                child.setmCloudPathID(((FileMetadata) m).getId());
            } else if (m instanceof FolderMetadata) {
                child = new NXFolder();
                child.setmCloudPathID(((FolderMetadata) m).getId());
                // folder no size and last modified time
            } else {
                // igonre this type
                continue;
            }

            // Handle the case that file name contains capital letters but find it will change lowercase letter when using m.getPathLower() to get local & cloud path.
            String pathLower = m.getPathLower();
            if (m instanceof FileMetadata && pathLower.contains("/")) {
                int index = pathLower.lastIndexOf('/');
                String subPath = pathLower.substring(0, index + 1);
                pathLower = subPath + m.getName();
            }

            child.setName(m.getName());
            child.setDisplayPath(m.getPathDisplay());
            child.setLocalPath(pathLower);
            child.setCloudPath(pathLower);

            file.addChild(child);
        }
    }

    @Override
    public void downloadFile(final INxFile document, final String localPath, final IDownLoadCallback callback) {
        new DownLoadFileAsyncTask(document, localPath, false, callback).executeOnExecutor(ExecutorPools.SelectSmartly(NETWORK_TASK), (Void) null);
    }

    @Override
    public void downloadFilePartial(INxFile document, String localPath, int start, int length, IDownLoadCallback callback) {
        new DownLoadFileAsyncTask(document, localPath, start, length, true, callback).executeOnExecutor(ExecutorPools.SelectSmartly(NETWORK_TASK), (Void) null);
    }

    @Override
    public void uploadFile(final INxFile parentFolder, final String fileName, final File localFile, final IUploadFileCallback callback) {
        class UploadFileAsyncTask extends AsyncTask<Void, Long, Boolean> implements ICancelable {

            protected String cloudPath;
            boolean isCanceled = false;
            private FileUploadException fileUploadException = null;
            private FileMetadata updatedFileMeta = null;

            public UploadFileAsyncTask() {
                String cloudPath = parentFolder.getCloudPath();
                if (!cloudPath.endsWith("/")) {
                    cloudPath = cloudPath + "/";
                }
                this.cloudPath = cloudPath + fileName;
            }

            @Override
            public void cancel() {
                isCanceled = true;
                cancel(true);
            }

            @Override
            protected void onPreExecute() {
                if (callback != null) {
                    callback.cancelHandler(this);
                }
            }

            @Override
            protected void onProgressUpdate(Long... values) {
                if (callback != null) {
                    callback.progressing(values[0]);
                }
            }

            private void checkIfUserCanceled() throws FileUploadException {
                if (isCanceled) {
                    throw new FileUploadException("user canceled", FileUploadException.ExceptionCode.Common);
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {

                FileInputStream is = null;
                UploadUploader uploadUploader = null;
                try {
                    uploadUploader = sDbxClient.files()
                            .uploadBuilder(cloudPath)
                            .withMode(WriteMode.ADD)
                            .withClientModified(new Date())
                            .start();

                    checkIfUserCanceled();

                    // begin with support Listener
                    OutputStream os = uploadUploader.getOutputStream();
                    is = new FileInputStream(localFile);

                    long lenth = localFile.length();
                    long totalWrite = 0L;
                    byte[] buf = new byte[4096];
                    while (true) {
                        int write = is.read(buf);
                        if (write < 0) {
                            break;
                        }
                        // write to server
                        os.write(buf, 0, write);
                        totalWrite += write;

                        checkIfUserCanceled();

                        // tell listener
                        long percentage = (long) (totalWrite / (double) lenth * 100);
                        log.v("percentage:" + percentage);
                        publishProgress(percentage);
                    }
                    //time consuming, tick listener
                    publishProgress(98L);
                    updatedFileMeta = uploadUploader.finish();

                    return true;
                } catch (FileUploadException e) {
                    log.e(e);
                    fileUploadException = e;
                } catch (Exception e) {
                    log.e(e);
                    fileUploadException = new FileUploadException(e.getMessage(), FileUploadException.ExceptionCode.Common);
                } finally {
                    try {
                        uploadUploader.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean status) {
                if (callback != null) {
                    if (!status) {
                        callback.onFinishedUpload(status, null, fileUploadException);
                        return;
                    }
                    // amend NxDoc;
                    NXDocument doc = new NXDocument();
                    doc.setName(fileName);
                    doc.setDisplayPath(cloudPath);
                    doc.setLocalPath(cloudPath);
                    doc.setCloudPath(cloudPath);
                    doc.setmCloudPathID(updatedFileMeta.getId());
                    doc.setNewCreated(true);
                    doc.setCached(true);
                    doc.setLastModifiedTimeLong(updatedFileMeta.getServerModified().getTime()); // can also use getClientModified either
                    doc.updateRefreshTimeWisely();
                    doc.setBoundService(parentFolder.getService());
                    callback.onFinishedUpload(true, doc, fileUploadException);
                }
            }


        }

        new UploadFileAsyncTask().executeOnExecutor(ExecutorPools.SelectSmartly(NETWORK_TASK), (Void) null);
    }

    @Override
    public void updateFile(INxFile parentFolder, INxFile updateFile, File localFile, IUploadFileCallback callback) {
        throw new RuntimeException("not impl");
    }

    @Override
    public void deleteFile(final INxFile file) {
        // we should not modify 3rd parity repo

//        try {
//            sDbxClient.files().delete(file.getCloudPath());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void createFolder(INxFile parentFolder, String subFolderName) throws FolderCreateException {
        try {
            String cloudPath = parentFolder.getCloudPath();
            if (!cloudPath.endsWith("/")) {
                cloudPath = cloudPath + "/";
            }
            String newPath = cloudPath + subFolderName;
            FolderMetadata metat = sDbxClient.files().createFolder(newPath);
            log.v(metat.toStringMultiline());
        } catch (CreateFolderErrorException e) {
            log.e(e);
            WriteError err = e.errorValue.getPathValue();
            if (err.isConflict()) {
                throw new FolderCreateException("name has existed", FolderCreateException.ExceptionCode.NamingCollided);
            } else if (err.isDisallowedName()) {
                throw new FolderCreateException("name invalid", FolderCreateException.ExceptionCode.NamingViolation);
            } else if (err.isInsufficientSpace()) {
                throw new FolderCreateException("insufficient space", FolderCreateException.ExceptionCode.Common);
            } else if (err.isMalformedPath()) {
                throw new FolderCreateException("path invalid", FolderCreateException.ExceptionCode.ParamInvalid);
            } else if (err.isNoWritePermission()) {
                throw new FolderCreateException("no write permission", FolderCreateException.ExceptionCode.ParamInvalid);
            } else {
                // another
                throw new FolderCreateException(e.getMessage(), FolderCreateException.ExceptionCode.ParamInvalid);
            }
        } catch (Exception e) {
            log.e(e);
            throw new FolderCreateException(e.getMessage(), FolderCreateException.ExceptionCode.Common);
        }
    }

    @Override
    public boolean getInfo(RemoteRepoInfo info) {
        try {
            SpaceUsage su = sDbxClient.users().getSpaceUsage();
            info.remoteUsedSpace = su.getUsed();
            // fix bug, need to consider team
            if (su.getAllocation().isIndividual()) {
                info.remoteTotalSpace = su.getAllocation().getIndividualValue().getAllocated();
            } else if (su.getAllocation().isTeam()) {
                info.remoteTotalSpace = su.getAllocation().getTeamValue().getAllocated();
            } else {
                // unkonw
                info.remoteTotalSpace = -1;
            }
            FullAccount act = sDbxClient.users().getCurrentAccount();
            info.displayName = act.getName().getDisplayName();
            info.email = act.getEmail();
            return true;
        } catch (DbxException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static class GetAccountAsyncTask extends AsyncTask<Void, Void, FullAccount> {

        private String accessToken;
        private IGetAccountAsyncTask mCallBack = null;

        public void setCallBack(String accessToken, IGetAccountAsyncTask mCallBack) {
            this.accessToken = accessToken;
            this.mCallBack = mCallBack;
        }

        @Override
        protected FullAccount doInBackground(Void... params) {
            try {
                NxDropBox2 dropBox2 = new NxDropBox2(accessToken);
                FullAccount account = dropBox2.getsDbxClient().users().getCurrentAccount();
                log.v(account.getAccountId() + account.getEmail() + account.getName().getDisplayName());
                return account;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(FullAccount account) {
            if (mCallBack != null) {
                mCallBack.onFinishGet(account);
            }
        }

        public interface IGetAccountAsyncTask {
            void onFinishGet(FullAccount account);
        }
    }

    private class DownLoadFileAsyncTask extends AsyncTask<Void, Long, Boolean> implements ICancelable {

        private INxFile document;
        private String localPath;
        private IDownLoadCallback callback;
        private int start = -1;
        private int length = -1;
        private boolean bPartialDownload = false;

        private FileDownloadException fileDownloadException = null;
        private boolean isCanceled = false;

        DownLoadFileAsyncTask(final INxFile document, final String localPath, boolean bPartialDownload, final IDownLoadCallback callback) {
            this.document = document;
            this.localPath = localPath;
            this.bPartialDownload = bPartialDownload;
            this.callback = callback;
        }

        DownLoadFileAsyncTask(final INxFile document, final String localPath, int start, int length, boolean bPartialDownload, final IDownLoadCallback callback) {
            this.document = document;
            this.localPath = localPath;
            this.start = start;
            this.length = length;
            this.bPartialDownload = bPartialDownload;
            this.callback = callback;
        }

        private void checkIfUserCanceled() throws FileDownloadException {
            if (isCanceled) {
                throw new FileDownloadException("user canceld", FileDownloadException.ExceptionCode.UserCanceled);
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            BufferedOutputStream bos = null;
            InputStream ins = null;
            OutputStream os = null;
            try {
                DbxDownloader<FileMetadata> downloader = null;
                if (bPartialDownload) {
                    DownloadBuilder downloadBuilder = sDbxClient.files().downloadBuilder(document.getCloudPath());
                    // int offSet = 1024 * 16; // 16 KB
                    downloadBuilder.range(start, length);
                    downloader = downloadBuilder.start();
                } else {
                    downloader = sDbxClient.files().download(document.getCloudPath());
                }

                checkIfUserCanceled();

                // for server result:
                FileMetadata meta = downloader.getResult();
                log.v("download: " + meta.toStringMultiline());

                checkIfUserCanceled();

                // trying to get inputstream
                ins = downloader.getInputStream();
                // prepage output stream
                File local = new File(localPath);
                Helper.makeSureDocExist(local);
                os = new FileOutputStream(local);

                checkIfUserCanceled();

                // begin download
                long totalRead = 0L;
                long length = meta.getSize();

                bos = new BufferedOutputStream(os);
                byte[] e = new byte[4096];

                while (true) {
                    int read = ins.read(e);  // -1 means eof
                    if (read < 0) {
                        if (length >= 0L && totalRead < length && !bPartialDownload) {
                            throw new FileDownloadException("size error", FileDownloadException.ExceptionCode.IllegalOperation);
                        }
                        bos.flush();
                        os.flush();
                        try {
                            ((FileOutputStream) os).getFD().sync();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        break;
                    }
                    // update
                    bos.write(e, 0, read);
                    totalRead += read;
                    // tell listener
                    long percentage = (long) (totalRead / (double) length * 100);
                    publishProgress(percentage);

                    checkIfUserCanceled();
                }
                return true;
            } catch (FileDownloadException e) {
                fileDownloadException = e;
            } catch (DbxException e) {
                fileDownloadException = new FileDownloadException(e.getMessage(), FileDownloadException.ExceptionCode.IllegalOperation);
                e.printStackTrace();
            } catch (IOException e) {
                fileDownloadException = new FileDownloadException(e.getMessage(), FileDownloadException.ExceptionCode.NetWorkIOFailed);
            } catch (Exception e) {
                fileDownloadException = new FileDownloadException(e.getMessage(), FileDownloadException.ExceptionCode.Common);
            } finally {
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (ins != null) {
                    try {
                        ins.close();
                    } catch (Exception e) {
                    }
                }
                if (isCanceled) {
                    Helper.deleteFile(new File(localPath));
                }
            }
            return false;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (callback != null) {
                callback.cancelHandler(this);
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (callback != null) {
                if (aBoolean) {
                    ((NxFileBase) document).setCached(true);
                } else {
                    ((NxFileBase) document).setCached(false);
                }
                if (fileDownloadException != null) {
                    log.e(fileDownloadException);
                }
                callback.onFinishedDownload(aBoolean, localPath, fileDownloadException);
            }
        }

        @Override
        protected void onProgressUpdate(Long... values) {
            if (callback != null) {
                callback.progressing(values[0]);
            }
        }

        @Override
        public void cancel() {
            isCanceled = true;
            cancel(true);
        }
    }


}
