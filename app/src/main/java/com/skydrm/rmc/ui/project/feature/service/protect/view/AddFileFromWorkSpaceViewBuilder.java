package com.skydrm.rmc.ui.project.feature.service.protect.view;

import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.engine.Render.RenderHelper;
import com.skydrm.rmc.engine.eventBusMsg.ProjectAddCompleteNotifyEvent;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.ui.project.feature.service.protect.IViewBuilder;
import com.skydrm.rmc.ui.project.service.ProjectOperateActivity;
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

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.Map;
import java.util.Set;

public class AddFileFromWorkSpaceViewBuilder implements IViewBuilder {
    private Context mCtx;
    private View mRoot;

    private File mWorkingFile;
    private IProject mProject;
    private String mParentPathId;
    private boolean bIsNxl;
    private AddFileCallback mAddFileCallback;


    private RightsSpecifyView mRightsSpecifyView;
    private LoadingDialog2 mLoadingDialog;

    public AddFileFromWorkSpaceViewBuilder(Context ctx, View root,
                                           IProject p) {
        this.mCtx = ctx;
        this.mRoot = root;
        this.mProject = p;
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
        mRightsSpecifyView.setExpiry(mProject.getExpiry());
        mRightsSpecifyView.setWatermark(mProject.getWatermark());
        mRightsSpecifyView.setTagsThenInvalidate(mProject.getClassificationRaw());

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
    public void configButton(Button button) {
        if (button == null) {
            return;
        }
        button.setText(button.getContext().getString(R.string.add_file));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRightsSpecifyView.isADHocSelect()) {
                    addFileWithADHocRights();
                } else {
                    addFileWithClassifications();
                }
            }
        });
    }

    @Override
    public void configNavigator(Toolbar toolbar) {
        if (toolbar == null) {
            return;
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupFragment();
            }
        });
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
    public boolean needInterceptBackPress() {
        return true;
    }

    @Override
    public void interceptBackPress() {
        popupFragment();
    }

    private void addFileWithADHocRights() {
        if (mRightsSpecifyView == null || mProject == null || mWorkingFile == null) {
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

        mProject.addFile(mWorkingFile.getPath(),
                rights, obligations, expiry,
                mParentPathId,
                mAddFileCallback);
    }

    private void addFileWithClassifications() {
        if (mRightsSpecifyView == null || mProject == null) {
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
            mProject.addFile(mWorkingFile, tag, mParentPathId, mAddFileCallback);
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

    @Override
    public void onReleaseResource() {
        if (mAddFileCallback != null) {
            mAddFileCallback = null;
        }
    }

    private class AddFileCallback implements IProject.IAddFileCallback {
        @Override
        public void onPreAdd() {
            showLoadingDialog();
        }

        @Override
        public void onSuccess() {
            dismissLoadingDialog();

            EventBus.getDefault().post(new ProjectAddCompleteNotifyEvent(mParentPathId));

            CommonUtils.popupProjectAddFileSuccessTip(mCtx, mRoot, mWorkingFile.getName());
        }

        @Override
        public void onFailed(Exception e) {
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

    private void popupFragment() {
        if (mCtx == null) {
            return;
        }
        if (mCtx instanceof ProjectOperateActivity) {
            ProjectOperateActivity activity = (ProjectOperateActivity) mCtx;
            activity.popup();
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
