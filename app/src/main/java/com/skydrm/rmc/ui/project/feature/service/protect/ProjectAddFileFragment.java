package com.skydrm.rmc.ui.project.feature.service.protect;

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
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.datalayer.repo.project.ProjectFile;
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
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.ui.base.BaseFragment;
import com.skydrm.rmc.ui.project.feature.files.view.ProjectLibraryActivity;
import com.skydrm.rmc.ui.project.feature.service.protect.download.LibraryFileDownloader;
import com.skydrm.rmc.ui.project.feature.service.protect.download.ProjectFileDownloader;
import com.skydrm.rmc.ui.project.feature.service.protect.download.WorkSpaceFileDownloader;
import com.skydrm.rmc.ui.project.feature.service.protect.view.AddFileFromProjectViewBuilder;
import com.skydrm.rmc.ui.project.feature.service.protect.view.AddFileFromWorkSpaceViewBuilder;
import com.skydrm.rmc.ui.project.feature.service.share.view.ShareToPersonViewBuilder;
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

public class ProjectAddFileFragment extends BaseFragment {
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
    @BindView(R.id.tv_change)
    TextView mTvChange;
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
    private static int PROJECT_CHANGE_PATH_REQUEST_CODE = 0x02;
    private InternalBundle mDataBundle;

    private String mProjectName;

    private File mWorkingFile;
    private String mFileName;

    private String mParentPathId;

    private IFileDownloader mDownloader;
    private IViewBuilder mViewBuilder;

    private IFileDownloader.ICallBack mDownloadCallback;

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


