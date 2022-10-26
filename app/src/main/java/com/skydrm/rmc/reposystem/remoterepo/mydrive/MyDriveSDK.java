package com.skydrm.rmc.reposystem.remoterepo.mydrive;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

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
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.user.IRmUser;
import com.skydrm.sdk.ui.uploadprogress.ProgressRequestListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import static com.skydrm.rmc.ExecutorPools.Select_Type.NETWORK_TASK;

/**
 * Nextlabs Designed RMS maintained Repository
 */

public class MyDriveSDK implements IRemoteRepo {
    private RestAPI api;
    private IRmUser user;
    private DevLog log = new DevLog(MyDriveSDK.class.getSimpleName());

    public MyDriveSDK(@NonNull RestAPI api, @NonNull IRmUser user) {
        this.api = api;
        this.user = user;
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

        try {
            JSONObject entries = api.getMyDriveService(user).myDriveList(path);
            log.i("listFolder:\n" + entries.toString());
            return parseFolder(file, entries);
        } catch (RmsRestAPIException e) {
            log.e(e);
            switch (e.getDomain()) {
                case AuthenticationFailed:
                    throw new FileListException(e.getMessage(), FileListException.ExceptionCode.AuthenticationFailed);
                default:
                    throw new FileListException(e.getMessage());
            }
        } catch (Exception e) {
            log.e(e);
            throw new FileListException(e.getMessage());
        }
    }

