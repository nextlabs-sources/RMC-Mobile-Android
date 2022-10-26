package com.skydrm.rmc.engine;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.base.NxlDoc;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.engine.Render.FileRenderProxy;
import com.skydrm.rmc.engine.Render.RenderHelper;
import com.skydrm.rmc.engine.eventBusMsg.ConvertResultNotifyEvent;
import com.skydrm.rmc.engine.eventBusMsg.ShowConvertProgressEvent;
import com.skydrm.rmc.engine.eventBusMsg.ViewFileResultNotifyEvent;
import com.skydrm.rmc.errorHandler.GenericError;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.reposystem.exception.FileDownloadException;
import com.skydrm.rmc.reposystem.localrepo.helper.Helper;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.rmc.ui.myspace.myvault.ICommand;
import com.skydrm.rmc.ui.myspace.myvault.data.Error;
import com.skydrm.rmc.ui.myspace.myvault.data.IMyVaultCommand;
import com.skydrm.rmc.ui.myspace.myvault.data.MyVaultCommandExecutor;
import com.skydrm.rmc.ui.myspace.myvault.data.Result;
import com.skydrm.rmc.ui.widget.customcontrol.SafeProgressDialog;
import com.skydrm.rmc.utils.FileHelper;
import com.skydrm.rmc.utils.NxCommonUtils;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.IRecipients;
import com.skydrm.sdk.exception.NotGrantedShareRights;
import com.skydrm.sdk.exception.NotNxlFileException;
import com.skydrm.sdk.exception.RightsExpiredException;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.exception.TokenAccessDenyException;
import com.skydrm.sdk.nxl.FileInfo;
import com.skydrm.sdk.policy.AdhocPolicy;
import com.skydrm.sdk.policy.CentralPolicy;
import com.skydrm.sdk.policy.Expiry;
import com.skydrm.sdk.policy.Obligations;
import com.skydrm.sdk.policy.Policy;
import com.skydrm.sdk.policy.Rights;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.rest.myVault.MyVaultUploadFileParams;
import com.skydrm.sdk.rms.rest.myVault.MyVaultUploadFileResult;
import com.skydrm.sdk.rms.rest.project.ProjectMetaDataResult;
import com.skydrm.sdk.rms.rest.project.file.ProjectDownloadHeader;
import com.skydrm.sdk.rms.rest.project.file.UploadFileResult;
import com.skydrm.sdk.rms.rest.project.file.UploadForNXLFileParam;
import com.skydrm.sdk.rms.types.RemoteViewProjectFileParas;
import com.skydrm.sdk.rms.types.RemoteViewRepoFileParas;
import com.skydrm.sdk.rms.types.RemoteViewResult2;
import com.skydrm.sdk.rms.user.IRmUser;
import com.skydrm.sdk.rms.user.membership.ProjectMemberShip;
import com.skydrm.sdk.ui.uploadprogress.ProgressRequestListener;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.skydrm.rmc.ExecutorPools.Select_Type.FIRED_BY_UI;
import static com.skydrm.rmc.ExecutorPools.Select_Type.NETWORK_TASK;
import static com.skydrm.sdk.exception.RmsRestAPIException.ExceptionDomain.AccessDenied;
import static com.skydrm.sdk.exception.RmsRestAPIException.ExceptionDomain.AuthenticationFailed;
import static com.skydrm.sdk.exception.RmsRestAPIException.ExceptionDomain.FileTypeNotSupported;

/**
 * Created by aning on 11/22/2016.
 * - handle some basic operation of file, such as:
 * - file encrypt, decrypt, sharing, convert etc.
 */

public class FileOperation {
    private static final DevLog log = new DevLog(FileOperation.class.getSimpleName());

    /**
     * used to decrypt nxl file
     */
    public static void decryptNxlFile(
            final Context context,
            final File file,
            final String viewType,
            boolean allowOfflineLoad,
            final FileRenderProxy.IDecryptCallback decryptCallback) {

        //create a tmp file that used to hold decrypted data.
        String strTmpFileName = file.getName().substring(0, file.getName().lastIndexOf("."));
        final String tmpDecryptedPath = context.getApplicationContext().getCacheDir().getPath() + "/" + strTmpFileName;
        File mTmpLocal = null;
        try {
            mTmpLocal = new File(tmpDecryptedPath);
            if (!mTmpLocal.exists()) {
                mTmpLocal.createNewFile();
            }
        } catch (IOException e) {
            log.d("In decryptNxlFile:create temporary file failed!");
            e.printStackTrace();
        }
        new Task(context, file, tmpDecryptedPath, viewType, allowOfflineLoad, decryptCallback)
                .executeOnExecutor(ExecutorPools.SelectSmartly(FIRED_BY_UI));
    }

    public static void decryptNxlFile(
            final Context context,
            final File file,
            final String viewType,
            int sharedSpaceType, int sharedSpaceId, String sharedSpaceUserMembership,
            boolean allowOfflineLoad,
            final FileRenderProxy.IDecryptCallback decryptCallback) {

        //create a tmp file that used to hold decrypted data.
        String strTmpFileName = file.getName().substring(0, file.getName().lastIndexOf("."));
        final String tmpDecryptedPath = context.getApplicationContext().getCacheDir().getPath() + "/" + strTmpFileName;
        File mTmpLocal = null;
        try {
            mTmpLocal = new File(tmpDecryptedPath);
            if (!mTmpLocal.exists()) {
                mTmpLocal.createNewFile();
            }
        } catch (IOException e) {
            log.d("In decryptNxlFile:create temporary file failed!");
            e.printStackTrace();
        }
        new Task2(context, file, tmpDecryptedPath,
                viewType, allowOfflineLoad,
                sharedSpaceType, sharedSpaceId, sharedSpaceUserMembership,
                decryptCallback)
                .executeOnExecutor(ExecutorPools.SelectSmartly(FIRED_BY_UI));
    }

    static class Task extends AsyncTask<Void, Void, INxlFileFingerPrint> {
        private WeakReference<Context> ctx;
        private File file;
        private String tmpDecryptedPath;
        private Exception mExp;
        private FileRenderProxy.IDecryptCallback decryptCallback;
        private String viewType;
        private boolean allowOfflineLoad;

        Task(Context c, File f, String p, String type,
             boolean allowOfflineLoad,
             FileRenderProxy.IDecryptCallback callback) {
            this.ctx = new WeakReference<>(c);
            this.file = f;
            this.tmpDecryptedPath = p;
            this.viewType = type;
            this.allowOfflineLoad = allowOfflineLoad;
            this.decryptCallback = callback;
        }

