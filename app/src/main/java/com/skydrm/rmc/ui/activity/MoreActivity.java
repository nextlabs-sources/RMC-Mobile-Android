package com.skydrm.rmc.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.base.NxlDoc;
import com.skydrm.rmc.datalayer.repo.myvault.MyVaultFile;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.engine.DownloadManager;
import com.skydrm.rmc.engine.FileOperation;
import com.skydrm.rmc.engine.Render.RenderHelper;
import com.skydrm.rmc.engine.enumData.CmdOperate;
import com.skydrm.rmc.engine.enumData.FileFrom;
import com.skydrm.rmc.engine.enumData.FileType;
import com.skydrm.rmc.engine.eventBusMsg.HomePageToMorePageEvent;
import com.skydrm.rmc.engine.eventBusMsg.MyVaultFileShareEvent;
import com.skydrm.rmc.engine.eventBusMsg.MyVaultViewFileInfoEvent;
import com.skydrm.rmc.engine.eventBusMsg.ViewPageToMorePageEvent;
import com.skydrm.rmc.engine.eventBusMsg.favorites.FavoriteStatusChangeFromMorePageEvent;
import com.skydrm.rmc.errorHandler.GenericError;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.reposystem.IRemoteRepo;
import com.skydrm.rmc.reposystem.exception.FileDownloadException;
import com.skydrm.rmc.reposystem.localrepo.helper.Helper;
import com.skydrm.rmc.reposystem.remoterepo.ICancelable;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.rmc.ui.service.favorite.model.eventmsg.FavoriteFileUpdateFromMyVaultEvent;
import com.skydrm.rmc.ui.myspace.MySpaceFileItemHelper;
import com.skydrm.rmc.ui.myspace.myvault.model.adapter.RightsAdapter;
import com.skydrm.rmc.ui.myspace.myvault.view.task.GetMyVaultMetadataTask;
import com.skydrm.rmc.ui.service.offline.IOfflineCallback;
import com.skydrm.rmc.ui.service.offline.display.msg.MsgNotifySearchItem;
import com.skydrm.rmc.ui.service.offline.exception.OfflineException;
import com.skydrm.rmc.ui.project.architecture.IProjectFileInfoView;
import com.skydrm.rmc.ui.project.feature.centralpolicy.CentralRightsView;
import com.skydrm.rmc.ui.project.feature.centralpolicy.PolicyEvaluation;
import com.skydrm.rmc.ui.project.service.ProjectFileToViewParameter;
import com.skydrm.rmc.ui.widget.customcontrol.DrawableCenterButton;
import com.skydrm.rmc.utils.FileUtils;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;
import com.skydrm.rmc.utils.commonUtils.DensityHelper;
import com.skydrm.rmc.utils.commonUtils.IconHelper;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.INxlRights;
import com.skydrm.sdk.policy.Rights;
import com.skydrm.sdk.rms.rest.myVault.MyVaultMetaDataResult;
import com.skydrm.sdk.rms.types.RemoteViewResult2;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by aning on 5/5/2017.
 * <p>
 * The activity used to display file attribute & rights, and supply some operation entrance such as protect, share and print.
 */

public class MoreActivity extends BaseActivity implements IProjectFileInfoView {
    private static final DevLog log = new DevLog(MoreActivity.class.getSimpleName());
    private static final DateFormat sDF = new SimpleDateFormat("EEEE,MMMM d,yyyy");
    // used to partial download file.
    private static final int start = 0;
    private static final int length = 1024 * 16; // 16 KB
    // bind view
    @BindView(R.id.imageView_back)
    ImageView mBack;
    @BindView(R.id.iv_file_icon)
    ImageView mIvFileIcon;
    @BindView(R.id.tv_first)
    TextView mTvFileName;
    @BindView(R.id.tv_second)
    TextView mTvFilePath;
    @BindView(R.id.file_size)
    TextView mTvFileSize;
    @BindView(R.id.last_modified_time)
    TextView mTvModifiedTime;
    @BindView(R.id.make_favorite)
    ImageView mFavorite;
    @BindView(R.id.make_offline)
    ImageView mOffline;
    @BindView(R.id.share_file_layout)
    RelativeLayout mShareFileButton;
    @BindView(R.id.share_icon)
    ImageView mShareIcon;
    @BindView(R.id.share_with_text)
    TextView mShareWithText;
    @BindView(R.id.share_arrow)
    ImageView mShareArrow;
    @BindView(R.id.Rl_content_layout)
    RelativeLayout mRlContentLayout;
    @BindView(R.id.mark_layout)
    LinearLayout mMarkLayout;
    @BindView(R.id.favorite_layout)
    LinearLayout mFavoriteLayout;
    @BindView(R.id.tv_make_favorite)
    TextView mTvMakeFavorite;
    @BindView(R.id.tv_make_offline)
    TextView mTvMakeOffline;
    @BindView(R.id.offline_progressbar)
    ProgressBar mOfflineProgressBar;
    @BindView(R.id.no_rights_tip)
    TextView mTvRightsTip;
    @BindView(R.id.read_rights_loading_layout)
    LinearLayout mReadRightsLoading;
    @BindView(R.id.rl_fileAttribute)
    LinearLayout mLlFileAttribute;
    @BindView(R.id.view_rights_layout)
    RelativeLayout mRlRightsLayout;
    @BindView(R.id.normal_sub_view)
    RelativeLayout mSubView;
    @BindView(R.id.ll_content)
    LinearLayout mLlContent;
    @BindView(R.id.rights_view)
    GridView rightView;
    @BindView(R.id.offline_layout)
    LinearLayout mOfflineLayout;

    // display validity expiry
    @BindView(R.id.validity_layout)
    LinearLayout validityLayout;
    @BindView(R.id.validity_content)
    TextView validityContent;
    @BindView(R.id.rl_rights_container)
    RelativeLayout mRlRightsContainer;

    // download progress layout (need to load the view dynamically)
    private View mProgressView;
    private TextView mProgressValue;
    private ProgressBar mProgressBar;

    private Context mContext;
    // flag that if is nxl file
    private boolean bIsNxl = false;
    // flag that favorite if is checked
    private boolean bFavChecked = false;
    // flag that offline if is checked
    private boolean bOffChecked = false;
    // flag that whether is partial download.
    private boolean bPartialDownload = false;
    // clicked item node
    private INxFile mClickFileItem;
    // file finger: rights
    private INxlFileFingerPrint mINxlFileFingerPrint;
    // the document
    private File mWorkingFile;
    // download callback of DownloadManager.
    private DownloadManager.IDownloadCallBack mDownloadCallback;

