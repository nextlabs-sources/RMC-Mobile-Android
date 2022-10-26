package com.skydrm.rmc.ui.service.protect.view;


import android.content.Context;
import android.view.View;

import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.repo.workspace.WorkSpaceRepo;
import com.skydrm.rmc.engine.Render.RenderHelper;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.ui.common.IDestroyable;
import com.skydrm.rmc.ui.service.protect.IProtectFile;
import com.skydrm.rmc.ui.service.protect.IProtectService;
import com.skydrm.rmc.ui.service.protect.IViewBuilder;
import com.skydrm.rmc.ui.service.protect.ProtectActivity;
import com.skydrm.rmc.ui.widget.LoadingDialog2;
import com.skydrm.rmc.ui.widget.customcontrol.rights.RightsSpecifyView;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.policy.Expiry;
import com.skydrm.sdk.policy.Obligations;
import com.skydrm.sdk.policy.Rights;
import com.skydrm.sdk.rms.rest.user.User;
import com.skydrm.sdk.rms.user.IRmUser;

import java.io.File;
import java.util.Map;
import java.util.Set;

public class NormalViewBuilder implements IViewBuilder, IDestroyable {
    private Context mCtx;
    private View mRoot;

    private IProtectService mService;
    private IProtectFile mFile;
    private RightsSpecifyView mRightsSpecifyView;

    private File mWorkingFile;
    private boolean bIsNxl;
    private String mParentPathId;

    private LoadingDialog2 mLoadingDialog;
    private AddFileCallback mAddFileCallback;

    public NormalViewBuilder(Context ctx, View root,
                             IProtectService service,
                             IProtectFile file) {
        this.mCtx = ctx;
        this.mRoot = root;
        this.mService = service;
        this.mFile = file;
    }

    @Override
    public boolean isPreviewNeeded() {
        return false;
    }

    @Override
    public View buildRoot(Context ctx) {
        mRightsSpecifyView = new RightsSpecifyView(ctx);
        if (isAdHocDisabled()) {
            mRightsSpecifyView.changeIntoSelectTagModeOnly();
        }
        mRightsSpecifyView.setExpiry(mService.getIExpiry());
        mRightsSpecifyView.setWatermark(mService.getWatermark());
        mRightsSpecifyView.setTagsThenInvalidate(mService.getClassificationRaw());

        return mRightsSpecifyView;
    }

    @Override
    public void showLoading(int type) {

    }

    @Override
    public void hideLoading(int type) {

    }

    @Override
    public void bindFingerPrint(INxlFileFingerPrint fp, File f, boolean nxl) {
        if (f == null) {
            return;
        }
        this.mWorkingFile = f;
        this.bIsNxl = nxl;
    }

    @Override
    public void updateExpiry(User.IExpiry expiry) {
        if (mRightsSpecifyView != null) {
            mRightsSpecifyView.setExpiry(expiry);
        }
    }

    @Override
    public void updateExtractStatus(boolean checked) {
        if (mRightsSpecifyView != null) {
            mRightsSpecifyView.setSwExtractChecked(checked);
        }
    }

    @Override
    public void updateParentPath(String parentPathId) {
        this.mParentPathId = parentPathId;
    }

    @Override
    public void onAddFilePerformed() {
        if (mRightsSpecifyView.isADHocSelect()) {
            addFileWithADHocRights();
        } else {
            addFileWithClassifications();
        }
    }

    @Override
    public boolean needInterceptBackPress() {
        return false;
    }

    @Override
    public void interceptBackPress() {
        popupFragment();
    }

    @Override
    public void onReleaseResource() {

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

    protected void showToast(String msg) {
        if (mCtx == null) {
            return;
        }
        ToastUtil.showToast(mCtx, msg);
    }

    protected void showToast(int resId) {
        if (mCtx == null) {
            return;
        }
        ToastUtil.showToast(mCtx, resId);
    }

    private void popupFragment() {
        if (mCtx == null) {
            return;
        }
        if (mCtx instanceof ProtectActivity) {
            ProtectActivity activity = (ProtectActivity) mCtx;
            activity.popup();
        }
    }

    private void addFileWithADHocRights() {
        if (mRightsSpecifyView == null || mService == null || mWorkingFile == null) {
            return;
        }

        if (isNxlFile()) {
            showToast(R.string.hint_msg_deny_upload_nxl_file_to_project);
            return;
        }

        Rights rights = mRightsSpecifyView.getADHocRights();
        Obligations obligations = mRightsSpecifyView.getADHocWatermarkInfo();
        Expiry expiry = mRightsSpecifyView.getADHocExpiry();
        boolean expired = mRightsSpecifyView.isADHocRightsExpired();

        if (expired) return;

        if (mAddFileCallback == null) {
            mAddFileCallback = new AddFileCallback();
        }

        mService.protect(mWorkingFile.getPath(),
                rights, obligations, expiry,
                mParentPathId,
                mAddFileCallback);
    }

    private void addFileWithClassifications() {
        if (mRightsSpecifyView == null || mService == null) {
            return;
        }

        if (isNxlFile()) {
            showToast(R.string.hint_msg_deny_upload_nxl_file_to_project);
            return;
        }

        boolean allow = mRightsSpecifyView.checkCentralMandatory();
        if (allow) {
            Map<String, Set<String>> tag = mRightsSpecifyView.getCentralSelectedTags();
            mAddFileCallback = new AddFileCallback();
            mService.protect(mWorkingFile.getPath(), tag, mParentPathId, mAddFileCallback);
        }
    }

    /**
     * Judge current file if is nxl file.
     * Note: for Add,can invoke function to judge, for Protect & Share(file may is not in local,need to get), use postfix to judge first;
     * then invoke function to judge after getting.
     */
    private boolean isNxlFile() {
        if (mWorkingFile != null) {
            return RenderHelper.isNxlFile(mWorkingFile.getPath());
        }
        return bIsNxl;
    }

    private class AddFileCallback implements IProtectService.IProtectCallback {

        @Override
        public void onPreProtect() {
            showLoadingDialog();
        }

        @Override
        public void onProtectSuccess() {
            dismissLoadingDialog();
            if (mFile != null) {
                mFile.release();
            }
            //EventBus.getDefault().post(new ProjectAddCompleteNotifyEvent(mParentPathId));

            if (mService instanceof WorkSpaceRepo) {
                CommonUtils.popupWorkSpaceAddFileSuccessTip(mRoot, mWorkingFile.getName());
            } else {
                CommonUtils.popupProjectAddFileSuccessTip(mCtx, mRoot, mWorkingFile.getName());
            }

        }

        @Override
        public void onProtectFailed(Exception e) {
            dismissLoadingDialog();
            ExceptionHandler.handleException(mCtx, e);
        }
    }

    private boolean isAdHocDisabled() {
        try {
            IRmUser rmUser = SkyDRMApp.getInstance()
                    .getSession()
                    .getRmUser();
            return !rmUser.isADHocEnabled();
        } catch (InvalidRMClientException e) {
            e.printStackTrace();
        }
        return false;
    }
}
