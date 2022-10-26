package com.skydrm.rmc.ui.fragment.protect;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.reposystem.IRemoteRepo;
import com.skydrm.rmc.reposystem.exception.FileUploadException;
import com.skydrm.rmc.reposystem.remoterepo.ICancelable;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.reposystem.types.NXDocument;
import com.skydrm.rmc.ui.base.IDisplayWatermark;
import com.skydrm.rmc.ui.fragment.CmdAddFragment;
import com.skydrm.rmc.ui.fragment.IViewBuilder;
import com.skydrm.rmc.ui.fragment.task.ProtectTask;
import com.skydrm.rmc.ui.widget.LoadingDialog;
import com.skydrm.rmc.ui.widget.LoadingDialog2;
import com.skydrm.rmc.ui.widget.customcontrol.rights.ADHocRightsSelectView;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.policy.Expiry;
import com.skydrm.sdk.policy.Obligations;
import com.skydrm.sdk.policy.Rights;
import com.skydrm.sdk.rms.rest.user.User;

import java.io.File;

public class NormalViewBuilder implements IViewBuilder {
    private File mWorkingFile;
    private ADHocRightsSelectView mADHocRightsSelectView;

    private Context mCtx;
    private View mRoot;
    private INxFile mRepoFile;
    private BoundService mBoundService;
    private INxFile mDestFolder;
    private boolean protectThenUpload;

    private ProtectCallback mProtectCallback;
    private UploadCallback mUploadCallback;
    private LoadingDialog2 mLoadingDialog;

    public NormalViewBuilder(Context ctx, View root, INxFile repoFile) {
        this.mCtx = ctx;
        this.mRoot = root;
        this.mRepoFile = repoFile;
    }

    public NormalViewBuilder(Context ctx, View root, BoundService boundService, INxFile destFolder) {
        this.mCtx = ctx;
        this.mRoot = root;
        this.mBoundService = boundService;
        this.mDestFolder = destFolder;
        protectThenUpload = true;
    }

    @Override
    public View buildRoot(Context ctx) {
        mADHocRightsSelectView = new ADHocRightsSelectView(ctx);
        return mADHocRightsSelectView;
    }

    @Override
    public void showLoading(int type) {

    }

    @Override
    public void hideLoading(int type) {

    }

    @Override
    public void bindFingerPrint(INxlFileFingerPrint fingerPrint, File workingFile, boolean nxl) {
        mWorkingFile = workingFile;
        mADHocRightsSelectView.setExpiry(SkyDRMApp.getInstance().getSession().getUserPreference().getExpiry());
    }

    @Override
    public void configureShareOrProtectButton(Button button) {
        if (button == null) {
            return;
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                protectFile();
            }
        });
    }

    @Override
    public void updateExpiry(User.IExpiry expiry) {
        mADHocRightsSelectView.setExpiry(expiry);
    }

    @Override
    public void wrapContacts(Intent data) {

    }

    @Override
    public void onReleaseResource() {
        if (mProtectCallback != null) {
            mProtectCallback = null;
        }
        if (mUploadCallback != null) {
            mUploadCallback = null;
        }
    }

    private void protectFile() {
        String plainPath = "";
        if (mWorkingFile != null) {
            plainPath = mWorkingFile.getPath();
        }
        Rights rights = mADHocRightsSelectView.getRights();
        Obligations obligations = mADHocRightsSelectView.getObligations();
        Expiry expiry = mADHocRightsSelectView.getExpiry();
        boolean expired = mADHocRightsSelectView.isExpired();
        // When protect file is user choose a past period is not allowed.
        if (expired) {
            return;
        }

        mProtectCallback = new ProtectCallback();

        ProtectTask task = new ProtectTask(plainPath,
                rights, obligations, expiry,
                mRepoFile,
                mProtectCallback);
        task.run();
    }

    class ProtectCallback implements ProtectTask.ITaskCallback<ProtectTask.Result, Exception> {

        @Override
        public void onTaskPreExecute() {
            showLoadingDialog();
        }

        @Override
        public void onTaskExecuteSuccess(ProtectTask.Result results) {
            dismissLoadingDialog();

            if (protectThenUpload) {
                uploadFile();
                return;
            }
            if (mCtx == null || mRoot == null || mWorkingFile == null) {
                return;
            }
            CommonUtils.popupProtectSucceedTip(mCtx, mRoot, mWorkingFile.getName());
        }

        @Override
        public void onTaskExecuteFailed(Exception e) {
            dismissLoadingDialog();
            ExceptionHandler.handleException(mCtx, e);
        }
    }

    class UploadCallback implements IRemoteRepo.IUploadFileCallback {

        @Override
        public void cancelHandler(ICancelable handler) {

        }

        @Override
        public void onFinishedUpload(boolean taskStatus, @Nullable NXDocument uploadedDoc,
                                     @Nullable FileUploadException e) {
            dismissLoadingDialog();

            if (taskStatus) {
                if (mCtx == null || mRoot == null || mWorkingFile == null) {
                    return;
                }
                CommonUtils.popupProtectSucceedTip(mCtx, mRoot, mWorkingFile.getName());

            } else {
                String errorMsg = mCtx.getString(R.string.Upload_file_failed_no_period);
                if (e != null) {
                    errorMsg += ", " + e.getMessage() + ".";
                }
                ToastUtil.showToast(mCtx, errorMsg);
            }
        }

        @Override
        public void progressing(long newValue) {

        }
    }

    private void uploadFile() {
        showLoadingDialog();
        mUploadCallback = new UploadCallback();
        try {
            SkyDRMApp.getInstance().getRepoSystem().uploadFile(mBoundService,
                    mDestFolder,
                    mWorkingFile.getName(),
                    mWorkingFile, mUploadCallback);
        } catch (FileUploadException e) {
            e.printStackTrace();

            dismissLoadingDialog();
            ExceptionHandler.handleException(mCtx, e);
        }
    }

    private void showLoadingDialog() {
        mLoadingDialog = LoadingDialog2.newInstance();
        mLoadingDialog.showModalDialog(mCtx);
    }

    private void dismissLoadingDialog() {
        if (mLoadingDialog != null) {
            mLoadingDialog.dismissDialog();
            mLoadingDialog = null;
        }
    }
}
