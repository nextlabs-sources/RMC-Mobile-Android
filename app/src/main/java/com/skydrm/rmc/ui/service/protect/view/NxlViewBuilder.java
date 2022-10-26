package com.skydrm.rmc.ui.service.protect.view;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.workspace.WorkSpaceRepo;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.ui.common.IDestroyable;
import com.skydrm.rmc.ui.project.feature.service.core.MarkException;
import com.skydrm.rmc.ui.project.feature.service.share.IShare;
import com.skydrm.rmc.ui.service.Helper;
import com.skydrm.rmc.ui.service.protect.IProtectFile;
import com.skydrm.rmc.ui.service.protect.IProtectService;
import com.skydrm.rmc.ui.service.protect.IViewBuilder;
import com.skydrm.rmc.ui.service.protect.ProtectActivity;
import com.skydrm.rmc.ui.service.protect.task.UploadNxlFileTask;
import com.skydrm.rmc.ui.widget.LoadingDialog2;
import com.skydrm.rmc.ui.widget.customcontrol.rights.ADHocRightsDisplayView;
import com.skydrm.rmc.ui.widget.customcontrol.rights.CentralTagDisplayView;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.rms.rest.user.User;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NxlViewBuilder implements IViewBuilder, IDestroyable,
        CentralTagDisplayView.IInvokePolicyEvaluationListener, IShare.IPolicyCallback {
    private Context mCtx;
    private View mRoot;
    private LinearLayout mLlSubRoot;

    private CentralTagDisplayView mTagView;

    private IProtectService mService;
    private IProtectFile mFile;

    private File mWorkingFile;
    private INxlFileFingerPrint mFp;

    private boolean bIsNxl;
    private String mParentPathId;

    private LoadingDialog2 mLoadingDialog;
    private UploadCallback mUploadCallback;

    public NxlViewBuilder(Context ctx, View root,
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
        mLlSubRoot = new LinearLayout(ctx);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        mLlSubRoot.setLayoutParams(params);

        return mLlSubRoot;
    }

    @Override
    public void showLoading(int type) {

    }

    @Override
    public void hideLoading(int type) {

    }

    @Override
    public void bindFingerPrint(INxlFileFingerPrint fp, File f, boolean nxl) {
        this.mFp = fp;
        this.mWorkingFile = f;
        this.bIsNxl = nxl;

        if (nxl) {
            if (fp == null) {
                ToastUtil.showToast(mCtx, "Failed to read rights of the nxl file.");
                return;
            }
            mLlSubRoot.removeAllViews();
            if (fp.hasRights()) {
                mLlSubRoot.addView(buildADHocRightsDisplayView(mCtx, fp));
            } else {
                mLlSubRoot.addView(mTagView = buildCentralTagDisplayView(mCtx, fp));
                mTagView.setUserVisibleHint(true);
            }
        }
    }

    @Override
    public void updateExpiry(User.IExpiry expiry) {

    }

    @Override
    public void updateExtractStatus(boolean checked) {

    }

    @Override
    public void updateParentPath(String parentPathId) {
        this.mParentPathId = parentPathId;
    }

    @Override
    public void onAddFilePerformed() {
        if (mUploadCallback == null) {
            mUploadCallback = new UploadCallback();
        }
        UploadNxlFileTask task = new UploadNxlFileTask(mService,
                mParentPathId, mWorkingFile,
                mUploadCallback);
        task.run();
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
    public void beginInvoke(Map<String, Set<String>> tags) {
        if (mFp == null || mWorkingFile == null) {
            return;
        }
        if (mTagView == null) {
            return;
        }
        mTagView.showLoading(true);
        String name = mWorkingFile.getName();
        String membershipId = mFp.getOwnerID();
        String duid = mFp.getDUID();
        Helper.doPolicyEvaluation(membershipId, name, duid, tags, this);
    }

    @Override
    public void onSuccess(List<String> rights, String obligations) {
        if (mTagView == null) {
            return;
        }
        mTagView.showLoading(false);
        mTagView.showRights(rights, obligations);
    }

    @Override
    public void onFailed(MarkException e) {
        if (mCtx != null) {
            ExceptionHandler.handleException(mCtx, e);
        }
    }

    @Override
    public void onReleaseResource() {
        CommonUtils.releaseResource(mTagView);
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

    private ADHocRightsDisplayView buildADHocRightsDisplayView(Context ctx,
                                                               INxlFileFingerPrint fp) {
        if (ctx == null) {
            return null;
        }
        ADHocRightsDisplayView ret = new ADHocRightsDisplayView(ctx);
        if (fp == null) {
            ret.showNoRightsTip();
            return ret;
        }
        ret.displayRights(fp);
        ret.showWatermark(fp.getDisplayWatermark());
        ret.showValidity(fp.formatString());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        ret.setLayoutParams(params);
        return ret;
    }

    private CentralTagDisplayView buildCentralTagDisplayView(Context ctx,
                                                             INxlFileFingerPrint fp) {
        CentralTagDisplayView ret = new CentralTagDisplayView(ctx);
        ret.setDescText(ctx.getString(R.string.company_defined_rights));
        ret.setDescTextSize(16);
        ret.setDescTextColor(ctx.getResources().getColor(android.R.color.black));
        if (fp == null) {
            ret.showNoPolicyTip();
            return ret;
        }
        ret.setInvokePolicyEvaluationListener(this);
        ret.setTags(fp.getAll());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        ret.setLayoutParams(params);
        return ret;
    }


    class UploadCallback implements UploadNxlFileTask.ITaskCallback<UploadNxlFileTask.Result, Exception> {

        @Override
        public void onTaskPreExecute() {
            showLoadingDialog();
        }

        @Override
        public void onTaskExecuteSuccess(UploadNxlFileTask.Result results) {
            dismissLoadingDialog();

            if (mFile != null) {
                mFile.release();
            }

            if (mService instanceof WorkSpaceRepo) {
                CommonUtils.popupWorkSpaceAddFileSuccessTip(mRoot, mWorkingFile.getName());
            } else {
                CommonUtils.popupProjectAddFileSuccessTip(mCtx, mRoot, mWorkingFile.getName());
            }
        }

        @Override
        public void onTaskExecuteFailed(Exception e) {
            dismissLoadingDialog();
            ExceptionHandler.handleException(mCtx, e);
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
