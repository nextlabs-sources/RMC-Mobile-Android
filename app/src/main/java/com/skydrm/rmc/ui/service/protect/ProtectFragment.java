package com.skydrm.rmc.ui.service.protect;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.project.Project;
import com.skydrm.rmc.datalayer.repo.workspace.WorkSpaceRepo;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.engine.FileOperation;
import com.skydrm.rmc.engine.Render.FileRenderProxy;
import com.skydrm.rmc.engine.Render.IFileRender;
import com.skydrm.rmc.engine.Render.RenderHelper;
import com.skydrm.rmc.engine.Render.ThreeD;
import com.skydrm.rmc.engine.Render.WebViewRender;
import com.skydrm.rmc.engine.eventBusMsg.ChangeExpiryDateEvent;
import com.skydrm.rmc.engine.eventBusMsg.ShowConvertProgressEvent;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.reposystem.exception.FileDownloadException;
import com.skydrm.rmc.ui.base.BaseFragment;
import com.skydrm.rmc.ui.project.feature.files.view.ProjectLibraryActivity;
import com.skydrm.rmc.ui.project.feature.service.protect.MsgExtractSwitchStatus;
import com.skydrm.rmc.ui.service.protect.view.NormalViewBuilder;
import com.skydrm.rmc.ui.service.protect.view.NxlViewBuilder;
import com.skydrm.rmc.ui.workspace.WorkSpaceLibraryActivity;
import com.skydrm.rmc.utils.ViewUtils;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;
import com.skydrm.rmc.utils.commonUtils.DensityHelper;
import com.skydrm.sdk.INxlFileFingerPrint;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.Locale;

import butterknife.BindView;

import static android.app.Activity.RESULT_OK;

public class ProtectFragment extends BaseFragment {
    @BindView(R.id.rl_root)
    RelativeLayout mRlRoot;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.tv_cancel)
    TextView mTvCancel;
    @BindView(R.id.fl_content)
    RelativeLayout mRlPreviewRoot;
    @BindView(R.id.command_change_path)
    LinearLayout mLlChangePath;
    @BindView(R.id.iv_preview)
    ImageView mIvPreview;
    @BindView(R.id.tv_preview_tip)
    TextView mTvPreviewTip;
    @BindView(R.id.tv_file_name)
    TextView mTvFileName;
    //    @BindView(R.id.tv_change)
