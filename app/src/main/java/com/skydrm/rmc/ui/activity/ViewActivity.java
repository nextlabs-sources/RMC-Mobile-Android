package com.skydrm.rmc.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sap.ve.DVLCore;
import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.myvault.MyVaultFile;
import com.skydrm.rmc.datalayer.repo.project.ProjectFile;
import com.skydrm.rmc.datalayer.repo.project.QueryOwnerIdTask;
import com.skydrm.rmc.datalayer.repo.project.SharedWithProjectFile;
import com.skydrm.rmc.datalayer.repo.sharedwithme.SharedWithMeFile;
import com.skydrm.rmc.datalayer.repo.workspace.WorkSpaceFile;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.engine.DownloadManager;
import com.skydrm.rmc.engine.FileOperation;
import com.skydrm.rmc.engine.LogSystem;
import com.skydrm.rmc.engine.Render.Audio;
import com.skydrm.rmc.engine.Render.FileRenderProxy;
import com.skydrm.rmc.engine.Render.IFileRender;
import com.skydrm.rmc.engine.Render.PDFRender;
import com.skydrm.rmc.engine.Render.RenderHelper;
import com.skydrm.rmc.engine.Render.ThreeD;
import com.skydrm.rmc.engine.Render.Video;
import com.skydrm.rmc.engine.Render.WebViewRender;
import com.skydrm.rmc.engine.enumData.CmdOperate;
import com.skydrm.rmc.engine.enumData.FileFrom;
import com.skydrm.rmc.engine.enumData.FileType;
import com.skydrm.rmc.engine.eventBusMsg.ViewPageToMorePageEvent;
import com.skydrm.rmc.engine.eventBusMsg.ViewPageToProtectPageEvent;
import com.skydrm.rmc.engine.eventBusMsg.ViewPageToSharePageEvent;
import com.skydrm.rmc.errorHandler.GenericError;
import com.skydrm.rmc.errorHandler.IErrorResult;
import com.skydrm.rmc.reposystem.exception.FileDownloadException;
import com.skydrm.rmc.reposystem.localrepo.helper.Helper;
import com.skydrm.rmc.reposystem.remoterepo.ICancelable;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.rmc.ui.common.NxlItemHelper;
import com.skydrm.rmc.ui.service.fileinfo.IFileInfo;
import com.skydrm.rmc.ui.service.modifyrights.task.RemoteModifiedCheckTask;
import com.skydrm.rmc.ui.widget.LoadingDialog2;
import com.skydrm.rmc.ui.widget.customcontrol.DragImageView;
import com.skydrm.rmc.ui.widget.customcontrol.DragViewGroup;
import com.skydrm.rmc.ui.widget.customcontrol.DrawableCenterButton;
import com.skydrm.rmc.utils.FileHelper;
import com.skydrm.rmc.utils.NxCommonUtils;
import com.skydrm.rmc.utils.PatchedTextView;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;
import com.skydrm.rmc.utils.commonUtils.DensityHelper;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.rms.types.RemoteViewResult2;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by aning on 5/5/2017.
 * <p>
 * The activity used to view file by loading respective render.
 */

public class ViewActivity extends BaseActivity {
    private static final DevLog log = new DevLog(ViewActivity.class.getSimpleName());
    // for tmp path
    private static final String TMP_MAIL_PATH = "tmp/copyFromMailAttachment";
    private static final String TMP_DRIVE_PATH = "tmp/copyFromThirdPartyDrive";
    // bind view
    @BindView(R.id.imageView_close)
    ImageView mClose;
    @BindView(R.id.projects_files_info_fileName)
    PatchedTextView mFileName;
    @BindView(R.id.driveName)
    TextView mDriveName;
    @BindView(R.id.imageView_menu)
    ImageView mMoreMenu;
    @BindView(R.id.view_content)
    RelativeLayout mMainLayout;
    @BindView(R.id.arrow)
    DragImageView mArrow;
    @BindView(R.id.favorite_button)
    ImageButton mFavoriteButton;
    @BindView(R.id.view_short_line)
    View mViewShortLine;
    @BindView(R.id.offline_button)
    ImageButton mOfflineButton;
    @BindView(R.id.protect_button)
    ImageButton mProtectButton;
    @BindView(R.id.share_button)
    DrawableCenterButton mShareButton;
    @BindView(R.id.shortcut_menu)
    DragViewGroup mShortcutMenuLayout;
    @BindView(R.id.view_overlay)
    FrameLayout mOverlayFrameLayout;
    // root layout
    @BindView(R.id.viewActivity)
    RelativeLayout mRlRootLayout;
    // will use it when display VDS 3D
    @BindView(R.id.id_recyclerview_horizontal)
    RecyclerView m_recyclerView;
    // simple remote view loading
    @BindView(R.id.remote_view_loading)
    LinearLayout mSimpleRemoteViewLoadingLayout;
    private Context mContext;
    private boolean bIsFavoriteButtonSelected = false;
    private boolean bIsOfflineButtonSelected = false;
    private boolean bIsShortcutMenuHidden = false;
    // open file flag as third party.
    private boolean bAsThirdPartyOpen = false;
    // label file if have finished download, cancel download when is downloading will delete the tmp file.
    private boolean isDownloadFinished = false;
    // label if rotate screen
    private boolean bIsRotateScreen = false;
    //    private int mArrowStartBottom;
    private boolean bFirstClickArrow = false;
    //  the clicked file item of homepage list.
    private INxFile mClickFileItem;
    private File mWorkingFile;
    // flag that whether view file info from this page.
    private boolean bViewFileInfo = false;
    // control the video play
    private boolean bIsHomeIntent = false;
    // label current activity if is visible
    private boolean mIsActivityVisibility;
    // from the myVault
    private boolean mIsFromMyVault = false;
    // from share link with me
    private boolean mIsFromShareLink = false;
    private Uri mShareWithMeUri;
    // from projects file
    private boolean mIsFromProjects = false;
    // current file if is nxl file
    private boolean mIsNxlFile = false;
    // for download and cancel download
    private ICancelable mDownLoadCancelHandler;
    //    private IRemoteRepo.IDownLoadCallback mIDownLoadCallback = null;
    private DownloadFinish mIDownloadFinish = null;
    // for download progress
    private View mDownloadProgress;
    private ProgressBar mProgressBar;
    private TextView mProgressValue;
    private TextView mProgressFileName;
    // file render proxy
    private FileRenderProxy mFileRenderProxy;
    private IFileRender mIFileRender;
    private INxlFileFingerPrint mNxlFileFingerPrint;
    // control the video and audio play when the screen off.
    private BroadcastReceiver mBroadcastReceive;
    // the owner id(user id) of the project.
    private int mUserId = -1;
    // For share link file
    private String mInitFilePath;
    // remote view result data
    private RemoteViewResult2.ResultsBean mRemoteViewResultBean;

    private INxlFile mNxlBaseFile;

    private boolean isFromSharedWithMe;
    private boolean isFromWorkSpace;

    // project name
    private String mProjectName;
    private int mProjectId;