    private FileType mFileType;
    private FileFrom mFileFrom;
    // for myVault view file info
    //private IMyVaultFile mIMyVaultFileEntry;
    private INxlFile mFileBase;

    private MyVaultMetaDataResult mMyVaultMetaData;
    // for project name
    private String mProjectName;
    // project file list fileBean
    private ProjectFileToViewParameter mProjectFileToViewParameter;
    // remote view result data
    private RemoteViewResult2.ResultsBean mRemoteViewResultBean;

    private int mProjectId;
    private CentralRightsView mCentralRightsView;
    private boolean offline;
    private RightsAdapter mRightsAdapter;

    private OfflineCallback mOfflineCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more3);

        ButterKnife.bind(this);
        EventBus.getDefault().register(this);

        mContext = this;
        // init
        initView();
    }

    /**
     * eventBus message handler for view page.
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onViewPageEventHandler(ViewPageToMorePageEvent eventMsg) {
        mWorkingFile = eventMsg.getWorkingFile();
        mClickFileItem = eventMsg.getClickFileItem();
        mFileType = eventMsg.getFileType();
        mFileFrom = eventMsg.getFileFrom();
        bIsNxl = eventMsg.isbNxlFile();
        mProjectFileToViewParameter = eventMsg.getProjectFileToViewParameter();
        mProjectId = eventMsg.getProjectId();
        mProjectName = eventMsg.getProjectName();
        mFileBase = eventMsg.getMyVaultFileEntry();
        mRemoteViewResultBean = eventMsg.getmRemoteViewResultBean();
        offline = eventMsg.isOffline();
    }

    /**
     * eventBus message handler for home page.
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onHomePageEventHandler(HomePageToMorePageEvent eventMsg) {
        mClickFileItem = eventMsg.getClickFileItem();
        mFileType = eventMsg.getFileType();
        mFileFrom = eventMsg.getFileFrom();
    }

    /**
     * eventBus message handler for myVault view file info.
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onMyVaultEventHandler(MyVaultViewFileInfoEvent eventMsg) {
        mFileBase = eventMsg.getMyVaultFileEntry();
        mWorkingFile = eventMsg.getWorkingFile();
        mFileType = eventMsg.getFileType();
        mFileFrom = eventMsg.getFileFrom();
        offline = eventMsg.isOffline();
    }

    // bind click listener
    @OnClick({R.id.imageView_back, R.id.make_favorite, R.id.make_offline, R.id.share_file_layout})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageView_back:
                finish();
                break;
            case R.id.make_favorite:
                if (mFileFrom == FileFrom.FILE_FROM_MYVAULT || mFileType == FileType.MY_VAULT_FILE) {
                    doMyVaultFavorite();
                } else {
                    doFavorite();
                }
                break;
            case R.id.make_offline:
                if (mFileFrom == FileFrom.FILE_FROM_MYVAULT || mFileType == FileType.MY_VAULT_FILE) {
                    doMyVaultOffline();
                } else if (mFileFrom == FileFrom.FILE_FROM_PROJECT_PAGE || mFileType == FileType.PROJECT_FILE) {
                    doProjectOffline();
                } else {
                    doOffline();
                }
                break;
            case R.id.share_file_layout:
                doShare();
                break;
            default:
                break;
        }
    }

    private void doMyVaultOffline() {
        if (mFileBase == null) {
            return;
        }
        if (!bOffChecked) {
            mOfflineCallback = new OfflineCallback(this);
            mFileBase.markAsOffline(mOfflineCallback);
        } else {
            mFileBase.unMarkAsOffline();
            bOffChecked = false;
            // set bg
            mOffline.setImageResource(R.drawable.download_offline_icon3);
            mTvMakeOffline.setText(getString(R.string.Make_available_offline));
            mTvMakeOffline.setTextColor(getResources().getColor(R.color.Black));
        }
        EventBus.getDefault().post(new MsgNotifySearchItem());
    }

    private void doProjectOffline() {

    }

    private void doFavorite() {
        // will is null when open file as third party.
        if (mClickFileItem == null) {
            return;
        }
        if (!bFavChecked) {
            // mark
            SkyDRMApp.getInstance().getRepoSystem().markAsFavorite(mClickFileItem);
            bFavChecked = true;
            // set bg
            mFavorite.setImageResource(R.drawable.favorited_icon3);
            mTvMakeFavorite.setText(getString(R.string.Favorited));
            mTvMakeFavorite.setTextColor(getResources().getColor(R.color.Gray));
        } else {
            // unmark
            SkyDRMApp.getInstance().getRepoSystem().unmarkAsFavorite(mClickFileItem);
            bFavChecked = false;
            // set bg
            mFavorite.setImageResource(R.drawable.make_as_favorite_icon3);
            mTvMakeFavorite.setText(getString(R.string.Make_as_favorite));
            mTvMakeFavorite.setTextColor(getResources().getColor(R.color.Black));
        }
        EventBus.getDefault().post(new FavoriteStatusChangeFromMorePageEvent(mClickFileItem, bFavChecked));
    }

    private void doMyVaultFavorite() {
        if (mFileBase == null) {
            return;
        }
        if (!bFavChecked) {
            mFileBase.markAsFavorite();
            bFavChecked = true;
            // set bg
            mFavorite.setImageResource(R.drawable.favorited_icon3);
            mTvMakeFavorite.setText(getString(R.string.Favorited));
            mTvMakeFavorite.setTextColor(getResources().getColor(R.color.Gray));
        } else {
            mFileBase.unMarkAsFavorite();
            bFavChecked = false;
            // set bg
            mFavorite.setImageResource(R.drawable.make_as_favorite_icon3);
            mTvMakeFavorite.setText(getString(R.string.Make_as_favorite));
            mTvMakeFavorite.setTextColor(getResources().getColor(R.color.Black));
        }
        EventBus.getDefault().post(new FavoriteFileUpdateFromMyVaultEvent());
    }

    private void doOffline() {
        // will is null when open file as third party.
        if (mClickFileItem == null) {
            return;
        }

        if (!bOffChecked) {
            // mark
            SkyDRMApp.getInstance().getRepoSystem().markAsOffline(mClickFileItem);
            bOffChecked = true;
            // set bg
            mOffline.setImageResource(R.drawable.available_offline_icon3);
            mTvMakeOffline.setText(getString(R.string.Available_offline));
            mTvMakeOffline.setTextColor(getResources().getColor(R.color.Gray));

            // note: the following code need to optimize, or else will cause the ui can't response rapidly sometimes.
            initDownload();
            File document = DownloadManager.getInstance().tryGetFile(mContext, mClickFileItem, mOfflineProgressBar, null, false, mDownloadCallback);
            if (document == null) {  // download
                mOfflineProgressBar.setVisibility(View.VISIBLE);
            }

        } else {
            // unmark
            SkyDRMApp.getInstance().getRepoSystem().unmarkAsOffline(mClickFileItem);
            bOffChecked = false;
            // set background and text
            mOffline.setImageResource(R.drawable.download_offline_icon3);
            mTvMakeOffline.setText(getString(R.string.Make_available_offline));
            mTvMakeOffline.setTextColor(getResources().getColor(R.color.Black));
            // cancel download ------ check if need this feature?
            DownloadManager.Downloader downloader = DownloadManager.getInstance().tryGetDownloader(mClickFileItem);
            if (downloader != null) {
                ICancelable downloadFileCancelHandler = downloader.getDownLoadCancelHandler();
                if (downloadFileCancelHandler != null) {
                    log.e("onClick:mDownloadFileCancel");
                    downloadFileCancelHandler.cancel();
                    mOfflineProgressBar.setVisibility(View.INVISIBLE);
                    // remove download
                    DownloadManager.getInstance().removeDownloader(mClickFileItem);
                }
            }

        }
    }

    private void doShare() {
        // the menu command operate "view file info" of myVault.
        if (mFileFrom == FileFrom.FILE_FROM_MYVAULT) {
            MyVaultFileShareEvent eventMsg = new MyVaultFileShareEvent(mFileBase,
                    mMyVaultMetaData,
                    FileFrom.FILE_FROM_MYVAULT,
                    CmdOperate.SHARE,
                    bIsNxl,
                    null);
            EventBus.getDefault().postSticky(eventMsg);
            startActivity(new Intent(MoreActivity.this, ProtectShareActivity.class));
            return;
        }

        // Simple remote view myVault's Office & pdf in viewPage.
        if (mFileType == FileType.MY_VAULT_FILE && mRemoteViewResultBean != null) {
            MyVaultFileShareEvent eventMsg = new MyVaultFileShareEvent(mFileBase,
                    mMyVaultMetaData,
                    FileFrom.FILE_FROM_VIEW_PAGE,
                    CmdOperate.SHARE,
                    bIsNxl,
                    mRemoteViewResultBean);
            EventBus.getDefault().postSticky(eventMsg);
            startActivity(new Intent(MoreActivity.this, ProtectShareActivity.class));
            return;
        }

        // other case(include myVault file that don't use simple remote view)
        // mIMyVaultFileEntry is null for other drives.
        MySpaceFileItemHelper.share(this, mClickFileItem);
    }

    private void doProtect() {
        MySpaceFileItemHelper.protect(this, mClickFileItem);
    }

    // Get myVault file rights.
    private void viewMyVaultRights() {
        if (mFileBase == null) {
            return;
        }
        new GetMyVaultMetadataTask((MyVaultFile) mFileBase, new LoadTask.ITaskCallback<GetMyVaultMetadataTask.Result, Exception>() {
            @Override
            public void onTaskPreExecute() {

            }

            @Override
            public void onTaskExecuteSuccess(GetMyVaultMetadataTask.Result results) {
                MyVaultMetaDataResult result = results.result;
                if (result == null) { // can't get rights.
                    mTvRightsTip.setVisibility(View.VISIBLE);
                    mRlRightsContainer.setVisibility(View.INVISIBLE);
                    mTvRightsTip.setText(mContext.getResources().getString(R.string.read_rights_failed));
                    disableShareBtn();
                } else {
                    mMyVaultMetaData = result;
                    List<String> rights = mMyVaultMetaData.getResults().getDetail().getRights();
                    // myVault files must be owner.
                    showNxlRights(rights, true);

                    // display validity expiry value in ui.
                    long startDate = mMyVaultMetaData.getResults().getDetail().getValidity().getStartDate();
                    long endDate = mMyVaultMetaData.getResults().getDetail().getValidity().getEndDate();
                    String content = "";
                    if (startDate == 0) {
                        if (endDate == 0) { // Never
                            validityContent.setText(getResources().getString(R.string.never_expire));
                        } else { // Relative or Absolute
                            content = getResources().getString(R.string.Until) + " " + sDF.format(new Date(endDate));
                            validityContent.setText(content);
                        }
                    } else {
                        if (endDate != 0) { // Date range
                            content = sDF.format(new Date(startDate)) + " - " + sDF.format(new Date(endDate));
                            validityContent.setText(content);
                        }
                    }
                }
            }

            @Override
            public void onTaskExecuteFailed(Exception e) {
                ExceptionHandler.handleException(MoreActivity.this, e);
            }
        }).run();
    }

    // display the nxl file rights.
    private void showNxlRights(List<String> rights, boolean isOwner) {
        if (mRightsAdapter == null) {
            mRightsAdapter = new RightsAdapter(this);
        }
        rightView.setAdapter(mRightsAdapter);
        mRightsAdapter.showRights(rights);

        // owner has all rights tip info.
//        if (isOwner) {
//            mTvStewardRightsTip.setVisibility(View.VISIBLE);
//        }
    }

    // show rights by fingerPrint
    private void showNxlRights(INxlFileFingerPrint fingerPrint) {
        if (mRightsAdapter == null) {
            mRightsAdapter = new RightsAdapter(mContext);
        }
        rightView.setAdapter(mRightsAdapter);
        mRightsAdapter.showRights(fingerPrint);
        mRightsAdapter.notifyDataSetChanged();

        // means have no any rights.
        if (!fingerPrint.hasRights()) {
            mTvRightsTip.setVisibility(View.VISIBLE);
        }

        // disable share button if not share rights
        if (!fingerPrint.hasShare() && !SkyDRMApp.getInstance().isStewardOf(fingerPrint.getOwnerID())) {
            disableShareBtn();
        }

        // tip steward have all rights
//        if (mFileType != null && mFileType == FileType.PROJECT_FILE) {  // for project nxl file.
//            if (mProjectFileToViewParameter != null && SkyDRMApp.getInstance().isStewardOf(mProjectFileToViewParameter.getOwner().getUserId()))
////                mTvStewardRightsTip.setVisibility(View.VISIBLE);
//        } else {  // for other nxl file
//            if (SkyDRMApp.getInstance().isStewardOf(fingerPrint.getOwnerID()))
//                mTvStewardRightsTip.setVisibility(View.VISIBLE);
//        }
    }

    private void initView() {
        // init file attribute
        initAttribute();
        // init mark status
        initFavoriteAndOffline();
        // init sub view
        if (mFileFrom == FileFrom.FILE_FROM_VIEW_PAGE) { // viewPage --> view file info
            displaySubView();
        } else if (mFileFrom == FileFrom.FILE_FROM_MYSPACE_PAGE) { // mySpace menu command --> view file info
            // judge if is nxl file roughly by postfix.
            if (mClickFileItem.getName().toLowerCase().endsWith(".nxl")) {
                bIsNxl = true;
                showMySpaceFileRights();
            } else {
                bIsNxl = false;
                displaySubView();
            }
        } else if (mFileFrom == FileFrom.FILE_FROM_MYVAULT) { // myVault menu command --> view file info & Files(Shared by me) menu command --> view file info.
            bIsNxl = true;
            displaySubView();
        }
    }

    //   mTvRightsTip.setVisibility(View.VISIBLE);
//            mTvStewardRightsTip.setVisibility(View.VISIBLE);
//            mTvRightsTip.setText(mContext.getResources().getString(R.string.read_rights_failed));
//    disableShareBtn();
    private void displaySubView() {
        if (bIsNxl) { // nxl
            if (mFileFrom == FileFrom.FILE_FROM_MYVAULT) { // the menu command operate "view file info" of myVault.
                mRlRightsLayout.setVisibility(View.GONE);
                validityLayout.setVisibility(View.GONE);
                initMyVaultSubView();
            } else {
                if (mRlRightsLayout.getVisibility() == View.INVISIBLE) {
                    mRlRightsLayout.setVisibility(View.VISIBLE);
                }

                if (validityLayout.getVisibility() == View.INVISIBLE) {
                    validityLayout.setVisibility(View.VISIBLE);
                }

                // forbid validity for project
//                if (mFileType == FileType.PROJECT_FILE) {
//                    validityLayout.setVisibility(View.GONE);
//                }

                readRights();
                if (offline) {
                    disableShareBtn();
                }
            }
        } else { // normal
            mRlRightsLayout.setVisibility(View.GONE);
            validityLayout.setVisibility(View.GONE);
            initProtectSubView();
        }

        // for project file, should disable share and mark
        if (mFileType == FileType.PROJECT_FILE) {
            mShareFileButton.setVisibility(View.GONE);
            mMarkLayout.setVisibility(View.GONE);
        }

        // for myVault file, should hide share when the nxl file is revoked or deleted.
        if (mFileFrom == FileFrom.FILE_FROM_MYVAULT || mFileFrom == FileFrom.FILE_FROM_VIEW_PAGE) {
            // Fix bug 56538. For myVault file hide share & mark favorite & mark offline sites.
//            MyVaultFile doc = (MyVaultFile) mFileBase;
//            if (doc != null && (doc.isRevoked() || doc.isDeleted())) {
//                mShareFileButton.setVisibility(View.GONE);
//            }
            mShareFileButton.setVisibility(View.GONE);
        }

//        // For view myVault file, should hide mark fav & off operate
        if (mFileType == FileType.MY_VAULT_FILE) {
            // Fix bug 56538. For myVault file hide share & mark favorite & mark offline sites.
//            MyVaultFile doc = (MyVaultFile) mFileBase;
//            if (doc != null && doc.isDeleted()) {
//                mMarkLayout.setVisibility(View.GONE);
//            }
            mMarkLayout.setVisibility(View.GONE);
        }
    }

    private void initMyVaultSubView() {
        mRlRightsLayout.setVisibility(View.VISIBLE);
        validityLayout.setVisibility(View.VISIBLE);

        // if the myVault file is simple remote view file and the RemoteViewResultBean is in local cache, then directly get.
        SkyDRMApp.RemoteViewerOfficePdfCache cache = SkyDRMApp.getInstance().getSession().getRemoteViwerCache();
        SkyDRMApp.RemoteViewerOfficePdfCache.Value value = cache.getCache(mFileBase.getPathId());
        if (isSimpleRemoteView() && value != null) {
            mRemoteViewResultBean = new RemoteViewResult2.ResultsBean();
            mRemoteViewResultBean.setViewerURL(value.getViewerURL());
            mRemoteViewResultBean.setCookies(value.getCookies());
            mRemoteViewResultBean.setPermissions(value.getPermissions());
            mRemoteViewResultBean.setOwner(value.isOwner());
            getSimpleRemoteViewRights();
            return;
        }

        NxlDoc doc = (NxlDoc) mFileBase;
        String localPath = doc.getLocalPath();
        if (localPath == null || localPath.isEmpty()) {
            // read rights by get myVault metaData
            viewMyVaultRights();
        } else {
            mWorkingFile = new File(localPath);
            readRights();
            if (offline) {
                disableShareBtn();
            }
        }
    }

    private void initProtectSubView() {
        mSubView.setVisibility(View.VISIBLE);
        DrawableCenterButton protectBtn = (DrawableCenterButton) mSubView.findViewById(R.id.button);
        // set drawableStart
        Drawable drawable = getResources().getDrawable(R.drawable.protect_white_icon3);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        protectBtn.setCompoundDrawables(drawable, null, null, null);
        protectBtn.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        protectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doProtect();
            }
        });
    }

    // Add download progress view
    private void showProgress() {
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.addRule(RelativeLayout.BELOW, R.id.horizontal_line);
        // set the margin top.
        lp.setMargins(0, DensityHelper.dip2px(this, 15), 0, 0);
        mRlContentLayout.addView(mProgressView, lp);
    }

    // init download progress.
    private void initProgress() {
        mProgressView = getLayoutInflater().inflate(R.layout.download_progressbar_layout3, null);
        mProgressBar = (ProgressBar) mProgressView.findViewById(R.id.progress);
        mProgressValue = (TextView) mProgressView.findViewById(R.id.textView_progress);
    }

    // init favorite and offline status
    private void initFavoriteAndOffline() {

        // set favorite layout's width dynamically(is the half of screen width since hide the offline layout temporarily)
        mFavoriteLayout.setLayoutParams(new LinearLayout.LayoutParams(CommonUtils.getScreenWidth((Activity) mContext) / 2, ViewGroup.LayoutParams.WRAP_CONTENT));
        if (mClickFileItem != null) { // is null when opening nxl file as third party or open myVault file

            // visible favorite layout only for myDrive
            if (mClickFileItem.getService().alias.equals(BoundService.MYDRIVE)) {
                mFavoriteLayout.setVisibility(View.VISIBLE);
            }

            // favorite
            if (mClickFileItem.isMarkedAsFavorite()) {
                bFavChecked = true;
                // set background and text
                mFavorite.setImageResource(R.drawable.favorited_icon3);
                mTvMakeFavorite.setText(getString(R.string.Favorited));
                mTvMakeFavorite.setTextColor(getResources().getColor(R.color.Gray));
            } else {
                bFavChecked = false;
                mFavorite.setImageResource(R.drawable.make_as_favorite_icon3);
                mTvMakeFavorite.setText(getString(R.string.Make_as_favorite));
                mTvMakeFavorite.setTextColor(getResources().getColor(R.color.Black));
            }

            // offline
            if (mClickFileItem.isMarkedAsOffline()) {
                bOffChecked = true;
                // set background and text
                mOffline.setImageResource(R.drawable.available_offline_icon3);
                mTvMakeOffline.setText(getString(R.string.Available_offline));
                mTvMakeOffline.setTextColor(getResources().getColor(R.color.Gray));

                // try get file(look at the file whether is in local, if not will download.)
                if (!mClickFileItem.getName().toLowerCase().endsWith(".nxl")) { // for nxl file, already display download progress in order to display rights.
                    File doc = DownloadManager.getInstance().tryGetFile(mContext, mClickFileItem, mOfflineProgressBar, null, false, mDownloadCallback);
                    // if the file is downloading, need also display the progress bar
                    if (doc == null && DownloadManager.getInstance().tryGetDownloader(mClickFileItem).isbIsDownloading()) {
                        // display the progress
                        mOfflineProgressBar.setVisibility(View.VISIBLE);
                    }
                }

            } else {
                bOffChecked = false;
                // set background and text
                mOffline.setImageResource(R.drawable.download_offline_icon3);
                mTvMakeOffline.setText(getString(R.string.Make_available_offline));
                mTvMakeOffline.setTextColor(getResources().getColor(R.color.Black));
            }
        } else if (mFileBase != null) {
            NxlDoc doc = (NxlDoc) mFileBase;
            // visible favorite layout
            mFavoriteLayout.setVisibility(View.VISIBLE);
            mOfflineLayout.setVisibility(View.VISIBLE);
            // favorite
            if (doc.isFavorite()) {
                bFavChecked = true;
                // set background and text
                mFavorite.setImageResource(R.drawable.favorited_icon3);
                mTvMakeFavorite.setText(getString(R.string.Favorited));
                mTvMakeFavorite.setTextColor(getResources().getColor(R.color.Gray));
            } else {
                bFavChecked = false;
                mFavorite.setImageResource(R.drawable.make_as_favorite_icon3);
                mTvMakeFavorite.setText(getString(R.string.Make_as_favorite));
                mTvMakeFavorite.setTextColor(getResources().getColor(R.color.Black));
            }
            if (doc.isOffline()) {
                bOffChecked = true;
                // set background and text
                mOffline.setEnabled(true);
                mOffline.setImageResource(R.drawable.available_offline_icon3);
                mTvMakeOffline.setText(getString(R.string.Available_offline));
                mTvMakeOffline.setTextColor(getResources().getColor(R.color.Gray));
            } else {
                int status = doc.getOperationStatus();
                if (status == INxlFile.PROCESS) {
                    bOffChecked = false;
                    mOffline.setEnabled(false);
                    mOffline.setImageResource(R.drawable.icon_offline_rotate);
                    mTvMakeOffline.setText(getString(R.string.updating));
                } else {
                    bOffChecked = false;
                    mOffline.setEnabled(true);
                    // set background and text
                    mOffline.setImageResource(R.drawable.download_offline_icon3);
                    mTvMakeOffline.setText(getString(R.string.Make_available_offline));
                    mTvMakeOffline.setTextColor(getResources().getColor(R.color.Black));
                }
            }
        }
    }

    // in order to get myDrive nxl simple remote file's rights & expiry value, need to download first.
    // (Because of the returned value from rms don't contains expiry value)
    private void tryGetFile() {
        initProgress();
        initDownload();
        mWorkingFile = DownloadManager.getInstance().tryGetFile(mContext, mClickFileItem, mProgressBar, mProgressValue, true, mDownloadCallback); // parameter: true  --- need opitimized
        // local file, then read rights directly
        if (mWorkingFile != null) {
            bIsNxl = RenderHelper.isNxlFile(mWorkingFile.getPath());
            if (!bIsNxl && mWorkingFile.getName().toLowerCase().endsWith(".nxl")) { // invalid nxl file
                GenericError.showUI(this, mContext.getString(R.string.hint_msg_nxl_invalid_type), true, false, true, null);
            }
            displaySubView();
        } else { // remote file
            // need to download, so display the progress.
            showProgress();
        }
    }

    /**
     * If file is in local cache(full size), get it; or else will download partial file content
     */
    private File getFileOrPartialDownload() {
        try {
            // if in cache then directly return the file or else return null(at the same time execute partial download in bg)
            mWorkingFile = SkyDRMApp.getInstance().getRepoSystem().getFilePartialContent(mClickFileItem, start, length, new IRemoteRepo.IDownLoadCallback() {
                @Override
                public void cancelHandler(ICancelable handler) {
                }

                @Override
                public void onFinishedDownload(boolean taskStatus, String localPath, @Nullable FileDownloadException e) {
                    mWorkingFile = new File(localPath);
                    readRights();
                }

                @Override
                public void progressing(long newValue) {
                }
            });

            return mWorkingFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    // get simple remote view rights: remote view repo(myDrive), myVault & project.
    private void getSimpleRemoteViewRights() {
        if (mRemoteViewResultBean != null) {
            int permissions = mRemoteViewResultBean.getPermissions();
            boolean isOwner = mRemoteViewResultBean.isOwner();
            Rights rights = new Rights();
            rights.setPermissions(permissions);
            rights.IntegerToRights();
            showNxlRights(rights.toList(), isOwner);
        }
    }

    // judge if is simple remote view.
    private boolean isSimpleRemoteView() {
        if (mFileType != null) {
            // myVault
            if (mFileType == FileType.MY_VAULT_FILE && RenderHelper.isNeedSimpleRemoteView(mFileBase.getName())) {
                return true;
            }
            // myDrive
            if (mClickFileItem != null
                    && mClickFileItem.getService().alias.equals(BoundService.MYDRIVE)
                    && RenderHelper.isNeedSimpleRemoteView(mClickFileItem.getName())) {
                return true;
            }

            // project --  next release
            if (mFileType == FileType.PROJECT_FILE && RenderHelper.isNeedSimpleRemoteView(mProjectFileToViewParameter.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get and show mySpace nxl file rights when execute menu command "view file info"
     */
    private void showMySpaceFileRights() {
        // need to display nxl rights
        mWorkingFile = getFileOrPartialDownload();
        if (mWorkingFile != null) { // have file cache in local(full size file)
            readRights();
        } else { // partial download
            // display progressBar loading
            mReadRightsLoading.setVisibility(View.VISIBLE);
            bPartialDownload = true;
        }

    }

    // read nxl file rights
    private void readRights() {

        // get the file rights that simple remote view
        // Note: since project simple remote viewer has not used in this release, so here need to add the condition: "mFileType != FileType.PROJECT_FILE"
        // --- now only myVault simple remote file;  for myDrive, since need to get rights expiry, so get rights by read nxl file header.
        if (isSimpleRemoteView() && mFileFrom == FileFrom.FILE_FROM_VIEW_PAGE && mFileType != FileType.PROJECT_FILE) {
            mRlRightsLayout.setVisibility(View.VISIBLE);
            validityLayout.setVisibility(View.VISIBLE);
            /**
             * since now need to display rights expiry, but don't have the value in remoteViewResultBean, for myVault, the metaData contains these,
             * so we try to get myVault metaData.
             */
            if (mFileType == FileType.MY_VAULT_FILE) {
                // read rights by get myVault metaData
                viewMyVaultRights();
                return;
            } else if (mFileType == FileType.DRIVE_FILE) {
                // getSimpleRemoteViewRights(); // discard this because of can't get rights expiry

                // now get rights and expiry value by download file for myDrive simple remote file.
                if (mWorkingFile == null) {
                    tryGetFile();
                    return;
                }
            }

        }

        // display progressBar loading
        mReadRightsLoading.setVisibility(View.VISIBLE);
        // get finger print
        FileOperation.readNxlFingerPrint(mContext, mWorkingFile, false, Constant.VIEW_TYPE_NORMAL,
                new FileOperation.IGetFingerPrintCallback() {
                    @Override
                    public void onGetFingerPrintFinished(INxlFileFingerPrint fingerPrint) {
                        if (mFileFrom == FileFrom.FILE_FROM_MYSPACE_PAGE) {
                            mShareFileButton.setVisibility(View.GONE);
                            mMarkLayout.setVisibility(View.GONE);
                        }
                        // hide read rights loading progress
                        mReadRightsLoading.setVisibility(View.GONE);
                        // read rights failed.
                        if (fingerPrint == null) {
                            mTvRightsTip.setVisibility(View.VISIBLE);
                            mTvRightsTip.setText(mContext.getResources().getString(R.string.read_rights_failed));
                            mRlRightsContainer.setVisibility(View.INVISIBLE);
                            disableShareBtn();
                            validityLayout.setVisibility(View.GONE);
                            return;
                        }

                        mINxlFileFingerPrint = fingerPrint;


                        // display rights validity
                        String expiry = mINxlFileFingerPrint.formatString();
                        validityContent.setText(expiry);

                        String fileName = mWorkingFile.getName();
                        // delete the downloaded partial file content after getting fingerPrint.
                        if (bPartialDownload) {
                            Helper.deleteFile(mWorkingFile);
                            mWorkingFile = null;
                        }

                        if (fingerPrint.hasTags()) {
                            initCentralRightsView(fingerPrint, fileName, fingerPrint.getAll());
                        } else if (fingerPrint.hasRights()) {
                            // show rights
                            showNxlRights(fingerPrint);
                        } else if (!fingerPrint.hasRights() && !fingerPrint.hasTags()) {
                            initCentralRightsView(fingerPrint, fileName, fingerPrint.getAll());
                        }

                    }
                });
    }

    private void initCentralRightsView(INxlFileFingerPrint fingerPrint, String fileName, Map<String, Set<String>> tags) {
        mCentralRightsView = new CentralRightsView(mContext);
        mCentralRightsView.paddingData(tags);
        mRlContentLayout.removeAllViews();
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mRlContentLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
        mRlContentLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        mRlContentLayout.addView(mCentralRightsView, layoutParams);

        requestData(fileName, fingerPrint);
    }

    private void requestData(String fileName, INxlFileFingerPrint fingerPrint) {
        //request file info data from rms.
        //for central policy protected file need show rights configured by tags,
        // in this case fingerprint doesn't contains information need.so we need to request information from rms.
//        ProjectFileInfoPresenter.present(this, mProjectId,
//                mProjectFileToViewParameter.getPathId());
        policyEvaluation(fileName, fingerPrint);
    }

    private void policyEvaluation(String fileName, INxlFileFingerPrint fingerPrint) {
        loading(true);
        final int rights = INxlRights.VIEW + INxlRights.EDIT + INxlRights.PRINT
                + INxlRights.SHARE + INxlRights.DOWNLOAD
                + INxlRights.WATERMARK + INxlRights.DECRYPT;
        PolicyEvaluation.evaluate(PolicyEvaluation.buildEvalBean(fingerPrint, fileName, rights, 0),
                new PolicyEvaluation.IEvaluationCallback() {
                    @Override
                    public void onEvaluated(String result) {
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                log.d("result" + result);
                                JSONObject responseObj = new JSONObject(result);
                                if (responseObj.has("results")) {
                                    JSONObject resultsObj = responseObj.getJSONObject("results");
                                    int rights = resultsObj.getInt("rights");
                                    JSONArray obligations = resultsObj.getJSONArray("obligations");
                                    List<String> rightsArray = NxlDoc.integer2Rights(rights);
                                    if (obligations != null && obligations.length() != 0) {
                                        if (rightsArray != null) {
                                            rightsArray.add("WATERMARK");
                                        }
                                    }
                                    mCentralRightsView.paddingRights(rightsArray, false);
                                    mCentralRightsView.invalidate();
                                    loading(false);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailed(Exception e) {
                        mCentralRightsView.paddingRights(null, true);
                        mCentralRightsView.invalidate();
                        loading(false);
                    }
                });
    }

    private void disableShareBtn() {
        mShareFileButton.setEnabled(false);
        // set button gray
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // setImageTintList only support when API above 21.
            mShareIcon.setImageTintList(ColorStateList.valueOf(Color.GRAY));
            mShareWithText.setTextColor(Color.GRAY);
            mShareArrow.setImageTintList(ColorStateList.valueOf(Color.GRAY));
        }
    }

    // init downloader
    private void initDownload() {
        mDownloadCallback = new DownloadManager.IDownloadCallBack() {
            @Override
            public void onDownloadFinished(boolean taskStatus, String localPath, @Nullable FileDownloadException e) {
                mOfflineProgressBar.setVisibility(View.INVISIBLE);
                // remove progress view
                mRlContentLayout.removeView(mProgressView);
                // remove the downloader
                DownloadManager.getInstance().removeDownloader(mClickFileItem);
                // will display the rights if is nxl file.
                if (taskStatus) {
                    mWorkingFile = new File(localPath);
                    bIsNxl = RenderHelper.isNxlFile(mWorkingFile.getPath());
                    if (!bIsNxl && mWorkingFile.getName().toLowerCase().endsWith(".nxl")) { // invalid nxl file
                        GenericError.showUI(MoreActivity.this, mContext.getString(R.string.hint_msg_nxl_invalid_type), true, false, true, null);
                    }
                    displaySubView();
                } else {
                    mRlRightsLayout.setVisibility(View.VISIBLE);
                    validityLayout.setVisibility(View.VISIBLE);
                    // exception handler
                    if (e != null) {
                        switch (e.getErrorCode()) {
                            case AuthenticationFailed:
                                SkyDRMApp.getInstance().getSession().sessionExpiredHandler(mContext);
                                break;
                            case NetWorkIOFailed:
                                ToastUtil.showToast(mContext, mContext.getResources().getString(R.string.excep_network_unavailable));
                                break;
                            case ExportedFileTooLarge:
                                ToastUtil.showToast(mContext, mContext.getResources().getString(R.string.excep_export_google_file));
                                break;
                            default:
                                ToastUtil.showToast(mContext, mContext.getResources().getString(R.string.excep_operation_failed));
                                break;
                        }
                    }
                }
            }

            @Override
            public void onDownloadProgress(long value) {
                if (DownloadManager.getInstance().tryGetDownloader(mClickFileItem) != null
                        && DownloadManager.getInstance().tryGetDownloader(mClickFileItem).isbIsDownloading()) {
                    if (mProgressBar != null) {
                        mProgressBar.setProgress((int) value);
                    }
                    if (mProgressValue != null) {
                        String text = String.format(Locale.getDefault(), "%d", value) + "%";
                        mProgressValue.setText(text);
                    }
                }
            }
        };
    }

    // init file attribute
    private void initAttribute() {
        if (mClickFileItem != null) {
            // file name
            String fileName = RenderHelper.isGoogleFile(mClickFileItem) ? RenderHelper.getGoogleExportFileName(mClickFileItem) : mClickFileItem.getName();
            mTvFileName.setText(fileName);
            mTvFileName.setPadding(0, DensityHelper.dip2px(mContext, 10), 0, 0); // set paddingTop 10 dp
            mIvFileIcon.setVisibility(View.VISIBLE);
            mIvFileIcon.setImageResource(IconHelper.getNxlIconResourceIdByExtension(mClickFileItem.getName()));
            // file path: DriveDisplayName + displayPath
            String displayPath = RenderHelper.isGoogleFile(mClickFileItem)
                    ? RenderHelper.appendGoogleFileExportPostfix(mClickFileItem.getUserDefinedStr(), mClickFileItem.getDisplayPath())
                    : mClickFileItem.getDisplayPath();
            String filePath = mClickFileItem.getService().getDisplayName() + displayPath;
            mTvFilePath.setText(filePath);
            mTvFilePath.setTextColor(Color.BLACK);

            // file size, N/A means Not Available.
            String fileSize = "";
            if (RenderHelper.isGoogleFile(mClickFileItem)) {
                if (mWorkingFile != null)
                    fileSize = FileUtils.transparentFileSize(mWorkingFile.length());
                else
                    fileSize = "N/A";
            } else {
                fileSize = FileUtils.transparentFileSize(mClickFileItem.getSize());
            }
            mTvFileSize.setText(fileSize);

            // modified time
            DateFormat modifyTime = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
            modifyTime.setTimeZone(TimeZone.getDefault());
            String modifyTimeResult = modifyTime.format(new Date(mClickFileItem.getLastModifiedTimeLong()));
            mTvModifiedTime.setText(modifyTimeResult);
        } else if (mWorkingFile != null) { // open nxl file as third party or open myVault file
            // file name
            mTvFileName.setText(mWorkingFile.getName());
            mIvFileIcon.setVisibility(View.VISIBLE);
            mIvFileIcon.setImageResource(IconHelper.getNxlIconResourceIdByExtension(mWorkingFile.getName()));
            // file path
            if (mFileType == FileType.PROJECT_FILE) {
                // for project, here set project name.
                mTvFilePath.setText(mProjectName);
                mTvFilePath.setTextColor(Color.BLACK);
                mTvFileName.setPadding(0, DensityHelper.dip2px(mContext, 10), 0, 0); // set paddingTop 10 dp
            } else {
                mTvFilePath.setVisibility(View.GONE);
                mTvFileName.setPadding(0, 0, 0, 0);
            }
            // file size
            mTvFileSize.setText(FileUtils.transparentFileSize(mWorkingFile.length()));
            // modified time
            DateFormat modifyTime = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
            modifyTime.setTimeZone(TimeZone.getDefault());
            String modifyTimeResult = modifyTime.format(new Date(mWorkingFile.lastModified()));
            mTvModifiedTime.setText(modifyTimeResult);
        } else if (mFileBase != null) { // myVault view file info or myVault simple remote file
            MyVaultFile f = (MyVaultFile) mFileBase;
            // file name
            mTvFileName.setText(f.getName());
            mTvFileName.setPadding(0, DensityHelper.dip2px(mContext, 10), 0, 0); // set paddingTop 10 dp
            mIvFileIcon.setVisibility(View.VISIBLE);
            mIvFileIcon.setImageResource(IconHelper.getNxlIconResourceIdByExtension(mFileBase.getName()));
            // file path
            mTvFilePath.setText(String.format("%s:%s", f.getSourceRepoName(), f.getSourceFilePathDisplay()));
            mTvFilePath.setTextColor(Color.BLACK);
            // file size
            mTvFileSize.setText(FileUtils.transparentFileSize(f.getFileSize()));
            // modified time
            DateFormat modifyTime = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
            modifyTime.setTimeZone(TimeZone.getDefault());
            String modifyTimeResult = modifyTime.format(new Date(f.getSharedOn()));
            mTvModifiedTime.setText(modifyTimeResult);
        } else if (mProjectFileToViewParameter != null) { // project simple remote file (next release)
            // file name
            mTvFileName.setText(mProjectFileToViewParameter.getName());
            mTvFileName.setPadding(0, DensityHelper.dip2px(mContext, 10), 0, 0); // set paddingTop 10 dp
            mIvFileIcon.setVisibility(View.VISIBLE);
            mIvFileIcon.setImageResource(IconHelper.getNxlIconResourceIdByExtension(mProjectFileToViewParameter.getName()));
            // file path
            mTvFilePath.setText(mProjectName);
            mTvFilePath.setTextColor(Color.BLACK);
            // file size
            mTvFileSize.setText(FileUtils.transparentFileSize(mProjectFileToViewParameter.getSize()));
            // modified time
            DateFormat modifyTime = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
            modifyTime.setTimeZone(TimeZone.getDefault());
            String modifyTimeResult = modifyTime.format(new Date(mProjectFileToViewParameter.getLastModified()));
            mTvModifiedTime.setText(modifyTimeResult);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // set favorite layout's width dynamically(is the half of screen width since hide the offline layout temporarily)
        mFavoriteLayout.setLayoutParams(new LinearLayout.LayoutParams(CommonUtils.getScreenWidth((Activity) mContext) / 2, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().removeAllStickyEvents();
        EventBus.getDefault().unregister(this);
        if (mOfflineCallback != null) {
            mOfflineCallback = null;
        }
    }

    @Override
    public void showFileInfo(String response) {
        parseResponse(response);
    }

    private void parseResponse(String response) {
        try {
            JSONObject responseObj = new JSONObject(response);
            if (responseObj.has("results")) {
                JSONObject resultsObj = responseObj.getJSONObject("results");
                if (resultsObj.has("fileInfo")) {
                    JSONObject fileInfoObj = resultsObj.getJSONObject("fileInfo");
                    String fileName = fileInfoObj.getString("name");
                    long fileLastModified = fileInfoObj.getLong("lastModified");
                    long fileSize = fileInfoObj.getLong("size");
                    JSONArray rightsArray = null;
                    if (fileInfoObj.has("rights")) {
                        rightsArray = fileInfoObj.getJSONArray("rights");
                    }
                    boolean owner = fileInfoObj.getBoolean("owner");
                    JSONObject tagsObj = null;
                    if (fileInfoObj.has("tags")) {
                        tagsObj = fileInfoObj.getJSONObject("tags");
                    }
                    updateData(fileName, fileLastModified, fileSize, tagsObj,
                            translateRightsArray(rightsArray), owner);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateData(String fileName, long fileLastModified, long fileSize,
                            JSONObject tagsObj, List<String> rights1, boolean owner) {
        List<String> rights = new ArrayList<>();
        rights.add(Constant.RIGHTS_VIEW);
        rights.add(Constant.RIGHTS_PRINT);
        rights.add(Constant.RIGHTS_DOWNLOAD);
        rights.add(Constant.RIGHTS_WATERMARK);

        mCentralRightsView.paddingRights(tagsObj, rights);
        mCentralRightsView.invalidate();
    }

    private List<String> translateRightsArray(JSONArray rightsArray) {
        List<String> rights = new ArrayList<>();
        if (rightsArray != null && rightsArray.length() != 0) {
            for (int i = 0; i < rightsArray.length(); i++) {
                try {
                    rights.add((String) rightsArray.get(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return rights;
    }

    @Override
    public void onLoading(boolean show) {
        loading(show);
    }

    private void loading(boolean show) {
        if (mCentralRightsView != null && mCentralRightsView.getLoadingLayout() != null) {
            mCentralRightsView.getLoadingLayout().setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onRequestError(final String errMsg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtil.showToast(getApplicationContext(), errMsg);
            }
        });
    }

    class OfflineCallback implements IOfflineCallback {
        private Context mCtx;

        OfflineCallback(Context ctx) {
            this.mCtx = ctx;
        }

        @Override
        public void onStarted() {
            if (mFileBase != null && mFileBase instanceof MyVaultFile) {
                MyVaultFile mvf = (MyVaultFile) mFileBase;
                mvf.setOperationStatus(INxlFile.PROCESS);
            }
            mOffline.setEnabled(false);
            mOffline.setImageResource(R.drawable.icon_offline_rotate);
            mTvMakeOffline.setText(getString(R.string.updating));
        }

        @Override
        public void onProgress() {

        }

        @Override
        public void onMarkDone() {
            mOffline.setEnabled(true);
            bOffChecked = true;
            // set bg
            mOffline.setImageResource(R.drawable.available_offline_icon3);
            mTvMakeOffline.setText(getString(R.string.Available_offline));
            mTvMakeOffline.setTextColor(getResources().getColor(R.color.Gray));
        }

        @Override
        public void onMarkFailed(OfflineException e) {
            if (mFileBase != null && mFileBase instanceof MyVaultFile) {
                MyVaultFile mvf = (MyVaultFile) mFileBase;
                mvf.setOperationStatus(INxlFile.MARK_ERROR);
            }
            if (mCtx == null) {
                return;
            }
            ExceptionHandler.handleException(mCtx, e);
            bOffChecked = false;
            // set bg
            mOffline.setEnabled(true);
            mOffline.setImageResource(R.drawable.download_offline_icon3);
            mTvMakeOffline.setText(getString(R.string.Make_available_offline));
            mTvMakeOffline.setTextColor(getResources().getColor(R.color.Black));
        }
    }
}