        @Override
        protected INxlFileFingerPrint doInBackground(Void... params) {
            try {
                return SkyDRMApp.getInstance()
                        .getSession()
                        .getRmsClient()
                        .decryptFromNxl(file.getAbsolutePath(),
                                tmpDecryptedPath,
                                true,
                                allowOfflineLoad);
            } catch (NotNxlFileException
                    | TokenAccessDenyException
                    | RightsExpiredException
                    | RmsRestAPIException e) {
                mExp = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(INxlFileFingerPrint result) {
            if (result != null) {
                if (decryptCallback != null) {
                    decryptCallback.onDecryptFinished(true, tmpDecryptedPath, result);
                }
            } else {
                if (decryptCallback != null) {
                    decryptCallback.onDecryptFinished(false, "", null);
                }
                if (mExp != null) {
                    Context context = ctx.get();
                    if (context == null) {
                        return;
                    }
                    if (mExp instanceof RmsRestAPIException) {
                        // SkyDRMApp.getInstance().getSession().sessionExpiredHandler(context);
                        switch (((RmsRestAPIException) mExp).getDomain()) {
                            case AuthenticationFailed:
                                SkyDRMApp.getInstance().getSession().sessionExpiredHandler(context);
                            case NetWorkIOFailed:
                                ToastUtil.showToast(context, context.getResources().getString(R.string.excep_network_unavailable));
                                break;
                            default:
                                ToastUtil.showToast(context, context.getResources().getString(R.string.excep_operation_failed));
                                break;
                        }
                    } else {
                        if (viewType.equals(Constant.VIEW_TYPE_NORMAL)) {
                            ExceptionHandler.handleException(context, mExp);
                        } else if (viewType.equals(Constant.VIEW_TYPE_PREVIEW)) {
                            if (mExp instanceof TokenAccessDenyException) {
                                EventBus.getDefault().post(new ViewFileResultNotifyEvent(false, context.getString(R.string.excep_token_access_deny)));
                            } else if (mExp instanceof RightsExpiredException) {
                                EventBus.getDefault().post(new ViewFileResultNotifyEvent(false, context.getString(R.string.excep_nxl_rights_expired)));
                            } else {
                                EventBus.getDefault().post(new ViewFileResultNotifyEvent(false, context.getString(R.string.hint_msg_error_while_process)));
                            }
                        }
                    }
                }
            }
        }
    }

    static class Task2 extends AsyncTask<Void, Void, INxlFileFingerPrint> {
        private WeakReference<Context> ctx;
        private File file;
        private String tmpDecryptedPath;
        private Exception mExp;
        private FileRenderProxy.IDecryptCallback decryptCallback;
        private String viewType;
        private boolean allowOfflineLoad;
        private int sharedSpaceType;
        private int sharedSpaceId;
        private String sharedSpaceUserMembership;

        Task2(Context c, File f, String p, String type,
              boolean allowOfflineLoad,
              int sharedSpaceType, int sharedSpaceId, String sharedSpaceUserMembership,
              FileRenderProxy.IDecryptCallback callback) {
            this.ctx = new WeakReference<>(c);
            this.file = f;
            this.tmpDecryptedPath = p;
            this.viewType = type;
            this.allowOfflineLoad = allowOfflineLoad;
            this.sharedSpaceType = sharedSpaceType;
            this.sharedSpaceId = sharedSpaceId;
            this.sharedSpaceUserMembership = sharedSpaceUserMembership;
            this.decryptCallback = callback;
        }

        @Override
        protected INxlFileFingerPrint doInBackground(Void... params) {
            try {
                return SkyDRMApp.getInstance()
                        .getSession()
                        .getRmsClient()
                        .decryptFromNxl(file.getAbsolutePath(), tmpDecryptedPath,
                                sharedSpaceType, sharedSpaceId, sharedSpaceUserMembership,
                                true, allowOfflineLoad);
            } catch (NotNxlFileException
                    | TokenAccessDenyException
                    | RightsExpiredException
                    | RmsRestAPIException e) {
                mExp = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(INxlFileFingerPrint result) {
            if (result != null) {
                if (decryptCallback != null) {
                    decryptCallback.onDecryptFinished(true, tmpDecryptedPath, result);
                }
            } else {
                if (decryptCallback != null) {
                    decryptCallback.onDecryptFinished(false, "", null);
                }
                if (mExp != null) {
                    Context context = ctx.get();
                    if (context == null) {
                        return;
                    }
                    if (mExp instanceof RmsRestAPIException) {
                        // SkyDRMApp.getInstance().getSession().sessionExpiredHandler(context);
                        switch (((RmsRestAPIException) mExp).getDomain()) {
                            case AuthenticationFailed:
                                SkyDRMApp.getInstance().getSession().sessionExpiredHandler(context);
                            case NetWorkIOFailed:
                                ToastUtil.showToast(context, context.getResources().getString(R.string.excep_network_unavailable));
                                break;
                            default:
                                ToastUtil.showToast(context, context.getResources().getString(R.string.excep_operation_failed));
                                break;
                        }
                    } else {
                        if (viewType.equals(Constant.VIEW_TYPE_NORMAL)) {
                            ExceptionHandler.handleException(context, mExp);
                        } else if (viewType.equals(Constant.VIEW_TYPE_PREVIEW)) {
                            if (mExp instanceof TokenAccessDenyException) {
                                EventBus.getDefault().post(new ViewFileResultNotifyEvent(false, context.getString(R.string.excep_token_access_deny)));
                            } else if (mExp instanceof RightsExpiredException) {
                                EventBus.getDefault().post(new ViewFileResultNotifyEvent(false, context.getString(R.string.excep_nxl_rights_expired)));
                            } else {
                                EventBus.getDefault().post(new ViewFileResultNotifyEvent(false, context.getString(R.string.hint_msg_error_while_process)));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * used to convert the 3D file
     */
    public static void Convert3dFiles(final Context context,
                                      File file,
                                      final String fileName,
                                      boolean isNXLFile,
                                      boolean isPdf,
                                      final String viewType,
                                      FileRenderProxy.IConvertAsyncTask callback) {

        class ConvertAsyncTask extends AsyncTask<Void, Integer, Boolean> {
            private Context mContext;
            private Activity mActivity;
            private File mFile;
            private String ConvertPath = "";
            private boolean isNXLFile = false;
            private boolean is3DPdf = false;

            private SafeProgressDialog mProgressDialog;
            private FileRenderProxy.IConvertAsyncTask mCallback;

            private RmsRestAPIException mRmsRestAPIException;

            private ConvertAsyncTask(Context context, File file, boolean isNxlFile,
                                     boolean is3DPdf,
                                     FileRenderProxy.IConvertAsyncTask mCallback) {
                this.mContext = context;
                this.mActivity = (Activity) mContext;
                this.mFile = file;
                this.isNXLFile = isNxlFile;
                this.is3DPdf = is3DPdf;
                this.mCallback = mCallback;
            }


            @Override
            protected void onPreExecute() {

                if (viewType.equals(Constant.VIEW_TYPE_NORMAL)) {

                    mProgressDialog = SafeProgressDialog.showDialog(context,
                            "",
                            context.getResources().getString(R.string.wait_load) + fileName,
                            ProgressDialog.STYLE_HORIZONTAL,
                            R.drawable.home_file_icon,
                            false);

                    mProgressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialogInterface, int keycode, KeyEvent keyEvent) {
                            try {
                                if (KeyEvent.KEYCODE_BACK == keycode && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                                        mProgressDialog.dismiss();
                                        mProgressDialog = null;
                                        SkyDRMApp.getInstance().getSession().getRmsRestAPI().cancel(); // cancel convert!
                                        ((Activity) mContext).finish();
                                    }
                                }
                                return false;
                            } catch (SessionInvalidException e) {
                                log.e("Convert3dFiles, dialog onKey", e);
                            }
                            return false;
                        }
                    });
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    byte[] convertedBinary = null;
                    try {
                        SkyDRMApp.Session2 session = SkyDRMApp.getInstance()
                                .getSession();
                        convertedBinary = session.getRmsRestAPI().convertCAD(session.getRmUser(), mFile, new RestAPI.IConvertListener() {
                            @Override
                            public void onConvertProgress(int current, int total) {
                                publishProgress(current, total);
                            }
                        });
                    } catch (SessionInvalidException | InvalidRMClientException e) {
                        log.e("In Convert3dFiles, Convert file fail, since Failed calling Rest api convertCAD");
                        return false;
                    }
                    if (convertedBinary == null) {
                        log.e("In Convert3dFiles, Convert file fail,convertedBinary is null");
                        return false;
                    }

                    String convertedName = URLEncoder.encode(mFile.getName(), "UTF-8") + ".hsf";
                    ConvertPath = RenderHelper.saveFile(mContext, convertedName, convertedBinary, isNXLFile);
                    if (TextUtils.isEmpty(ConvertPath)) {
                        log.e("In Convert3dFiles, Save file failed!");
                        return false;
                    }
                    return true;
                } catch (RmsRestAPIException e) {
                    mRmsRestAPIException = e;
                } catch (Exception e) {
                    log.e(e);
                }
                return false;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                if (viewType.equals(Constant.VIEW_TYPE_NORMAL)) {
                    if (mContext != null && !((Activity) mContext).isFinishing() && mProgressDialog != null) {
                        mProgressDialog.setProgress(values[0]);
                    }
                } else if (viewType.equals(Constant.VIEW_TYPE_PREVIEW)) {
                    EventBus.getDefault().post(new ShowConvertProgressEvent(ShowConvertProgressEvent.ConvertType.THREE_D_CONVERT, values[0]));
                }
            }

            @Override
            protected void onPostExecute(final Boolean success) {

                // this activity may be destroy by system when execute the background thread, so should judge the activity if
                // has finished after this background thread ends.
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }

                if (!success) {

                    if (mRmsRestAPIException != null && mRmsRestAPIException.getDomain() == AuthenticationFailed) {
                        SkyDRMApp.getInstance().getSession().sessionExpiredHandler(context);
                    } else {
                        if (viewType.equals(Constant.VIEW_TYPE_NORMAL)) {
                            GenericError.showUI((Activity) mContext, mContext.getString(R.string.hint_msg_error_while_process), true, false, true, null);
                        } else if (viewType.equals(Constant.VIEW_TYPE_PREVIEW)) {
                            EventBus.getDefault().post(new ConvertResultNotifyEvent(false, mContext.getString(R.string.hint_msg_error_while_process)));
                        }
                    }

                    return;
                }

                mCallback.onConvertFinish(ConvertPath);
            }

        }
        new ConvertAsyncTask(context, file, isNXLFile, isPdf, callback)
                .executeOnExecutor(ExecutorPools.SelectSmartly(NETWORK_TASK), (Void) null);
    }

    /**
     * Remote view local file, convert office file, 2d pdf, 3d pdf file, Microsoft Visio(vsd), dwg, tif and tiff by rms server.
     */
    public static void remoteViewLocal(final Context context,
                                       File file,
                                       final String fileName,
                                       final boolean isNxlFile,
                                       final String viewType,
                                       final FileRenderProxy.IRemoteViewerTask callback) {
        class RemoteViewerTask extends AsyncTask<Void, Integer, Boolean> {
            private Context mContext;
            private File mFile;
            private SafeProgressDialog mProgressDialog;
            private RemoteViewResult2.ResultsBean mRemoteViewerResult;
            private FileRenderProxy.IRemoteViewerTask mCallback;
            private RmsRestAPIException mRmsRestAPIException;

            private RemoteViewerTask(Context context, File file, FileRenderProxy.IRemoteViewerTask callback) {
                mContext = context;
                mFile = file;
                mCallback = callback;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (viewType.equals(Constant.VIEW_TYPE_NORMAL)) {

                    mProgressDialog = SafeProgressDialog.showDialog(context,
                            "",
                            context.getResources().getString(R.string.wait_load) + fileName,
                            ProgressDialog.STYLE_HORIZONTAL,
                            R.drawable.home_file_icon,
                            false);

                    mProgressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialogInterface, int keycode, KeyEvent keyEvent) {
                            try {
                                if (KeyEvent.KEYCODE_BACK == keycode && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                                        mProgressDialog.dismiss();
                                        mProgressDialog = null;
                                        // SkyDRMApp.getInstance().getSession().getRmsRestAPI().cancel(); // cancel convert!
                                        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
                                        session.getRmsRestAPI().getRemoteViewService(session.getRmUser()).cancel();
                                        ((Activity) mContext).finish();
                                    }
                                }
                            } catch (Exception e) {
                                log.e(e);
                            }
                            return false;
                        }
                    });
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    // if cached already return from cache
                    SkyDRMApp.RemoteViewerOfficePdfCache cache = SkyDRMApp.getInstance()
                            .getSession()
                            .getRemoteViwerCache();

                    SkyDRMApp.RemoteViewerOfficePdfCache.Value value = cache.getCache(mFile.getAbsolutePath());
                    if (value != null) {
                        // cache matched
                        // check time
                        mRemoteViewerResult = new RemoteViewResult2.ResultsBean();
                        mRemoteViewerResult.setViewerURL(value.getViewerURL());
                        mRemoteViewerResult.setCookies(value.getCookies());
                        mRemoteViewerResult.setPermissions(value.getPermissions());
                        mRemoteViewerResult.setOwner(value.isOwner());
                        return true;
                    }

                    SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
                    String tenantId = session.getRmsClient().getTenant().getTenantId();

                    RemoteViewResult2 result = SkyDRMApp.getInstance()
                            .getSession()
                            .getRmsRestAPI()
                            .getRemoteViewService(session.getRmUser())
                            .remoteViewLocalFile(tenantId, mFile, new RestAPI.IConvertListener() {
                                @Override
                                public void onConvertProgress(int current, int total) {
                                    publishProgress(current, total);
                                }
                            });

                    if (result == null) {
                        return false;
                    }
                    int code = result.getStatusCode();
                    if (code < 200 || code > 300) {
                        return false;
                    }
                    mRemoteViewerResult = result.getResults();
                    return true;

                } catch (SessionInvalidException | InvalidRMClientException e) {
                    log.e(e);
                    mRmsRestAPIException = new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.AuthenticationFailed);
                } catch (RmsRestAPIException e) {
                    log.e(e);
                    mRmsRestAPIException = e;
                } catch (Exception e) {
                    log.e(e);
                    mRmsRestAPIException = new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
                }
                return false;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                if (viewType.equals(Constant.VIEW_TYPE_NORMAL)) {
                    if (mContext != null && !((Activity) mContext).isFinishing() && mProgressDialog != null) {
                        mProgressDialog.setProgress(values[0]);
                    }
                } else if (viewType.equals(Constant.VIEW_TYPE_PREVIEW)) {
                    EventBus.getDefault().post(new ShowConvertProgressEvent(ShowConvertProgressEvent.ConvertType.OFFICE_CONVERT, values[0]));
                }

            }

            @Override
            protected void onPostExecute(Boolean result) {

                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }

                if (!result && mRmsRestAPIException != null) {
                    if (mRmsRestAPIException.getDomain() == AuthenticationFailed) {
                        SkyDRMApp.getInstance().getSession().sessionExpiredHandler(context);
                    } else if (mRmsRestAPIException.getDomain() == FileTypeNotSupported) {
                        GenericError.showUI((Activity) context, context.getString(R.string.hint_msg_error_view_unsupported_file_type),
                                true, false, true, null);
                    } else {
                        if (viewType.equals(Constant.VIEW_TYPE_NORMAL)) {
                            if (mRmsRestAPIException.getDomain() == AccessDenied) {
                                GenericError.showUI((Activity) mContext, mContext.getString(R.string.hint_msg_error_token_access_deny), true, false, true, null);
                            } else {
                                GenericError.showUI((Activity) mContext, mContext.getString(R.string.hint_msg_error_while_process), true, false, true, null);
                            }

                        } else if (viewType.equals(Constant.VIEW_TYPE_PREVIEW)) {
                            if (mRmsRestAPIException.getDomain() == AccessDenied) {
                                EventBus.getDefault().post(new ConvertResultNotifyEvent(false, mContext.getString(R.string.hint_msg_error_token_access_deny)));

                            } else {
                                EventBus.getDefault().post(new ConvertResultNotifyEvent(false, mContext.getString(R.string.hint_msg_error_while_process)));
                            }
                        }
                    }
                    return;
                }

                // set this result for cache
                // now disable the cache first, because the rms changed the data cache time,
                // email content: The file that is being viewed is cached in the server for a short duration. When the user closes the browser window of the viewer,
                // we clear this cache for that particular document to free up memory on the server side. Hence you can’t re-use the remote viewer URL.

  /*                if(mRemoteViewerResult != null) {
                    SkyDRMApp.getInstance()
                            .getSession()
                            .getRemoteViwerCache()
                            .addCache(mFile.getAbsolutePath(),
                                    new SkyDRMApp.RemoteViewerOfficePdfCache.Value(mRemoteViewerResult.getViewerURL(),
                                            mRemoteViewerResult.getCookies(),
                                            mRemoteViewerResult.getPermissions(),
                                            mRemoteViewerResult.isOwner()));
                }*/

                // callback
                mCallback.onConvertFinish(mRemoteViewerResult);

            }
        }
        new RemoteViewerTask(context, file, callback).executeOnExecutor(ExecutorPools.SelectSmartly(FIRED_BY_UI), (Void) null);
    }