//    TextView mTvChange;
    @BindView(R.id.tv_path)
    TextView mTvPath;
    @BindView(R.id.tv_change_save_location)
    TextView mTvChangeSaveLocation;

    @BindView(R.id.rl_sub_content)
    RelativeLayout mRlSubContent;
    @BindView(R.id.ll_preview_content)
    LinearLayout mLlPreviewContainer;

    @BindView(R.id.bt_add_file)
    Button mBtAddFile;

    // change path request code.
    private static int PROTECT_CHANGE_PATH_REQUEST_CODE = 0x02;

    private IViewBuilder mViewBuilder;
    private IProtectFile.ICallBack mDownloadCallback;

    private RelativeLayout mProgressLayout;
    private TextView mDownloadingConverting;
    private ProgressBar mProgressBar;
    private TextView mProgressValue;
    private TextView mTipUserInfo;

    private RelativeLayout mPreviewFileLayout;

    //File render proxy
    private FileRenderProxy mFileRenderProxy;
    private IFileRender mIFileRender;

    // converting progress whether has shown
    private boolean bConvertingProgressIsShow = false;
    private boolean showPreview;

    private IProtectFile mFile;
    private IProtectService mService;
    private String mParentPathId;
    private File mWorkingFile;

    public static ProtectFragment newInstance() {
        return new ProtectFragment();
    }

    public boolean needInterceptBackPress() {
        if (mViewBuilder == null) {
            return false;
        }
        return mViewBuilder.needInterceptBackPress();
    }

    public void interceptBackPress() {
        if (mViewBuilder == null) {
            return;
        }
        mViewBuilder.interceptBackPress();
    }

    /**
     * eventBus message handler for display Office & 3D convert progress notification.
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConvertProgressEventHandler(ShowConvertProgressEvent eventMsg) {
        showConvertingProgress(eventMsg);
    }

    /**
     * eventBus message for change expiry date.
     *
     * @param msg
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onChangeExpiryDateEvent(ChangeExpiryDateEvent msg) {
        if (mViewBuilder != null) {
            mViewBuilder.updateExpiry(msg.iExpiry);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onExtractSwitchStateChange(MsgExtractSwitchStatus msg) {
        if (mViewBuilder != null) {
            mViewBuilder.updateExtractStatus(msg.checked);
        }
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
        if (!initData(getArguments())) {
            return;
        }
        if (mService == null) {
            return;
        }

        View previewRoot = LayoutInflater.from(_activity).inflate(R.layout.layout_preview_file,
                mLlPreviewContainer, true);
        mPreviewFileLayout = previewRoot.findViewById(R.id.preview_file);

        mTipUserInfo = previewRoot.findViewById(R.id.tip_no_view_right);

        mProgressLayout = previewRoot.findViewById(R.id.download_progress);
        mDownloadingConverting = previewRoot.findViewById(R.id.projects_file_info_tv_download);
        mProgressBar = previewRoot.findViewById(R.id.progress);
        mProgressValue = previewRoot.findViewById(R.id.textView_progress);

        mToolbar.setTitle(mService.getServiceName(_activity));
        mTvFileName.setText(mFile.getName());

        setSaveDisplayLocation();

        mRlPreviewRoot.setVisibility(View.GONE);
        initDownloadListener();

        mFile.tryGetFile(_activity, mDownloadCallback);

        initEvents();
    }

    protected IViewBuilder createNormalViewBuilder() {
        return new NormalViewBuilder(_activity, mRlRoot, mService, mFile);
    }

    protected IViewBuilder createNxlViewBuilder() {
        return new NxlViewBuilder(_activity, mRlRoot, mService, mFile);
    }

    @Override
    protected View getLoadingTargetView() {
        return null;
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.fragment_protect;
    }

    @Override
    protected boolean isBindEventBusHere() {
        return true;
    }

    // get the changed path.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // project change path results
        if (requestCode == PROTECT_CHANGE_PATH_REQUEST_CODE && resultCode == RESULT_OK) {
            mParentPathId = data.getStringExtra(Constant.PROJECT_PATH_ID);
            mViewBuilder.updateParentPath(mParentPathId);
            setSaveDisplayLocation();
        }
    }

    protected boolean initData(Bundle args) {
        if (args == null) {
            return false;
        }
        mFile = args.getParcelable(Constant.PROTECT_FILE_ENTRY);
        mService = args.getParcelable(Constant.PROTECT_SERVICE);
        mParentPathId = args.getString(Constant.NAME_CURRENT_PATH_ID);

        return true;
    }

    protected void initDownloadListener() {
        mDownloadCallback = new IProtectFile.ICallBack() {

            @Override
            public void onPreDownload() {
                showProgress();
                mBtAddFile.setEnabled(false);
            }

            @Override
            public void onDownloadFinished(String localPath) {
                hideProgress();

                mBtAddFile.setEnabled(true);

                mWorkingFile = new File(localPath);

                // do some special handler for nxl file
                boolean bIsNxl = RenderHelper.isNxlFile(mWorkingFile.getPath());
                if (bIsNxl) {
                    mViewBuilder = createNxlViewBuilder();
                    tryReadNxlRights(mWorkingFile);
                } else {
                    mViewBuilder = createNormalViewBuilder();
                    initializeItemViewsByAction();
                    mViewBuilder.bindFingerPrint(null, mWorkingFile, false);
                }

                mViewBuilder.updateParentPath(mParentPathId);

                if (mViewBuilder.isPreviewNeeded()) {
                    mRlPreviewRoot.setVisibility(View.VISIBLE);
                    initResizePreviewWindowEvent();
                    setPreviewLayoutParams(mPreviewFileLayout, 3);

                    fileRender(mWorkingFile, mPreviewFileLayout);
                }
            }

            @Override
            public void onDownloadProgress(long value) {
                if (mProgressBar != null) {
                    mProgressBar.setProgress((int) value);
                }
                if (mProgressValue != null) {
                    String text = String.format(Locale.getDefault(), "%d", value) + "%";
                    mProgressValue.setText(text);
                }
            }

            @Override
            public void onDownloadFailed(FileDownloadException e) {
                hideProgress();
                mBtAddFile.setEnabled(false);

                // exception handler
                if (e != null) {
                    ExceptionHandler.handleException(_activity, e);
                }
            }
        };
    }

    private void initEvents() {
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isProtectFromScanDoc()) {
                    finishParent();
                } else {
                    if (mViewBuilder != null) {
                        mViewBuilder.interceptBackPress();
                    }
                }
            }
        });
        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishParent();
            }
        });

        mBtAddFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mViewBuilder != null) {
                    mViewBuilder.onAddFilePerformed();
                }
            }
        });

        mTvChangeSaveLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goSelectPath();
            }
        });

    }

    private void initResizePreviewWindowEvent() {
        mLlChangePath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!showPreview) {
                    showPreview = true;
                    if (ViewUtils.isGone(mLlPreviewContainer)) {
                        mLlPreviewContainer.setVisibility(View.VISIBLE);
                    }
                    resizeLayoutParams(CommonUtils.getScreenHeight(_activity) / 3 + dp(20), mLlPreviewContainer);
                } else {
                    if (ViewUtils.isVisible(mLlPreviewContainer)) {
                        mLlPreviewContainer.setVisibility(View.GONE);
                    }
                    showPreview = false;
                    //resizeLayoutParams(dp(0), mLlPreviewContainer);
                }
            }
        });
    }

    // file render
    private void fileRender(File workingFile, RelativeLayout mainLayout) {
        // render document
        if (interceptFileRender(workingFile)) {
            return;
        }

        boolean bIsNxl = RenderHelper.isNxlFile(workingFile.getPath());
        String fileName = (TextUtils.isEmpty(workingFile.getName()) ? workingFile.getName() : workingFile.getName());
        mFileRenderProxy = new FileRenderProxy(_activity, mainLayout, bIsNxl, workingFile, fileName, Constant.VIEW_TYPE_PREVIEW);
        mFileRenderProxy.buildRender(new FileRenderProxy.IBuildRenderCallback() {
            @Override
            public void onBuildRenderFinish() {
                // render file
                mFileRenderProxy.fileRender();
                mIFileRender = mFileRenderProxy.getIFileRender();

                if (mIFileRender instanceof WebViewRender) {
                    generateWebRenderThumbnail((WebViewRender) mIFileRender);
                } else if (mIFileRender instanceof ThreeD) {
                    generate3DRenderThumbnail((ThreeD) mIFileRender);
                } else {
                    mTvPreviewTip.setVisibility(View.VISIBLE);
                    mTvPreviewTip.setText("Does not support preview.");
                }
            }
        });
    }

    /**
     * @param workingFile target file
     * @return true means intercept file rendering|false means allow
     */
    private boolean interceptFileRender(File workingFile) {
        if (workingFile == null) {
            return true;
        }
        if (RenderHelper.judgeFileType(workingFile) == FileRenderProxy.FileType.FILE_TYPE_VIDEO) {
            mTipUserInfo.setVisibility(View.VISIBLE);
            mTipUserInfo.setText(getString(R.string.Not_view_mp4));
            //show small preview window unsupported file tip.
            mTvPreviewTip.setVisibility(View.VISIBLE);
            mTvPreviewTip.setText(getString(R.string.Not_view_mp4));
            return true;
        }
//        if (RenderHelper.judgeFileType(workingFile) == FileRenderProxy.FileType.FILE_TYPE_3D) {
//            mTipUserInfo.setVisibility(View.VISIBLE);
//            mTipUserInfo.setText(getResources().getString(R.string.Not_view_3d));
//            //show small preview window unsupported file tip.
//            mPreviewTip.setVisibility(View.VISIBLE);
//            mPreviewTip.setText(getResources().getString(R.string.Not_view_3d));
//            return true;
//        }
        return false;
    }

    private void generateWebRenderThumbnail(final WebViewRender render) {
        if (render == null) {
            return;
        }
        if (mIvPreview == null) {
            return;
        }
        render.setOnWebViewLoadListener(new WebViewRender.IWebViewLoadCallback() {
            @Override
            public void onPageFinished(final Bitmap bitmap) {
                if (bitmap != null) {
                    mIvPreview.setImageBitmap(bitmap);
                } else {
                    mTvPreviewTip.setVisibility(View.VISIBLE);
                    mTvPreviewTip.setText(getString(R.string.excep_operation_failed));
                }
                mLlPreviewContainer.setVisibility(View.GONE);
            }
        });
    }

    private void generate3DRenderThumbnail(ThreeD render) {
        if (render == null || mIvPreview == null) {
            return;
        }
        //show small preview window unsupported file tip.
        mTvPreviewTip.setVisibility(View.VISIBLE);
        mTvPreviewTip.setText(getString(R.string.Not_view_3d));
        mLlPreviewContainer.setVisibility(View.GONE);
    }

    // read nxl file rights
    private void tryReadNxlRights(final File f) {
        //Need initialize item view first, then display loading status belong to target view.
        initializeItemViewsByAction();

        // display progressBar loading
        mViewBuilder.showLoading(-1);
        // get finger print
        FileOperation.readNxlFingerPrint(_activity, f, false, Constant.VIEW_TYPE_PREVIEW,
                new FileOperation.IGetFingerPrintCallback() {
                    @Override
                    public void onGetFingerPrintFinished(INxlFileFingerPrint fingerPrint) {
                        mViewBuilder.hideLoading(-1);
                        mViewBuilder.bindFingerPrint(fingerPrint, f, true);
                    }
                });
    }

    // show the convert progress for Office & 3D
    private void showConvertingProgress(ShowConvertProgressEvent eventMsg) {
        if (!bConvertingProgressIsShow) {
            showProgress();
            bConvertingProgressIsShow = true;
        }
        mDownloadingConverting.setText(getString(R.string.c_Processing_with3dots));
        mProgressBar.setProgress(eventMsg.getProgressValue());
        String text = String.format(Locale.getDefault(), "%d", eventMsg.getProgressValue()) + "%";
        mProgressValue.setText(text);
    }

    private void showProgress() {
        if (mProgressLayout == null) {
            return;
        }
        if (ViewUtils.isGone(mLlPreviewContainer)
                || ViewUtils.isInVisible(mLlPreviewContainer)) {
            mLlPreviewContainer.setVisibility(View.VISIBLE);
        }
        if (ViewUtils.isGone(mProgressLayout) || ViewUtils.isInVisible(mProgressLayout)) {
            mProgressLayout.setVisibility(View.VISIBLE);
        }
    }

    private void hideProgress() {
        if (mProgressLayout == null) {
            return;
        }
        if (ViewUtils.isVisible(mLlPreviewContainer)) {
            mLlPreviewContainer.setVisibility(View.GONE);
        }
        if (ViewUtils.isVisible(mProgressLayout)) {
            mProgressLayout.setVisibility(View.GONE);
        }
    }

    private void initializeItemViewsByAction() {
        mRlSubContent.removeAllViews();
        mRlSubContent.addView(mViewBuilder.buildRoot(_activity));
    }

    private void goSelectPath() {
        if (mService instanceof Project) {
            Intent intent = new Intent(_activity, ProjectLibraryActivity.class);
            intent.setAction(Constant.ACTION_PROJECT_ADD_FILE);
            intent.putExtra(Constant.PROJECT_DETAIL, (Parcelable) mService);
            startActivityForResult(intent, PROTECT_CHANGE_PATH_REQUEST_CODE);
        }
        if (mService instanceof WorkSpaceRepo) {
            Intent intent = new Intent(_activity, WorkSpaceLibraryActivity.class);
            intent.setAction(Constant.ACTION_PROJECT_ADD_FILE);
            startActivityForResult(intent, PROTECT_CHANGE_PATH_REQUEST_CODE);
        }
    }

    private void setSaveDisplayLocation() {
        mTvPath.setText(String.format("%s%s", mService.getServiceName(_activity), mParentPathId));
    }

    private boolean isProtectFromScanDoc() {
        Bundle args = getArguments();
        if (args == null) {
            return false;
        }
        return Constant.ACTION_WORKSPACE_PROTECT_FROM_SCAN_DOC
                .equals(args.getString(Constant.KEY_ACTION_BELONG_ADD_FILE_FRAG));
    }

    private void setPreviewLayoutParams(RelativeLayout previewLayout, int ratio) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                CommonUtils.getScreenHeight(_activity) / ratio);
        layoutParams.setMargins(dp(20), dp(10), dp(20), dp(0));
        previewLayout.setLayoutParams(layoutParams);
    }

    private void resizeLayoutParams(int marginTop, LinearLayout content) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        params.height = marginTop;
//        params.setMargins(0, marginTop, 0, 0);
        content.setLayoutParams(params);
    }

    private int dp(float value) {
        return DensityHelper.dip2px(_activity, value);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFile != null) {
            mFile.release();
        }
    }

}