    private CheckRemoteModifiedCallback mCheckRemoteModifiedCallback;
    private LoadingDialog2 mLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view3);
        ButterKnife.bind(this);
        mContext = this;

        initView();
        initDownload();
        getAndRenderFile();
        initData();
        initBroadcastReceiver();
    }

    /**
     * init some layout view
     */
    private void initView() {
        // set the width of file name textView.
        int width = CommonUtils.getScreenWidth(this) - DensityHelper.dip2px(this, 128); // 24+54+24+16+10
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mFileName.getLayoutParams();
        params.width = width;
        mFileName.setLayoutParams(params);

        // set touch listener for the root layout.
        mRlRootLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // if the location of touch is in the title view, don't control the display and hidden of the shortcut menu.
                RelativeLayout titleLayout = (RelativeLayout) findViewById(R.id.title);
                if (CommonUtils.isInRangeOfView(titleLayout, event)) {
                    return false;
                }
                //  controlShortcutMenu();
                return false;
            }
        });

        // now remove the shortcut menu first, so hide it now
        mArrow.setVisibility(GONE);

        // set mArrow location dynamically
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mArrow.getLayoutParams();
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        mArrow.setLayoutParams(layoutParams);
    }

    /**
     * init downloader
     */
    private void initDownload() {
        mIDownloadFinish = new ViewActivity.DownloadFinish(this);
    }

    /**
     * init broadcast receiver that controls the audio and video pause play
     */
    private void initBroadcastReceiver() {
        // stop video and audio play when receive the screen off broadcast event that from system.
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mBroadcastReceive = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                    if (mIFileRender != null && mIFileRender instanceof Audio) {
                        ((Audio) mIFileRender).pause();
                    }
                    if (mIFileRender != null && mIFileRender instanceof Video) {
                        ((Video) mIFileRender).pausePlayVideo();
                    }
                }
                if (Intent.ACTION_SCREEN_ON.equals(action)) {
                }
            }
        };
        registerReceiver(mBroadcastReceive, filter);
    }

    /**
     * init mark status,drive name and hide some buttons
     */
    private void initData() {

        // init favorite and offline status
        if (mClickFileItem != null) {
            if (mClickFileItem.isMarkedAsFavorite()) {
                setFavoriteStatus();
            } else {
                unSetFavoriteStatus();
            }

            if (mClickFileItem.isMarkedAsOffline()) {
                setOfflineStatus();
            } else {
                unSetOfflineStatus();
            }
        }

        // set drive name
        if (mClickFileItem != null) {
            mDriveName.setText(mClickFileItem.getService().alias);
        }

        // disable protect button
        if (mIsNxlFile) {
            mProtectButton.setEnabled(false);
        }

        // for myVault and external file, don't support this, so disabled favorite button
        if (mIsFromMyVault || bAsThirdPartyOpen) {
            mFavoriteButton.setEnabled(false);
        }

        // for myVault and external file, don't support this, so disabled favorite button
        if (mIsFromMyVault || bAsThirdPartyOpen) {
            mOfflineButton.setEnabled(false);
        }
    }

    /**
     * init download progress bar
     */
    private void initProgressBar() {
        mDownloadProgress = getLayoutInflater().inflate(R.layout.view_download_of_viewactivity2, null);

        // click "close" button means cancel download here.
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isDownloadFinished) {  // is downloading
                    if (mIsFromMyVault || mIsFromProjects || isFromSharedWithMe) {
                        try {
                            // cancel myVault download file.
                            SkyDRMApp.getInstance().getSession().getRmsRestAPI().cancel();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        // cancel other drive download file.
                        if (mDownLoadCancelHandler != null) {
                            mDownLoadCancelHandler.cancel();
                            // remove the downloader
                            DownloadManager.getInstance().removeDownloader(mClickFileItem);
                        }
                    }
                }

                ViewActivity.this.finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
            }
        });

        mProgressBar = (ProgressBar) mDownloadProgress.findViewById(R.id.progress);
        mProgressValue = (TextView) mDownloadProgress.findViewById(R.id.textView_progress);
        mProgressFileName = (TextView) mDownloadProgress.findViewById(R.id.textView_fileName);
        if (mClickFileItem != null) {
            mProgressFileName.setText(mClickFileItem.getName());
        }

    }

    /**
     * Show the download progress layout.
     */
    private void showProgressView() {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mMainLayout.getLayoutParams();
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        mMainLayout.addView(mDownloadProgress, lp);
    }


    private void getAndRenderFile() {
        // judge the file source, from home items, from protect view or as third party open
        RenderHelper.FileSource fileSource = RenderHelper.judgeFileSource(getIntent());
        if (fileSource == RenderHelper.FileSource.FROM_HOME) {
            getAndRenderMySpaceFile();
        } else if (fileSource == RenderHelper.FileSource.FROM_MYVAULT) {
            getAndRenderMyVaultFile();
        } else if (fileSource == RenderHelper.FileSource.FROM_PROJECTS) {
            getAndRenderProjectFile();
        } else if (fileSource == RenderHelper.FileSource.FROM_SHARED_WITH_ME) {
            getAndRenderSharedWithMeFile();
        } else if (fileSource == RenderHelper.FileSource.OPEN_AS_THIRD_PARTY) {
            getAndRenderExternalFile();
        } else if (fileSource == RenderHelper.FileSource.FROM_WORKSPACE_VIEW) {
            getAndRenderWorkSpaceFile();
        }
    }

    /**
     * Get and render the files in mySpace
     */
    private void getAndRenderMySpaceFile() {
        bIsHomeIntent = true;
        mClickFileItem = (INxFile) getIntent().getSerializableExtra("click_file");
        String fileName = RenderHelper.isGoogleFile(mClickFileItem) ? RenderHelper.getGoogleExportFileName(mClickFileItem) : mClickFileItem.getName();
        mFileName.setText(fileName);

        // remote view for myDrive files which can't render in local.
        if (mClickFileItem != null && mClickFileItem.getService().alias.equals(BoundService.MYDRIVE)) {
            if (RenderHelper.isNeedSimpleRemoteView(mClickFileItem.getName())) {
                remoteViewRepoFile(RenderHelper.FileSource.FROM_HOME);
            } else {
                tryGetMySpaceFile();
            }

        } else { // local view for third party Drives
            tryGetMySpaceFile();
        }
    }

    /**
     * Get and render the files in myVault
     */
    private void getAndRenderMyVaultFile() {
        mIsFromMyVault = true;
        mArrow.setVisibility(View.INVISIBLE); // hide this button

        mNxlBaseFile = getIntent().getParcelableExtra("file_entry");
        if (RenderHelper.isNeedSimpleRemoteView(mNxlBaseFile.getName())) {
            mIsNxlFile = true;// myVault are all nxl files
            // set file name
            String filePathDisplay = mNxlBaseFile.getPathDisplay();
            mFileName.setText(filePathDisplay.substring(filePathDisplay.lastIndexOf("/") + 1));
            // remote view
            remoteViewRepoFile(RenderHelper.FileSource.FROM_MYVAULT);
        } else {
            tryGetMyVaultFile();
        }
    }

    /**
     * Get and render the files in project
     */
    private void getAndRenderProjectFile() {
        mIsFromProjects = true;
        mArrow.setVisibility(View.INVISIBLE); // hide this button

        mNxlBaseFile = getIntent().getParcelableExtra("file_entry");
        mProjectName = getIntent().getStringExtra(Constant.PROJECT_NAME);
        mProjectId = getIntent().getIntExtra(Constant.PROJECT_ID, -1);
        tryGetProjectFile();
    }

    private void getAndRenderSharedWithMeFile() {
        isFromSharedWithMe = true;
        mArrow.setVisibility(View.INVISIBLE); // hide this button
        mNxlBaseFile = getIntent().getParcelableExtra("file_entry");
        tryGetShareWithMeFile();
    }

    /**
     * Get and render the external files(or to open file as third party), include the share email link.
     */
    private void getAndRenderExternalFile() {
        bAsThirdPartyOpen = true;
        mArrow.setVisibility(View.INVISIBLE); // hide this button
        // parse the scheme -- content, file, http(s)
        Uri uri = Uri.parse(getIntent().getStringExtra("NXVIEW"));
        if (uri.toString().startsWith("content://")) {
            String attachFileName = RenderHelper.handleFileOnMail(mContext, uri);
            if (!TextUtils.isEmpty(attachFileName)) {
                mFileName.setText(attachFileName);
                mWorkingFile = RenderHelper.copyData(mContext, uri, attachFileName, TMP_MAIL_PATH);
                if (mWorkingFile == null) {
                    return;
                }

                // render document
                fileRender(mWorkingFile);

//                IBaseRepo repo = RepoFactory.getRepo(RepoType.TYPE_PROJECT);
//                if (repo instanceof ProjectRepo) {
//                    ProjectRepo projectRepo = (ProjectRepo) repo;
//                    mQueryProjectOwnerIdTaskCallback = new QueryProjectOwnerIdTaskCallback();
//                    projectRepo.queryProjectOwnerId(mWorkingFile, mQueryProjectOwnerIdTaskCallback);
//                } else {
//                    // render document
//                    fileRender(mWorkingFile);
//                }
            }
        } else if (uri.toString().startsWith("file://")) {
            String pathPrefix = "file://";
            String path = uri.toString().substring(pathPrefix.length());
            path = Uri.decode(path);
            int start = path.lastIndexOf("/");
            String fileName = path.substring(start + 1);
            if (start != -1) {
                mFileName.setText(fileName);
            } else {
                mFileName.setText(getResources().getString(R.string.protect_view));
            }

            mWorkingFile = RenderHelper.copyData(mContext, uri, fileName, TMP_DRIVE_PATH);
            if (mWorkingFile == null) {
                return;
            }

            // render document
            fileRender(mWorkingFile);

            // setAttrPara(); // may not need
        } else if (uri.toString().startsWith("http://") || uri.toString().startsWith("https://")) { // mWorkingFile is null in this case.
            mIsFromShareLink = true;
            mShareWithMeUri = uri;
            tryGetShareLinkFile();
        }

        if (mWorkingFile == null) {
            mMoreMenu.setVisibility(GONE);
        }
    }

    private void renderOfflineFile(INxlFile base, File workingFile) {
        mIsNxlFile = RenderHelper.isNxlFile(workingFile.getPath());
        String filename = workingFile.getName();
        mFileRenderProxy = new FileRenderProxy(mContext, mMainLayout, mIsNxlFile, workingFile, filename, Constant.VIEW_TYPE_NORMAL);
        mFileRenderProxy.setFileBase(base);
        mFileRenderProxy.buildOfflineRender(new FileRenderProxy.IBuildRenderCallback() {

            @Override
            public void onBuildRenderFinish() {
                mFileRenderProxy.fileRender();
                mIFileRender = mFileRenderProxy.getIFileRender();
            }
        });
    }

    private void getAndRenderWorkSpaceFile() {
        isFromWorkSpace = true;
        mArrow.setVisibility(View.INVISIBLE); // hide this button

        mNxlBaseFile = getIntent().getParcelableExtra("file_entry");
        tryGetWorkSpaceFile();
    }

    // bind lick listener
    @OnClick({R.id.imageView_close, R.id.imageView_menu, R.id.favorite_button, R.id.offline_button, R.id.protect_button, R.id.share_button, R.id.arrow})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageView_close:
                finish();
                break;
            case R.id.imageView_menu:
                doMenu();
                break;
            case R.id.favorite_button:
                doFavorite();
                break;
            case R.id.offline_button:
                doOffline();
                break;
            case R.id.protect_button:
                doProtect();
                break;
            case R.id.share_button:
                doShare();
                break;
            case R.id.arrow:
                arrowClick();
                break;
            default:
                break;
        }
    }

    private void doMenu() {
        bViewFileInfo = true;
        if (mIsFromMyVault) { // myVault
            NxlItemHelper.viewFileInfo(mContext, (IFileInfo) mNxlBaseFile);
        } else if (mIsFromProjects || isFromSharedWithMe || isFromWorkSpace) { // project
            NxlItemHelper.viewFileInfo(mContext, (IFileInfo) mNxlBaseFile);
        } else if (bAsThirdPartyOpen) { // external file
            ViewPageToMorePageEvent eventMsg = new ViewPageToMorePageEvent(mWorkingFile,
                    mClickFileItem,// is null
                    FileType.EXTERNAL_FILE,
                    FileFrom.FILE_FROM_VIEW_PAGE,
                    mIsNxlFile,
                    mRemoteViewResultBean);
            EventBus.getDefault().postSticky(eventMsg);
            startActivity(new Intent(this, MoreActivity.class));
        } else {
            ViewPageToMorePageEvent eventMsg = new ViewPageToMorePageEvent(mWorkingFile,// is null when simple remote view (myDrive)
                    mClickFileItem,
                    FileType.DRIVE_FILE,
                    FileFrom.FILE_FROM_VIEW_PAGE,
                    mIsNxlFile,
                    mRemoteViewResultBean);
            EventBus.getDefault().postSticky(eventMsg);
            startActivity(new Intent(this, MoreActivity.class));
        }

        // pause play video & audio
        pausePlay();
    }

    private void doFavorite() {
        // means should forbid to trigger button click event when shortcut is sliding.
        if (mShortcutMenuLayout.isbIsSlide()) {
            return;
        }
        // do favorite
        if (!bIsFavoriteButtonSelected) {
            // mark
            if (mClickFileItem != null) { // will is null when open file as third party.
                SkyDRMApp.getInstance().getRepoSystem().markAsFavorite(mClickFileItem);
            }
            // change button status
            setFavoriteStatus();
        } else {
            if (mClickFileItem != null) {
                SkyDRMApp.getInstance().getRepoSystem().unmarkAsFavorite(mClickFileItem);
            }
            unSetFavoriteStatus();
        }
    }

    private void doOffline() {
        // means should forbid to trigger button click event when shortcut is sliding.
        if (mShortcutMenuLayout.isbIsSlide()) {
            return;
        }

        // do offline -- the file must be in local for viewActivity, so don't need to download
        if (!bIsOfflineButtonSelected) {
            // mark
            if (mClickFileItem != null) { // will is null when open file as third party.
                SkyDRMApp.getInstance().getRepoSystem().markAsOffline(mClickFileItem);
            }
            // change button status
            setOfflineStatus();
        } else {
            if (mClickFileItem != null) {
                SkyDRMApp.getInstance().getRepoSystem().unmarkAsOffline(mClickFileItem);
            }
            unSetOfflineStatus();
        }
    }

    private void doProtect() {
        // means should forbid to trigger button click event when shortcut is sliding.
        if (mShortcutMenuLayout.isbIsSlide()) {
            return;
        }

        ViewPageToProtectPageEvent eventMsg = new ViewPageToProtectPageEvent(mWorkingFile,
                mClickFileItem,
                CmdOperate.PROTECT,
                FileFrom.FILE_FROM_VIEW_PAGE,
                mIsNxlFile);

        EventBus.getDefault().postSticky(eventMsg);
        startActivity(new Intent(this, ProtectShareActivity.class));

        // pause play video & audio
        pausePlay();
    }

    private void doShare() {
        // means the shortcut menu have been gone by gesture slid (here need to forbid click this button caused by gesture slide)
        // means should forbid to trigger button click event when shortcut is sliding.
        if (mShortcutMenuLayout.isbIsSlide()) {
            return;
        }

        ViewPageToSharePageEvent eventMsg = new ViewPageToSharePageEvent(mWorkingFile,
                mClickFileItem,
                CmdOperate.SHARE,
                FileFrom.FILE_FROM_VIEW_PAGE,
                mIsNxlFile);

        EventBus.getDefault().postSticky(eventMsg);
        startActivity(new Intent(this, ProtectShareActivity.class));

        // pause play video & audio
        pausePlay();
    }

    private void arrowClick() {

        if (!bFirstClickArrow) {
            bFirstClickArrow = true;
            RelativeLayout.LayoutParams para = (RelativeLayout.LayoutParams) mShortcutMenuLayout.getLayoutParams();
            para.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            para.addRule(RelativeLayout.ALIGN_PARENT_END);
            // set start bottom margin is 16dp
            para.bottomMargin = DensityHelper.dip2px(mContext, 16);
            mShortcutMenuLayout.setLayoutParams(para);

        } else {
            if (mArrow.isbIsSlided()) {
                RelativeLayout.LayoutParams para = (RelativeLayout.LayoutParams) mShortcutMenuLayout.getLayoutParams();
                para.bottomMargin = Math.abs(mArrow.getmOffFirstOffY());
                mShortcutMenuLayout.setLayoutParams(para);
            } else {
                RelativeLayout.LayoutParams para = (RelativeLayout.LayoutParams) mShortcutMenuLayout.getLayoutParams();
                para.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                para.addRule(RelativeLayout.ALIGN_PARENT_END);
                // set start bottom margin is 16dp
                para.bottomMargin = DensityHelper.dip2px(mContext, 16);
                mShortcutMenuLayout.setLayoutParams(para);
            }
        }

        // arrow button is moving along Y axis.
        if (mArrow.isbIsSliding()) {
            return;
        }
        mArrow.setVisibility(GONE);
        mShortcutMenuLayout.setVisibility(VISIBLE);

        // not authorized to view the nxl file, so not have any other rights.
        if (mIsNxlFile && mFileRenderProxy != null && mFileRenderProxy.getmNxlFileFingerPrint() == null) {
            mShareButton.setEnabled(false);
        }

        if (mIsNxlFile) {
            mProtectButton.setEnabled(false);
        }
    }

    private void setOfflineStatus() {
        mOfflineButton.setBackgroundResource(R.drawable.bg_offline_button_selected_2);
        mOfflineButton.setImageResource(R.drawable.offline_selected_icon_2);
        bIsOfflineButtonSelected = true;
        // changed the short line background into gray
        if (bIsFavoriteButtonSelected) {
            mViewShortLine.setBackgroundColor(getResources().getColor(R.color.Gray));
        }
    }

    private void unSetOfflineStatus() {
        mOfflineButton.setBackgroundResource(R.drawable.bg_offline_button_2);
        mOfflineButton.setImageResource(R.drawable.offline_icon_2);
        bIsOfflineButtonSelected = false;
        // changed the short line background into black
        if (!bIsFavoriteButtonSelected) {
            mViewShortLine.setBackgroundColor(getResources().getColor(R.color.transparent_Black));
        }
    }

    private void setFavoriteStatus() {
        mFavoriteButton.setBackgroundResource(R.drawable.bg_favorite_button_selected_2);
        mFavoriteButton.setImageResource(R.drawable.favorite_selected_icon_2);
        bIsFavoriteButtonSelected = true;
        // changed the short line background into gray
        if (bIsOfflineButtonSelected) {
            mViewShortLine.setBackgroundColor(getResources().getColor(R.color.Gray));
        }
    }

    private void unSetFavoriteStatus() {
        mFavoriteButton.setBackgroundResource(R.drawable.bg_favorite_button_2);
        mFavoriteButton.setImageResource(R.drawable.star_icon_2);
        bIsFavoriteButtonSelected = false;
        // changed the short line background into black
        if (!bIsOfflineButtonSelected) {
            mViewShortLine.setBackgroundColor(getResources().getColor(R.color.transparent_Black));
        }
    }

    public View getArrow() {
        return mArrow;
    }

    public boolean isbIsRotateScreen() {
        return bIsRotateScreen;
    }

    public FrameLayout getmOverlayFrameLayout() {
        return mOverlayFrameLayout;
    }


    /**
     * get the userId of project
     */
    public int getmUserId() {
        return mUserId;
    }

    /**
     * used in ThreeD render when render vds file.
     */
    public RecyclerView getM_recyclerView() {
        return m_recyclerView;
    }

    /**
     * Do some setting before remote view repo file
     */
    private void beforeRemoteViewRepo() {
        // display loading ui and disable More menu
        mSimpleRemoteViewLoadingLayout.setVisibility(VISIBLE);
        mMoreMenu.setEnabled(false);
        setMoreMenuTint(Color.GRAY);
    }

    /**
     * Do some setting after remote view repo file.
     */
    private void afterRemoteViewRepo() {
        // hide the loading ui and enable the menu
        mSimpleRemoteViewLoadingLayout.setVisibility(GONE);
        mMoreMenu.setEnabled(true);
        setMoreMenuTint(Color.BLACK);
    }

    /**
     * Set the tint for the "More" imageView button
     *
     * @param color set tint color
     */
    private void setMoreMenuTint(int color) {
        // setImageTintList only support when API above 21.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mMoreMenu.setImageTintList(ColorStateList.valueOf(color));
        }
    }

    /**
     * remote view repo file -- myDrive & myVault
     */
    private void remoteViewRepoFile(RenderHelper.FileSource fileSource) {
        mFileRenderProxy = new FileRenderProxy(mContext, mMainLayout, Constant.VIEW_TYPE_NORMAL);
        if (fileSource == RenderHelper.FileSource.FROM_HOME) { // myDrive
            // initial judgement whether is nxl file, not be precise.
            if (mClickFileItem.getName().endsWith(".nxl")) {
                mIsNxlFile = true;
            }
            // do setting
            beforeRemoteViewRepo();
            mFileRenderProxy.buildRemoteViewGeneric(mClickFileItem, new FileRenderProxy.IBuildRenderRemoteViewCallback() {
                @Override
                public void onBuildRenderRemoteViewFinish(RemoteViewResult2.ResultsBean result) {
                    if (result != null) {
                        mRemoteViewResultBean = result;
                        mFileRenderProxy.fileRender();
                        mIFileRender = mFileRenderProxy.getIFileRender();
                        // restore setting
                        afterRemoteViewRepo();
                    }
                }
            });

        } else if (fileSource == RenderHelper.FileSource.FROM_MYVAULT) { // myVault
            // do setting
            beforeRemoteViewRepo();
            mFileRenderProxy.buildRemoteViewGeneric(mNxlBaseFile, new FileRenderProxy.IBuildRenderRemoteViewCallback() {
                @Override
                public void onBuildRenderRemoteViewFinish(RemoteViewResult2.ResultsBean result) {
                    if (result != null) {
                        mRemoteViewResultBean = result;
                        mFileRenderProxy.fileRender();
                        mIFileRender = mFileRenderProxy.getIFileRender();
                        // restore setting
                        afterRemoteViewRepo();
                    }
                }
            });
        }
    }

    /**
     * remote view project file.
     */
    private void remoteViewProjectFile(int projectId, INxlFile f) {
        // do setting
        beforeRemoteViewRepo();
        mFileRenderProxy = new FileRenderProxy(mContext, mMainLayout, Constant.VIEW_TYPE_NORMAL);
        mFileRenderProxy.buildRenderRemoteViewProject(projectId, f, new FileRenderProxy.IBuildRenderRemoteViewCallback() {
            @Override
            public void onBuildRenderRemoteViewFinish(RemoteViewResult2.ResultsBean result) {
                if (result != null) {
                    mFileRenderProxy.fileRender();
                    mIFileRender = mFileRenderProxy.getIFileRender();
                    // restore setting
                    afterRemoteViewRepo();
                }
            }
        });
    }

    /**
     * try to get mySpace file from the local, if not, will get it from remote.
     */
    private void tryGetMySpaceFile() {
        initProgressBar();
        File doc = DownloadManager.getInstance().tryGetFile(mContext,
                mClickFileItem,
                mProgressBar,
                mProgressValue,
                true,
                mIDownloadFinish
        );
        // get cancel handler
        if (DownloadManager.getInstance().tryGetDownloader(mClickFileItem) != null) {
            mDownLoadCancelHandler = DownloadManager.getInstance().tryGetDownloader(mClickFileItem).getDownLoadCancelHandler();
        }
        // this file is loading
        if (doc == null && DownloadManager.getInstance().tryGetDownloader(mClickFileItem) != null &&
                DownloadManager.getInstance().tryGetDownloader(mClickFileItem).isbIsDownloading()) {
            // here also need to continue to display the download progress.
            showProgressView();
            // hidden arrow
            mArrow.setVisibility(View.INVISIBLE);
            // forbid the click action of Menu and Print until download finishsed
            mMoreMenu.setEnabled(false);
            setMoreMenuTint(Color.GRAY);
            return;
        }

        if (doc != null) {
            mWorkingFile = doc;
            // render document
            fileRender(mWorkingFile);
        } else {
            showProgressView();
            // hidden arrow
            mArrow.setVisibility(View.INVISIBLE);
            // forbid the click action of Menu and Print until download finishsed
            mMoreMenu.setEnabled(false);
            setMoreMenuTint(Color.GRAY);
        }
    }

    /**
     * this method used to try to get nxl file from myVault, if not cache in local, will download it, else will return null, means need to download.
     */
    private void tryGetMyVaultFile() {
        if (mNxlBaseFile == null) {
            return;
        }
        MyVaultFile doc = (MyVaultFile) mNxlBaseFile;

        mFileName.setText(mNxlBaseFile.getName());
        mDriveName.setText(mContext.getResources().getString(R.string.myVault));
        String localPath = doc.getLocalPath();
        if (localPath == null || localPath.isEmpty()) {
            // forbid the click action of Menu and Print until download finished
            mMoreMenu.setEnabled(false);
            setMoreMenuTint(Color.GRAY);
            initProgressBar();
            showProgressView();
            mProgressFileName.setText(mNxlBaseFile.getName());
            //downloadFile
            FileOperation.downloadNxlFile(mContext, 1,
                    mNxlBaseFile, mIDownloadFinish);
        } else {
            mWorkingFile = new File(localPath);
            if (doc.isOffline()
                    && !SkyDRMApp.getInstance().isNetworkAvailable()) {
                renderOfflineFile(mNxlBaseFile, mWorkingFile);
            } else {
                // render document
                fileRender(mWorkingFile);
            }
        }
    }

    /**
     * this method used to try to get nxl file from one project, if not cache in local, will download it, else will return null, means need to download.
     */
    private void tryGetProjectFile() {
        mFileName.setText(mNxlBaseFile.getName());
        mDriveName.setText(mContext.getResources().getString(R.string.project));

        if (mNxlBaseFile instanceof ProjectFile) {
            ProjectFile pf = (ProjectFile) mNxlBaseFile;
            mUserId = pf.getOwner().getUserId();

            String localPath = pf.getLocalPath();
            if (localPath == null || localPath.isEmpty()) {
                mMoreMenu.setEnabled(false);
                setMoreMenuTint(Color.GRAY);
                initProgressBar();
                showProgressView();
                mProgressFileName.setText(pf.getName());
                FileOperation.downloadNxlFile(mContext, 1, mNxlBaseFile, mIDownloadFinish);
            } else {
                mWorkingFile = new File(localPath);
                if (pf.isOffline()) {
                    if (SkyDRMApp.getInstance().isNetworkAvailable()) {
                        if (mCheckRemoteModifiedCallback == null) {
                            mCheckRemoteModifiedCallback = new CheckRemoteModifiedCallback(true);
                        }
                        pf.checkRemoteRightsModifiedAsync(mCheckRemoteModifiedCallback);
                    } else {
                        renderOfflineFile(mNxlBaseFile, mWorkingFile);
                    }
                } else {
                    if (mCheckRemoteModifiedCallback == null) {
                        mCheckRemoteModifiedCallback = new CheckRemoteModifiedCallback(false);
                    }
                    pf.checkRemoteRightsModifiedAsync(mCheckRemoteModifiedCallback);
                }
            }
        }
        if (mNxlBaseFile instanceof SharedWithProjectFile) {
            SharedWithProjectFile swpf = (SharedWithProjectFile) mNxlBaseFile;
            String localPath = swpf.getLocalPath();
            if (localPath == null || localPath.isEmpty()) {
                mMoreMenu.setEnabled(false);
                setMoreMenuTint(Color.GRAY);
                initProgressBar();
                showProgressView();
                mProgressFileName.setText(swpf.getName());
                FileOperation.downloadNxlFile(mContext, 1, mNxlBaseFile, mIDownloadFinish);
            } else {
                mWorkingFile = new File(localPath);
                if (swpf.isOffline()
                        && !SkyDRMApp.getInstance().isNetworkAvailable()) {
                    renderOfflineFile(mNxlBaseFile, mWorkingFile);
                } else {
                    // render document
                    fileRender(mWorkingFile);
                }
            }
        }
    }

    /**
     * Will be used when display project overlay.
     */
    public boolean isFromProject() {
        return mIsFromProjects;
    }

    /**
     * used to try to get share file link, will download it if not in the local.
     */
    private void tryGetShareLinkFile() {
        File sharedWithMeMountPoint = RenderHelper.getShareLinkFileMountPoint();
        if (sharedWithMeMountPoint == null) {
            ToastUtil.showToast(mContext, "Get sharedWithMe mount point failed!");
            return;
        }
        // now use transaction id as the tmp file name
        Uri u = Uri.parse(mShareWithMeUri.getFragment());
        String transactionId = u.getQueryParameter("d");
        String transactionCode = u.getQueryParameter("c");
        // now use transaction id as the tmp file name
        mFileName.setText("");
        mDriveName.setText("");
        mInitFilePath = Helper.nxPath2AbsPath(sharedWithMeMountPoint, transactionId);
        File document = Helper.getLocalSharedWithMeFile(mInitFilePath);
        // Download the file everytime
        // forbid the click action of Menu and Print until download finished
        mMoreMenu.setEnabled(false);
        setMoreMenuTint(Color.GRAY);
        initProgressBar();
        showProgressView();
        mProgressFileName.setText("");
        File file = new File(mInitFilePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileOperation.sharedWithMeDownloadFile(mContext, transactionId, transactionCode,
                true, file, -1, -1, true,
                mIDownloadFinish);
    }

    private void tryGetShareWithMeFile() {
        mFileName.setText(mNxlBaseFile.getName());
        mDriveName.setText("");

        SharedWithMeFile swmf = (SharedWithMeFile) mNxlBaseFile;

        String localPath = swmf.getLocalPath();
        if (localPath == null || localPath.isEmpty()) {
            // forbid the click action of Menu and Print until download finished
            mMoreMenu.setEnabled(false);
            setMoreMenuTint(Color.GRAY);
            initProgressBar();
            showProgressView();
            mProgressFileName.setText("");
            FileOperation.downloadNxlFile(mContext, 1,
                    mNxlBaseFile, mIDownloadFinish);
        } else {
            mWorkingFile = new File(localPath);
            if (swmf.isOffline()
                    && !SkyDRMApp.getInstance().isNetworkAvailable()) {
                renderOfflineFile(mNxlBaseFile, mWorkingFile);
            } else {
                // render document
                fileRender(mWorkingFile);
            }
        }
    }

    private void tryGetWorkSpaceFile() {
        mFileName.setText(mNxlBaseFile.getName());
        mDriveName.setText(mContext.getResources().getString(R.string.name_workspace));

        WorkSpaceFile wsf = (WorkSpaceFile) mNxlBaseFile;

        String localPath = wsf.getLocalPath();
        if (localPath == null || localPath.isEmpty()) {
            mMoreMenu.setEnabled(false);
            setMoreMenuTint(Color.GRAY);
            initProgressBar();
            showProgressView();
            mProgressFileName.setText(wsf.getName());
            FileOperation.downloadNxlFile(mContext, 1, mNxlBaseFile, mIDownloadFinish);
        } else {
            mWorkingFile = new File(localPath);
            if (wsf.isOffline()) {
                if (SkyDRMApp.getInstance().isNetworkAvailable()) {
                    if (mCheckRemoteModifiedCallback == null) {
                        mCheckRemoteModifiedCallback = new CheckRemoteModifiedCallback(true);
                    }
                    wsf.checkRemoteRightsModifiedAsync(mCheckRemoteModifiedCallback);
                } else {
                    renderOfflineFile(mNxlBaseFile, mWorkingFile);
                }
            } else {
                if (mCheckRemoteModifiedCallback == null) {
                    mCheckRemoteModifiedCallback = new CheckRemoteModifiedCallback(false);
                }
                wsf.checkRemoteRightsModifiedAsync(mCheckRemoteModifiedCallback);
            }
        }
    }

    /**
     * pause play video or audio when leave this view page.
     */
    private void pausePlay() {
        // pause play video & audio
        if (mIFileRender != null) {
            if (mIFileRender instanceof Audio && ((Audio) mIFileRender).getAudioPlayer().isPlaying()) {
                ((Audio) mIFileRender).pause();
            }
            if (mIFileRender instanceof Video && ((Video) mIFileRender).isVideoPlaying()) {
                ((Video) mIFileRender).pausePlayVideo();
            }
        }
    }

    /**
     * render file
     */
    private void fileRender(final File workingFile) {
        // render document
        mIsNxlFile = RenderHelper.isNxlFile(workingFile.getPath());
        String fileName = (mClickFileItem == null ? workingFile.getName() : mClickFileItem.getName());
        // handle google file name.
        if (mClickFileItem != null) {
            fileName = RenderHelper.isGoogleFile(mClickFileItem) ? RenderHelper.getGoogleExportFileName(mClickFileItem) : mClickFileItem.getName();
        }
        mFileRenderProxy = new FileRenderProxy(mContext, mMainLayout, mIsNxlFile, workingFile, fileName, Constant.VIEW_TYPE_NORMAL);
        mFileRenderProxy.setFileBase(mNxlBaseFile);
        mFileRenderProxy.buildRender(new FileRenderProxy.IBuildRenderCallback() {
            @Override
            public void onBuildRenderFinish() {
                mFileRenderProxy.fileRender();
                mIFileRender = mFileRenderProxy.getIFileRender();
                mNxlFileFingerPrint = mFileRenderProxy.getmNxlFileFingerPrint();

                if (mNxlFileFingerPrint != null) {
                    // display spotLight  --- now close the Spot Light.
//                    if (!SkyDRMApp.getInstance().isStewardOf(mNxlFileFingerPrint.getOwnerID())) {
//                        mSpotLight.setVisibility(VISIBLE);
//                    }
                    //  disabled share button
                    if (mIsNxlFile && !mNxlFileFingerPrint.hasShare()
                            && !SkyDRMApp.getInstance().isStewardOf(mNxlFileFingerPrint.getOwnerID())) {
                        mShareButton.setEnabled(false);
                    }
                    //send allow view log.
                    LogSystem.sendAllowViewLog(workingFile, mNxlFileFingerPrint.getDUID());
                }
            }
        });
    }

    @Deprecated
    private void fileRender(Uri uri) {
        new WebViewRender(mContext, mMainLayout).loadSharingLink(uri);
    }

    /**
     * Controls click event for 3D
     */
    public void toolbarButtonPressed(View view) {
        if (mIFileRender != null && mIFileRender instanceof ThreeD) {
            ((ThreeD) mIFileRender).toolbarButtonPressed(view);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // reset the flag.
        bViewFileInfo = false;

        if (mIFileRender != null && mIFileRender instanceof Video) {
            if (bIsHomeIntent) {
                ((Video) mIFileRender).resetVideo();
                bIsHomeIntent = false;
            } else {
                ((Video) mIFileRender).continuePlayVideo();
            }
        }
        if ((mIFileRender) != null && mIFileRender instanceof Audio && !((Audio) mIFileRender).getAudioPlayer().isPlaying()) {
            ((Audio) mIFileRender).continuePlay();
        }
    }

    // fixed bug 37516 that Open a 3D file in DropBox then back to opened 3D file skydrm page will cause the skydrm black screen, then will crash when do more action.
    // this activity comes to the foreground that exclude the switch between viewActivity and MoreActivity(file info).
    @Override
    protected void onRestart() {
        super.onRestart();

        if (bViewFileInfo) {
            return;
        }

        if (mIFileRender != null && mIFileRender instanceof ThreeD && ((ThreeD) mIFileRender).getmSurfaceView() != null) {
            ((ThreeD) mIFileRender).removeSubView();
            ((ThreeD) mIFileRender).addSubView();
            mMainLayout.requestLayout();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsActivityVisibility = true;
        if (mIFileRender != null && mIFileRender instanceof ThreeD) {
            if (((ThreeD) mIFileRender).getHSFViewFlag()) {
                ((ThreeD) mIFileRender).getmSurfaceView().clearTouches();
            }
        }
    }

    // fixed bug 37516
    // this activity comes to the background that exclude the switch between viewActivity and MoreActivity(file info).
    @Override
    protected void onPause() {
        super.onPause();

        if (bViewFileInfo) {
            return;
        }

        if (mIFileRender != null && mIFileRender instanceof ThreeD && ((ThreeD) mIFileRender).getmSurfaceView() != null) {
            ((ThreeD) mIFileRender).removeSubView();
            mMainLayout.requestLayout();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        log.e("------------onDestroy");
        // stop audio play
        if (mIFileRender != null && mIFileRender instanceof Audio && ((Audio) mIFileRender).getAudioPlayer().isPlaying()) {
            ((Audio) mIFileRender).stopPlay();
        }

        if (mIFileRender != null && mIFileRender instanceof WebViewRender) {
            WebView webView = ((WebViewRender) mIFileRender).getmWebView();
            if (webView != null) {
                webView.clearHistory();
                webView.loadUrl("about:blank");
                webView.destroy();
            }
        }

        if (mIFileRender != null && mIFileRender instanceof PDFRender) {
            PDFRender pdfRender = (PDFRender) mIFileRender;
            pdfRender.stopRender();
        }

        // delete the temporary converted 3D file if is nxl file.
//        if (mFileRenderProxy != null && mFileRenderProxy.isbConvertSucceed() &&  mIsNxlFile) {
//            if (!IconHelper.delFile(mFileRenderProxy.getTmpConvertPath())) {
//                Log.d(TAG, "delete failed:" + mFileRenderProxy.getTmpConvertPath());
//            }
//        }

        // delete the temporary decrypt file (/data/data/xxx)
        if (mFileRenderProxy != null && mFileRenderProxy.isbIsDecryptSucceed()) {
            String tmpDecryptPath = mFileRenderProxy.getDecryptedFilePath();
            if (!FileHelper.delFile(tmpDecryptPath)) {
                log.d("delete failed:" + tmpDecryptPath);
            }

            int iPos = tmpDecryptPath.lastIndexOf("/");
            FileHelper.delFile(getApplicationContext().getCacheDir().getPath() + "/ov" + tmpDecryptPath.substring(iPos + 1));
        }

        // delete some tmp shallowCopy data when open as third party
        if (bAsThirdPartyOpen) {
            if (mWorkingFile != null && !FileHelper.delFile(mWorkingFile.getPath())) {
                log.d("delete failed:" + mWorkingFile.getPath());
            }
        }

        // dispose the DVL core
        String name = mFileName.getText().toString();
        if (name.endsWith(".vds") && mIFileRender != null && mIFileRender instanceof ThreeD) {
            DVLCore dvlCore = ((ThreeD) mIFileRender).getCore();
            if (dvlCore != null) {
                try {
                    dvlCore.dispose();
                } finally {
                    dvlCore = null;
                }
            }
        }

        //release object avoid memory leak
        if (mIDownloadFinish != null) {
            mIDownloadFinish = null;
        }
        // unregister receiver.
        if (mBroadcastReceive != null) {
            unregisterReceiver(mBroadcastReceive);
        }

        if (mCheckRemoteModifiedCallback != null) {
            mCheckRemoteModifiedCallback = null;
        }

        // dispose the callback of hoops load task.
        if (mIFileRender instanceof ThreeD) {
            CommonUtils.releaseResource((ThreeD) mIFileRender);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (!isDownloadFinished) {  // is downloading
            if (mIsFromMyVault || mIsFromProjects || isFromSharedWithMe) {
                // cancel myVault download file.
                try {
                    SkyDRMApp.getInstance().getSession().getRmsRestAPI().cancel();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // cancel other drive download file.
                if (mDownLoadCancelHandler != null) {
                    mDownLoadCancelHandler.cancel();
                    // remove the downloader
                    DownloadManager.getInstance().removeDownloader(mClickFileItem);
                }
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Show our Operators/Modes/User Code menu options
        // These will get placed on the action bar if they can fit.

        if (mIFileRender != null
                && mIFileRender instanceof ThreeD
                && ((ThreeD) mIFileRender).getHSFViewFlag()) {
            getMenuInflater().inflate(R.menu.menu_viewfile_toolbars, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mIFileRender != null && mIFileRender instanceof ThreeD) {
            switch (item.getItemId()) {
                case R.id.menu_operators:
                    // Show operators toolbar
                    ((ThreeD) mIFileRender).reloadOperators();
                    return true;
                case R.id.menu_modes:
                    // Show user modes toolbar
                    ((ThreeD) mIFileRender).reloadModes();
                    return true;
                case R.id.menu_user_code:
                    // Show user code toolbar
                    ((ThreeD) mIFileRender).reloadUserCode();
                    return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        bIsRotateScreen = true;
        NxCommonUtils.setTextViewWidth(ViewActivity.this, mFileName);
        if (mIFileRender != null && mIFileRender instanceof Video) {
            if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                ((Video) mIFileRender).getSwitchScreenButton().setImageResource(R.drawable.spread_32);
                ((Video) mIFileRender).portraitDisplay();
            } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                ((Video) mIFileRender).getSwitchScreenButton().setImageResource(R.drawable.shrink_32);
                ((Video) mIFileRender).landscapeDisplay();
            }
        }

        if (mIFileRender != null && mIFileRender instanceof ThreeD) {
            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (((ThreeD) mIFileRender).getHSFViewFlag()) {
                    ((ThreeD) mIFileRender).reloadSubView();
                }
            } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (((ThreeD) mIFileRender).getHSFViewFlag()) {
                    ((ThreeD) mIFileRender).reloadSubView();
                }
            }
        }

        // set location dynamically
        if (mArrow != null && mShortcutMenuLayout != null) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mArrow.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            mArrow.setLayoutParams(layoutParams);

            // reset some flag
            mArrow.setbIsSlided(false);
            mArrow.setbFirstClick(false);

            RelativeLayout.LayoutParams para = (RelativeLayout.LayoutParams) mShortcutMenuLayout.getLayoutParams();
            para.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            para.addRule(RelativeLayout.ALIGN_PARENT_END);
            // set start bottom margin is 16dp
            para.bottomMargin = DensityHelper.dip2px(mContext, 16);
            mShortcutMenuLayout.setLayoutParams(para);
        }
    }

    private static class DownloadFinish implements DownloadManager.IDownloadCallBack {
        private WeakReference<ViewActivity> weakRef;

        private DownloadFinish(ViewActivity viewActivity) {
            weakRef = new WeakReference<>(viewActivity);
        }

        @Override
        public void onDownloadFinished(boolean taskStatus, String localPath, @Nullable FileDownloadException e) {
            final ViewActivity instance = weakRef.get();
            if (instance == null) {
                return;
            }
            // un-register BackButton response
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) instance.mMainLayout.getLayoutParams();
            lp.removeRule(RelativeLayout.CENTER_IN_PARENT);
            instance.mMainLayout.removeView(instance.mDownloadProgress);

            // remove the downloader
            if (!instance.mIsFromMyVault && !instance.mIsFromProjects && !instance.mIsFromShareLink && !instance.isFromSharedWithMe) {
                DownloadManager.getInstance().removeDownloader(instance.mClickFileItem);
            }

            if (taskStatus) {
                // for shared link
                if (instance.mIsFromShareLink) {
                    instance.mFileName.setText(localPath.substring(localPath.lastIndexOf("/") + 1));
                    instance.mMoreMenu.setVisibility(VISIBLE);
                    instance.setMoreMenuTint(Color.BLACK);
                    try {
                        RenderHelper.copyFileStreams(new File(instance.mInitFilePath), new File(localPath));
                        instance.mWorkingFile = new File(localPath);
                    } catch (IOException E) {
                        E.printStackTrace();
                    }
                } else {
                    instance.mWorkingFile = new File(localPath);
                }

                instance.isDownloadFinished = true;
                // render document
                instance.fileRender(instance.mWorkingFile);

                if (!instance.mIsFromProjects && !instance.mIsFromMyVault && !instance.bAsThirdPartyOpen) {
                    // instance.mArrow.setVisibility(View.VISIBLE); // ---- hide shortcut menu now.
                }
                // enable button
                instance.mMoreMenu.setEnabled(true);
                instance.setMoreMenuTint(Color.BLACK);
            } else {
                // download failed, will delete part tmp file.
                Helper.deleteFile(new File(localPath));

                // exception handler
                if (e != null) {
                    switch (e.getErrorCode()) {
                        case AuthenticationFailed:
                            SkyDRMApp.getInstance().getSession().sessionExpiredHandler(instance.mContext);
                            break;
                        case NetWorkIOFailed:
                            ToastUtil.showToast(instance.mContext, instance.mContext.getResources().getString(R.string.excep_network_unavailable));
                            break;
                        case ExportedFileTooLarge:
                            if (instance.mIsActivityVisibility) {
                                GenericError.showUI(instance, instance.getString(R.string.excep_export_google_file), true, false, true, null);
                            }
                            break;
                        default:
                            if (instance.mIsActivityVisibility) {
                                GenericError.showUI(instance, instance.getString(R.string.down_load_failed), true, true, false, new IErrorResult() {
                                    @Override
                                    public void cancelHandler() {
                                        instance.finish();
                                    }

                                    @Override
                                    public void okHandler() {  // retry to download file.
                                        if (instance.mIsFromMyVault) {
                                            // try get myVault file
                                            instance.tryGetMyVaultFile();
                                        } else if (instance.mIsFromProjects) {
                                            // try get project file
                                            instance.tryGetProjectFile();
                                        } else if (instance.mIsFromShareLink) {
                                            // try get shareWithMe file
                                            instance.tryGetShareLinkFile();
                                        } else if (instance.isFromSharedWithMe) {
                                            instance.tryGetShareWithMeFile();
                                        } else {
                                            // try get the file
                                            instance.tryGetMySpaceFile();
                                        }
                                    }
                                });
                            }
                            break;
                    }
                }
            }
        }

        @Override
        public void onDownloadProgress(long value) {
            ViewActivity instance = weakRef.get();
            if (instance != null) {
                if (instance.mProgressBar != null) {
                    instance.mProgressBar.setProgress((int) value);
                }
                if (instance.mProgressValue != null) {
                    String text = String.format(Locale.getDefault(), "%d", value) + "%";
                    instance.mProgressValue.setText(text);
                }
            }
        }
    }

    class CheckRemoteModifiedCallback implements LoadTask.ITaskCallback<RemoteModifiedCheckTask.Result, Exception> {
        boolean offline;

        CheckRemoteModifiedCallback(boolean offline) {
            this.offline = offline;
        }

        @Override
        public void onTaskPreExecute() {
            showLoadingDialog();
        }

        @Override
        public void onTaskExecuteSuccess(RemoteModifiedCheckTask.Result results) {
            dismissLoadingDialog();
            renderAfterCheck();
        }

        @Override
        public void onTaskExecuteFailed(Exception e) {
            dismissLoadingDialog();
            renderAfterCheck();
        }

        private void renderAfterCheck() {
            if (offline && !SkyDRMApp.getInstance().isNetworkAvailable()) {
                renderOfflineFile(mNxlBaseFile, mWorkingFile);
            } else {
                if (mWorkingFile != null) {
                    // render document
                    fileRender(mWorkingFile);
                }
            }
        }
    }

    class QueryProjectOwnerIdTaskCallback implements QueryOwnerIdTask.ITaskCallback<QueryOwnerIdTask.Result, Exception> {

        @Override
        public void onTaskPreExecute() {
            showLoadingDialog();
        }

        @Override
        public void onTaskExecuteSuccess(QueryOwnerIdTask.Result results) {
            dismissLoadingDialog();
            mUserId = results.ownerId;

            // render document
            fileRender(mWorkingFile);
            // setAttrPara(); // may not need
        }

        @Override
        public void onTaskExecuteFailed(Exception e) {
            e.printStackTrace();

            dismissLoadingDialog();

            // render document
            fileRender(mWorkingFile);
        }
    }

    private void showLoadingDialog() {
        if (mLoadingDialog == null) {
            mLoadingDialog = LoadingDialog2.newInstance();
        }
        mLoadingDialog.showModalDialog(this);
    }

    private void dismissLoadingDialog() {
        if (mLoadingDialog != null) {
            mLoadingDialog.dismissDialog();
            mLoadingDialog = null;
        }
    }
}
