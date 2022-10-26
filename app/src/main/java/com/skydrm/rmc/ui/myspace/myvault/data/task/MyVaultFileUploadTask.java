package com.skydrm.rmc.ui.myspace.myvault.data.task;

import android.content.Context;

import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.reposystem.localrepo.helper.Helper;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.rmc.ui.myspace.myvault.ICommand;
import com.skydrm.rmc.ui.myspace.myvault.data.Error;
import com.skydrm.rmc.ui.myspace.myvault.data.Result;
import com.skydrm.rmc.ui.widget.customcontrol.SafeProgressDialog;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.rest.myVault.MyVaultUploadFileParams;
import com.skydrm.sdk.rms.rest.myVault.MyVaultUploadFileResult;
import com.skydrm.sdk.ui.uploadprogress.ProgressRequestListener;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by hhu on 4/28/2018.
 */

public class MyVaultFileUploadTask extends LoadTask<Void, MyVaultUploadFileResult> {
    private static final DevLog log = new DevLog(MyVaultFileUploadTask.class.getSimpleName());
    private WeakReference<Context> mContext;
    private boolean bDisplayUi;
    private MyVaultUploadFileParams mUploadPrams;
    private ICommand.ICommandExecuteCallback<Result.UploadResult, Error> mCallBack;
    private SafeProgressDialog mProgressDialog;
    private RmsRestAPIException mRmsRestAPIExcep;

    public MyVaultFileUploadTask(Context context,
                                 MyVaultUploadFileParams uploadPrams,
                                 boolean bDisplayUi,
                                 ICommand.ICommandExecuteCallback<Result.UploadResult, Error> callBack) {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
        this.mContext = new WeakReference<>(context);
        this.mUploadPrams = uploadPrams;
        this.bDisplayUi = bDisplayUi;
        this.mCallBack = callBack;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (bDisplayUi) {
            Context context = checkNoNull(mContext);
            mProgressDialog = SafeProgressDialog.showDialog(context,
                    "", context.getResources().getString(R.string.wait_upload),
                    true);
        }
    }

    @Override
    protected MyVaultUploadFileResult doInBackground(Void... voids) {
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        try {
            return session.getRmsRestAPI().getMyVaultService(session.getRmUser()).uploadFileToMyVault(mUploadPrams,
                    new ProgressRequestListener() {
                        @Override
                        public void onRequestProgress(long bytesWritten, long contentLength,
                                                      boolean done) throws IOException {
                            log.i("In uploadFileToMyVault:\nbytesWritten: ="
                                    + bytesWritten
                                    + "\tcontentLength =" + contentLength
                                    + "\tdone =" + done);
                        }
                    });
        } catch (RmsRestAPIException e) {
            this.mRmsRestAPIExcep = e;
        } catch (InvalidRMClientException e) {
            this.mRmsRestAPIExcep = new RmsRestAPIException(e.getMessage(),
                    RmsRestAPIException.ExceptionDomain.Common);
        } catch (SessionInvalidException e) {
            this.mRmsRestAPIExcep = new RmsRestAPIException(e.getMessage(),
                    RmsRestAPIException.ExceptionDomain.AuthenticationFailed);
        }
        return null;
    }

    @Override
    protected void onPostExecute(MyVaultUploadFileResult result) {
        super.onPostExecute(result);
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        if (result != null) {
            if (mCallBack != null) {
                mCallBack.onInvoked(new Result.UploadResult(result));
            }
        } else {
            releaseTmpFile();
            if (mRmsRestAPIExcep != null) {
                if (mCallBack != null) {
                    mCallBack.onFailed(new Error(mRmsRestAPIExcep, mRmsRestAPIExcep.getMessage()));
                }
            }
        }
    }

    private void releaseTmpFile() {
        try {
            Helper.deleteFile(mUploadPrams.getNxlFile());
        } catch (Exception e) {
            log.e("try to release resource failed:\n" + e.getMessage());
        }
    }
}
