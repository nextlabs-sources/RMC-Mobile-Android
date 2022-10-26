package com.skydrm.rmc.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.engine.FileOperation;
import com.skydrm.rmc.engine.Render.RenderHelper;
import com.skydrm.rmc.engine.enumData.CmdOperate;
import com.skydrm.rmc.engine.eventBusMsg.ChangeExpiryDateEvent;
import com.skydrm.rmc.engine.eventBusMsg.ShowConvertProgressEvent;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.reposystem.exception.FileDownloadException;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.ui.activity.CmdOperateFileActivity2;
import com.skydrm.rmc.ui.base.BaseFragment;
import com.skydrm.rmc.ui.fragment.share.NormalViewBuilder;
import com.skydrm.rmc.ui.fragment.share.NxlViewBuilder;
import com.skydrm.rmc.ui.project.feature.service.protect.IFileDownloader;
import com.skydrm.rmc.ui.project.feature.service.protect.download.LibraryFileDownloader;
import com.skydrm.rmc.ui.project.feature.service.protect.download.WorkSpaceFileDownloader;
import com.skydrm.rmc.utils.ViewUtils;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;
import com.skydrm.sdk.INxlFileFingerPrint;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.Serializable;
import java.util.Locale;

import butterknife.BindView;

public abstract class CmdBaseFragment extends BaseFragment {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.bt_cancel)
    Button mBtCancel;
    @BindView(R.id.scroll_view)
    ScrollView mSvView;
    @BindView(R.id.ll_sub_root)
    LinearLayout mLlSubRoot;

    protected CmdOperate mOperateType;
    protected String mFilePath;
    protected INxFile mRepoFile;
    protected INxFile mDestFolder;
    protected BoundService mBoundService;

    protected IViewBuilder mViewBuilder;
    private IFileDownloader mFileDownloader;
    private IFileDownloader.ICallBack mDownloadCallback;

    private RelativeLayout mPreviewFileLayout;
    private TextView mTvTipUserInfo;
    private RelativeLayout mRlProgressLayout;
    private TextView mTvDownloadConverting;
    private ProgressBar mProgressBar;
    private TextView mTvProgressValue;

    private File mWorkingFile;
    // converting progress whether has shown
    private boolean bConvertingProgressIsShow = false;

    protected abstract IViewBuilder createNxlViewBuilder(Context ctx, View root);

    protected abstract IViewBuilder createNormalViewBuilder(Context ctx, View root, boolean upload);

    protected abstract Button getProtectedOrShareButton();

    @Override
    protected void initViewAndEvents() {
        if (!resolveIntent()) {
            finishParent();
        }
        tryGetFile();
        initListener();
    }

    @Override
    protected boolean isBindEventBusHere() {
        return true;
    }

    @Override
    protected View getLoadingTargetView() {
        return mSvView;
    }

    /**
     * eventBus message for change expiry date.
     *
     * @param eventMsg
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onChangeExpiryDateEvent(ChangeExpiryDateEvent eventMsg) {
        if (mViewBuilder != null) {
            mViewBuilder.updateExpiry(eventMsg.iExpiry);
        }
    }

    /**
     * eventBus message handler for display Office & 3D convert progress notification.
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConvertProgressEventHandler(ShowConvertProgressEvent eventMsg) {
        showConvertingProgress(eventMsg);
    }

    private boolean resolveIntent() {
        Bundle args = getArguments();
        if (args == null) {
            return false;
        }

        mOperateType = (CmdOperate) args.get(Constant.CMD_OPERATE_TYPE);
        if (isProtectFromLibrary() || isShareFromLibrary()) {
            mFilePath = args.getString(Constant.LIBRARY_FILE_ENTRY);
        }

        if (isProtectByScan()) {
            mFilePath = args.getString(Constant.LIBRARY_FILE_ENTRY);
        }

        if (isProtectThenAdd() || isShareThenAdd()) {
            mBoundService = (BoundService) args.getSerializable(Constant.BOUND_SERVICE);
            mDestFolder = (INxFile) args.getSerializable(Constant.DEST_FOLDER);
            mFilePath = args.getString(Constant.LIBRARY_FILE_ENTRY);
        }

        if (isProtectFromRepo()
                || isShareFromRepo()
                || isProtect()
                || isShare()) {
            mRepoFile = (INxFile) args.getSerializable(Constant.LIBRARY_FILE_ENTRY);
        }

        return true;
    }

    private void tryGetFile() {
        View previewRoot = LayoutInflater.from(_activity).inflate(R.layout.layout_preview_file,
                mLlSubRoot, true);
        mPreviewFileLayout = previewRoot.findViewById(R.id.preview_file);

        mTvTipUserInfo = previewRoot.findViewById(R.id.tip_no_view_right);

        mRlProgressLayout = previewRoot.findViewById(R.id.download_progress);
        mTvDownloadConverting = previewRoot.findViewById(R.id.projects_file_info_tv_download);
        mProgressBar = previewRoot.findViewById(R.id.progress);
        mTvProgressValue = previewRoot.findViewById(R.id.textView_progress);

        initDownloadListener();

        if (isProtectFromLibrary()
                || isShareFromLibrary() || isProtectByScan()
                || isProtectThenAdd() || isShareThenAdd()) {
            mFileDownloader = new LibraryFileDownloader(mFilePath);
        }
        if (isProtect() || isShare() || isProtectFromRepo() || isShareFromRepo()) {
            mFileDownloader = new WorkSpaceFileDownloader(mRepoFile, mProgressBar, mTvProgressValue);
        }

        if (mFileDownloader != null) {
            mFileDownloader.tryGetFile(_activity, mDownloadCallback);
        }
    }

    private void initListener() {
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isProtect() || isShare() || isProtectByScan()) {
                    finishParent();
                } else {
                    if (_activity instanceof CmdOperateFileActivity2) {
                        CmdOperateFileActivity2 activity = (CmdOperateFileActivity2) _activity;
                        activity.popup();
                    }
                }
            }
        });
        if (isProtect() || isShare()) {
            mBtCancel.setVisibility(View.GONE);
        } else {
            mBtCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finishParent();
                }
            });
        }
    }

    private void initDownloadListener() {
        mDownloadCallback = new IFileDownloader.ICallBack() {
            @Override
            public void onPreDownload() {
                showProgress();
            }

            @Override
            public void onDownloadFinished(String localPath) {
                hideProgress();

                mWorkingFile = new File(localPath);

//                if (mViewBuilder.isPreviewNeeded()) {
//                    fileRender(mWorkingFile, mPreviewFileLayout);
//                }

                // do some special handler for nxl file
                boolean bIsNxl = RenderHelper.isNxlFile(mWorkingFile.getPath());
                if (bIsNxl) {
                    mViewBuilder = createNxlViewBuilder(_activity, mLlSubRoot);
                    mViewBuilder.configureShareOrProtectButton(getProtectedOrShareButton());
                    tryReadNxlRights();
                } else {
                    mViewBuilder = createNormalViewBuilder(_activity, mLlSubRoot,
                            isProtectThenAdd() || isShareThenAdd());

                    mViewBuilder.configureShareOrProtectButton(getProtectedOrShareButton());
                    initializeItemViewsByAction();
                    mViewBuilder.bindFingerPrint(null, mWorkingFile, false);
                }
            }

            @Override
            public void onDownloadProgress(long value) {
                if (mProgressBar != null) {
                    mProgressBar.setProgress((int) value);
                }
                if (mTvProgressValue != null) {
                    String text = String.format(Locale.getDefault(), "%d", value) + "%";
                    mTvProgressValue.setText(text);
                }
            }

            @Override
            public void onDownloadFailed(FileDownloadException e) {
                hideProgress();
                ExceptionHandler.handleException(_activity, e);
            }
        };
    }

    private void tryReadNxlRights() {
        // display progressBar loading
        mViewBuilder.showLoading(-1);
        // get finger print
        FileOperation.readNxlFingerPrint(_activity, mWorkingFile, false, Constant.VIEW_TYPE_PREVIEW,
                new FileOperation.IGetFingerPrintCallback() {
                    @Override
                    public void onGetFingerPrintFinished(INxlFileFingerPrint fingerPrint) {
                        mViewBuilder.hideLoading(-1);
                        initializeItemViewsByAction();
                        mViewBuilder.bindFingerPrint(fingerPrint, mWorkingFile, true);
                    }
                });
    }

    private void initializeItemViewsByAction() {
        mLlSubRoot.removeAllViews();
        View subRoot = mViewBuilder.buildRoot(_activity);
        if (subRoot != null) {
            mLlSubRoot.addView(subRoot);
        }
    }

    // show the convert progress for Office & 3D
    private void showConvertingProgress(ShowConvertProgressEvent eventMsg) {
        if (!bConvertingProgressIsShow) {
            showProgress();
            bConvertingProgressIsShow = true;
        }
        mTvDownloadConverting.setText(getString(R.string.c_Processing_with3dots));
        mProgressBar.setProgress(eventMsg.getProgressValue());
        String text = String.format(Locale.getDefault(), "%d", eventMsg.getProgressValue()) + "%";
        mTvProgressValue.setText(text);
    }

    private void showProgress() {
        if (mRlProgressLayout == null) {
            return;
        }
        if (ViewUtils.isGone(mRlProgressLayout) || ViewUtils.isInVisible(mRlProgressLayout)) {
            mRlProgressLayout.setVisibility(View.VISIBLE);
        }
    }

    private void hideProgress() {
        if (mRlProgressLayout == null) {
            return;
        }
        if (ViewUtils.isVisible(mRlProgressLayout)) {
            mRlProgressLayout.setVisibility(View.GONE);
        }
    }

    private boolean isProtect() {
        return CmdOperate.PROTECT == mOperateType;
    }

    private boolean isShare() {
        return CmdOperate.SHARE == mOperateType;
    }

    private boolean isProtectFromLibrary() {
        return CmdOperate.COMMAND_PROTECT_FROM_LIB == mOperateType;
    }

    private boolean isShareFromLibrary() {
        return CmdOperate.COMMAND_SHARE_FROM_LIB == mOperateType;
    }

    private boolean isProtectFromRepo() {
        return CmdOperate.COMMAND_PROTECT_FROM_REPO == mOperateType;
    }

    private boolean isShareFromRepo() {
        return CmdOperate.COMMAND_SHARE_FROM_REPO == mOperateType;
    }

    private boolean isProtectByScan() {
        return CmdOperate.COMMAND_SCAN == mOperateType;
    }

    private boolean isProtectThenAdd() {
        return CmdOperate.COMMAND_PROTECT_THEN_ADD == mOperateType;
    }

    private boolean isShareThenAdd() {
        return CmdOperate.COMMAND_SHARE_THEN_ADD == mOperateType;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDownloadCallback != null) {
            mDownloadCallback = null;
        }
        CommonUtils.releaseResource(mViewBuilder);
    }
}
