package com.skydrm.rmc.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.engine.enumData.CmdOperate;
import com.skydrm.rmc.engine.eventBusMsg.ViewFileResultNotifyEvent;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.reposystem.IRemoteRepo;
import com.skydrm.rmc.reposystem.exception.FileUploadException;
import com.skydrm.rmc.reposystem.remoterepo.ICancelable;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.reposystem.types.NXDocument;
import com.skydrm.rmc.reposystem.types.NXFolder;
import com.skydrm.rmc.ui.activity.CmdOperateFileActivity;
import com.skydrm.rmc.ui.activity.CmdOperateFileActivity2;
import com.skydrm.rmc.ui.activity.LibraryActivity;
import com.skydrm.rmc.ui.base.BaseFragment;
import com.skydrm.rmc.ui.fragment.preview.PreviewImpl;
import com.skydrm.rmc.ui.widget.LoadingDialog2;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.Serializable;

import butterknife.BindView;

import static android.app.Activity.RESULT_OK;
import static com.skydrm.rmc.ui.common.ContextMenu.ACTION_SELECT_PATH;

public class CmdAddFragment extends BaseFragment {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.bt_cancel)
    Button mBtCancel;

    @BindView(R.id.tv_change)
    TextView mTvChange;
    @BindView(R.id.tv_path)
    TextView mTvPath;

    @BindView(R.id.rl_preview_content)
    RelativeLayout mRlPreviewContent;

    @BindView(R.id.rl_protect_site)
    RelativeLayout mRlProtectSite;
    @BindView(R.id.rl_share_site)
    RelativeLayout mRlShareSite;
    @BindView(R.id.tv_upload_normal)
    TextView mTvUploadNormal;

    private String mLibraryFile;
    private CmdOperate mCmdOperate;

    private IPreview mPreview;

    // the dest of upload file
    private INxFile mDestFolder;
    private BoundService mBoundService;

    private boolean emptyFile;
    private File mWorkingFile;
    private UploadCallback mUploadCallback;
    private LoadingDialog2 mLoadingDialog;


    public static CmdAddFragment newInstance() {
        return new CmdAddFragment();
    }

    @Override
    protected void onUserFirstVisible() {

    }

    @Override
    protected void onUserVisible() {

    }

    @Override
    protected void onUserInvisible() {

    }

    @Override
    protected void initViewAndEvents() {
        if (!resolveIntent()) {
            return;
        }
        mPreview = new PreviewImpl();
        mWorkingFile = new File(mLibraryFile);
        mPreview.buildRender(_activity, mRlPreviewContent, mWorkingFile);
        mPreview.start();

        String path = configureAddToFolderPath();
        mTvPath.setText(path);
        mToolbar.setTitle(mWorkingFile.getName());

        emptyFile = mWorkingFile.length() == 0;

        initListener();
    }

    @Override
    protected View getLoadingTargetView() {
        return null;
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.layout_cmd_add_fragment;
    }

    @Override
    protected boolean isBindEventBusHere() {
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == Constant.REQUEST_CODE_MYSPACE_CHANGE_PATH) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    mBoundService = (BoundService) extras.getSerializable(Constant.BOUND_SERVICE);
                    mDestFolder = (INxFile) extras.getSerializable(Constant.DEST_FOLDER);
                    if (mBoundService != null && mDestFolder != null) {
                        mTvPath.setText(String.format("%s:%s", mBoundService.getDisplayName(), mDestFolder.getDisplayPath()));
                    }
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPreview != null) {
            mPreview.stop();
        }
        if (mUploadCallback != null) {
            mUploadCallback = null;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mPreview != null) {
            mPreview.stop();
        }
        if (mUploadCallback != null) {
            mUploadCallback = null;
        }
    }

    /**
     * eventBus message handler for preview file result notify -- succeed or failed(may don't support some file type).
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPreviewFileResultEventHandler(ViewFileResultNotifyEvent eventMsg) {
        // open file failed(not supported file)
        if (!eventMsg.isbSucceed()) {
            TextView tips = new TextView(_activity);
            tips.setTextSize(20);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            mRlPreviewContent.removeAllViews();
            mRlPreviewContent.addView(tips, params);
            tips.setText(eventMsg.getResultMsg());
        }
    }

    private boolean resolveIntent() {
        Bundle args = getArguments();
        if (args == null) {
            return false;
        }
        mLibraryFile = args.getString(Constant.LIBRARY_FILE_ENTRY);
        mCmdOperate = (CmdOperate) args.getSerializable(Constant.CMD_OPERATE_TYPE);
        return true;
    }

    private String configureAddToFolderPath() {
        INxFile workingFolder = null;
        try {
            workingFolder = SkyDRMApp.getInstance().getRepoSystem().findWorkingFolder();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (workingFolder != null) {
            if (workingFolder.getService() != null) { // current folder for operating some repo
                mBoundService = workingFolder.getService();
                if (!mBoundService.type.equals(BoundService.ServiceType.MYDRIVE)) {
                    mDestFolder = new NXFolder("/", "/", "", 0);
                    mBoundService = CommonUtils.getDefaultUploadedBoundService();
                } else {
                    mDestFolder = workingFolder;
                }
                return getResources().getString(R.string.MyDrive) + " " + mDestFolder.getDisplayPath();
            } else { // synthetic root
                mDestFolder = new NXFolder("/", "/", "", 0);
                mBoundService = CommonUtils.getDefaultUploadedBoundService();
                return getResources().getString(R.string.MyDrive) + " " + mDestFolder.getDisplayPath();
            }
        }
        return "";
    }

    private void initListener() {
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isScanDoc()) {
                    finishParent();
                    return;
                }
                if (_activity instanceof CmdOperateFileActivity2) {
                    CmdOperateFileActivity2 activity = (CmdOperateFileActivity2) _activity;
                    activity.popup();
                } else {
                    finishParent();
                }
            }
        });
        mBtCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishParent();
            }
        });
        mRlProtectSite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emptyFile) {
                    handleEmptyFile();
                    return;
                }
                CmdOperateFileActivity2 activity = (CmdOperateFileActivity2) _activity;
                activity.replaceLoadCmdProtectFragmentAsRoot(mBoundService, mDestFolder, mLibraryFile);
            }
        });
        mRlShareSite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emptyFile) {
                    handleEmptyFile();
                    return;
                }
                CmdOperateFileActivity2 activity = (CmdOperateFileActivity2) _activity;
                activity.replaceLoadCmdShareFragmentAsRoot(mBoundService, mDestFolder, mLibraryFile);
            }
        });
        mTvUploadNormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emptyFile) {
                    handleEmptyFile();
                    return;
                }
                uploadFile();
            }
        });
        mTvChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goSelectPath();
            }
        });
    }

    private void goSelectPath() {
        Intent i = new Intent();
        i.setAction(Constant.ACTION_MYSPACE_CREATE_FOLDER_SELECT_PATH);
        i.putExtra(Constant.BOUND_SERVICE, CommonUtils.getDefaultUploadedBoundService());
        i.setClass(_activity, CmdOperateFileActivity2.class);
        startActivityForResult(i, Constant.REQUEST_CODE_MYSPACE_CHANGE_PATH);
    }

    private void handleEmptyFile() {
        ToastUtil.showToast(_activity, getString(R.string.empty_file_performed));
    }

    private void uploadFile() {
        try {
            showLoadingDialog();
            mUploadCallback = new UploadCallback();
            SkyDRMApp.getInstance().getRepoSystem().uploadFile(mBoundService,
                    mDestFolder,
                    mWorkingFile.getName(),
                    mWorkingFile, mUploadCallback);
        } catch (FileUploadException e) {
            e.printStackTrace();

            dismissLoadingDialog();
            ExceptionHandler.handleException(_activity, e);
        }
    }

    private boolean isScanDoc() {
        if (mCmdOperate == null) {
            return false;
        }
        return mCmdOperate == CmdOperate.COMMAND_SCAN;
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
                ToastUtil.showToast(_activity, getString(R.string.Upload_file_succeed));
                finishParent();
            } else {
                String errorMsg = getString(R.string.Upload_file_failed_no_period);
                if (e != null) {
                    errorMsg += ", " + e.getMessage() + ".";
                }
                ToastUtil.showToast(_activity, errorMsg);
            }
        }

        @Override
        public void progressing(long newValue) {

        }
    }

    private void showLoadingDialog() {
        mLoadingDialog = LoadingDialog2.newInstance();
        mLoadingDialog.showModalDialog(_activity);
    }

    private void dismissLoadingDialog() {
        if (mLoadingDialog != null) {
            mLoadingDialog.dismissDialog();
            mLoadingDialog = null;
        }
    }
}
