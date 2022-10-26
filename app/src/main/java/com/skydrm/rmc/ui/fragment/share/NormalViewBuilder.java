package com.skydrm.rmc.ui.fragment.share;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.engine.eventBusMsg.ShareCompleteNotifyEvent;
import com.skydrm.rmc.exceptions.ExceptionDialog;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.reposystem.IRemoteRepo;
import com.skydrm.rmc.reposystem.exception.FileUploadException;
import com.skydrm.rmc.reposystem.remoterepo.ICancelable;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.reposystem.types.NXDocument;
import com.skydrm.rmc.ui.fragment.IViewBuilder;
import com.skydrm.rmc.ui.fragment.task.ShareTask;
import com.skydrm.rmc.ui.widget.LoadingDialog;
import com.skydrm.rmc.ui.widget.LoadingDialog2;
import com.skydrm.rmc.ui.widget.customcontrol.rights.ADHocRightsSelectView;
import com.skydrm.rmc.ui.widget.customcontrol.share.ShareView;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.policy.Expiry;
import com.skydrm.sdk.policy.Obligations;
import com.skydrm.sdk.policy.Rights;
import com.skydrm.sdk.rms.rest.user.User;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.List;

public class NormalViewBuilder implements IViewBuilder {
    private ADHocRightsSelectView mADHocRightsSelectView;
    private ShareView mShareView;

    private File mWorkingFile;
    private BoundService mBoundService;
    private INxFile mDestFolder;

    private ShareCallback mShareCallback;
    private UploadCallback mUploadCallback;

    private Context mCtx;
    private View mMainSubLayout;
    private LoadingDialog2 mLoadingDialog;

    private boolean shareThenUpload;

    public NormalViewBuilder(Context ctx, View subLayout) {
        this.mCtx = ctx;
        this.mMainSubLayout = subLayout;
    }

    public NormalViewBuilder(Context ctx, View root, BoundService boundService, INxFile destFolder) {
        this.mCtx = ctx;
        this.mMainSubLayout = root;
        this.mBoundService = boundService;
        this.mDestFolder = destFolder;
        shareThenUpload = true;
    }

    @Override
    public View buildRoot(Context ctx) {
        if (ctx == null) {
            return null;
        }
        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        mADHocRightsSelectView = new ADHocRightsSelectView(ctx);
        mADHocRightsSelectView.setExpiry(SkyDRMApp.getInstance().getSession().getUserPreference().getExpiry());

        root.addView(mADHocRightsSelectView);

        mShareView = new ShareView(ctx);
        root.addView(mShareView);
        return root;
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
    }

    @Override
    public void configureShareOrProtectButton(Button button) {
        if (button == null) {
            return;
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareFile();
            }
        });
    }

    @Override
    public void updateExpiry(User.IExpiry expiry) {
        if (mADHocRightsSelectView != null) {
            mADHocRightsSelectView.setExpiry(expiry);
        }
    }

    @Override
    public void wrapContacts(Intent data) {
        if (mShareView != null) {
            mShareView.wrapContactParcel(data);
        }
    }

    @Override
    public void onReleaseResource() {
        if (mShareCallback != null) {
            mShareCallback = null;
        }
        if (mUploadCallback != null) {
            mUploadCallback = null;
        }
    }

    private void shareFile() {
        if (mCtx == null) {
            return;
        }
        if (!mShareView.tryShare()) {
            return;
        }

        boolean expired = mADHocRightsSelectView.isExpired();
        if (expired) {
            return;
        }

        Rights rights = mADHocRightsSelectView.getRights();
        Obligations obligations = mADHocRightsSelectView.getObligations();
        Expiry expiry = mADHocRightsSelectView.getExpiry();

        List<String> validEmailList = mShareView.getValidEmailList();
        String comments = mShareView.getComments();

        mShareCallback = new ShareCallback();
        ShareTask task = new ShareTask(mWorkingFile,
                rights, obligations, expiry,
                validEmailList, comments, mShareCallback);
        task.run();
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
            dismissLoadingDialog();
            ExceptionHandler.handleException(mCtx, e);
        }
    }

    class ShareCallback implements ShareTask.ITaskCallback<ShareTask.Result, Exception> {

        @Override
        public void onTaskPreExecute() {
            showLoadingDialog();
        }

        @Override
        public void onTaskExecuteSuccess(ShareTask.Result results) {
            dismissLoadingDialog();

            // hidden soft-keyboard
            InputMethodManager imm = (InputMethodManager) mCtx.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                // the first para is windowToken, which can be any current view's window token.
                imm.hideSoftInputFromWindow(mShareView.getWindowToken(), 0);
            }

            EventBus.getDefault().post(new ShareCompleteNotifyEvent());

            if (shareThenUpload) {
                uploadFile();
                return;
            }

            CommonUtils.popupShareSucceedTip(mCtx, mWorkingFile.getName(), mMainSubLayout,
                    mShareView.getValidEmailList(), false);

        }

        @Override
        public void onTaskExecuteFailed(Exception e) {
            e.printStackTrace();

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
                if (mCtx == null || mMainSubLayout == null || mWorkingFile == null) {
                    return;
                }

                CommonUtils.popupShareSucceedTip(mCtx, mWorkingFile.getName(), mMainSubLayout,
                        mShareView.getValidEmailList(), false);

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

    private void showLoadingDialog() {
        if (mCtx == null) {
            return;
        }
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