    public static ProjectAddFileFragment newInstance() {
        return new ProjectAddFileFragment();
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

    public void wrapContactParcel(Intent data) {
        if (mViewBuilder instanceof ShareToPersonViewBuilder) {
            ShareToPersonViewBuilder shareToPersonViewBuilder = (ShareToPersonViewBuilder) mViewBuilder;
            shareToPersonViewBuilder.wrapContactParcel(data);
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
        initData();
        tryGetFile();

        mTvFileName.setText(mFileName);
        setSaveDisplayLocation();

        initEvents();
    }

    private void setSaveDisplayLocation() {
        mTvPath.setText(String.format("%s%s", mProjectName, mParentPathId));
    }

    @Override
    protected View getLoadingTargetView() {
        return null;
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.fragment_project_add_file;
    }

    @Override
    protected boolean isBindEventBusHere() {
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mDownloadCallback != null) {
            mDownloadCallback = null;
        }
        CommonUtils.releaseResource(mViewBuilder);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDownloadCallback != null) {
            mDownloadCallback = null;
        }
        CommonUtils.releaseResource(mViewBuilder);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onExtractSwitchStateChange(MsgExtractSwitchStatus statusMsg) {
        if (mViewBuilder != null) {
            mViewBuilder.updateExtractStatus(statusMsg.checked);
        }
    }

    /**
     * eventBus message handler for display Office & 3D convert progress notification.
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConvertProgressEventHandler(ShowConvertProgressEvent eventMsg) {
        showConvertingProgress(eventMsg);
    }

    // get the changed path.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // project change path results
        if (requestCode == PROJECT_CHANGE_PATH_REQUEST_CODE && resultCode == RESULT_OK) {
            mParentPathId = data.getStringExtra(Constant.PROJECT_PATH_ID);
            mViewBuilder.updateParentPath(mParentPathId);
            setSaveDisplayLocation();
        }
    }

    private void initializeItemViewsByAction() {
        mRlSubContent.removeAllViews();
        mRlSubContent.addView(mViewBuilder.buildRoot(_activity));
    }

    private void initData() {
        mDataBundle = new InternalBundle(getArguments());
        mFileName = mDataBundle.mFileName;
        mParentPathId = mDataBundle.mParentPathId;

        IProject project = mDataBundle.mProject;
        mProjectName = project.getName();
    }

    private void tryGetFile() {
        View previewRoot = LayoutInflater.from(_activity).inflate(R.layout.layout_preview_file,
                mLlPreviewContainer, true);
        mPreviewFileLayout = previewRoot.findViewById(R.id.preview_file);

        mTipUserInfo = previewRoot.findViewById(R.id.tip_no_view_right);

        mProgressLayout = previewRoot.findViewById(R.id.download_progress);
        mDownloadingConverting = previewRoot.findViewById(R.id.projects_file_info_tv_download);
        mProgressBar = previewRoot.findViewById(R.id.progress);
        mProgressValue = previewRoot.findViewById(R.id.textView_progress);

        initDownloadListener();

        if (isAddFileFromWorkSpace()) {
            mDownloader = new WorkSpaceFileDownloader(mDataBundle.mClickFileItem,
                    mProgressBar, mProgressValue);
            mViewBuilder = new AddFileFromWorkSpaceViewBuilder(_activity, mRlRoot,
                    mDataBundle.mProject);
            mViewBuilder.updateParentPath(mParentPathId);
        }

        if (isAddFileFromLibrary() || isAddFileFromScanDoc()) {
            mDownloader = new LibraryFileDownloader(mDataBundle.mLibraryFilePath);
            mViewBuilder = new AddFileFromWorkSpaceViewBuilder(_activity, mRlRoot,
                    mDataBundle.mProject);
            mViewBuilder.updateParentPath(mParentPathId);
        }

        if (isAddFileFromProject()) {
            mDownloader = new ProjectFileDownloader(mDataBundle.mFileItem);
            mViewBuilder = new AddFileFromProjectViewBuilder(_activity, mRlRoot,
                    mDataBundle.mProject, (ProjectFile) mDataBundle.mFileItem);
            mViewBuilder.updateParentPath(mParentPathId);
        }

        if (isShareToPerson()) {
            mLlChangePath.setVisibility(View.GONE);
            mDownloader = new ProjectFileDownloader(mDataBundle.mFileItem);
            mViewBuilder = new ShareToPersonViewBuilder(_activity, mRlRoot, mDataBundle.mProject, mDataBundle.mFileItem);
        }

        if (mViewBuilder != null && mViewBuilder.isPreviewNeeded()) {
            setPreviewLayoutParams(mPreviewFileLayout, 3);
        } else {
            mRlPreviewRoot.setVisibility(View.GONE);
        }

        mDownloader.tryGetFile(_activity, mDownloadCallback);
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

    // init downloader
    private void initDownloadListener() {
        mDownloadCallback = new IFileDownloader.ICallBack() {
            @Override
            public void onPreDownload() {
                showProgress();
                mBtAddFile.setEnabled(false);
            }

            @Override
            public void onDownloadFinished(String localPath) {
                hideProgress();
                initializeItemViewsByAction();

                mBtAddFile.setEnabled(true);

                mWorkingFile = new File(localPath);

                if (mViewBuilder.isPreviewNeeded()) {
                    fileRender(mWorkingFile, mPreviewFileLayout);
                }

                // do some special handler for nxl file
                boolean bIsNxl = RenderHelper.isNxlFile(mWorkingFile.getPath());
                if (bIsNxl) {
                    tryReadNxlRights();
                } else {
                    mViewBuilder.bindFingerPrint(null, mWorkingFile, false);
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

    // read nxl file rights
    private void tryReadNxlRights() {
        // display progressBar loading
        mViewBuilder.showLoading(-1);
        // get finger print
        FileOperation.readNxlFingerPrint(_activity, mWorkingFile, false, Constant.VIEW_TYPE_PREVIEW,
                new FileOperation.IGetFingerPrintCallback() {
                    @Override
                    public void onGetFingerPrintFinished(INxlFileFingerPrint fingerPrint) {
                        mViewBuilder.hideLoading(-1);
                        mViewBuilder.bindFingerPrint(fingerPrint, mWorkingFile, true);
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

    // file render
    private void fileRender(File workingFile, RelativeLayout mainLayout) {
        // render document
        if (interceptFileRender(mWorkingFile)) {
            return;
        }

        boolean bIsNxl = RenderHelper.isNxlFile(workingFile.getPath());
        String fileName = (TextUtils.isEmpty(mFileName) ? workingFile.getName() : mFileName);
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
        if (render == null) {
            return;
        }
        if (mIvPreview == null) {
            return;
        }
        //show small preview window unsupported file tip.
        mTvPreviewTip.setVisibility(View.VISIBLE);
        mTvPreviewTip.setText(getString(R.string.Not_view_3d));
        mLlPreviewContainer.setVisibility(View.GONE);
    }

    private void initEvents() {

        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishParent();
            }
        });

        if (isAddFileFromScanDoc()) {
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finishParent();
                }
            });
        } else {
            mViewBuilder.configNavigator(mToolbar);
        }
        mViewBuilder.configButton(mBtAddFile);

        if (mViewBuilder.isPreviewNeeded()) {
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

        mTvChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mTvChangeSaveLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goSelectPath();
            }
        });
    }

    private boolean isAddFileFromProject() {
        return mDataBundle.isAddFileFromProject();
    }

    private boolean isAddFileFromWorkSpace() {
        return mDataBundle.isAddFileFromWorkSpace();
    }

    private boolean isAddFileFromLibrary() {
        return mDataBundle.isAddFileFromLibrary();
    }

    private boolean isShareToPerson() {
        return mDataBundle.isShareToPerson();
    }

    private boolean isAddFileFromScanDoc() {
        return mDataBundle.isAddFileFromScanDoc();
    }

    private void goSelectPath() {
        if (mDataBundle.mProject == null) {
            return;
        }
        Intent intent = new Intent(_activity, ProjectLibraryActivity.class);
        intent.setAction(Constant.ACTION_PROJECT_ADD_FILE);
        intent.putExtra(Constant.PROJECT_DETAIL, (Parcelable) mDataBundle.mProject);
        startActivityForResult(intent, PROJECT_CHANGE_PATH_REQUEST_CODE);
    }

    class InternalBundle {
        String mAction;
        IProject mProject;
        String mParentPathId;
        //3rd library file entry.
        INxFile mClickFileItem;
        //Project file entry.
        INxlFile mFileItem;
        String mFileName;
        String mDisplayParentPath;
        String mLibraryFilePath;

        InternalBundle(Bundle bundle) {
            if (bundle == null) {
                return;
            }
            mAction = bundle.getString(Constant.KEY_ACTION_BELONG_ADD_FILE_FRAG);
            bindDataByAction(bundle);
        }

        void bindDataByAction(Bundle bundle) {
            if (Constant.VALUE_ADD_FILE_FROM_PROJECT.equals(mAction)) {
                this.mProject = bundle.getParcelable(Constant.PROJECT_DETAIL);
                this.mParentPathId = bundle.getString(Constant.PROJECT_PARENT_PATH_ID);
                this.mFileItem = bundle.getParcelable(Constant.PROJECT_FILE_ENTRY);
                if (mFileItem != null) {
                    this.mFileName = mFileItem.getName();
                }
            } else if (Constant.VALUE_ADD_FILE_FROM_WORKSPACE.equals(mAction)) {
                this.mProject = bundle.getParcelable(Constant.PROJECT_DETAIL);
                this.mParentPathId = bundle.getString(Constant.PROJECT_PARENT_PATH_ID);
                this.mClickFileItem = (INxFile) bundle.getSerializable(Constant.LIBRARY_FILE_ENTRY);
                if (mClickFileItem != null) {
                    mFileName = mClickFileItem.getName();
                }
            } else if (Constant.VALUE_SHARE_TO_PERSON.equals(mAction)) {
                this.mProject = bundle.getParcelable(Constant.PROJECT_DETAIL);
                this.mFileItem = bundle.getParcelable(Constant.PROJECT_FILE_ENTRY);
                this.mDisplayParentPath = mProject.getDisplayName() + mFileItem.getParent();
                if (mFileItem != null) {
                    this.mFileName = mFileItem.getName();
                }
            } else if (Constant.VALUE_ADD_FILE_FROM_LIBRARY.equals(mAction)) {
                this.mProject = bundle.getParcelable(Constant.PROJECT_DETAIL);
                this.mParentPathId = bundle.getString(Constant.PROJECT_PARENT_PATH_ID);
                this.mLibraryFilePath = bundle.getString(Constant.LIBRARY_FILE_ENTRY);
                if (mLibraryFilePath != null) {
                    mFileName = new File(mLibraryFilePath).getName();
                }
            } else if (Constant.VALUE_ADD_FILE_FROM_SCAN_DOC.equals(mAction)) {
                this.mProject = bundle.getParcelable(Constant.PROJECT_DETAIL);
                this.mParentPathId = bundle.getString(Constant.PROJECT_PARENT_PATH_ID);
                this.mLibraryFilePath = bundle.getString(Constant.LIBRARY_FILE_ENTRY);
                if (mLibraryFilePath != null) {
                    mFileName = new File(mLibraryFilePath).getName();
                }
            }
        }

        private boolean isAddFileFromProject() {
            return mAction.equals(Constant.VALUE_ADD_FILE_FROM_PROJECT);
        }

        private boolean isAddFileFromWorkSpace() {
            return mAction.equals(Constant.VALUE_ADD_FILE_FROM_WORKSPACE);
        }

        private boolean isAddFileFromLibrary() {
            return mAction.equals(Constant.VALUE_ADD_FILE_FROM_LIBRARY);
        }

        private boolean isShareToPerson() {
            return mAction.equals(Constant.VALUE_SHARE_TO_PERSON);
        }

        private boolean isAddFileFromScanDoc() {
            return mAction.equals(Constant.VALUE_ADD_FILE_FROM_SCAN_DOC);
        }
    }

    private int dp(float value) {
        return DensityHelper.dip2px(_activity, value);
    }
}
