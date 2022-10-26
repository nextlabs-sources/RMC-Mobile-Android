package com.skydrm.rmc.reposystem.remoterepo.sharepointonline;

import android.os.AsyncTask;
import android.util.Log;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.reposystem.IRemoteRepo;
import com.skydrm.rmc.reposystem.RemoteRepoInfo;
import com.skydrm.rmc.reposystem.exception.FileDownloadException;
import com.skydrm.rmc.reposystem.exception.FileListException;
import com.skydrm.rmc.reposystem.exception.FileUploadException;
import com.skydrm.rmc.reposystem.exception.FolderCreateException;
import com.skydrm.rmc.reposystem.remoterepo.ICancelable;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.reposystem.types.NXDocument;
import com.skydrm.rmc.reposystem.types.NXFolder;
import com.skydrm.rmc.reposystem.types.NxFileBase;

import java.io.File;
import java.io.IOException;

import static com.skydrm.rmc.ExecutorPools.Select_Type.NETWORK_TASK;


public class NXSharePointOnline implements IRemoteRepo {
    private static final String TAG = "NXSharePointOnline";
    private static final boolean DEBUG = SkyDRMApp.getInstance().isDebug();
    private SharePointOnlineSdk sdk = null;

    public NXSharePointOnline(String url, String userName, String token) {
        sdk = new SharePointOnlineSdk(url, userName, token);
    }

    @Override
    public void updateToken(String accessToken) {

    }