    private NXFolder parseFolder(@NonNull NXFolder folder, JSONObject entries) throws Exception {
        if (!entries.has("entries") || entries.isNull("entries")) {
            return null;
        }
        //
        //fix-bug, must use a new node instead root, or root will be inserted new items which have existed
        // --------- NXFolder f = new NXFolder(folder.getLocalPath(), folder.getCloudPath(), folder.getName(), 0);
        //
        NXFolder f = (NXFolder) folder.clone();
        // important!!! prevent to add same child;
        f.getChildren().clear();
        JSONArray array = entries.getJSONArray("entries");
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            if (obj.getBoolean("folder")) {
                String path = obj.getString("pathId");
                if (path.length() > 1 && path.endsWith("/")) {
                    path = path.substring(0, path.length() - 1);
                }
                String pathDisplay = obj.getString("pathDisplay");
                if (pathDisplay.length() > 1 && pathDisplay.endsWith("/")) {
                    pathDisplay = pathDisplay.substring(0, pathDisplay.length() - 1);
                }
                String name = obj.getString("name");
                // not last_modified , use current instead
                // no size
                NXFolder aFolder = new NXFolder(path, path, name, 0);
                aFolder.setLocalPath(pathDisplay);
                aFolder.setDisplayPath(pathDisplay);
                aFolder.setCloudPath(path);
                aFolder.setmCloudPathID(path);
                aFolder.setLastModifiedTimeLong(System.currentTimeMillis()); // folder in myDrive no lastModified
                f.addChild(aFolder);
            } else {
                // parse document
                String path = obj.getString("pathId");
                String pathDisplay = obj.getString("pathDisplay");
                String name = obj.getString("name");
                long last_modified = obj.getLong("lastModified");
                long size = obj.getLong("size");

                NXDocument aDoc = new NXDocument();
                aDoc.setName(name);
                aDoc.setDisplayPath(pathDisplay);
                aDoc.setLocalPath(pathDisplay);
                aDoc.setCloudPath(path);
                aDoc.setmCloudPathID(path);
                aDoc.setLastModifiedTimeLong(last_modified);
                aDoc.setSize(size);
                f.addChild(aDoc);
            }
        }
        f.updateRefreshTimeWisely();
        return f;
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
    public void uploadFile(final INxFile parentFolder, final String fileName, final File localFile, final IUploadFileCallback callback) {

        // sanity check , whether is a legal path in fileName

        String cloudPath = parentFolder.getCloudPath();
        if (!cloudPath.endsWith("/")) {
            cloudPath = cloudPath + "/";
        }
        final String finalCloudPath = cloudPath;
        final String finalLocalFullPath1 = cloudPath + fileName;
        class Task extends AsyncTask<Void, Long, Boolean> implements ICancelable {
            boolean bAbortTask = false;
            FileUploadException fileUploadException = null;

            @Override
            public void cancel() {
                bAbortTask = true;
            }

            @Override
            protected void onProgressUpdate(Long... values) {
                if (callback != null) {
                    callback.progressing(values[0]);
                }
            }

            @Override
            protected void onPreExecute() {
                if (callback != null) {
                    callback.cancelHandler(this);
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    api.getMyDriveService(user).myDriveUploadProgress(finalCloudPath, fileName, localFile, new ProgressRequestListener() {
                        @Override
                        public void onRequestProgress(long bytesWritten, long contentLength, boolean done) throws IOException {
                            if (bAbortTask) {
                                log.i("user canceled");
                                throw new IOException("user canceled");
                            }
                            long percentage = (long) (bytesWritten / (double) contentLength * 100);
                            publishProgress(percentage);
                        }
                    });
                    return true;
                } catch (RmsRestAPIException e) {
                    switch (e.getDomain()) {
                        case AuthenticationFailed:
                            fileUploadException = new FileUploadException(e.getMessage(), FileUploadException.ExceptionCode.AuthenticationFailed);
                            break;
                        case FileAlreadyExists:
                            fileUploadException = new FileUploadException(e.getMessage(), FileUploadException.ExceptionCode.NamingCollided);
                            break;
                        case MalformedRequest:
                        case InternalServerError:
                        case DriveStorageExceeded:
                            fileUploadException = new FileUploadException(e.getMessage(), FileUploadException.ExceptionCode.IllegalOperation);
                            break;
                        default:
                            fileUploadException = new FileUploadException(e.getMessage(), FileUploadException.ExceptionCode.Common);
                            break;
                    }
                    log.e(e);
                } catch (Exception e) {
                    log.e(e);
                    fileUploadException = new FileUploadException(e.getMessage(), FileUploadException.ExceptionCode.Common);
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                if (!aBoolean) {
                    log.i("onPostExecute:upload failed");
                    callback.onFinishedUpload(false, null, fileUploadException);
                    return;
                }
                // amend NxDoc;
                NXDocument doc = new NXDocument();
                doc.setName(fileName);
                doc.setDisplayPath(finalLocalFullPath1);
                doc.setLocalPath(finalLocalFullPath1);
                doc.setCloudPath(finalLocalFullPath1.toLowerCase());
                doc.setmCloudPathID(finalLocalFullPath1.toLowerCase());
                doc.setNewCreated(true);
                doc.setCached(true);
                doc.setSize(localFile.length());
                doc.setLastModifiedTimeLong(System.currentTimeMillis());
                doc.updateRefreshTimeWisely();
                doc.setBoundService(parentFolder.getService());
                callback.onFinishedUpload(true, doc, null);
            }
        }
        new Task().executeOnExecutor(ExecutorPools.SelectSmartly(NETWORK_TASK));
    }

    @Override
    public void updateFile(INxFile parentFolder, INxFile updateFile, File localFile, IUploadFileCallback callback) {
        throw new RuntimeException("not impl");
    }

    @Override
    public void deleteFile(final INxFile file) {
        try {
            String path = file.getCloudPath();
            if (file.isFolder()) {
                path += "/";
            }
            api.getMyDriveService(user).myDriveDelete(path);
        } catch (RmsRestAPIException e) {
            // TODO: 4/7/2017
            e.printStackTrace();
        }

    }

    @Override
    public void createFolder(INxFile parentFolder, String subFolderName) throws FolderCreateException {
        try {
            String cloudPath = parentFolder.getCloudPath();
            if (!cloudPath.endsWith("/")) {
                cloudPath = cloudPath + "/";
            }
            api.getMyDriveService(user).myDriveCreateFolder(cloudPath, subFolderName);
        } catch (RmsRestAPIException e) {
            switch (e.getDomain()) {
                case AuthenticationFailed:
                    throw new FolderCreateException(e.getMessage(), FolderCreateException.ExceptionCode.AuthenticationFailed);
                case FileAlreadyExists:
                    throw new FolderCreateException(e.getMessage(), FolderCreateException.ExceptionCode.NamingCollided);
                case InvalidFolderName:
                    throw new FolderCreateException(e.getMessage(), FolderCreateException.ExceptionCode.NamingViolation);
                default:
                    throw new FolderCreateException(e.getMessage(), FolderCreateException.ExceptionCode.Common);
            }
        }
    }

    @Override
    public boolean getInfo(RemoteRepoInfo info) {
        try {
            JSONObject jsonObject = api.getMyDriveService(user).myDriveStorageUsed();
            info.email = user.getEmail();
            info.displayName = user.getName();
            info.remoteTotalSpace = jsonObject.getLong("quota");
            info.remoteUsedSpace = jsonObject.getLong("usage");
            return true;

        } catch (final RmsRestAPIException e) {
            // TODO: 4/18/2017
        } catch (Exception e) {
            log.e(e);
        }
        return false;
    }

    private class Task extends AsyncTask<Void, Long, Boolean> implements ICancelable {
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

        Task(final INxFile document, final String localPath, int start, int length, boolean bPartialDownload, final IDownLoadCallback callback) {
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
            try {
                if (bPartialDownload) { // download partial content for reading nxl rights.
                    api.getMyDriveService(user).myDriveDownload(document.getCloudPath(), length, localPath,
                            new RestAPI.DownloadListener() {
                                @Override
                                public void current(int i) {
                                    publishProgress((long) i);
                                }

                                @Override
                                public void cancel() {

                                }
                            });
                } else {
                    //for different storage provider such as Amazon(S3) and Microsoft(OD4B)
                    //for .pptx(also docx) stores in the two drive will be different. when user call download all function we just ignore file length.
                    api.getMyDriveService(user).myDriveDownload(document.getCloudPath(), 0, localPath,
                            new RestAPI.DownloadListener() {
                                @Override
                                public void current(int i) {
                                    publishProgress((long) i);
                                }

                                @Override
                                public void cancel() {

                                }
                            });
                }
            } catch (RmsRestAPIException e) {
                // background thread capture exception and then keep it in a variable for UI code will use it
                switch (e.getDomain()) {
                    case AuthenticationFailed:
                        fileDownloadException = new FileDownloadException(e.getMessage(), FileDownloadException.ExceptionCode.AuthenticationFailed);
                        break;
                    case NetWorkIOFailed:
                        fileDownloadException = new FileDownloadException(e.getMessage(), FileDownloadException.ExceptionCode.NetWorkIOFailed);
                        break;
                    default:
                        fileDownloadException = new FileDownloadException(e.getMessage(), FileDownloadException.ExceptionCode.Common);
                        break;
                }
                log.e(e);
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


}