    /**
     * Remote view repository file -- only for myDrive and myVault
     *
     * @param bNeedCache Note: add this para to control if need to cache the result,
     *                   for myDrive, now should not cache the result,or else,still can view the nxl file when it has been revoked or remove the user.
     */
    public static void remoteViewRepo(final Context context,
                                      final String repoId,
                                      final String pathId,
                                      final String pathDisplay,
                                      final String repoName,
                                      final String repoType,
                                      final long lastModifiedDate,
                                      final String viewType,
                                      final boolean bNeedCache,
                                      final FileRenderProxy.IRemoteViewerTask callback) {
        class RemoteViewerTask extends AsyncTask<Void, Integer, Boolean> {

            private RemoteViewResult2.ResultsBean mRemoteViewerResult;
            private RmsRestAPIException mRmsRestAPIException;

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    // if cached already return from cache
                    SkyDRMApp.RemoteViewerOfficePdfCache cache = SkyDRMApp.getInstance()
                            .getSession()
                            .getRemoteViwerCache();

                    SkyDRMApp.RemoteViewerOfficePdfCache.Value value = cache.getCache(pathId);
                    if (value != null) {
                        // cache matched
                        // check time
                        mRemoteViewerResult = new RemoteViewResult2.ResultsBean();
                        mRemoteViewerResult.setViewerURL(value.getViewerURL());
                        mRemoteViewerResult.setCookies(value.getCookies());
                        mRemoteViewerResult.setPermissions(value.getPermissions());
                        mRemoteViewerResult.setOwner(value.isOwner());
                        return true;
                    }

                    SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
                    String tenantId = session.getRmsClient().getTenant().getTenantId();
                    // set parameters.
                    RemoteViewRepoFileParas paras = new RemoteViewRepoFileParas(repoId,
                            pathId,
                            pathDisplay,
                            repoName,
                            repoType,
                            session.getRmUser().getEmail(),
                            tenantId,
                            lastModifiedDate
                    );
                    // invoke API
                    RemoteViewResult2 result = SkyDRMApp.getInstance()
                            .getSession()
                            .getRmsRestAPI()
                            .getRemoteViewService(session.getRmUser())
                            .remoteViewRepoFile(paras);


                    if (result == null) {
                        return false;
                    }
                    int code = result.getStatusCode();
                    if (code < 200 || code > 300) {
                        return false;
                    }
                    mRemoteViewerResult = result.getResults();
                    return true;

                } catch (SessionInvalidException | InvalidRMClientException e) {
                    log.e(e);
                    mRmsRestAPIException = new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.AuthenticationFailed);
                } catch (RmsRestAPIException e) {
                    log.e(e);
                    mRmsRestAPIException = e;
                } catch (Exception e) {
                    log.e(e);
                    mRmsRestAPIException = new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (!result && mRmsRestAPIException != null) {
                    if (mRmsRestAPIException.getDomain() == AuthenticationFailed) {
                        SkyDRMApp.getInstance().getSession().sessionExpiredHandler(context);
                    } else {
                        if (viewType.equals(Constant.VIEW_TYPE_NORMAL)) {
                            if (mRmsRestAPIException.getDomain() == AccessDenied) {
                                GenericError.showUI((Activity) context, context.getString(R.string.hint_msg_error_token_access_deny), true, false, true, null);
                            } else if (mRmsRestAPIException.getDomain() == FileTypeNotSupported) {
                                GenericError.showUI((Activity) context, context.getString(R.string.hint_msg_error_view_unsupported_file_type),
                                        true, false, true, null);
                            } else {
                                GenericError.showUI((Activity) context, context.getString(R.string.hint_msg_error_while_process), true, false, true, null);
                            }

                        } else if (viewType.equals(Constant.VIEW_TYPE_PREVIEW)) {
                            if (mRmsRestAPIException.getDomain() == AccessDenied) {
                                EventBus.getDefault().post(new ConvertResultNotifyEvent(false, context.getString(R.string.hint_msg_error_token_access_deny)));
                            } else {
                                EventBus.getDefault().post(new ConvertResultNotifyEvent(false, context.getString(R.string.hint_msg_error_while_process)));
                            }
                        }
                    }

                    return;
                }

                // set this result for cache
/*                if (bNeedCache && mRemoteViewerResult != null) {
                    SkyDRMApp.getInstance()
                            .getSession()
                            .getRemoteViwerCache()
                            .addCache(pathId,
                                    new SkyDRMApp.RemoteViewerOfficePdfCache.Value(mRemoteViewerResult.getViewerURL(),
                                            mRemoteViewerResult.getCookies(),
                                            mRemoteViewerResult.getPermissions(),
                                            mRemoteViewerResult.isOwner()));
                }*/

                // callback
                callback.onConvertFinish(mRemoteViewerResult);
            }
        }
        new RemoteViewerTask().executeOnExecutor(ExecutorPools.SelectSmartly(NETWORK_TASK), (Void) null);
    }

    /**
     * Remote view repository file -- for project. === use in next release
     */
    public static void remoteViewProject(final Context context,
                                         final RemoteViewProjectFileParas paras,
                                         final String viewType,
                                         final FileRenderProxy.IRemoteViewerTask callback) {
        class RemoteViewerTask extends AsyncTask<Void, Integer, Boolean> {

            private RemoteViewResult2.ResultsBean mRemoteViewerResult;
            private RmsRestAPIException mRmsRestAPIException;

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    // if cached already return from cache
                    SkyDRMApp.RemoteViewerOfficePdfCache cache = SkyDRMApp.getInstance()
                            .getSession()
                            .getRemoteViwerCache();

                    SkyDRMApp.RemoteViewerOfficePdfCache.Value value = cache.getCache(paras.getPathId());
                    if (value != null) {
                        // cache matched
                        // check time
                        mRemoteViewerResult = new RemoteViewResult2.ResultsBean();
                        mRemoteViewerResult.setViewerURL(value.getViewerURL());
                        mRemoteViewerResult.setCookies(value.getCookies());
                        mRemoteViewerResult.setPermissions(value.getPermissions());
                        mRemoteViewerResult.setOwner(value.isOwner());
                        return true;
                    }

                    SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
                    // invoke API
                    RemoteViewResult2 result = SkyDRMApp.getInstance()
                            .getSession()
                            .getRmsRestAPI()
                            .getRemoteViewService(session.getRmUser())
                            .remoteViewProjectFile(paras);


                    if (result == null) {
                        return false;
                    }
                    int code = result.getStatusCode();
                    if (code < 200 || code > 300) {
                        return false;
                    }
                    mRemoteViewerResult = result.getResults();
                    return true;

                } catch (SessionInvalidException e) {
                    log.e(e);
                    mRmsRestAPIException = new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.AuthenticationFailed);
                } catch (RmsRestAPIException e) {
                    log.e(e);
                    mRmsRestAPIException = e;
                } catch (Exception e) {
                    log.e(e);
                    mRmsRestAPIException = new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (!result) {
                    if (mRmsRestAPIException != null
                            && mRmsRestAPIException.getDomain() == AuthenticationFailed) {
                        SkyDRMApp.getInstance().getSession().sessionExpiredHandler(context);
                    } else if (mRmsRestAPIException != null
                            && mRmsRestAPIException.getDomain() == FileTypeNotSupported) {
                        GenericError.showUI((Activity) context, context.getString(R.string.hint_msg_error_view_unsupported_file_type),
                                true, false, true, null);
                    } else {
                        if (viewType.equals(Constant.VIEW_TYPE_NORMAL)) {
                            GenericError.showUI((Activity) context, context.getString(R.string.hint_msg_error_while_process), true, false, true, null);
                        } else if (viewType.equals(Constant.VIEW_TYPE_PREVIEW)) {
                            EventBus.getDefault().post(new ConvertResultNotifyEvent(false, context.getString(R.string.hint_msg_error_while_process)));
                        }
                    }

                    return;
                }
                // set this result for cache
/*                if (mRemoteViewerResult != null) {
                    SkyDRMApp.getInstance()
                            .getSession()
                            .getRemoteViwerCache()
                            .addCache(paras.getPathId(),
                                    new SkyDRMApp.RemoteViewerOfficePdfCache.Value(mRemoteViewerResult.getViewerURL(),
                                            mRemoteViewerResult.getCookies(),
                                            mRemoteViewerResult.getPermissions(),
                                            mRemoteViewerResult.isOwner()));
                }*/

                // callback
                callback.onConvertFinish(mRemoteViewerResult);

            }
        }
        new RemoteViewerTask().executeOnExecutor(ExecutorPools.SelectSmartly(NETWORK_TASK), (Void) null);
    }

    /**
     * used to encrypt normal file into nxl file.
     */
    @Deprecated
    public static void protectFile(final Context context,
                                   final File workingFile,
                                   final Rights nxrights,
                                   final Obligations obligations,
                                   final Expiry expiry,
                                   final IProtectFileFinish protectCallback) {
        try {
            protectFile(context, SkyDRMApp.getInstance().getSession().getRmUser().getMembershipId(), workingFile, nxrights, obligations, expiry, protectCallback);
        } catch (InvalidRMClientException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is used to encrypt normal file in project into nxl file.
     *
     * @param context         used to show loading dialog.
     * @param ownerID         project membership id[null means padding it with userId-caller doesn't need to care about it].
     * @param workingFile     target file need to be protected.
     * @param nxrights        rights need apply to target file.
     * @param obligations     obligations bundle to target file.
     * @param expiry          expiration set to target file.
     * @param protectCallback callback when protect operation finished.
     */
    @Deprecated
    public static void protectFile(final Context context,
                                   @Nullable final String ownerID,
                                   final File workingFile,
                                   final Rights nxrights,
                                   final Obligations obligations,
                                   final Expiry expiry,
                                   final IProtectFileFinish protectCallback) {
        class Task extends AsyncTask<Void, Void, Boolean> {
            private String nxlPath;
            private Exception mException;
            private ProgressDialog protectProgressDialog = null;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                protectProgressDialog = SafeProgressDialog.showDialog(context,
                        "", context.getResources().getString(R.string.wait_protect),
                        true);
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    Rights r = new Rights();
                    {
                        // set r;
                        r.setView(nxrights.hasView());
                        r.setPrint(nxrights.hasPrint());
                        r.setEdit(nxrights.hasEdit());
                        r.setDownload(nxrights.hasDownload());
                        r.setShare(nxrights.hasShare());
                        r.setWatermark(nxrights.hasWatermark());
                        r.setDecrypt(nxrights.hasDecrypt());
                    }
                    Obligations o = new Obligations();
                    {
                        //set o;
                        o.setObligation(obligations.getObligation());
                    }
                    // Commnets for ownerID is null, will use current login user
                    AdhocPolicy adhocPolicy = new AdhocPolicy(ownerID, r, o, expiry);

                    Policy policy = new Policy(adhocPolicy);
                    {
                        //Build file info section.
                        FileInfo fileInfo = new FileInfo.Builder()
                                .setDateCreated(System.currentTimeMillis())
                                .setDateModified(System.currentTimeMillis())
                                .setCreatedBy(ownerID)
                                .setModifiedBy(ownerID)
                                .setFileName(workingFile.getName())
                                .setFileExtension(RenderHelper.getFileExtension(Uri.fromFile(workingFile).toString()))
                                .build();
                        policy.setFileInfo(fileInfo);
                    }

                    nxlPath = NxCommonUtils.reNameNxlFile(workingFile.getPath());
                    return SkyDRMApp.getInstance().getSession().getRmsClient().encryptToNxl(
                            workingFile.getPath(),
                            nxlPath,
                            policy,
                            true
                    );
                } catch (RmsRestAPIException e) {
                    log.e(e);
                    mException = e;
                } catch (FileNotFoundException e) {
                    log.e(e);
                    mException = e;
                } catch (Exception e) {
                    log.e(e);
                    mException = e;
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (protectProgressDialog != null) {
                    protectProgressDialog.dismiss();
                }
                if (result) {
                    // upload after protect
                    protectCallback.onProtectFinished(nxlPath);
                } else {
                    FileHelper.delFile(nxlPath);
                    if (mException != null && mException instanceof RmsRestAPIException) {
                        GenericError.handleCommonException(context, true, (RmsRestAPIException) mException);
                    }
                }
            }
        }
        new Task().executeOnExecutor(ExecutorPools.SelectSmartly(NETWORK_TASK));
    }


    /**
     * This method is used to protect normal file with central policy.
     *
     * @param targetFile   File to be protected.
     * @param membershipId This params is used to mark the nxl file belong to which project.
     * @param tags         Tags bundled to the target file.
     * @param callback     Callbacks of protect file including [protecting|protected] the two status.
     */
    public static void protectFile(@NonNull File targetFile, @NonNull String membershipId,
                                   @NonNull Map<String, Set<String>> tags, @Nullable IProtectFileCallback callback) {
        new ProtectTask(targetFile, membershipId, tags, callback)
                .executeOnExecutor(ExecutorPools.SelectSmartly(NETWORK_TASK));
    }

    static class ProtectTask extends AsyncTask<Void, Void, Boolean> {
        private String mNxlPath;
        private File mTargetFile;
        private String mMembershipId;
        private IProtectFileCallback mCallback;
        private Map<String, Set<String>> mTags;
        private Exception mException;

        ProtectTask(File targetFile, String membershipId, Map<String, Set<String>> tags,
                    IProtectFileCallback callback) {
            this.mTargetFile = targetFile;
            this.mMembershipId = membershipId;
            this.mCallback = callback;
            this.mTags = tags;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mCallback != null) {
                mCallback.onFileProtecting();
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                CentralPolicy centralPolicy = new CentralPolicy.Builder()
                        .setMembershipId(mMembershipId)
                        .addTags(mTags)
                        .build();
                Policy policy = new Policy(centralPolicy);
                mNxlPath = NxCommonUtils.reNameNxlFile(mTargetFile.getPath());

                {
                    //Build file info section.
                    FileInfo fileInfo = new FileInfo.Builder()
                            .setDateCreated(System.currentTimeMillis())
                            .setDateModified(System.currentTimeMillis())
                            .setCreatedBy(mMembershipId)
                            .setModifiedBy(mMembershipId)
                            .setFileName(mTargetFile.getName())
                            .setFileExtension(RenderHelper.getFileExtension(Uri.fromFile(mTargetFile).toString()))
                            .build();
                    policy.setFileInfo(fileInfo);
                }

                return SkyDRMApp.getInstance().getSession().getRmsClient()
                        .encryptToNxl(mTargetFile.getPath(), mNxlPath, policy, true);
            } catch (FileNotFoundException | RmsRestAPIException e) {
                mException = e;
                if (mCallback != null) {
                    mCallback.onFileProtectError(e.getMessage());
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                if (mCallback != null) {
                    mCallback.onFileProtected(mNxlPath);
                }
            } else {
                FileHelper.delFile(mNxlPath);
                if (mCallback != null) {
                    mCallback.onFileProtectError(mException != null ? mException.getMessage()
                            : "Operation failed.");
                }
            }
        }
    }

    public static String protectFile(String ownerID,
                                     File file,
                                     Rights rights,
                                     Obligations obligations,
                                     Expiry expiry) throws FileNotFoundException, RmsRestAPIException {
        String retVal = "";
        if (ownerID == null || ownerID.isEmpty()) {
            return retVal;
        }
        if (file == null || !file.exists() || file.isDirectory()) {
            return retVal;
        }
        if (rights == null || obligations == null) {
            return retVal;
        }
        Rights r = new Rights();
        {
            // set r;
            r.setView(rights.hasView());
            r.setPrint(rights.hasPrint());
            r.setEdit(rights.hasEdit());
            r.setDownload(rights.hasDownload());
            r.setShare(rights.hasShare());
            r.setWatermark(rights.hasWatermark());
            r.setDecrypt(rights.hasDecrypt());
        }
        Obligations o = new Obligations();
        {
            //set o;
            o.setObligation(obligations.getObligation());
        }
        // Commnets for ownerID is null, will use current login user
        AdhocPolicy adhocPolicy = new AdhocPolicy(ownerID, r, o, expiry);
        Policy policy = new Policy(adhocPolicy);
        {
            //Build file info section.
            FileInfo fileInfo = new FileInfo.Builder()
                    .setDateCreated(System.currentTimeMillis())
                    .setDateModified(System.currentTimeMillis())
                    .setCreatedBy(ownerID)
                    .setModifiedBy(ownerID)
                    .setFileName(file.getName())
                    .setFileExtension(RenderHelper.getFileExtension(Uri.fromFile(file).toString()))
                    .build();
            policy.setFileInfo(fileInfo);
        }
        String nxlPath = NxCommonUtils.reNameNxlFile(file.getPath());
        boolean result = SkyDRMApp.getInstance().getSession().getRmsClient().encryptToNxl(
                file.getPath(),
                nxlPath,
                policy,
                true);
        return result ? nxlPath : retVal;
    }

    public static String protectFile(File file,
                                     String membershipId,
                                     Map<String, Set<String>> tags) throws FileNotFoundException, RmsRestAPIException {
        // Sanity check first.
        String retVal = "";
        if (file == null || !file.exists() || file.isDirectory()) {
            return retVal;
        }
        if (membershipId == null || membershipId.isEmpty()) {
            return retVal;
        }

        CentralPolicy centralPolicy = new CentralPolicy.Builder()
                .setMembershipId(membershipId)
                .addTags(tags)
                .build();
        Policy policy = new Policy(centralPolicy);

        String nxlPath = NxCommonUtils.reNameNxlFile(file.getPath());
        {
            //Build file info section.
            FileInfo fileInfo = new FileInfo.Builder()
                    .setDateCreated(System.currentTimeMillis())
                    .setDateModified(System.currentTimeMillis())
                    .setCreatedBy(membershipId)
                    .setModifiedBy(membershipId)
                    .setFileName(file.getName())
                    .setFileExtension(RenderHelper.getFileExtension(Uri.fromFile(file).toString()))
                    .build();
            policy.setFileInfo(fileInfo);
        }

        boolean result = SkyDRMApp.getInstance().getSession().getRmsClient()
                .encryptToNxl(file.getPath(), nxlPath, policy, true);

        return result ? nxlPath : retVal;
    }

    /**
     * get nxl file FingerPrint, then get the rights.
     * -- this is a asynchronous operation, need to display the waiting UI when read nxl file rights.
     */
    public static void readNxlFingerPrint(final Context context,
                                          final File nxlFile,
                                          final boolean bIsDisplayUi,
                                          final String viewType,
                                          final IGetFingerPrintCallback callback) {
        class ReadFingerPrintTask extends LoadTask<Void, Boolean> {
            private INxlFileFingerPrint mFingerPrint;
            private SafeProgressDialog getRightsProgressDialog = null;
            // handle exception
            private Exception mException = null;

            @Override
            protected void onPreExecute() {
                if (bIsDisplayUi) {
                    getRightsProgressDialog = SafeProgressDialog.showDialog(context, "",
                            context.getResources().getString(R.string.check_rights), true);
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {

                try {
                    mFingerPrint = SkyDRMApp.getInstance().getSession().getRmsClient().extractFingerPrint(nxlFile.getPath());
                } catch (FileNotFoundException e) {
                    mException = e;
                    return false;
                } catch (TokenAccessDenyException e) {
                    mException = e;
                    return false;
                } catch (Exception e) {
                    mException = e;
                    return false;
                }
                return mFingerPrint != null;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (getRightsProgressDialog != null) {
                    getRightsProgressDialog.dismiss();
                }
                // callback
                callback.onGetFingerPrintFinished(mFingerPrint);
                // error handle for read rights
                if (!result) {
                    if (bIsDisplayUi) {
                        if (mException != null) {
                            if (mException instanceof RmsRestAPIException)
                                GenericError.handleCommonException(context, true, (RmsRestAPIException) mException);
                            else
                                ExceptionHandler.handleException(context, mException);
                        }
                    }
                }
            }
        }
        new ReadFingerPrintTask().executeOnExecutor(ExecutorPools.SelectSmartly(NETWORK_TASK), (Void) null);
    }


    /**
     * Share local normal or nxl file.
     */
    public static void shareLocalFile(final Context context,
                                      final File file,
                                      final List<String> emails,
                                      @Nullable Rights rights,
                                      @Nullable Obligations obligations, // contains watermark
                                      final boolean bAsAttachment,
                                      @Nullable final String comment,
                                      @Nullable final Expiry expiry,
                                      final IShareFileFinish callback) {
        class Task extends AsyncTask<Void, Void, Boolean> {
            private SafeProgressDialog shareProgressDialog = null;
            private Rights mRights;
            private Obligations mObligations;
            // private String mNxlFilePath;
            private IShareFileFinish mCallback;
            // will return the duid if sharing succeed!
            private String mDuid = null;
            // handle exception
            private Exception mException = null;

            private Task(Rights rights, Obligations obligations, IShareFileFinish callback) {
                mRights = rights;
                mObligations = obligations;
                mCallback = callback;
            }

            @Override
            protected void onPreExecute() {
                if (!isCancelled()) {
                    shareProgressDialog = SafeProgressDialog.showDialog(context, "", context.getResources().getString(R.string.wait_share), true);
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
                if (!RenderHelper.isNxlFile(file.getPath())) { // ---------------------- normal file
                    // normal file
                    try {
                        // found a way to merge NxRights and NXObligations to AdhocPolicy
                        Rights r = new Rights();
                        // set r;
                        if (mRights != null) {
                            r.setView(mRights.hasView());
                            r.setDownload(mRights.hasDownload());
                            r.setEdit(mRights.hasEdit());
                            r.setPrint(mRights.hasPrint());
                            r.setShare(mRights.hasShare());
                            // added later
                            r.setWatermark(mRights.hasWatermark());
                        }

                        Obligations o = new Obligations();
                        if (mObligations != null) {
                            //set o;
                            o.setObligation(mObligations.getObligation());
                            r.setWatermark(mObligations.hasWatermark());
                        }
                        AdhocPolicy adhocPolicy = new AdhocPolicy(
                                SkyDRMApp.getInstance().getSession().getRmUser().getMembershipId(), r, o, expiry);
                        String filePathId = "Local:" + file.getName();
                        mDuid = session.getRmsClient().shareLocalPlainFileToMyVault(file.getPath(), bAsAttachment, adhocPolicy,
                                filePathId, filePathId,
                                new IRecipients() {
                                    @Override
                                    public Iterator<String> iterator() {
                                        return emails.iterator();
                                    }
                                }, comment);
                        if (mDuid == null) {
                            return false;
                        }
                    } catch (FileNotFoundException e) {
                        log.e(e);
                        mException = e;
                        return false;
                    } catch (RmsRestAPIException e) {
                        log.e(e);
                        mException = e;
                        return false;
                    } catch (Exception e) {
                        log.e(e);
                        mException = e;
                        return false;
                    }
                } else {
                    // for .nxl --- share nxl file or do re-share: don't need to pass watermark and expiry
                    try {
                        mDuid = session.getRmsClient().shareLocalNxlFileToMyVault(file.getPath(), bAsAttachment, new IRecipients() {
                            @Override
                            public Iterator<String> iterator() {
                                return emails.iterator();
                            }
                        }, comment);
                        if (mDuid == null) {
                            return false;
                        }
                    } catch (NotNxlFileException e) {
                        log.e(e);
                        mException = e;
                        return false;

                    } catch (TokenAccessDenyException e) {
                        log.e(e);
                        mException = e;
                        return false;
                    } catch (NotGrantedShareRights e) {
                        log.e(e);
                        mException = e;
                        return false;
                    } catch (RmsRestAPIException e) {
                        log.e(e);
                        mException = e;
                        return false;
                    } catch (Exception e) {
                        log.e(e);
                        mException = e;
                        return false;
                    }
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);

                if (shareProgressDialog != null) {
                    shareProgressDialog.dismiss();
                }
                if (result) {
                    mCallback.onShareFileFinished();
                } else {
                    if (context == null || ((Activity) context).isFinishing())
                        return;

                    if (mException != null && mException instanceof RmsRestAPIException) {
                        switch (((RmsRestAPIException) mException).getDomain()) {
                            case AuthenticationFailed:
                                SkyDRMApp.getInstance().getSession().sessionExpiredHandler(context);
                            case FileHasBeenRevoked:
                                ToastUtil.showToast(context, context.getResources().getString(R.string.this_file_has_been_revoked));
                                break;
                            case NetWorkIOFailed:
                                ToastUtil.showToast(context, context.getResources().getString(R.string.excep_network_unavailable));
                                break;
                            default:
                                ToastUtil.showToast(context, context.getResources().getString(R.string.excep_operation_failed));
                                break;
                        }
                    } else if (mException != null && mException instanceof NotGrantedShareRights) {
                        ToastUtil.showToast(context, context.getResources().getString(R.string.excep_not_granted_share_rights));
                    }
                }
            }

        }
        new Task(rights, obligations, callback).executeOnExecutor(ExecutorPools.SelectSmartly(NETWORK_TASK), (Void) null);
    }

    /**
     * Share repository file
     *
     * @param context
     * @param fileName      file name.
     * @param repositoryId  repository id
     * @param filePathId    file path id
     * @param filePath      file path
     * @param permissions   the rights value
     * @param emails        the email address will be shared
     * @param bAsAttachment whether sharing file as attachment
     * @param comment       the comment about share(optional)
     * @param callback      share call back.
     */
    @Deprecated
    public static void shareRepoFile(final Context context,
                                     final String fileName,
                                     final String repositoryId,
                                     final String filePathId,
                                     final String filePath,
                                     final int permissions,
                                     final List<String> emails,
                                     final boolean bAsAttachment,
                                     @Nullable final String comment,
                                     @Nullable final String watermark,
                                     @Nullable final Expiry expiry,
                                     final IShareFileFinish callback) {
        class ShareRepoTask extends AsyncTask<Void, Void, Boolean> {

            private SafeProgressDialog shareProgressDialog = null;
            // will return the duid if sharing succeed!
            private String mDuid = null;
            // handle exception
            private Exception mException = null;

            @Override
            protected void onPreExecute() {
                if (!isCancelled()) {
                    shareProgressDialog = SafeProgressDialog.showDialog(context, "", context.getResources().getString(R.string.wait_share), true);
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
                try {
                    mDuid = session.getRmsClient().shareRepoFileToMyVault(fileName,
                            bAsAttachment,
                            repositoryId,
                            filePathId,
                            filePath,
                            permissions,
                            new IRecipients() {
                                @Override
                                public Iterator<String> iterator() {
                                    return emails.iterator();
                                }
                            },
                            comment, watermark, expiry);

                } catch (FileNotFoundException e) {
                    log.e(e);
                    mException = e;
                    return false;
                } catch (RmsRestAPIException e) {
                    log.e(e);
                    mException = e;
                    return false;
                } catch (Exception e) {
                    log.e(e);
                    mException = e;
                    return false;
                }

                return mDuid != null;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (shareProgressDialog != null) {
                    shareProgressDialog.dismiss();
                }
                if (result) {
                    callback.onShareFileFinished();
                } else {
                    if (context == null || ((Activity) context).isFinishing())
                        return;

                    if (mException != null && mException instanceof RmsRestAPIException) {
                        switch (((RmsRestAPIException) mException).getDomain()) {
                            case AuthenticationFailed:
                                SkyDRMApp.getInstance().getSession().sessionExpiredHandler(context);
                            case FileHasBeenRevoked:
                                ToastUtil.showToast(context, context.getResources().getString(R.string.this_file_has_been_revoked));
                                break;
                            case FILE_EXPIRED:
                                ToastUtil.showToast(context.getApplicationContext(), mException == null ? context.getResources().getString(R.string.hint_msg_share_file_expired) : mException.getMessage());
                                break;
                            case NetWorkIOFailed:
                                ToastUtil.showToast(context, context.getResources().getString(R.string.excep_network_unavailable));
                                break;
                            default:
                                ToastUtil.showToast(context, context.getResources().getString(R.string.excep_operation_failed));
                                break;
                        }
                    }

                }
            }

        }
        new ShareRepoTask().executeOnExecutor(ExecutorPools.SelectSmartly(NETWORK_TASK), (Void) null);
    }

    /**
     * the API is used to upload a nxl file into myVault
     * --- note: when select a local file and upload into Drive(with protect selection)
     * if upload to drive successfully, then will also upload nxl file into myVault(note: will also upload the drive info into myVault at the same time!)
     * if upload to drive failed, will not upload nxl file into myVault.
     *
     * @param context
     * @param nxlFile:            Introduced into an existing file
     * @param srcFilePathDisplay: source file path (Drive)
     * @param srcFilePathId:      source file path id (Drive)
     * @param srcRepoId:          repository id (no need to pass the parameter)
     * @param srcRepoName:        rmsNickName
     * @param srcRepoType:        alias
     * @param callBack
     */
    public static void uploadFileToMyVault(final Context context,
                                           final File nxlFile,
                                           final String srcFilePathId,
                                           final String srcFilePathDisplay,
                                           final String srcRepoId,
                                           final String srcRepoName,
                                           final String srcRepoType,
                                           final boolean bDisplayUi,
                                           final IUploadToMyVaultFinish callBack) {
        MyVaultUploadFileParams params = new MyVaultUploadFileParams.Builder()
                .setNxlFile(nxlFile)
                .setSrcPathId(srcFilePathId)
                .setSrcPathDisplay(srcFilePathDisplay)
                .setSrcRepoId(srcRepoId)
                .setSrcRepoName(srcRepoName)
                .setSrcRepoType(srcRepoType)
                .build();
        log.v("uploadFileToMyVault padding params:\n" + params);
        IMyVaultCommand command = new MyVaultCommandExecutor();
        command.uploadFileToMyVault(context, params, bDisplayUi, new ICommand.ICommandExecuteCallback<Result.UploadResult, Error>() {
            @Override
            public void onInvoked(Result.UploadResult result) {
                if (callBack != null) {
                    callBack.onUploadToMyVaultFinished(result.result);
                }
            }

            @Override
            public void onFailed(Error error) {
                GenericError.handleCommonException(context,
                        true, error.mException);
            }
        });
    }

    /**
     * This method is used to upload local files to MyVaultRepo.
     * Note that the following two parameters were omitted:srcPathId & srcRepoId
     *
     * @param context            used to show uploading dialog
     * @param nxlFile            target nxl file need uploading to rms.
     * @param srcFilePathDisplay target file's display path
     * @param bDisplayUi         true means show dialog or
     * @param callBack           MyVaultFileBean upload callback
     */
    public static void uploadFileToMyVaultLocal(final Context context,
                                                final File nxlFile,
                                                final String srcFilePathDisplay,
                                                final boolean bDisplayUi,
                                                final IUploadToMyVaultFinish callBack) {
        MyVaultUploadFileParams params = new MyVaultUploadFileParams.Builder()
                .setNxlFile(nxlFile)
                .setSrcPathId("Local")
                .setSrcPathDisplay("Local")
                .setSrcRepoId("Local")
                .setSrcRepoName("Local")
                .setSrcRepoType("Local")
                .build();
        log.v("uploadFileToMyVault padding params:\n" + params);
        IMyVaultCommand command = new MyVaultCommandExecutor();
        command.uploadFileToMyVault(context, params, bDisplayUi, new ICommand.ICommandExecuteCallback<Result.UploadResult, Error>() {
            @Override
            public void onInvoked(Result.UploadResult result) {
                if (callBack != null) {
                    callBack.onUploadToMyVaultFinished(result.result);
                }
            }

            @Override
            public void onFailed(Error error) {
                log.v("uploadFileToMyVault onFailed:\n" + error.msg);
                GenericError.handleCommonException(context,
                        true, error.mException);
            }
        });
    }

    /**
     * used to download file from one project
     *
     * @param context
     * @param projectId:       download file from specific project
     * @param pathId:          file path ---- current directory + fileName, like:  /test/for_test-2017-02-09-07-44-48.xlsx.nxl"
     * @param localPath
     * @param type:            set true if client is downloading this file for rendering in viewer (user is not downloading the file).
     * @param downloadCallBack
     */
    @Deprecated
    public static void projectDownloadFile(final Context context,
                                           final int projectId,
                                           final String pathId,
                                           final String localPath,
                                           final int type,
                                           final DownloadManager.IDownloadCallBack downloadCallBack,
                                           final int... args) {

        class DownloadTask extends AsyncTask<Void, Long, Boolean> {

            private ProjectDownloadHeader projectDownloadHeader;
            private RmsRestAPIException mRmsRestAPIException;
            private boolean bCancel = false;

            @Override
            protected Boolean doInBackground(Void... params) {
                SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();

                try {
                    projectDownloadHeader = session.getRmsRestAPI().projectDownloadFile(session.getRmUser(), projectId, pathId, localPath, type, new RestAPI.DownloadListener() {
                        @Override
                        public void current(int i) {
                            publishProgress((long) i);
                        }

                        @Override
                        public void cancel() {  // cancel download(means download failed, will delete tmp file in onDownloadFinished callback)
                            bCancel = true;
                        }
                    }, args);
                    return projectDownloadHeader != null;
                } catch (RmsRestAPIException e) {
                    log.e(e);
                    mRmsRestAPIException = e;
                } catch (SessionInvalidException e) {
                    log.e(e);
                    mRmsRestAPIException = new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.AuthenticationFailed);
                } catch (Exception e) {
                    log.e(e);
                    mRmsRestAPIException = new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
                }
                return false;
            }

            @Override
            protected void onProgressUpdate(Long... values) {
                downloadCallBack.onDownloadProgress(values[0]);
            }

            @Override
            protected void onPostExecute(Boolean result) {  // actually we should return myVaultDownloadHeader here, need improve the callback.
                if (result) {
                    downloadCallBack.onDownloadFinished(true, localPath, null);
                } else {
                    if (mRmsRestAPIException != null) {

                        switch (mRmsRestAPIException.getDomain()) {
                            case AuthenticationFailed:
                                SkyDRMApp.getInstance().getSession().sessionExpiredHandler(context);
                                break;
                            case NetWorkIOFailed:
                                if (!TextUtils.equals(mRmsRestAPIException.getMessage(), "Canceled")) {
                                    ToastUtil.showToast(context, context.getResources().getString(R.string.excep_network_unavailable));
                                }
                                break;
                            case AccessDenied:
                                GenericError.showUI((Activity) context, context.getResources().getString(R.string.excep_forbidden), true, false, true, null);
                                break;
                            default:
                                ToastUtil.showToast(context, context.getResources().getString(R.string.excep_operation_failed));
                                downloadCallBack.onDownloadFinished(false, localPath, new FileDownloadException(mRmsRestAPIException.getMessage()));
                                break;
                        }
                    }

                    if (mRmsRestAPIException != null) {
                        switch (mRmsRestAPIException.getDomain()) {
                            case AuthenticationFailed:
                                SkyDRMApp.getInstance().getSession().sessionExpiredHandler(context);
                                break;
                            case NetWorkIOFailed:
                                if (!TextUtils.equals(mRmsRestAPIException.getMessage(), "Canceled")) {
                                    ToastUtil.showToast(context, context.getResources().getString(R.string.excep_network_unavailable));
                                }
                                break;
                            case AccessDenied:
                                GenericError.showUI((Activity) context, context.getResources().getString(R.string.excep_forbidden), true, false, true, null);
                                break;
                            default:
                                ToastUtil.showToast(context, context.getResources().getString(R.string.excep_operation_failed));
                                downloadCallBack.onDownloadFinished(false, localPath, new FileDownloadException(mRmsRestAPIException.getMessage()));
                                break;
                        }

                    }

                    // user cancel download
                    if (bCancel) {
                        // will delete part tmp file when user cancel it.
                        Helper.deleteFile(new File(localPath));
                    }

                }
            }
        }

        new DownloadTask().executeOnExecutor(ExecutorPools.SelectSmartly(NETWORK_TASK), (Void) null);
    }


    /**
     * The API used to upload a file into a project -- can upload both normal file and nxl file.
     * note: for nxl file, when upload it into one project, must make sure that it come from this project before (that's to say,
     * a project member can download the nxl file and then upload it back into the same project again, but if he upload it into different project, it will failed, throw tenant mismatch exception.)
     *
     * @param context
     * @param projectId:    the project id
     * @param file:         file stream
     * @param rights:       file right string, like: "VIEW" "SHARE"
     * @param parentPathId: the dest folder path that file will upload into
     * @param tags:         the style is like: tagName1=tagValue1|tagName2=tagValue2 (the value can is single or multiple, can split with comma when multiple.)
     *                      Classification=ITAR|Clearance=Confidiential,Top Secret
     */
    public static void projectUploadFile(final Context context,
                                         final int projectId,
                                         final File file,
                                         final List<String> rights,
                                         final String parentPathId,
                                         final @Nullable String tags,
                                         final IUploadToProjectFile callback) {

        class UploadTask extends AsyncTask<Void, Void, Boolean> {
            private UploadFileResult uploadFileResult;
            private SafeProgressDialog mProgressDialog = null;
            private RmsRestAPIException mRmsRestAPIException;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (context != null && !((Activity) context).isFinishing()) {
                    mProgressDialog = SafeProgressDialog.showDialog(context, "", context.getResources().getString(R.string.wait_upload), true);
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    uploadFileResult = uploadProjectFile(projectId, file, parentPathId, new ProgressRequestListener() {
                        @Override
                        public void onRequestProgress(long bytesWritten, long contentLength, boolean done) throws IOException {

                        }
                    });
                    return uploadFileResult != null;
                } catch (RmsRestAPIException e) {
                    log.e(e);
                    mRmsRestAPIException = e;
                } catch (SessionInvalidException | InvalidRMClientException e) {
                    log.e(e);
                    mRmsRestAPIException = new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.AuthenticationFailed);
                } catch (Exception e) {
                    log.e(e);
                    mRmsRestAPIException = new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
                if (result) {
                    // send log
                    LogSystem.sendProtectLog(null, false, file, true);
                    callback.onUploadToProjectFinished(uploadFileResult);
                } else {
                    releaseTmpFile();
                    GenericError.handleCommonException(context, true, mRmsRestAPIException);
                }
            }

            private void releaseTmpFile() {
                //when upload nxl file to project success,clear tmp nxl file in local.
                String path = file.getPath();
                if (!TextUtils.isEmpty(path) && path.endsWith(".nxl")) {
                    FileHelper.delFile(file.getPath());
                }
            }
        }
        new UploadTask().executeOnExecutor(ExecutorPools.SelectSmartly(NETWORK_TASK), (Void) null);
    }


    public static UploadFileResult uploadProjectFile(int projectId,
                                                     @NonNull File file,
                                                     @NonNull String parentPathId,
                                                     @Nullable ProgressRequestListener listener)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        UploadForNXLFileParam param = new UploadForNXLFileParam();
        UploadForNXLFileParam.ParametersBean parametersBean = new UploadForNXLFileParam.ParametersBean();
        parametersBean.setName(file.getName());
        parametersBean.setParentPathId(parentPathId);
        parametersBean.setType(0);
        param.setParameters(parametersBean);
        if (listener == null) {
            listener = new ProgressRequestListener() {
                @Override
                public void onRequestProgress(long bytesWritten, long contentLength, boolean done) throws IOException {

                }
            };
        }
        return session.getRmsRestAPI().getProjectService(session.getRmUser())
                .uploadNXLFile(projectId, param, file, listener);
    }

    /**
     * The API used to get one project metaData
     *
     * @param context
     * @param projectId: the project id
     */
    @Deprecated
    public static void getProjectMetaData(final Context context,
                                          final int projectId,
                                          final IProjectMetaData projectMetaData) {
        class ProjectDataTask extends AsyncTask<Void, Void, Boolean> {
            private ProjectMetaDataResult projectMetaDataResult;
            private RmsRestAPIException e;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
                    projectMetaDataResult = session.getRmsRestAPI().getProjectMetaData(session.getRmUser(), projectId);
                } catch (RmsRestAPIException e) {
                    log.e(e);
                    this.e = e;
                } catch (SessionInvalidException | InvalidRMClientException e) {
                    log.e(e);
                    this.e = new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.AuthenticationFailed);
                } catch (Exception e) {
                    log.e(e);
                    this.e = new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
                }
                return projectMetaDataResult != null;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                if (result) {
                    projectMetaData.onProjectMetaDataFinished(projectMetaDataResult);
                }
                if (null != e) {
                    projectMetaData.onError(e);
                }
            }
        }
        new ProjectDataTask().executeOnExecutor(ExecutorPools.SelectSmartly(NETWORK_TASK), (Void) null);
    }

    /**
     * used to accept a invitation.
     * note: the invitationId and code can get by listUserPendingInvitations.
     *
     * @param context
     * @param invitationId:      invitation id
     * @param code:
     * @param iAcceptInvitation: callback
     */
    public static void projectAcceptInvitation(final Context context,
                                               final int invitationId,
                                               final String code,
                                               final IAcceptInvitation iAcceptInvitation) {
        class AcceptInvitationTask extends AsyncTask<Void, Void, Boolean> {
            private RmsRestAPIException mRmsRestAPIException;
            private String mProjectId;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
                    String responseStr = session.getRmsRestAPI()
                            .getProjectService(session.getRmUser())
                            .acceptInvitation(invitationId, code);

                    JSONObject responseObj = new JSONObject(responseStr);
                    JSONObject results = responseObj.getJSONObject("results");

                    IRmUser rmUser = SkyDRMApp.getInstance().getSession().getRmUser();

                    if (null != rmUser) {
                        String id = results.optString("id");
                        int type = results.optInt("type");
                        String tokenGroupName = results.optString("tokenGroupName");
                        int projectId = results.optInt("projectId");

                        rmUser.updateOrInsertMembershipItem(new ProjectMemberShip(id,
                                type, tokenGroupName, projectId));
                    }

                    mProjectId = results.getString("projectId");
                    return !TextUtils.isEmpty(mProjectId);
                } catch (RmsRestAPIException e) {
                    mRmsRestAPIException = e;
                } catch (SessionInvalidException | InvalidRMClientException e) {
                    log.e(e);
                    mRmsRestAPIException = new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.AuthenticationFailed);
                } catch (Exception e) {
                    log.e(e);
                    mRmsRestAPIException = new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                if (mRmsRestAPIException != null) {
                    iAcceptInvitation.onError(mRmsRestAPIException);
                    if (context == null || ((Activity) context).isFinishing())
                        return;
                    switch (mRmsRestAPIException.getDomain()) {
                        case AuthenticationFailed:
                            SkyDRMApp.getInstance().getSession().sessionExpiredHandler(context);
                            break;
                        case NetWorkIOFailed:
                            ToastUtil.showToast(context, context.getResources().getString(R.string.excep_network_unavailable));
                            break;
                        case InvitationExpired:
                            ToastUtil.showToast(context, context.getResources().getString(R.string.excep_invitation_expired));
                            break;
                        case InvitationAlreadyDeclined:
                            ToastUtil.showToast(context, context.getResources().getString(R.string.excep_invitation_declined));
                            break;
                        case InvitationAlreadyAccepted:
                            ToastUtil.showToast(context, context.getResources().getString(R.string.excep_invitation_accepted));
                            break;
                        case EmailNotMatched:
                            ToastUtil.showToast(context, context.getResources().getString(R.string.email_address_mismatch));
                            break;
                        default:
                            ToastUtil.showToast(context, context.getResources().getString(R.string.excep_operation_failed));
                            break;
                    }
                }
                iAcceptInvitation.onAcceptInvitation(mProjectId);
            }
        }
        new AcceptInvitationTask().executeOnExecutor(ExecutorPools.SelectSmartly(NETWORK_TASK), (Void) null);
    }

    /**
     * used to decline a invitation
     * note: the invitationId and code can get by listUserPendingInvitations.
     *
     * @param context
     * @param invitationId:     invitation id
     * @param code
     * @param declineReason:    the reason for decline invitation
     * @param ignoreInvitation: callback
     */
    public static void projectIgnoreInvitation(final Context context,
                                               final int invitationId,
                                               final String code,
                                               final @Nullable String declineReason,
                                               final IIgnoreInvitation ignoreInvitation) {
        class IgnoreInvitationTask extends AsyncTask<Void, Void, Boolean> {
            private RmsRestAPIException mRmsRestAPIException;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
                    return session.getRmsRestAPI()
                            .getProjectService(session.getRmUser())
                            .denyInvitation(invitationId, code, declineReason);
                } catch (RmsRestAPIException e) {
                    e.printStackTrace();
                    mRmsRestAPIException = e;
                } catch (SessionInvalidException e) {
                    log.e(e);
                    mRmsRestAPIException = new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.AuthenticationFailed);
                } catch (Exception e) {
                    log.e(e);
                    mRmsRestAPIException = new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                if (mRmsRestAPIException != null) {
                    ignoreInvitation.onError(mRmsRestAPIException);
                    if (context == null || ((Activity) context).isFinishing())
                        return;
                    switch (mRmsRestAPIException.getDomain()) {
                        case AuthenticationFailed:
                            SkyDRMApp.getInstance().getSession().sessionExpiredHandler(context);
                            break;
                        case NetWorkIOFailed:
                            ToastUtil.showToast(context, context.getResources().getString(R.string.excep_network_unavailable));
                            break;
                        case InvitationExpired:
                            ToastUtil.showToast(context, context.getResources().getString(R.string.excep_invitation_expired));
                            break;
                        case InvitationAlreadyDeclined:
                            ToastUtil.showToast(context, context.getResources().getString(R.string.excep_invitation_declined));
                            break;
                        case InvitationAlreadyAccepted:
                            ToastUtil.showToast(context, context.getResources().getString(R.string.excep_invitation_accepted));
                            break;
                        case InvitationAlreadyRevoked:
                            ToastUtil.showToast(context, context.getResources().getString(R.string.excep_invitation_revoked));
                            break;
                        default:
                            ToastUtil.showToast(context, context.getResources().getString(R.string.excep_operation_failed));
                            break;
                    }
                }
                ignoreInvitation.onIgnoreInvitation(result);
            }
        }
        new IgnoreInvitationTask().executeOnExecutor(ExecutorPools.SelectSmartly(NETWORK_TASK), (Void) null);
    }

    /**
     * used to download shared file with me, including: email share link and files shared with me.
     *
     * @param context          context
     * @param file             local
     * @param transactionCode  shared with me transactionCode
     * @param transactionId    shared with me transactionId
     * @param bForViewer       should set it as true when user click "view icon" to view file, in this case, ignoring the check for download rights
     *                         should set it as false when user click "download icon" to download file(for android client, now not has this icon).
     * @param bForShareLink    judge if is email shared me link or not.
     * @param downloadCallBack download callback
     */
    public static void sharedWithMeDownloadFile(final Context context,
                                                final String transactionId,
                                                final String transactionCode,
                                                final boolean bForViewer,
                                                final File file,
                                                final int start,
                                                final int length,
                                                final boolean bForShareLink,
                                                final DownloadManager.IDownloadCallBack downloadCallBack) {

        class DownloadTask extends AsyncTask<Void, Long, Boolean> {
            private String mFileName;
            private RmsRestAPIException mRmsRestAPIException;
            private boolean bCancel = false;

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
                    mFileName = session.getRmsRestAPI().getSharedWithMeService(session.getRmUser())
                            .download(transactionId, transactionCode, bForViewer, file, start, length, new RestAPI.DownloadListener() {
                                @Override
                                public void current(int i) {
                                    publishProgress((long) i);
                                }

                                @Override
                                public void cancel() {
                                    bCancel = true;
                                }
                            });

                } catch (RmsRestAPIException e) {
                    log.e(e);
                    mRmsRestAPIException = e;
                } catch (SessionInvalidException e) {
                    log.e(e);
                    mRmsRestAPIException = new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.AuthenticationFailed);
                } catch (Exception e) {
                    log.e(e);
                    mRmsRestAPIException = new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
                }
                return mRmsRestAPIException == null;
            }

            @Override
            protected void onProgressUpdate(Long... values) {
                downloadCallBack.onDownloadProgress(values[0]);
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (result) { // succeed
                    if (bForShareLink) { // shared link
                        String tmpDir = file.getPath().substring(0, file.getPath().lastIndexOf("/"));
                        String rightFilePath = tmpDir + "/" + mFileName;
                        downloadCallBack.onDownloadFinished(true, rightFilePath, null);
                    } else { // files shared with me
                        downloadCallBack.onDownloadFinished(true, file.getAbsolutePath(), null);
                    }
                } else {
                    if (mRmsRestAPIException != null) {
                        switch (mRmsRestAPIException.getDomain()) {
                            case AuthenticationFailed:
                                SkyDRMApp.getInstance().getSession().sessionExpiredHandler(context);
                                break;
                            case NetWorkIOFailed:
                                GenericError.showUI((Activity) context, context.getResources().getString(R.string.excep_network_unavailable), true, false, true, null);
                                break;
                            case AccessDenied:
                                GenericError.showUI((Activity) context, context.getResources().getString(R.string.excep_share_link_denied_access), true, false, true, null);
                                break;
                            default:
                                GenericError.showUI((Activity) context, context.getResources().getString(R.string.excep_operation_failed), true, false, true, null);
                                break;
                        }

                    }
                }
            }
        }

        new DownloadTask().executeOnExecutor(ExecutorPools.SelectSmartly(FIRED_BY_UI), (Void) null);
    }

    public static void downloadNxlFile(final Context context,
                                       final int type,
                                       final INxlFile f,
                                       final DownloadManager.IDownloadCallBack downloadCallBack) {

        class DownloadTask extends AsyncTask<Void, Long, Boolean> {
            private RmsRestAPIException mRmsRestAPIException;
            private boolean bCancel = false;

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    f.download(type, new INxlFile.DownloadListener() {
                        @Override
                        public void onProgress(int i) {
                            publishProgress((long) i);
                        }

                        @Override
                        public void onComplete() {
                        }

                        @Override
                        public void cancel() {
                            bCancel = true;
                        }
                    });
                } catch (RmsRestAPIException e) {
                    mRmsRestAPIException = e;
                } catch (SessionInvalidException e) {
                    mRmsRestAPIException = new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.AuthenticationFailed);
                } catch (Exception e) {
                    mRmsRestAPIException = new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
                }
                return mRmsRestAPIException == null;
            }

            @Override
            protected void onProgressUpdate(Long... values) {
                downloadCallBack.onDownloadProgress(values[0]);
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (result) { // succeed
                    downloadCallBack.onDownloadFinished(true,
                            ((NxlDoc) f).getLocalPath(), null);
                } else {
                    if (mRmsRestAPIException != null) {
                        switch (mRmsRestAPIException.getDomain()) {
                            case AuthenticationFailed:
                                SkyDRMApp.getInstance().getSession().sessionExpiredHandler(context);
                                break;
                            case NetWorkIOFailed:
                                GenericError.showUI((Activity) context, context.getResources().getString(R.string.excep_network_unavailable),
                                        true, false, true, null);
                                break;
                            case AccessDenied:
                                GenericError.showUI((Activity) context, context.getResources().getString(R.string.excep_share_link_denied_access),
                                        true, false, true, null);
                                break;
                            case FileNotFound:
                                ExceptionHandler.handleException(context, mRmsRestAPIException);
                                break;
                            default:
                                GenericError.showUI((Activity) context, context.getResources().getString(R.string.excep_operation_failed),
                                        true, false, true, null);
                                break;
                        }
                    }
                }
            }
        }
        new DownloadTask().executeOnExecutor(ExecutorPools.SelectSmartly(FIRED_BY_UI), (Void) null);
    }
    // **************************************************  interface ***************************************************

    // -------------- file operate interface ----------------

    /**
     * protect file finish callback
     */
    public interface IProtectFileFinish {
        void onProtectFinished(String nxlPath);
    }

    public interface IProtectFileCallback {
        void onFileProtecting();

        void onFileProtected(String nxlFilePath);

        void onFileProtectError(String message);
    }

    /**
     * get fingerPrint finish callback
     */
    public interface IGetFingerPrintCallback {
        void onGetFingerPrintFinished(INxlFileFingerPrint fingerPrint);
    }

    /**
     * share file finish callback
     */
    public interface IShareFileFinish {
        void onShareFileFinished();
    }

    /**
     * upload to myVault callback
     */
    public interface IUploadToMyVaultFinish {
        void onUploadToMyVaultFinished(MyVaultUploadFileResult result);
    }


    // ------------------ project interface ---------------------------

    /**
     * get project metaData
     */
    public interface IProjectMetaData {
        void onProjectMetaDataFinished(ProjectMetaDataResult result);

        void onError(RmsRestAPIException e);
    }

    /**
     * upload file to project callback
     */
    public interface IUploadToProjectFile {
        void onUploadToProjectFinished(UploadFileResult result);
    }

    public interface IAcceptInvitation {
        void onAcceptInvitation(String projectId);

        void onError(RmsRestAPIException e);
    }

    public interface IIgnoreInvitation {
        void onIgnoreInvitation(boolean result);

        void onError(RmsRestAPIException e);
    }
    // -------------- project end ------------------------
}