    @Override
    public INxFile listFiles(NXFolder file) throws FileListException {

        if (file == null)
            return null;
        try {
            return sdk.listFiles(file);
        } catch (Exception e) {
            Log.v(TAG, e.toString());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void deleteFile(final INxFile file) {
        // we should not modify 3rd parity repo

//        Log.d("SharepointOnline", "Delete file:" + file.getName());
//        try {
//            sdk.delete(file);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void createFolder(INxFile parentFolder, String subFolderName) throws FolderCreateException {
        try {
            sdk.create(parentFolder, subFolderName);
        } catch (IOException e) {
            throw new FolderCreateException(e.getMessage(), FolderCreateException.ExceptionCode.NamingCollided);
        } catch (Exception e) {
            e.printStackTrace();
            throw new FolderCreateException(e.getMessage(), FolderCreateException.ExceptionCode.Common);
        }
    }

    @Override
    public void downloadFile(INxFile document, String localPath, IDownLoadCallback callback) {
        DownLoadFileAsyncTask DownloadAsyncTask = new DownLoadFileAsyncTask(sdk, document, localPath) {
        };
        DownloadAsyncTask.setCallback(callback);
        DownloadAsyncTask.executeOnExecutor(ExecutorPools.SelectSmartly(NETWORK_TASK));
    }

    @Override
    public void downloadFilePartial(INxFile document, String localPath, int start, int length, IDownLoadCallback callback) {

    }

    @Override
    public void uploadFile(INxFile parentFolder, String fileName, File localFile, IUploadFileCallback callback) {
        //uploadAndupdateFile(parentFolder, fileName, localFile, callback, false);
        UploadFileAsyncTask UploadAsyncTask = new UploadFileAsyncTask(sdk, parentFolder, fileName, localFile, false) {
        };
        UploadAsyncTask.setCallback(callback);
        UploadAsyncTask.executeOnExecutor(ExecutorPools.SelectSmartly(NETWORK_TASK));

    }

    @Override
    public void updateFile(INxFile parentFolder, INxFile updateFile, File localFile, IUploadFileCallback callback) {
        //uploadAndupdateFile(parentFolder, updateFile.getName(), localFile, callback, true);
        UploadFileAsyncTask UploadAsyncTask = new UploadFileAsyncTask(sdk, parentFolder, updateFile.getName(), localFile, true) {
        };
        UploadAsyncTask.setCallback(callback);
        UploadAsyncTask.executeOnExecutor(ExecutorPools.SelectSmartly(NETWORK_TASK));
    }

    @Override
    public boolean getInfo(RemoteRepoInfo info) {
        if (DEBUG) {
            Log.e(TAG, "SharePointOnline:getInfo: " + info);
        }
        return sdk.getRepositoryInfo(info);
    }


    static class UploadFileAsyncTask extends AsyncTask<Void, Long, Boolean> implements ICancelable {
        protected String mCloudPath;
        private INxFile parentFolder;
        private File mFile;
        private long mFileLen;
        private IUploadFileCallback mCallback = null;
        private SharePointOnlineSdk sdk;

        public UploadFileAsyncTask(SharePointOnlineSdk sdk, INxFile parentFolder, String fileName, File localFile, boolean bUpdate) {
            this.parentFolder = parentFolder;
            this.mCloudPath = parentFolder.getCloudPath();
            this.mFile = localFile;
            this.mFileLen = mFile.length();
            this.sdk = sdk;
            sdk.startUploadFile(mCloudPath, fileName, bUpdate);
        }

        public void setCallback(IUploadFileCallback mCallback) {
            this.mCallback = mCallback;
        }

        @Override
        public void cancel() {
            sdk.abortUploadTask();
        }

        protected void onPreExecute() {
            if (mCallback != null) {
                mCallback.cancelHandler(this);
            }
        }

        @Override
        protected void onProgressUpdate(Long... values) {
            if (mCallback != null) {
                mCallback.progressing(values[0]);
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            return sdk.uploadFile(mFile, mFileLen, new SharePointOnlineSdk.IUpdateUploadFile() {
                @Override
                public void onUpdate(long newValue) {
                    publishProgress(newValue);
                }
            });
        }

        @Override
        protected void onPostExecute(Boolean status) {
            if (mCallback == null) {
                return;
            }
            if (!status) {
                mCallback.onFinishedUpload(false, null, new FileUploadException("server error", FileUploadException.ExceptionCode.Common));
                return;
            }

            // upload ok
            {
                // amend NxDoc;
                NXDocument doc = new NXDocument();
                doc.setName(mFile.getName());
                doc.setDisplayPath(parentFolder.getDisplayPath() + "/" + mFile.getName());
                doc.setLocalPath(parentFolder.getLocalPath() + "/" + mFile.getName());
                doc.setCloudPath("no use");
                doc.setmCloudPathID("no use");
                doc.setNewCreated(true);
                doc.setCached(true);
                doc.setLastModifiedTimeLong(System.currentTimeMillis());
                doc.updateRefreshTimeWisely();
                doc.setBoundService(parentFolder.getService());
                mCallback.onFinishedUpload(true, doc, null);
            }

        }

    }

    /**
     * Created by aning on 6/5/2015.
     */
    public static class DownLoadFileAsyncTask extends AsyncTask<Void, Long, Boolean> implements ICancelable {

        private static final String TAG = "DownLoadFileAsyncTask";
        INxFile document;
        private String LocalPath;
        private IDownLoadCallback mCallback;
        private SharePointOnlineSdk sdk;

        public DownLoadFileAsyncTask(SharePointOnlineSdk sdk, INxFile document, String LocalPath) {
            this.LocalPath = LocalPath;
            this.document = document;
            this.sdk = sdk;
            sdk.startDownloadFile(document.getCloudPath());
        }

        public void setCallback(IDownLoadCallback mCallback) {
            this.mCallback = mCallback;
        }

        @Override
        public void cancel() {
            sdk.abortTask();
            ((NxFileBase) document).setCached(false);
        }

        @Override
        protected void onProgressUpdate(Long... values) {
            if (mCallback != null) {
                mCallback.progressing(values[0]);
            }
        }

        @Override
        protected void onPreExecute() {
            if (mCallback != null) {
                mCallback.cancelHandler(this);
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (mCallback != null) {
                if (result) {
                    ((NxFileBase) document).setCached(true);
                } else {
                    ((NxFileBase) document).setCached(false);
                }
                if (result) {
                    mCallback.onFinishedDownload(result, LocalPath, null);
                } else {
                    mCallback.onFinishedDownload(result, LocalPath, new FileDownloadException("unknown"));
                }
            }
        }


        @Override
        protected Boolean doInBackground(Void... params) {

            return sdk.downloadFile(LocalPath, document.getSize(), new SharePointOnlineSdk.IUpdateDownLoadFile() {
                @Override
                public void onUpdate(long newValue) {
                    publishProgress(newValue);
                }
            });
        }

    }

    /**
     * Created by aning on 6/2/2015.
     */

    public static class GetAccountAsyncTask extends AsyncTask<Void, Void, SharePointOnlineSdk.Account> {

        private IGetAccountAsyncTask mCallBack = null;

        public void setCallBack(IGetAccountAsyncTask mCallBack) {
            this.mCallBack = mCallBack;
        }

        @Override
        protected SharePointOnlineSdk.Account doInBackground(Void... params) {
            SharePointOnlineSdk.Account account = null;
            try {
                account = SharePointOnlineSdk.getAuthAccount();
            } catch (Exception e) {

            }
            return account;
        }

        @Override
        protected void onPostExecute(SharePointOnlineSdk.Account account) {
            if (mCallBack != null) {
                mCallBack.onFinishGet(account);
            }
        }

        public interface IGetAccountAsyncTask {
            void onFinishGet(SharePointOnlineSdk.Account account);
        }
    }
}
