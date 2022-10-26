package com.skydrm.rmc.ui.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.engine.DownloadManager;
import com.skydrm.rmc.engine.ExecuteCommandImpl;
import com.skydrm.rmc.engine.FileOperation;
import com.skydrm.rmc.engine.Render.Audio;
import com.skydrm.rmc.engine.Render.FileRenderProxy;
import com.skydrm.rmc.engine.Render.IFileRender;
import com.skydrm.rmc.engine.Render.RenderHelper;
import com.skydrm.rmc.engine.Render.ThreeD;
import com.skydrm.rmc.engine.Render.WebViewRender;
import com.skydrm.rmc.engine.enumData.CmdOperate;
import com.skydrm.rmc.engine.enumData.FileFrom;
import com.skydrm.rmc.engine.eventBusMsg.ChangeExpiryDateEvent;
import com.skydrm.rmc.engine.eventBusMsg.CommandOperateEvent;
import com.skydrm.rmc.engine.eventBusMsg.ConvertResultNotifyEvent;
import com.skydrm.rmc.engine.eventBusMsg.ProjectAddCompleteNotifyEvent;
import com.skydrm.rmc.engine.eventBusMsg.ProtectCompleteNotifyEvent;
import com.skydrm.rmc.engine.eventBusMsg.ShareCompleteNotifyEvent;
import com.skydrm.rmc.engine.eventBusMsg.ShowConvertProgressEvent;
import com.skydrm.rmc.engine.eventBusMsg.ViewFileResultNotifyEvent;
import com.skydrm.rmc.engine.intereface.IAddComplete;
import com.skydrm.rmc.engine.intereface.IExecuteCommand;
import com.skydrm.rmc.engine.intereface.IProtectComplete;
import com.skydrm.rmc.engine.intereface.IShareComplete;
import com.skydrm.rmc.engine.watermark.EditWatermarkHelper;
import com.skydrm.rmc.engine.watermark.WatermarkSetInvalidEvent;
import com.skydrm.rmc.errorHandler.GenericError;
import com.skydrm.rmc.exceptions.ExceptionDialog;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.reposystem.exception.FileDownloadException;
import com.skydrm.rmc.reposystem.exception.FileUploadException;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.reposystem.types.NXDocument;
import com.skydrm.rmc.reposystem.types.NXFolder;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.ui.myspace.myvault.model.adapter.RightsAdapter;
import com.skydrm.rmc.ui.project.feature.centralpolicy.ClassifyAdapter;
import com.skydrm.rmc.ui.project.feature.centralpolicy.ClassifyItem;
import com.skydrm.rmc.ui.project.feature.centralpolicy.RightsSpecifyPageAdapter;
import com.skydrm.rmc.ui.project.feature.files.view.ProjectLibraryActivity;
import com.skydrm.rmc.ui.project.feature.service.protect.MsgExtractSwitchStatus;
import com.skydrm.rmc.ui.project.service.ProjectOperateActivity;
import com.skydrm.rmc.ui.widget.NoScrollViewPager;
import com.skydrm.rmc.ui.widget.customcontrol.CommentWidget;
import com.skydrm.rmc.ui.widget.customcontrol.EditWatermarkWidget;
import com.skydrm.rmc.ui.widget.customcontrol.FlowLayout;
import com.skydrm.rmc.ui.widget.customcontrol.SafeProgressDialog;
import com.skydrm.rmc.ui.widget.customcontrol.rights.ADHocRightsDisplayView;
import com.skydrm.rmc.utils.FileUtils;
import com.skydrm.rmc.utils.ViewUtils;
import com.skydrm.rmc.utils.commonUtils.CalenderUtils;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;
import com.skydrm.rmc.utils.commonUtils.DensityHelper;
import com.skydrm.rmc.utils.commonUtils.ExpiryChecker;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.policy.Expiry;
import com.skydrm.sdk.policy.Obligations;
import com.skydrm.sdk.policy.Rights;
import com.skydrm.sdk.rms.rest.myVault.MyVaultUploadFileResult;
import com.skydrm.sdk.rms.rest.project.file.UploadFileResult;
import com.skydrm.sdk.rms.rest.user.User;
import com.skydrm.sdk.rms.types.ClassificationProfileRetrieveResult;
import com.skydrm.sdk.rms.user.IRmUser;
import com.skydrm.sdk.rms.user.membership.IMemberShip;
import com.skydrm.sdk.rms.user.membership.ProjectMemberShip;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.skydrm.rmc.ui.common.ContextMenu.ACTION_SELECT_PATH;

/**
 * Created by aning on 5/5/2017.
 * <p>
 * The activity used to the following case:
 * 1. Add file(upload local file) by menu command "Add file" -- handle preview the file and protect or share it
 * 2. Protect repo file by menu command "Protect" -- handle preview the file and protect it
 * 3. Share repo file by menu command "Share" -- handle preview the file and share it
 */

@Deprecated
public class CmdOperateFileActivity extends BaseActivity {
    private static final DevLog log = new DevLog(CmdOperateFileActivity.class.getSimpleName());
    // change path request code.
    private static int MYSPACE_CHANGE_PATH_REQUEST_CODE = 0x01;
    private static int PROJECT_CHANGE_PATH_REQUEST_CODE = 0x02;
    private static int FILENAME_CANCEL_TEXTVIEW_SPACE = 20;
    private static int CONTACT_EMAIL_REQUEST_CODE = 0x03;
    // expiry date type
    private static final String EXPIRY_NEVER_EXPIRE = "Never Expire";
    private static final String EXPIRY_RELATIVE = "Relative";
    private static final String EXPIRY_DATE_RANGE = "Date Range";
    private static final String EXPIRY_ABSOLUTE_DATE = "Absolute Date";
    // bind common controls
    @BindView(R.id.Root_layout)
    ScrollView mRootLayout;
    // the following need load dynamically
    // common controls
    private TextView mFileName;
    private TextView mCancel;
    private TextView mChangTipInfo;
    private TextView mChange;
    private TextView mFileLocation;
    private RelativeLayout mPreviewFileLayout;
    private RelativeLayout mProgressLayout;
    private TextView mTipUserInfo;
    // rights toggle --- for protect and share
    private SwitchCompat mToggleView;
    private SwitchCompat mTogglePrint;
    private SwitchCompat mToggleEdit;
    private SwitchCompat mToggleSaveAs;
    private SwitchCompat mToggleReShare;
    // extended.
    private SwitchCompat mToggleWatermark;
    private SwitchCompat mToggleValidity;
    // rights textView
    private TextView mTextView;
    private TextView mTextPrint;
    private TextView mTextEdit;
    private TextView mTextSaveAs;
    private TextView mTextReShare;
    private TextView mTextWatermark;
    private TextView mTextValidity;

    /*********** Begin watermark ***************/
    private RelativeLayout watermarkChangeLayout;
    private TextView watermark;
    private TextView change;
    private EditWatermarkWidget editWatermarkWidget;
    private String watermarkValue = "";
    private Button btnEditWatermarkCancel;
    private Button btnEditWatermarkOk;
    /*********** End watermark ***************/

    /*********** Begin expiry date ***************/
    private LinearLayout changeNeverDate;
    private LinearLayout changeCommonDate;
    private LinearLayout changeAbsoluteDate;
    private TextView fromDay;
    private TextView fromMonth;
    private TextView fromWeek;
    private TextView toDay;
    private TextView toMonth;
    private TextView toWeek;
    private TextView countDay;
    // Absolute date (use different layout)
    private TextView absToDay;
    private TextView absToMonth;
    private TextView absToWeek;
    private TextView absCountDay;
    // calendar
    private Calendar absoluteEndDate;
    private Calendar rangeStartDate;
    private Calendar rangeEndDate;
    // for expiry date
    private User.IExpiry iExpiry;
    // record whether is expired
    private boolean bIsExpired = false;
    /*********** End expiry date ***************/

    private RightsAdapter rightsAdapter;

    // display validity layout
    private LinearLayout validityLayout;
    private TextView validityContent;

    // Protect & Share file
    private Button mProtectBtn;
    private Button mShareBtn;
    private EditText mEmailInput;
    // comment widget
    private CommentWidget mCommentWidget;
    // rights scrollView
    private ScrollView mScrollView;
    // flow layout used to hold wrap emails
    private List<String> mEmailList = new ArrayList<>();
    private FlowLayout mFlowLayout;

    // download progress
    private TextView mProgressValue;
    private ProgressBar mProgressBar;
    // converting progress whether has shown
    private boolean bConvertingProgressIsShow = false;
    // download callback of DownloadManager.
    private DownloadManager.IDownloadCallBack mDownloadCallback;

    // main subView -- add, protect & share
    private View mMainSubLayout;
    // check nxl file rights
    private View mViewRightsLayout;
    // tip for downloading or converting
    private TextView mDownloadingConverting;

    private Context mContext;
    // drive file, local file
    private FileFrom mFileFrom;
    // add, share or protect
    private CmdOperate mCmdOperate;
    // added local file or downloaded remote file
    private File mWorkingFile;
    // selected repo file
    private INxFile mClickFileItem;
    // the dest of upload file
    private INxFile mDestFolder;
    private BoundService mBoundService;
    // project id
    private int mProjectId;
    // project upload file dest folder
    private String mParentPathId;
    // project ProjectExpiry
    private String mProjectExpiryValue;
    // project watermark
    private String mProjectWatermarkValue;

    // note: if is nxl file, also can re-share(should display rights), but should disable protect.
    private boolean bIsNxl = false;
    // file render proxy
    private FileRenderProxy mFileRenderProxy;
    private IFileRender mIFileRender;
    // execute command operate: protect, share & Add
    private IExecuteCommand mIExecuteCommand;
    private Button addBtn;
    private ImageView mBack;
    private TextView mOperationTip;
    private ImageView mSubPreview;
    private List<View> mRightsSpecifyViews = new ArrayList<>();
    private View mAdhocRightsView;
    private View mCenterRightsView;
    private ClassifyAdapter mClassifyAdapter;
    private ClassificationProfileRetrieveResult mClassificationCache;
    private NoScrollViewPager mViewPager;
    private ProtectType mProtectType = ProtectType.ADHOC_POLICY;
    private Map<String, Set<String>> mPolicyTags = new HashMap<>();
    private Map<String, Boolean> mMandatoryMaps = new HashMap<>();
    private Set<String> mGroupSelectLabels;
    private RelativeLayout mRlChangePath;
    private TextView mPreviewTip;
    private RelativeLayout mRlUserDefined;
    private RelativeLayout mRlCompanyDefined;
    private LinearLayout mLlPreviewContent;
    private View mPreviewRoot;
    private boolean showPreview = false;
    private LinearLayout mLlSubContent;
    private IProject mProject;
    private boolean isExtractToggleChecked;

    private ADHocRightsDisplayView mADHocRightsDisplayView;
    private RelativeLayout mRlSubRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cmdoperatefile3);

        ButterKnife.bind(this);
        EventBus.getDefault().register(this);

        mContext = this;
        mIExecuteCommand = new ExecuteCommandImpl();

        // init main layout
        initMainLayout();
        // preview file.
        previewFile();
    }

    /**
     * eventBus message handler for command protect & share
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onCommandOperateEventHandler(CommandOperateEvent eventMsg) {
        mClickFileItem = eventMsg.getClickFileItem();
        mCmdOperate = eventMsg.getCmdOperate();
    }

    /**
     * eventBus message handler for command Add: MySpace Add & ProjectMemberShip Add
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onCommandOperateEventHandler(CommandOperateEvent.CommandAddMsg eventMsg) {
        mFileFrom = eventMsg.getFileFrom();
        mWorkingFile = eventMsg.getWorkingFile();
        mCmdOperate = eventMsg.getCmdOperate();
        if (mFileFrom == FileFrom.FILE_FROM_PROJECT_PAGE) {
            mProject = eventMsg.project;
            mProjectId = mProject.getId();
            mProjectExpiryValue = mProject.getExpiry();
            mProjectWatermarkValue = mProject.getWatermark();
            mParentPathId = eventMsg.getCurrentPathId();
        }
    }

    /**
     * eventBus message handler for command project add from three party
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onCommandOperateEventHandler(CommandOperateEvent.CommandProjectAddFrom3D eventMsg) {
        mClickFileItem = eventMsg.getNxFile();
        mProject = eventMsg.project;
        mProjectId = mProject.getId();
        mParentPathId = eventMsg.getCurrentPathId();
        mCmdOperate = eventMsg.getCmdOperate();
        mFileFrom = eventMsg.getFileFrom();
        mProjectExpiryValue = mProject.getExpiry();
        mProjectWatermarkValue = mProject.getWatermark();
    }

    /**
     * eventBus message handler for command Scan: MySpace Scan & ProjectMemberShip Scan
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onCommandOperateEventHandler(CommandOperateEvent.CommandScanMsg eventMsg) {
        mWorkingFile = eventMsg.getFile();
        mProject = eventMsg.getProject();
        mProjectId = mProject.getId();
        mParentPathId = eventMsg.getCurrentParentId();
        mCmdOperate = eventMsg.getCmdOperate();
        mFileFrom = eventMsg.getFileFrom();
        mProjectExpiryValue = mProject.getExpiry();
        mProjectWatermarkValue = mProject.getWatermark();
    }

    /**
     * eventBus message handler for display Office & 3D convert progress notification.
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConvertProgressEventHandler(ShowConvertProgressEvent eventMsg) {
        showConvertingProgress(eventMsg);
    }

    /**
     * eventBus message handler for converting Office or 3D result notify
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConvertResultEventHandler(ConvertResultNotifyEvent eventMsg) {
        // convert failed
        if (!eventMsg.isbSucceed()) {
            hideProgress();
            mTipUserInfo.setVisibility(View.VISIBLE);
            mTipUserInfo.setText(eventMsg.getResultMsg());
        }
    }

    /**
     * eventBus message handler for preview file result notify -- succeed or failed(may don't support some file type).
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPreviewFileResultEventHandler(ViewFileResultNotifyEvent eventMsg) {
        // open file failed(not supported file)
        if (!eventMsg.isbSucceed()) {
            mTipUserInfo.setVisibility(View.VISIBLE);
            mTipUserInfo.setText(eventMsg.getResultMsg());
        }
    }

    /**
     * eventBus message handler for Edit watermark if is set empty.
     *
     * @param eventMsg
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEditWatermarkSetEmpty(WatermarkSetInvalidEvent eventMsg) {
        if (eventMsg.bSetInvalid) {
            btnEditWatermarkOk.setEnabled(false);
        } else {
            btnEditWatermarkOk.setEnabled(true);
        }
    }

    /**
     * eventBus message for change expiry date.
     *
     * @param eventMsg
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onChangeExpiryDateEvent(ChangeExpiryDateEvent eventMsg) {
        iExpiry = eventMsg.iExpiry;
        displayExpiryDate(mMainSubLayout);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onExtractSwitchStateChange(MsgExtractSwitchStatus statusMsg) {
        isExtractToggleChecked = statusMsg.checked;
    }

    // load main sub view: add file view, protect view or share view.
    private void initMainLayout() {
        if (mCmdOperate != null) {
            if (mCmdOperate == CmdOperate.COMMAND_ADD || mCmdOperate == CmdOperate.COMMAND_SCAN || mCmdOperate == CmdOperate.COMMAND_PROJECT_ADD_FROM_3D) {

                // handle the max file size limit when upload file to myVault or project
                if (mWorkingFile != null && FileUtils.convertByteToMb(mWorkingFile.length()) >= 150) {
                    GenericError.showUI(this, getString(R.string.hint_msg_upload_file_maxsize_limit), true, false, true, null);
                }

                if (mFileFrom == FileFrom.FILE_FROM_MYSPACE_PAGE) {
                    mMainSubLayout = getLayoutInflater().inflate(R.layout.command_myspace_add_layout3, mRootLayout);
                    initCommonView();
                    initMySpaceAddView();
                } else if (mFileFrom == FileFrom.FILE_FROM_PROJECT_PAGE) {
                    mMainSubLayout = getLayoutInflater().inflate(R.layout.command_myproject_add_layout3, mRootLayout);
                    initCommonView();
                    initProjectAddView();
                }
            } else if (mCmdOperate == CmdOperate.COMMAND_PROTECT_FROM_REPO || mCmdOperate == CmdOperate.COMMAND_PROTECT_FROM_LIB) {
                mMainSubLayout = getLayoutInflater().inflate(R.layout.command_protect_layout3, mRootLayout);
                initCommonView();
                initProtectView();
            } else if (mCmdOperate == CmdOperate.COMMAND_SHARE_FROM_REPO || mCmdOperate == CmdOperate.COMMAND_SHARE_FROM_LIB) {
                mMainSubLayout = getLayoutInflater().inflate(R.layout.command_share_layout3, mRootLayout);
                initCommonView();
                initShareView();
            }
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
        } else if (mClickFileItem != null) {
            return mClickFileItem.getName().toLowerCase().endsWith(".nxl");
        }
        return bIsNxl;
    }

    /**
     * Used to try to get file and preview file.
     */
    private void previewFile() {
        if (mCmdOperate == CmdOperate.COMMAND_PROTECT_FROM_REPO
                || mCmdOperate == CmdOperate.COMMAND_SHARE_FROM_REPO) {
            tryGetFile();
        } else if (mCmdOperate == CmdOperate.COMMAND_PROJECT_ADD_FROM_3D) {
            tryGetFile();
        } else if (mCmdOperate == CmdOperate.COMMAND_ADD
                || mCmdOperate == CmdOperate.COMMAND_SCAN
                || mCmdOperate == CmdOperate.COMMAND_PROTECT_FROM_LIB
                || mCmdOperate == CmdOperate.COMMAND_SHARE_FROM_LIB) {

            bIsNxl = RenderHelper.isNxlFile(mWorkingFile.getPath());

            // read rights for nxl file
            if (bIsNxl) {
                // mySpace add nxl file, not need to read rights.
                if (mCmdOperate == CmdOperate.COMMAND_ADD
                        && mFileFrom == FileFrom.FILE_FROM_MYSPACE_PAGE) {
                    return;
                }

                if (mCmdOperate == CmdOperate.COMMAND_PROTECT_FROM_REPO
                        || mCmdOperate == CmdOperate.COMMAND_PROTECT_FROM_LIB) {
                    mProtectBtn.setEnabled(false);
                    return;
                }

                readNxlRights();
            } else {
                fileRender(mWorkingFile, mPreviewFileLayout);
            }
        }
    }

    // file render
    private void fileRender(File workingFile, RelativeLayout mainLayout) {
        // render document
        if (interceptFileRender(mWorkingFile)) {
            return;
        }

        bIsNxl = RenderHelper.isNxlFile(workingFile.getPath());
        String fileName = (mClickFileItem == null ? workingFile.getName() : mClickFileItem.getName());
        mFileRenderProxy = new FileRenderProxy(mContext, mainLayout, bIsNxl, workingFile, fileName, Constant.VIEW_TYPE_PREVIEW);
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
                    if (mPreviewTip != null) {
                        mPreviewTip.setVisibility(View.VISIBLE);
                        mPreviewTip.setText("Does not support preview.");
                    }
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
            mTipUserInfo.setText(getResources().getString(R.string.Not_view_mp4));
            //show small preview window unsupported file tip.
            mPreviewTip.setVisibility(View.VISIBLE);
            mPreviewTip.setText(getResources().getString(R.string.Not_view_mp4));
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

    private void generate3DRenderThumbnail(ThreeD render) {
        if (render == null) {
            return;
        }
        if (mSubPreview == null) {
            return;
        }
        //show small preview window unsupported file tip.
        mPreviewTip.setVisibility(View.VISIBLE);
        mPreviewTip.setText(getResources().getString(R.string.Not_view_3d));
        mLlPreviewContent.setVisibility(View.GONE);
    }

    private void generateWebRenderThumbnail(final WebViewRender render) {
        if (render == null) {
            return;
        }
        if (mSubPreview == null) {
            return;
        }
        render.setOnWebViewLoadListener(new WebViewRender.IWebViewLoadCallback() {
            @Override
            public void onPageFinished(final Bitmap bitmap) {
                if (bitmap != null) {
                    mSubPreview.setImageBitmap(bitmap);
                } else {
                    mPreviewTip.setVisibility(View.VISIBLE);
                    mPreviewTip.setText("Operation failed.");
                }
                mLlPreviewContent.setVisibility(View.GONE);
            }
        });
//        mLlPreviewContent.setVisibility(View.GONE);
    }

    // try get the file
    private void tryGetFile() {
        initProgress();
        initDownload();
        mWorkingFile = DownloadManager.getInstance().tryGetFile(mContext, mClickFileItem, mProgressBar, mProgressValue, true, mDownloadCallback); // parameter: true  --- need opitimized
        // local file
        if (mWorkingFile != null) {
            // file render.
            fileRender(mWorkingFile, mPreviewFileLayout);

            // do some special handler for nxl file
            bIsNxl = RenderHelper.isNxlFile(mWorkingFile.getPath());
            if (bIsNxl) {
                if (mCmdOperate == CmdOperate.COMMAND_PROTECT_FROM_REPO) {
                    initProtectRights();
                    mProtectBtn.setEnabled(false);
                    return;
                }
                readNxlRights();
            }
        } else { // remote file
            beforeDownloadSetting();
        }
    }

    // do some setting before download
    private void beforeDownloadSetting() {
        showProgress();
        // disable some control operate
        if (mCmdOperate == CmdOperate.COMMAND_PROTECT_FROM_REPO) {
            mProtectBtn.setEnabled(false);
        } else if (mCmdOperate == CmdOperate.COMMAND_SHARE_FROM_REPO) {
            mShareBtn.setEnabled(false);
            mEmailInput.setEnabled(false);
            mCommentWidget.getEditText().setEnabled(false);
        } else if (mCmdOperate == CmdOperate.COMMAND_PROJECT_ADD_FROM_3D) {
            addBtn.setEnabled(false);
        }
    }

    // do some setting after download
    private void afterDownloadSetting() {
        // enable some control operate
        if (mCmdOperate == CmdOperate.COMMAND_PROTECT_FROM_REPO) {
            mProtectBtn.setEnabled(true);
        } else if (mCmdOperate == CmdOperate.COMMAND_SHARE_FROM_REPO) {
            mShareBtn.setEnabled(true);
            mEmailInput.setEnabled(true);
            mCommentWidget.getEditText().setEnabled(true);
        } else if (mCmdOperate == CmdOperate.COMMAND_PROJECT_ADD_FROM_3D) {
            addBtn.setEnabled(true);
        }
    }

    // show the convert progress for Office & 3D
    private void showConvertingProgress(ShowConvertProgressEvent eventMsg) {
        if (!bConvertingProgressIsShow) {
            initProgress();
            showProgress();
            bConvertingProgressIsShow = true;
        }
        mDownloadingConverting.setText(getResources().getString(R.string.c_Processing_with3dots));
        mProgressBar.setProgress(eventMsg.getProgressValue());
        String text = String.format(Locale.getDefault(), "%d", eventMsg.getProgressValue()) + "%";
        mProgressValue.setText(text);
    }

    private void showProgress() {
        if (mProgressLayout != null) {
            mProgressLayout.setVisibility(View.VISIBLE);
        }
    }

    private void hideProgress() {
        if (mProgressLayout != null) {
            mProgressLayout.setVisibility(View.GONE);
        }
    }

    // init downloader
    private void initDownload() {
        mDownloadCallback = new DownloadManager.IDownloadCallBack() {
            @Override
            public void onDownloadFinished(boolean taskStatus, String localPath, @Nullable FileDownloadException e) {

                hideProgress();
                // remove the downloader
                DownloadManager.getInstance().removeDownloader(mClickFileItem);
                // will display the rights if is nxl file.
                if (taskStatus) {
                    afterDownloadSetting();
                    mWorkingFile = new File(localPath);
                    fileRender(mWorkingFile, mPreviewFileLayout);

                    // do some special handler for nxl file
                    bIsNxl = RenderHelper.isNxlFile(mWorkingFile.getPath());
                    if (bIsNxl) {
                        if (mCmdOperate == CmdOperate.COMMAND_PROTECT_FROM_REPO) {
                            initProtectRights();
                            mProtectBtn.setEnabled(false);
                            return;
                        }
                        readNxlRights();
                    }
                } else {
                    // exception handler
                    if (e != null) {
                        switch (e.getErrorCode()) {
                            case AuthenticationFailed:
                                SkyDRMApp.getInstance().getSession().sessionExpiredHandler(mContext);
                                break;
                            case NetWorkIOFailed:
                                ToastUtil.showToast(mContext, mContext.getResources().getString(R.string.excep_network_unavailable));
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

    // init download progress.
    private void initProgress() {
        mProgressLayout = (RelativeLayout) mPreviewRoot.findViewById(R.id.download_progress);
        mDownloadingConverting = (TextView) mPreviewRoot.findViewById(R.id.projects_file_info_tv_download);
        mProgressBar = (ProgressBar) mPreviewRoot.findViewById(R.id.progress);
        mProgressValue = (TextView) mPreviewRoot.findViewById(R.id.textView_progress);
    }

    /**
     * Set file name textView width dynamically in order to adapter different size phone width when display different file name length.
     * calculate: width = screenWidth - cancelTextViewWidth - cancelMarginStart(20dp) - cancelMarginEnd(20dp) - Space(the space of fileName and cancel, here specify 20dp);
     */
//    private void setFileNameWidth() {
//        // measure the width of cancel textView.
//        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
//        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
//        mCancel.measure(w, h);
//        int tvCancelWidth = mCancel.getMeasuredWidth();
//        // set file name textView width
//        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mFileName.getLayoutParams();
//        layoutParams.width = CommonUtils.getScreenWidth(this) - tvCancelWidth - DensityHelper.dip2px(mContext, 20) * 2 - DensityHelper.dip2px(mContext, FILENAME_CANCEL_TEXTVIEW_SPACE);
//        mFileName.setLayoutParams(layoutParams);
//    }

    // init common view layout
    private void initCommonView() {
        mBack = (ImageView) mMainSubLayout.findViewById(R.id.iv_back);
        mRlChangePath = (RelativeLayout) mMainSubLayout.findViewById(R.id.command_change_path);
        mOperationTip = (TextView) mMainSubLayout.findViewById(R.id.file_operation_tip);
        mCancel = (TextView) mMainSubLayout.findViewById(R.id.cancel);

        mFileName = (TextView) mMainSubLayout.findViewById(R.id.filename);
        mChangTipInfo = (TextView) mMainSubLayout.findViewById(R.id.tv_tip);
        mChange = (TextView) mMainSubLayout.findViewById(R.id.tv_change);
        mFileLocation = (TextView) mMainSubLayout.findViewById(R.id.tv_path);

        mLlSubContent = (LinearLayout) mMainSubLayout.findViewById(R.id.ll_sub_content);
        mLlPreviewContent = (LinearLayout) mMainSubLayout.findViewById(R.id.ll_preview_content);
        mPreviewRoot = LayoutInflater.from(mContext).inflate(R.layout.layout_preview_file, null);
        mLlPreviewContent.addView(mPreviewRoot);
        showPreview = false;
        mPreviewFileLayout = (RelativeLayout) mPreviewRoot.findViewById(R.id.preview_file);
        mTipUserInfo = (TextView) mPreviewRoot.findViewById(R.id.tip_no_view_right);
//        mLlPreviewContent = (LinearLayout) mMainSubLayout.findViewById(R.id.ll_preview_content);
//        mPreviewRoot = LayoutInflater.from(mContext).inflate(R.layout.layout_preview_file);
//        mPreviewFileLayout = (RelativeLayout) mMainSubLayout.findViewById(R.id.preview_file);
//        mTipUserInfo = (TextView) mMainSubLayout.findViewById(R.id.tip_no_view_right);

        // set file name
        if (mClickFileItem != null) {
            String fileName = RenderHelper.isGoogleFile(mClickFileItem) ? RenderHelper.getGoogleExportFileName(mClickFileItem) : mClickFileItem.getName();
            mFileName.setText(fileName);
        } else if (mWorkingFile != null) {
            mFileName.setText(mWorkingFile.getName());
        }

        if (mBack != null) {
            mBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        hideSoftInputIfNecessary();
    }

    private void hideSoftInputIfNecessary() {
        if (mEmailInput != null) {
            hideSoftInput(mEmailInput);
            mEmailInput = null;
        }
    }

    private void setPreviewLayoutParams(RelativeLayout previewLayout, int ratio) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                CommonUtils.getScreenHeight(CmdOperateFileActivity.this) / ratio);
        layoutParams.setMargins(dp(20), dp(10), dp(20), dp(0));
        previewLayout.setLayoutParams(layoutParams);
    }

    private int dp(float value) {
        return DensityHelper.dip2px(mContext, value);
    }

    // init mySpace add file view layout
    private void initMySpaceAddView() {
        // init upload dest path
        // Normally will upload file into current folder in default, but if is in synthetic root, will upload into myDrive root.
        String defaultPath = null;
        try {
            INxFile workingFolder = SkyDRMApp.getInstance().getRepoSystem().findWorkingFolder();
            if (workingFolder != null) {
                if (workingFolder.getService() != null) { // current folder for operating some repo
                    mBoundService = workingFolder.getService();
                    if (!mBoundService.type.equals(BoundService.ServiceType.MYDRIVE)) {
                        mDestFolder = new NXFolder("/", "/", "", 0);
                        mBoundService = CommonUtils.getDefaultUploadedBoundService();
                    } else {
                        mDestFolder = workingFolder;
                    }
                    defaultPath = getResources().getString(R.string.MyDrive) + " " + mDestFolder.getDisplayPath();
                } else { // synthetic root
                    mDestFolder = new NXFolder("/", "/", "", 0);
                    mBoundService = CommonUtils.getDefaultUploadedBoundService();
                    defaultPath = getResources().getString(R.string.MyDrive) + " " + mDestFolder.getDisplayPath();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.d("find working folder failed: in initMySpaceAddView.");
        }
        mFileLocation.setText(defaultPath);
        mChangTipInfo.setText(getResources().getString(R.string.file_will_be_saved_to));
        // change upload location
        mChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //The function add file is not supported for 3rd repositories.
                Intent intent = new Intent();
                Bundle extra = new Bundle();
                extra.putInt(Constant.CHOOSE_ACTION, ACTION_SELECT_PATH);
                extra.putSerializable(Constant.BOUND_SERVICE, CommonUtils.getDefaultUploadedBoundService());
                intent.putExtras(extra);
                intent.setClass(CmdOperateFileActivity.this, LibraryActivity.class);
                startActivityForResult(intent, MYSPACE_CHANGE_PATH_REQUEST_CODE);
            }
        });

        // protect file
        View protect = (View) mMainSubLayout.findViewById(R.id.protect_file);
        protect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                if (isNxlFile()) {
//                    ToastUtil.showToast(mContext, getResources().getString(R.string.hint_msg_deny_protect_nxl_file));
//                    return;
//                }
//
//                CommandOperateEvent.CommandAddMsg eventMsg = new CommandOperateEvent.CommandAddMsg(mWorkingFile, mDestFolder, mBoundService, CmdOperate.COMMAND_ADD_PROTECT);
//                EventBus.getDefault().postSticky(eventMsg);
//                startActivity(new Intent(CmdOperateFileActivity.this, ProtectShareActivity.class));
            }
        });

        // share file
        View share = (View) mMainSubLayout.findViewById(R.id.share_file);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                CommandOperateEvent.CommandAddMsg eventMsg = new CommandOperateEvent.CommandAddMsg(mWorkingFile, mDestFolder, mBoundService, CmdOperate.COMMAND_ADD_SHARE);
//                EventBus.getDefault().postSticky(eventMsg);
//                startActivity(new Intent(CmdOperateFileActivity.this, ProtectShareActivity.class));
            }
        });

        // don't protect, only upload
        TextView uploadLocalToRepo = (TextView) mMainSubLayout.findViewById(R.id.do_not_protect_tip);
        uploadLocalToRepo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doCmdMySpaceAdd();
            }
        });
    }

    @Deprecated
    // init project add file view layout
    private void initProjectAddView() {
        // Default upload path is current working folder
        if (TextUtils.isEmpty(mParentPathId)) {
            mParentPathId = Constant.ROOT_PATH;
        }
        mFileLocation.setText(mParentPathId);
        mChangTipInfo.setText(getResources().getString(R.string.file_will_be_saved_to));
        mSubPreview = (ImageView) mMainSubLayout.findViewById(R.id.rl_sub_preview);
        mPreviewTip = (TextView) mMainSubLayout.findViewById(R.id.tv_preview_tip);
        setPreviewLayoutParams(mPreviewFileLayout, 3);

        // init event
        //in command_myspace_add_layout3 doesn't exist preview toggle
        //add this in common layout should avoid null object.

        if (mRlChangePath != null) {
            mRlChangePath.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!showPreview) {
                        showPreview = true;
                        mLlPreviewContent.setVisibility(View.VISIBLE);
                        resizeLayoutParams(CommonUtils.getScreenHeight(CmdOperateFileActivity.this) / 3 + dp(20), mLlSubContent);
                    } else {
                        mLlPreviewContent.setVisibility(View.GONE);
                        showPreview = false;
                        resizeLayoutParams(dp(0), mLlSubContent);
                    }
                }
            });
        }
        // change upload location
        mChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNxlFile()) {
                    ToastUtil.showToast(mContext, getResources().getString(R.string.hint_msg_deny_upload_nxl_file_to_project));
                    return;
                }
                selectProjectFolderPath();
            }
        });
        initializeItemViews(CmdOperateFileActivity.this);
        mViewPager = (NoScrollViewPager) mMainSubLayout.findViewById(R.id.nsvp_myproject_add_layout);
        mRlUserDefined = (RelativeLayout) mMainSubLayout.findViewById(R.id.rl_user_defined);
        mRlCompanyDefined = (RelativeLayout) mMainSubLayout.findViewById(R.id.rl_company_defined);
        PolicySwitchListener policySwitchListener = new PolicySwitchListener();
        mRlUserDefined.setOnClickListener(policySwitchListener);
        mRlCompanyDefined.setOnClickListener(policySwitchListener);

        mViewPager.setPageEnabled(false);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    mProtectType = ProtectType.ADHOC_POLICY;
                } else {
                    mProtectType = ProtectType.CENTRAL_POLICY;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        RightsSpecifyPageAdapter pageAdapter = new RightsSpecifyPageAdapter(mRightsSpecifyViews);
        mViewPager.setAdapter(pageAdapter);

        initClassificationData();
        // Add button
        addBtn = (Button) mMainSubLayout.findViewById(R.id.operate_button);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mProtectType) {
                    case ADHOC_POLICY:
                        doCmdProjectAddWithAdhocPolicy();
                        break;
                    case CENTRAL_POLICY:
                        if (isNxlFile()) {
                            ToastUtil.showToast(mContext, getResources().getString(R.string.hint_msg_deny_upload_nxl_file_to_project));
                            return;
                        }
                        if (mPolicyTags == null || mPolicyTags.isEmpty()) {
                            if (mMandatoryMaps != null && !mMandatoryMaps.isEmpty()) {
                                if (mMandatoryMaps.size() != 1) {
                                    ToastUtil.showToast(getApplicationContext(),
                                            "Mandatory categories require at least one classification label.");
                                } else {
                                    Set<String> keySets = mMandatoryMaps.keySet();
                                    for (String key : keySets) {
                                        ToastUtil.showToast(getApplicationContext(), "Mandatory category " + key +
                                                " require at least one classification label.");
                                    }
                                }
                            } else {
                                doCmdProjectAddWithCentPolicy(mPolicyTags);
                            }
                            break;
                        }
                        int mandatoryCheck = 0;
                        if (mMandatoryMaps != null && !mMandatoryMaps.isEmpty()) {
                            Set<String> keySets = mMandatoryMaps.keySet();
                            for (String key : keySets) {
                                if (mPolicyTags.containsKey(key)) {
                                    Set<String> valueLabels = mPolicyTags.get(key);
                                    if (valueLabels == null || valueLabels.size() == 0) {
                                        ToastUtil.showToast(getApplicationContext(), "Mandatory category " + key
                                                + " require at least one classification label.");
                                        break;
                                    } else {
                                        mandatoryCheck++;
                                    }
                                } else {
                                    ToastUtil.showToast(getApplicationContext(), "Mandatory category " + key
                                            + " require at least one classification label.");
                                    break;
                                }
                            }
                            if (mandatoryCheck != keySets.size()) {
                                break;
                            }
                        }
                        doCmdProjectAddWithCentPolicy(mPolicyTags);
                        break;
                }
            }
        });
    }

    private void selectProjectFolderPath() {
        Intent intent = new Intent(this, ProjectLibraryActivity.class);
        intent.setAction(Constant.ACTION_PROJECT_ADD_FILE);
        intent.putExtra(Constant.PROJECT_DETAIL, (Parcelable) mProject);
        startActivityForResult(intent, PROJECT_CHANGE_PATH_REQUEST_CODE);
    }

    private void resizeLayoutParams(int marginTop, LinearLayout content) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        params.setMargins(0, marginTop, 0, 0);
        content.setLayoutParams(params);
    }

    class PolicySwitchListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.rl_user_defined:
                    selectUserDefinedPanel();
                    break;
                case R.id.rl_company_defined:
                    selectCompanyDefinedPanel();
                    break;
            }
        }
    }

    private void selectUserDefinedPanel() {
        mViewPager.setCurrentItem(0);
        mRlUserDefined.setBackground(getDrawable(R.drawable.rb_bg_myproject_add_layout_selected));
        updateChildStatus((RadioButton) mRlUserDefined.getChildAt(0), true);
        if (mRlCompanyDefined.isEnabled()) {
            mRlCompanyDefined.setBackground(getDrawable(R.drawable.rb_bg_myproject_add_layout_normal));
            updateChildStatus((RadioButton) mRlCompanyDefined.getChildAt(0), false);
        }
    }

    private void selectCompanyDefinedPanel() {
        mViewPager.setCurrentItem(1);
        mRlUserDefined.setBackground(getDrawable(R.drawable.rb_bg_myproject_add_layout_normal));
        updateChildStatus((RadioButton) mRlUserDefined.getChildAt(0), false);
        mRlCompanyDefined.setBackground(getDrawable(R.drawable.rb_bg_myproject_add_layout_selected));
        updateChildStatus((RadioButton) mRlCompanyDefined.getChildAt(0), true);
    }

    private void setCompanyDefinedPanelEnableStatus(boolean enable) {
        mRlCompanyDefined.setEnabled(enable);
        mRlCompanyDefined.getChildAt(0).setEnabled(enable);
    }

    private void updateChildStatus(RadioButton child, boolean checked) {
        child.setChecked(checked);
    }

    private void initializeItemViews(Context context) {
        mAdhocRightsView = LayoutInflater.from(context).inflate(R.layout.specify_adhoc_policy_rights_project_add_file,
                null);
        mCenterRightsView = LayoutInflater.from(context).inflate(R.layout.specify_center_policy_rights_project_add_file,
                null);
        initAdhocRightsView(mAdhocRightsView);
        initCentralRightsView(mCenterRightsView, context);

        mRightsSpecifyViews.add(mAdhocRightsView);
        mRightsSpecifyViews.add(mCenterRightsView);
    }

    private void initAdhocRightsView(View parentView) {
        // init rights view (view rights or specify rights)
        // init view rights
        if (isNxlFile()) {
            // load view rights layout
            View specifyRights = parentView.findViewById(R.id.command_specify_rights);
            mViewRightsLayout = parentView.findViewById(R.id.view_rights);
            specifyRights.setVisibility(View.GONE);
            mViewRightsLayout.setVisibility(View.VISIBLE);
            // read rights
            if (mCmdOperate == CmdOperate.COMMAND_ADD || mCmdOperate == CmdOperate.COMMAND_SCAN) { // has already got mWorkingFile
                readNxlRights();
            }
        } else {
            // init specify rights -- is visible in default.
            initRightsToggle(parentView);
        }
    }

    private void initCentralRightsView(View parentView, Context context) {
        RecyclerView recyclerView = parentView.findViewById(R.id.rv_central_policy_rights);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setNestedScrollingEnabled(false);
        mClassifyAdapter = new ClassifyAdapter();
        recyclerView.setAdapter(mClassifyAdapter);
        mClassifyAdapter.setOnLabelSelectListener(new ClassifyAdapter.OnLabelSelectListener() {
            @Override
            public void onLabelSelect(ClassifyItem classifyItem,
                                      String label, boolean selected,
                                      boolean mandatory) {
                if (selected) {
                    if (!mPolicyTags.containsKey(classifyItem.getCategoryName())) {
                        mGroupSelectLabels = new HashSet<>();
                        mGroupSelectLabels.add(label);
                        mPolicyTags.put(classifyItem.getCategoryName(), mGroupSelectLabels);
                    } else {
                        Set<String> labels = mPolicyTags.get(classifyItem.getCategoryName());
                        labels.add(label);
                        mPolicyTags.put(classifyItem.getCategoryName(), labels);
                    }
                } else {
                    if (mPolicyTags.containsKey(classifyItem.getCategoryName())) {
                        Set<String> labels = mPolicyTags.get(classifyItem.getCategoryName());
                        labels.remove(label);
                        if (labels.isEmpty()) {
                            mPolicyTags.remove(classifyItem.getCategoryName());
                        }
                    }
                }
            }
        });
    }

    @Deprecated
    private void initClassificationData() {
        String classificationRaw = mProject.getClassificationRaw();
        mClassificationCache = new Gson().fromJson(classificationRaw, ClassificationProfileRetrieveResult.class);
        parseClassificationResult(mClassificationCache);
    }

    @Deprecated
    private void parseClassificationResult(ClassificationProfileRetrieveResult result) {
        if (result == null) {
            return;
        }
        List<ClassifyItem> classifyItems = new ArrayList<>();
        mPolicyTags.clear();
        mMandatoryMaps.clear();
        ClassificationProfileRetrieveResult.ResultsBean results = result.getResults();
        if (results == null) {
            return;
        }
        List<ClassificationProfileRetrieveResult.ResultsBean.CategoriesBean> categories = results.getCategories();
        if (categories != null && categories.size() != 0) {
            for (ClassificationProfileRetrieveResult.ResultsBean.CategoriesBean categoriesBean : categories) {
                mGroupSelectLabels = new HashSet<>();
                String categoryName = categoriesBean.getName();
                boolean multiSelect = categoriesBean.isMultiSelect();
                boolean mandatory = categoriesBean.isMandatory();
                if (mandatory) {
                    mMandatoryMaps.put(categoryName, true);
                }
                List<ClassificationProfileRetrieveResult.ResultsBean.CategoriesBean.LabelsBean> labels = categoriesBean.getLabels();
                List<ClassifyItem.LabelsBean> labelsBeans = new ArrayList<>();
                if (labels != null && labels.size() != 0) {
                    for (ClassificationProfileRetrieveResult.ResultsBean.CategoriesBean.LabelsBean labelsBean : labels) {
                        labelsBeans.add(new ClassifyItem.LabelsBean(labelsBean.getName(), labelsBean.isDefaultX()));
                        if (labelsBean.isDefaultX()) {
                            mGroupSelectLabels.add(labelsBean.getName());
                        }
                    }
                }

                ClassifyItem classifyItem = new ClassifyItem();
                classifyItem.setCategoryName(categoryName);
                classifyItem.setMultiSelect(multiSelect);
                classifyItem.setMandatory(mandatory);
                classifyItem.setLabels(labelsBeans);

                classifyItems.add(classifyItem);
                if (mGroupSelectLabels != null && mGroupSelectLabels.size() != 0) {
                    mPolicyTags.put(categoryName, mGroupSelectLabels);
                }
            }
            mClassifyAdapter.setData(classifyItems);

            if (!mRlCompanyDefined.isEnabled()) {
                setCompanyDefinedPanelEnableStatus(true);
            }
        } else {
            //Disable switch toggle
            if (mViewPager.getCurrentItem() == 1) {
                selectUserDefinedPanel();
            }
            if (mRlCompanyDefined.isEnabled()) {
                setCompanyDefinedPanelEnableStatus(false);
            }
            if (mProtectType != ProtectType.ADHOC_POLICY) {
                mProtectType = ProtectType.ADHOC_POLICY;
            }
        }
    }

    // init protect file view layout
    private void initProtectView() {
        mOperationTip.setText(getText(R.string.Create_protected_file));
        mChangTipInfo.setText(getResources().getString(R.string.protected_file_will_be_added_to));
        mChange.setVisibility(View.GONE);
        mFileLocation.setText(getResources().getString(R.string.MyVault));
        mRlSubRoot = mMainSubLayout.findViewById(R.id.rl_sub_root);

        if (isNxlFile()) {
            mRlChangePath.setVisibility(View.GONE);
            mPreviewFileLayout.setVisibility(View.GONE);

        } else {
            setPreviewLayoutParams(mPreviewFileLayout, 3);
            mRlChangePath.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!showPreview) {
                        mLlPreviewContent.setVisibility(View.VISIBLE);
                        resizeLayoutParams(CommonUtils.getScreenHeight(CmdOperateFileActivity.this) / 3 + dp(20), mLlSubContent);
                        showPreview = true;
                    } else {
                        mLlPreviewContent.setVisibility(View.GONE);
                        resizeLayoutParams(dp(0), mLlSubContent);
                        showPreview = false;
                    }
                }
            });
        }

        mSubPreview = mMainSubLayout.findViewById(R.id.rl_sub_preview);
        mPreviewTip = mMainSubLayout.findViewById(R.id.tv_preview_tip);

        // init rights view (view rights or specify rights)
        initProtectRightsView();
        // protect
        mProtectBtn = mMainSubLayout.findViewById(R.id.operate_button);
        mProtectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doCmdProtect();
            }
        });
    }

    private void initProtectRightsView() {
        // init view rights
        if (isNxlFile()) {
            ToastUtil.showToast(this, getString(R.string.hint_msg_already_a_protected_file));
            mADHocRightsDisplayView = new ADHocRightsDisplayView(this);
            mRlSubRoot.removeAllViews();
            mRlSubRoot.addView(mADHocRightsDisplayView);

            // read rights
            if (mCmdOperate == CmdOperate.COMMAND_ADD
                    || mCmdOperate == CmdOperate.COMMAND_SCAN
                    || mCmdOperate == CmdOperate.COMMAND_PROTECT_FROM_LIB) { // has already got mWorkingFile
                initProtectRights();
            }
        } else {
            // init specify rights -- is visible in default.
            initRightsToggle(mMainSubLayout);
        }
    }

    private void initProtectRights() {
        FileOperation.readNxlFingerPrint(mContext, mWorkingFile, false,
                Constant.VIEW_TYPE_PREVIEW, new FileOperation.IGetFingerPrintCallback() {

                    @Override
                    public void onGetFingerPrintFinished(INxlFileFingerPrint fp) {
                        if (mADHocRightsDisplayView == null) {
                            return;
                        }
                        if (fp == null) {
                            mADHocRightsDisplayView.showNoRightsTip();
                            return;
                        }

                        mADHocRightsDisplayView.displayRights(fp);
                        mADHocRightsDisplayView.showWatermark("");
                        mADHocRightsDisplayView.showValidity(fp.formatString());
                    }
                });
    }

    // init share file view layout
    private void initShareView() {
        mOperationTip.setText(getText(R.string.Share_protected_file));
        mChangTipInfo.setText(getResources().getString(R.string.shared_file_will_be_added_to));
        mChange.setVisibility(View.GONE);
        mFileLocation.setText(getResources().getString(R.string.MyVault));
        // display validity
        validityLayout = (LinearLayout) mMainSubLayout.findViewById(R.id.validity_layout);
        validityContent = (TextView) mMainSubLayout.findViewById(R.id.validity_content);
        if (!isNxlFile()) {
            validityLayout.setVisibility(View.GONE);
        }
        mSubPreview = (ImageView) mMainSubLayout.findViewById(R.id.rl_sub_preview);
        mPreviewTip = (TextView) mMainSubLayout.findViewById(R.id.tv_preview_tip);
        setPreviewLayoutParams(mPreviewFileLayout, 3);
        // init event
        //in command_myspace_add_layout3 doesn't exist preview toggle
        //add this in common layout should avoid null object.
        if (mRlChangePath != null) {
            mRlChangePath.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!showPreview) {
                        mLlPreviewContent.setVisibility(View.VISIBLE);
                        resizeLayoutParams(CommonUtils.getScreenHeight(CmdOperateFileActivity.this) / 3 + dp(20), mLlSubContent);
                        showPreview = true;
                    } else {
                        mLlPreviewContent.setVisibility(View.GONE);
                        resizeLayoutParams(dp(0), mLlSubContent);
                        showPreview = false;
                    }
                }
            });
        }
        // init rights
        initRightsView();
        // share sub-layout
        RelativeLayout rlShareSubView = (RelativeLayout) mMainSubLayout.findViewById(R.id.rl_share_subView);
        // used to hide the soft keyboard when user click other space field.
        rlShareSubView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });
        // email input
        mFlowLayout = (FlowLayout) mMainSubLayout.findViewById(R.id.flowLayout);
        mEmailInput = (EditText) mMainSubLayout.findViewById(R.id.et_email_address);
        // comment
        mCommentWidget = (CommentWidget) mMainSubLayout.findViewById(R.id.comment_widget);
        // rights scrollView
        mScrollView = (ScrollView) mMainSubLayout.findViewById(R.id.scrollView);
        // control the scrollbar is in the down
        mScrollView.post(new Runnable() {
            @Override
            public void run() {
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
        mEmailInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Drawable drawable = mEmailInput.getCompoundDrawables()[2];
                if (drawable == null) return false;
                if (motionEvent.getAction() != MotionEvent.ACTION_UP)
                    return false;
                if (motionEvent.getX() > mEmailInput.getWidth() - mEmailInput.getPaddingLeft() - drawable.getIntrinsicWidth()) {
                    mEmailInput.setFocusableInTouchMode(false);
                    mEmailInput.setFocusable(false);
                    lunchContactPageWithResult(Constant.REQUEST_CODE_SELECT_EMAILS);
                } else {
                    mEmailInput.setFocusableInTouchMode(true);
                    mEmailInput.setFocusable(true);
                }
                return CmdOperateFileActivity.super.onTouchEvent(motionEvent);
            }
        });
        mEmailInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                CommonUtils.wrapEmail(mContext, s.toString(), false, mEmailList, mFlowLayout, mEmailInput);
            }
        });

        // handle enter key to wrap text user input
        mEmailInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    CommonUtils.wrapEmail(mContext, mEmailInput.getText().append(" ").toString(), false, mEmailList, mFlowLayout, mEmailInput);
                }
                return false;
            }
        });
        // share
        mShareBtn = mMainSubLayout.findViewById(R.id.operate_button);
        mShareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doCmdShare();
            }
        });
    }

    private void initRightsView() {
        // init view rights
        if (isNxlFile()) {
            // load view rights layout
            View specifyRights = mMainSubLayout.findViewById(R.id.command_specify_rights);
            mViewRightsLayout = mMainSubLayout.findViewById(R.id.view_rights);
            specifyRights.setVisibility(View.GONE);
            mViewRightsLayout.setVisibility(View.VISIBLE);
            // read rights
            if (mCmdOperate == CmdOperate.COMMAND_ADD
                    || mCmdOperate == CmdOperate.COMMAND_SCAN) { // has already got mWorkingFile
                readNxlRights();
            }
        } else {
            // init specify rights -- is visible in default.
            initRightsToggle(mMainSubLayout);
        }
    }

    // get the changed path.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // select contact email results
        if (resultCode == RESULT_OK) {
            // mySpace change path results
            if (requestCode == MYSPACE_CHANGE_PATH_REQUEST_CODE) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    mBoundService = (BoundService) extras.getSerializable(Constant.BOUND_SERVICE);
                    mDestFolder = (INxFile) extras.getSerializable(Constant.DEST_FOLDER);
                    if (mBoundService != null && mDestFolder != null) {
                        mFileLocation.setText(String.format("%s:%s", mBoundService.getDisplayName(), mDestFolder.getDisplayPath()));
                    }
                }
            }
            // re-select project file path request code.
            if (requestCode == PROJECT_CHANGE_PATH_REQUEST_CODE) {
                mParentPathId = data.getStringExtra(Constant.PROJECT_PATH_ID);
                mFileLocation.setText(mParentPathId);
            }
            // select contact email request code.
            if (requestCode == Constant.REQUEST_CODE_SELECT_EMAILS) {
//                mFlowLayout.wrapEmailFromContact(data);
                Serializable serializableExtra = data.getSerializableExtra(Constant.SELECT_EMAIL_RESULT);
                if (serializableExtra instanceof HashSet) {
                    HashSet contacts = (HashSet) serializableExtra;
                    for (Object o : contacts) {
                        if (o instanceof String) {
                            CommonUtils.wrapEmail(mContext, (String) o, true, mEmailList, mFlowLayout, mEmailInput);
                        }
                    }
                }
            }
        }
    }

    // read nxl file rights
    private void readNxlRights() {
        // display progressBar loading
        final LinearLayout readRightsLoad = mViewRightsLayout.findViewById(R.id.read_rights_loading_layout);
        readRightsLoad.setVisibility(View.VISIBLE);

        // get finger print
        FileOperation.readNxlFingerPrint(mContext, mWorkingFile, false, Constant.VIEW_TYPE_PREVIEW, new FileOperation.IGetFingerPrintCallback() {
            @Override
            public void onGetFingerPrintFinished(INxlFileFingerPrint fingerPrint) {
                // display rights layout
                GridView rightView = (GridView) mViewRightsLayout.findViewById(R.id.rights_view);

                TextView mTvRightsTip = (TextView) mViewRightsLayout.findViewById(R.id.no_rights_tip);
                TextView mTvStewardRightsTip = (TextView) mViewRightsLayout.findViewById(R.id.steward_tip);
                // hide read rights loading progress
                readRightsLoad.setVisibility(View.GONE);
                // read rights failed.
                if (fingerPrint == null) {
                    mTvRightsTip.setVisibility(View.VISIBLE);
                    mTvRightsTip.setText(mContext.getResources().getString(R.string.read_rights_failed));
                    // disable
                    if ((mCmdOperate == CmdOperate.COMMAND_SHARE_FROM_REPO || mCmdOperate == CmdOperate.COMMAND_SHARE_FROM_LIB)) {
                        mShareBtn.setEnabled(false);
                        mEmailInput.setEnabled(false);
                        mCommentWidget.getEditText().setEnabled(false);
                    }
                    if (validityLayout != null) {
                        validityLayout.setVisibility(View.GONE);
                    }
                    return;
                }

                // MySpace Add nxl file will not display rights in this page, but may can't view the nxl file content, so need to give tip info.
                if (mCmdOperate == CmdOperate.COMMAND_ADD && mFileFrom == FileFrom.FILE_FROM_MYSPACE_PAGE) {
                    if (!fingerPrint.hasView()) {
                        mTipUserInfo.setVisibility(View.VISIBLE);
                    }
                    return;
                }

                rightsAdapter = new RightsAdapter(mContext);
                rightView.setAdapter(rightsAdapter);
                rightsAdapter.showRights(fingerPrint);

                if (fingerPrint.hasRights()) {
                    // display rights expiry when share nxl file.
                    if (mCmdOperate == CmdOperate.COMMAND_SHARE_FROM_REPO
                            || mCmdOperate == CmdOperate.COMMAND_SHARE_FROM_LIB
                            || mCmdOperate == CmdOperate.COMMAND_PROTECT_FROM_REPO
                            || mCmdOperate == CmdOperate.COMMAND_PROTECT_FROM_LIB) {
                        String expiry = fingerPrint.formatString();
                        if (validityContent != null) {
                            validityContent.setText(expiry);
                        }
                    }
                } else {  // means have no any rights.
                    // view rights tip
                    mTvRightsTip.setVisibility(View.VISIBLE);
                    // view file content tip
                    mTipUserInfo.setVisibility(View.VISIBLE);
                    // hide rights expiry
                    if (mCmdOperate == CmdOperate.COMMAND_SHARE_FROM_REPO
                            || mCmdOperate == CmdOperate.COMMAND_SHARE_FROM_LIB
                            || mCmdOperate == CmdOperate.COMMAND_PROTECT_FROM_REPO
                            || mCmdOperate == CmdOperate.COMMAND_PROTECT_FROM_LIB) {
                        validityLayout.setVisibility(View.GONE);
                    }
                }

                // disable share button if don't have share right.
                if ((mCmdOperate == CmdOperate.COMMAND_SHARE_FROM_REPO || mCmdOperate == CmdOperate.COMMAND_SHARE_FROM_LIB)
                        && !fingerPrint.hasShare() && !SkyDRMApp.getInstance().isStewardOf(fingerPrint.getOwnerID())) {
                    mShareBtn.setEnabled(false);
                    mEmailInput.setEnabled(false);
                    mCommentWidget.getEditText().setEnabled(false);
                }

                // share rights expire, should forbid share whether it's owner or not.
                bIsExpired = fingerPrint.isExpired();

                // for other nxl file, tip steward have all rights
                if (SkyDRMApp.getInstance().isStewardOf(fingerPrint.getOwnerID()))
                    mTvStewardRightsTip.setVisibility(View.VISIBLE);

            }
        });
    }

    private List<String> getRightsString() {
        List<String> rightsList = new ArrayList<String>();
        // default must has view right
        rightsList.add(Constant.RIGHTS_VIEW);
        if (mTogglePrint.isChecked()) {
            rightsList.add(Constant.RIGHTS_PRINT);
        }
        if (mToggleEdit.isChecked()) {
            rightsList.add(Constant.RIGHTS_EDIT);
        }
        if (mToggleSaveAs.isChecked()) {
            rightsList.add(Constant.RIGHTS_DOWNLOAD);
        }
        if (mToggleReShare.isChecked()) {
            rightsList.add(Constant.RIGHTS_SHARE);
        }
        // added for sharing local file
        if (mToggleWatermark.isChecked()) {
            rightsList.add(Constant.RIGHTS_WATERMARK);
        }
        return rightsList;
    }

    /**
     * get rights from UI for normal file
     */
    private Rights getRights() {
        Rights rights = new Rights();
        if (mToggleView.isChecked()) {
            rights.setView(true);
        }
        if (mTogglePrint.isChecked()) {
            rights.setPrint(true);
        }
        if (mToggleEdit.isChecked()) {
            rights.setEdit(true);
        }
        if (mToggleSaveAs.isChecked()) {
            rights.setDownload(true);
        }
        if (mToggleReShare.isChecked()) {
            rights.setShare(true);
        }
        // added for sharing local file
        if (mToggleWatermark.isChecked()) {
            rights.setWatermark(true);
        }
        if (isExtractToggleChecked) {
            rights.setDecrypt(true);
        }
        return rights;
    }

    /**
     * get obligations from UI for normal file, now only for overlay.
     */
    private Obligations getObligations() {
        Map<String, String> map = new HashMap<>();
        Obligations nxObligations = new Obligations();
        if (mToggleWatermark.isChecked()) {
            map.put(Constant.RIGHTS_WATERMARK, watermarkValue);
        } else {
            map.put(Constant.RIGHTS_WATERMARK, null); // means no watermark
        }
        nxObligations.setObligation(map);

        return nxObligations;
    }

    /**
     * Build expiry and write file header when protect or share
     *
     * @return
     */
    private Expiry buildExpiry() {

        // for share nxl file, will pass null
        if (iExpiry == null) {
            return null;
        }

        int option = iExpiry.getOption();
        switch (option) {
            case 0:
                return new Expiry.Builder().never().build();
            case 1:
                User.IRelative iRelative = (User.IRelative) iExpiry;
                return new Expiry.Builder().relative()
                        .setYear(iRelative.getYear())
                        .setMonth(iRelative.getMonth())
                        .setWeek(iRelative.getWeek())
                        .setDay(iRelative.getDay())
                        .build();
            case 2:
                User.IAbsolute iAbsolute = (User.IAbsolute) iExpiry;
                return new Expiry.Builder().absolute()
                        .setEndDate(iAbsolute.endDate())
                        .build();
            case 3:
                User.IRange iRange = (User.IRange) iExpiry;
                return new Expiry.Builder().range()
                        .setStartDate(iRange.startDate())
                        .setEndDate(iRange.endDate())
                        .build();
            default:
                throw new RuntimeException("Can not access this!");
        }
    }

    private void doCmdProtect() {
        Expiry expiry = buildExpiry();
        if (isExpired(iExpiry)) return;
        mIExecuteCommand.protect(mContext,
                mWorkingFile,
                getRights(),
                getObligations(),
                expiry,
                mClickFileItem,
                new IProtectComplete() {
                    @Override
                    public void onProtectComplete(MyVaultUploadFileResult result) {
                        // popup succeed prompt window
                        if (result != null) {
                            EventBus.getDefault().post(new ProtectCompleteNotifyEvent());
                            CommonUtils.popupProtectSucceedTip(mContext, mMainSubLayout, mWorkingFile.getName());
                        }
                    }
                });
    }

    private boolean isExpired(User.IExpiry expiry) {
        ExpiryChecker expiryChecker = new ExpiryChecker();
        if (!expiryChecker.isValidate(expiry)) {
            ExceptionDialog.showSimpleUI(mContext, getString(R.string.hint_msg_valid_period));
            return true;
        }
        return false;
    }

    private void doCmdShare() {
        // wrap emails
        if (!TextUtils.isEmpty(mEmailInput.getText().toString())) {
            CommonUtils.wrapEmail(mContext, mEmailInput.getText().toString(), true, mEmailList, mFlowLayout, mEmailInput);
        }
        // get valid emails
        final List<String> listEmail = CommonUtils.getValidEmails(mContext, mEmailList);
        if (listEmail == null || listEmail.size() == 0) {
            return;
        }

        // judge share whether expired for nxl file
        if (bIsExpired) { // or directly catch rest api exception then give prompt.
            ToastUtil.showToast(mContext, getResources().getString(R.string.share_rights_expired));
            return;
        }

        // share
        String comment = mCommentWidget.getText().toString();
        Expiry expiry = buildExpiry();
        if (isExpired(iExpiry)) return;
        mIExecuteCommand.shareLocalFile(mContext,
                mWorkingFile,
                listEmail,
                bIsNxl ? null : getRights(),
                bIsNxl ? null : getObligations(),
                comment,
                expiry,
                new IShareComplete() {
                    @Override
                    public void onShareComplete(boolean result) {
                        // hidden soft-keyboard
                        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            // the first para is windowToken, which can be any current view's window token.
                            imm.hideSoftInputFromWindow(mShareBtn.getWindowToken(), 0);
                        }
                        if (result) {
                            log.e("ShareCompleteNotifyEvent");
                            EventBus.getDefault().post(new ShareCompleteNotifyEvent());
                            // popup succeed prompt window
                            String fileName = (mClickFileItem == null ? mWorkingFile.getName() : mClickFileItem.getName());
                            // handle google file name.
                            if (mClickFileItem != null) {
                                fileName = RenderHelper.isGoogleFile(mClickFileItem) ? RenderHelper.getGoogleExportFileName(mClickFileItem) : mClickFileItem.getName();
                            }
                            CommonUtils.popupShareSucceedTip(mContext, fileName, mMainSubLayout, mEmailList, bIsNxl);
                            // clear emails
                            mEmailList.clear();
                        }
                    }
                });
    }

    private void doCmdProjectAddWithAdhocPolicy() {
        if (isNxlFile()) {
            ToastUtil.showToast(mContext, getResources().getString(R.string.hint_msg_deny_upload_nxl_file_to_project));
            return;
        }
        String projectMembershipIdFromUser = getProjectMembershipIdFromUser(mProjectId);
        if (TextUtils.isEmpty(projectMembershipIdFromUser)) {
            ToastUtil.showToast(getApplicationContext(), "Error occurred while processing protect operation.");
            return;
        }

        Expiry expiry = buildExpiry();
        if (isExpired(iExpiry)) return;
        FileOperation.protectFile(mContext, projectMembershipIdFromUser,
                mWorkingFile, getRights(), getObligations(), expiry,
                new FileOperation.IProtectFileFinish() {
                    @Override
                    public void onProtectFinished(String nxlPath) {
                        log.d("onProtectFinished" + nxlPath);
                        if (!TextUtils.isEmpty(nxlPath)) {
                            FileOperation.projectUploadFile(mContext, mProjectId, new File(nxlPath),
                                    Collections.<String>emptyList(), mParentPathId, null, mFileUploadToProjectCallback);
                        }
                    }
                });
    }

    private FileOperation.IProtectFileCallback mProtectFileCallback = new FileOperation.IProtectFileCallback() {
        private SafeProgressDialog mProtectingDialog;

        @Override
        public void onFileProtecting() {
            if (mProtectingDialog == null) {
                mProtectingDialog = SafeProgressDialog.showDialog(mContext,
                        "", getResources().getString(R.string.wait_protect), true);
            }
        }

        @Override
        public void onFileProtected(String nxlFilePath) {
            Log.d("TAG--", "nxlFilePath: " + nxlFilePath);
            dismissProtectingDialog(mProtectingDialog);
            if (!TextUtils.isEmpty(nxlFilePath)) {
                FileOperation.projectUploadFile(mContext, mProjectId, new File(nxlFilePath),
                        Collections.<String>emptyList(), mParentPathId, null, mFileUploadToProjectCallback);
            }
        }

        @Override
        public void onFileProtectError(final String message) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dismissProtectingDialog(mProtectingDialog);
                    ToastUtil.showToast(getApplicationContext(), message);
                }
            });
        }

        private void dismissProtectingDialog(ProgressDialog dialog) {
            if (dialog == null) {
                return;
            }
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    };

    private FileOperation.IUploadToProjectFile mFileUploadToProjectCallback = new FileOperation.IUploadToProjectFile() {
        @Override
        public void onUploadToProjectFinished(UploadFileResult result) {
            if (result == null) {
                CmdOperateFileActivity.this.finish();
                return;
            }
            UploadFileResult.ResultsBean results = result.getResults();
            if (results == null) {
                return;
            }
            UploadFileResult.ResultsBean.EntryBean entry = results.getEntry();
            if (entry == null) {
                return;
            }
            if (mRootLayout == null) {
                return;
            }
            if (result.getStatusCode() == 200) {
                EventBus.getDefault().post(new ProjectAddCompleteNotifyEvent(mParentPathId));
                CommonUtils.popupProjectAddFileSuccessTip(mContext, mRootLayout, entry.getName());
            }
        }
    };

    private void doCmdProjectAddWithCentPolicy(Map<String, Set<String>> tags) {
        String projectMembershipId = getProjectMembershipIdFromUser(mProjectId);
        if (TextUtils.isEmpty(projectMembershipId)) {
            ToastUtil.showToast(getApplicationContext(), "Error occurred while processing protect operation.");
            return;
        }
        FileOperation.protectFile(mWorkingFile, projectMembershipId, tags, mProtectFileCallback);
    }

    public String getProjectMembershipIdFromUser(int projectId) {
        try {
            log.d("projectId:" + projectId);
            IRmUser rmUser = SkyDRMApp.getInstance().getSession().getRmUser();
            List<IMemberShip> memberships = rmUser.getMemberships();
            log.d("memberships" + memberships);
            if (memberships != null && memberships.size() != 0) {
                for (IMemberShip m : memberships) {
                    if (m instanceof ProjectMemberShip) {
                        ProjectMemberShip pms = (ProjectMemberShip) m;
                        if (pms.getProjectId() == projectId) {
                            return pms.getId();
                        }
                    }
                }
            }
        } catch (InvalidRMClientException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void doCmdMySpaceAdd() {
        IExecuteCommand executeCmd = new ExecuteCommandImpl();
        executeCmd.AddFile(mContext,
                mBoundService,
                mDestFolder,
                mWorkingFile,
                new IAddComplete() {
                    @Override
                    public void onAddFileComplete(boolean taskStatus, @Nullable NXDocument uploadedDoc, FileUploadException e) {
                        if (taskStatus) {
                            ToastUtil.showToast(mContext, mContext.getString(R.string.Upload_file_succeed));
                            CmdOperateFileActivity.this.finish();
                            //// TODO: 5/14/2017 may need to notify ui to refresh
                        } else {
                            String errorMsg = mContext.getString(R.string.Upload_file_failed_no_period);
                            if (e != null) {
                                errorMsg += ", " + e.getMessage() + ".";
                            }
                            ToastUtil.showToast(mContext, errorMsg);
                        }
                    }
                });
    }

    // user click the rights text, also can set the checked to be true or false.
    private void initRightsTextClick(TextView textView, final SwitchCompat toggle) {
        if (null == textView) {
            return;
        }
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle.setChecked(!toggle.isChecked());
            }
        });
    }

    private void initRightsToggle(View parentView) {
        // get controls
        mToggleView = parentView.findViewById(R.id.toggle_view);
        mTogglePrint = parentView.findViewById(R.id.toggle_print);
        mToggleEdit = parentView.findViewById(R.id.toggle_edit);
        mToggleSaveAs = parentView.findViewById(R.id.toggle_download);
        mToggleReShare = parentView.findViewById(R.id.toggle_share);
        mToggleWatermark = parentView.findViewById(R.id.toggle_watermark);
        mToggleValidity = parentView.findViewById(R.id.toggle_validity);

        // get extract rights site.
        TextView tvMoreOptions = parentView.findViewById(R.id.tv_more_options);
        if (ViewUtils.isGone(tvMoreOptions)) {
            tvMoreOptions.setVisibility(View.VISIBLE);
            tvMoreOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    lunch2OptionPage();
                }
            });
        }
        mTextView = findViewById(R.id.textView);
        // initRightsTextClick(mTextView, mToggleView); // is disable for view.
        mTextPrint = findViewById(R.id.textPrint);
        initRightsTextClick(mTextPrint, mTogglePrint);

        mTextEdit = findViewById(R.id.textEdit);
        initRightsTextClick(mTextEdit, mToggleEdit);

        mTextSaveAs = findViewById(R.id.textDownload);
        initRightsTextClick(mTextSaveAs, mToggleSaveAs);

        mTextReShare = findViewById(R.id.textShare);
        initRightsTextClick(mTextReShare, mToggleReShare);

        mTextWatermark = findViewById(R.id.textWatermark);
        initRightsTextClick(mTextWatermark, mToggleWatermark);

        mTextValidity = findViewById(R.id.text_validity);
        // initRightsTextClick(mTextValidity, mToggleValidity);
        // init
        mTogglePrint.setChecked(false);
        mToggleEdit.setChecked(false);
        mToggleSaveAs.setChecked(false);
        mToggleReShare.setChecked(false);
        mToggleWatermark.setChecked(false);

        // for project,don't have share rights.
//        if (mFileFrom == null || mFileFrom != FileFrom.FILE_FROM_PROJECT_PAGE) {
//            initRightsTextClick(mTextReShare, mToggleReShare);
//        }

//        if (mFileFrom == FileFrom.FILE_FROM_PROJECT_PAGE) {
//            return;
//        }
        // init watermark change
        initWatermarkChange(parentView, mFileFrom);
        // init expire
        initExpire(parentView, mFileFrom);
    }

    private void lunch2OptionPage() {
        Intent i = new Intent(mContext, ProjectOperateActivity.class);
        i.setAction(Constant.ACTION_LUNCH_MORE_OPTIONS_FRAGMENT);
        i.putExtra(Constant.STATE_EXTRACT_SWITCH, isExtractToggleChecked);
        mContext.startActivity(i);
    }

    /**
     * Init expire change
     */
    private void initExpire(View parentView, FileFrom mFileFrom) {
        // init controls
        changeCommonDate = parentView.findViewById(R.id.change_common_layout);
        changeAbsoluteDate = parentView.findViewById(R.id.change_absolute_layout);
        changeNeverDate = parentView.findViewById(R.id.change_never_layout);

        fromDay = parentView.findViewById(R.id.from_day);
        fromMonth = parentView.findViewById(R.id.from_month);
        fromWeek = parentView.findViewById(R.id.from_week);

        toDay = parentView.findViewById(R.id.to_day);
        toMonth = parentView.findViewById(R.id.to_month);
        toWeek = parentView.findViewById(R.id.to_week);

        countDay = parentView.findViewById(R.id.count_day);
        // Absolute date
        absToDay = parentView.findViewById(R.id.abs_to_day);
        absToMonth = parentView.findViewById(R.id.abs_to_month);
        absToWeek = parentView.findViewById(R.id.abs_to_week);
        absCountDay = parentView.findViewById(R.id.abs_count_day);

        // try to get Expiry from heartbeat (so may be a little delayed).
        if (mFileFrom == FileFrom.FILE_FROM_PROJECT_PAGE) {

            JSONObject expiry = null;
            try {
                expiry = new JSONObject(mProjectExpiryValue);
                if (expiry.has("option")) {
                    int option = expiry.getInt("option");

                    switch (option) {
                        case 0:
                            iExpiry = new User.IExpiry() {
                                @Override
                                public int getOption() {
                                    return 0;
                                }
                            };
                            break;
                        case 1:
                            final JSONObject relativeDay = expiry.getJSONObject("relativeDay");
                            final int mYears = relativeDay.optInt("year");
                            final int mMonths = relativeDay.optInt("month");
                            final int mWeeks = relativeDay.optInt("week");
                            final int mDays = relativeDay.optInt("day");
                            iExpiry = new User.IRelative() {
                                @Override
                                public int getYear() {
                                    return mYears;
                                }

                                @Override
                                public int getMonth() {
                                    return mMonths;
                                }

                                @Override
                                public int getWeek() {
                                    return mWeeks;
                                }

                                @Override
                                public int getDay() {
                                    return mDays;
                                }

                                @Override
                                public int getOption() {
                                    return 1;
                                }
                            };
                            break;
                        case 2:
                            final long absoluteEndDate = expiry.getLong("endDate");
                            iExpiry = new User.IAbsolute() {
                                @Override
                                public long endDate() {
                                    return absoluteEndDate;
                                }

                                @Override
                                public int getOption() {
                                    return 2;
                                }
                            };
                            break;
                        case 3:
                            final long rangeStartDate = expiry.getLong("startDate");
                            final long rangeEndDate = expiry.getLong("endDate");
                            iExpiry = new User.IRange() {
                                @Override
                                public long startDate() {
                                    return rangeStartDate;
                                }

                                @Override
                                public long endDate() {
                                    return rangeEndDate;
                                }

                                @Override
                                public int getOption() {
                                    return 3;
                                }
                            };
                            break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            iExpiry = SkyDRMApp.getInstance().getSession().getUserPreference().getExpiry();
        }
        // init default expiry date.
        displayExpiryDate(parentView);
    }

    /**
     * Display expiry date
     */
    private void displayExpiryDate(View parentView) {

        switch (iExpiry.getOption()) {
            case 0: // Never expiry
                changeAbsoluteDate.setVisibility(View.GONE);
                changeCommonDate.setVisibility(View.GONE);
                changeNeverDate.setVisibility(View.VISIBLE);
                // change operate
                TextView neverChange = (TextView) parentView.findViewById(R.id.never_change);
                initExpiryChange(neverChange, EXPIRY_NEVER_EXPIRE);

                break;
            case 1: // relative
                changeNeverDate.setVisibility(View.GONE);
                changeAbsoluteDate.setVisibility(View.GONE);
                changeCommonDate.setVisibility(View.VISIBLE);
                // set data
                User.IRelative relative = (User.IRelative) iExpiry;
                updateRelativeEndDate(relative.getYear(), relative.getMonth(), relative.getDay(), relative.getWeek());

                // change operate
                TextView relativeChange = (TextView) parentView.findViewById(R.id.change);
                initExpiryChange(relativeChange, EXPIRY_RELATIVE);

                break;
            case 2: // absolute
                changeNeverDate.setVisibility(View.GONE);
                changeCommonDate.setVisibility(View.GONE);
                changeAbsoluteDate.setVisibility(View.VISIBLE);

                // set the "Expire on" text style
                TextView expireOn = (TextView) parentView.findViewById(R.id.expire_on);
                Spannable spannable = new SpannableString(mContext.getResources().getString(R.string.Rights_will_expire_on));
                spannable.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.green_light)), 12, 21, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                spannable.setSpan(new StyleSpan(Typeface.BOLD), 12, 21, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                expireOn.setText(spannable);

                User.IAbsolute absolute = (User.IAbsolute) iExpiry;
                // end date
                long date = absolute.endDate();
                absoluteEndDate = Calendar.getInstance(Locale.getDefault());
                absoluteEndDate.setTime(new Date(date));
                updateAbsoluteEndDateText(absoluteEndDate);
                // change operate
                TextView absChange = (TextView) parentView.findViewById(R.id.abs_change);
                initExpiryChange(absChange, EXPIRY_ABSOLUTE_DATE);

                break;
            case 3: // date range
                changeNeverDate.setVisibility(View.GONE);
                changeAbsoluteDate.setVisibility(View.GONE);
                changeCommonDate.setVisibility(View.VISIBLE);

                // set data --- convert mills to Calendar
                User.IRange range = (User.IRange) iExpiry;
                // start date
                long startDate = range.startDate();
                rangeStartDate = Calendar.getInstance(Locale.getDefault());
                rangeStartDate.setTime(new Date(startDate));
                // end date
                long endDate = range.endDate();
                rangeEndDate = Calendar.getInstance(Locale.getDefault());
                rangeEndDate.setTime(new Date(endDate));

                updateRelativeOrRangeEndDateText(rangeStartDate, rangeEndDate);
                // change operate
                TextView rangChange = (TextView) parentView.findViewById(R.id.change);
                initExpiryChange(rangChange, EXPIRY_DATE_RANGE);

                break;
            default:
                break;
        }
    }

    private void initExpiryChange(TextView change, final String selectItem) {
        change.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ExpiryWindow.class);
                // post expiry value to ExpiryWindow to change
                EventBus.getDefault().postSticky(new ChangeExpiryDateEvent(iExpiry));
                intent.putExtra("select_item", selectItem);
                intent.putExtra("use_default", false);
                startActivity(intent);
            }
        });
    }

    // update Relative date.
    private void updateRelativeEndDate(int growthYears, int growthMonths, int growthDays, int growthWeeks) {
        Calendar calendar = Calendar.getInstance();
        //Get current year,month and day ,as for week(translate it to days to calc)
        int curYears = calendar.get(Calendar.YEAR);
        int curMonths = calendar.get(Calendar.MONTH);
        int curDays = calendar.get(Calendar.DAY_OF_MONTH);

        calendar.set(Calendar.YEAR, curYears + growthYears);
        calendar.set(Calendar.MONTH, curMonths + growthMonths);
        calendar.set(Calendar.DAY_OF_MONTH, curDays + growthDays + growthWeeks * 7 - 1);
        calendar.set(Calendar.AM_PM, 1);
        calendar.set(Calendar.HOUR, calendar.getActualMaximum(Calendar.HOUR));
        calendar.set(Calendar.MINUTE, calendar.getActualMaximum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMaximum(Calendar.SECOND));
        //update date show TextView
        updateRelativeOrRangeEndDateText(Calendar.getInstance(), calendar);
    }

    private void updateRelativeOrRangeEndDateText(Calendar startDate, Calendar endDate) {
        //for from side
        fromMonth.setText(CalenderUtils.getMonthLabel(startDate));
        fromWeek.setText(CalenderUtils.getWeekLabel(startDate) + " " + startDate.get(Calendar.YEAR));
        fromDay.setText(String.valueOf(startDate.get(Calendar.DAY_OF_MONTH)));
        //for to side
        toMonth.setText(CalenderUtils.getMonthLabel(endDate));
        toWeek.setText(CalenderUtils.getWeekLabel(endDate) + " " + endDate.get(Calendar.YEAR));
        toDay.setText(String.valueOf(endDate.get(Calendar.DAY_OF_MONTH)));
        countDay.setText(String.valueOf(CalenderUtils.countDays(startDate, endDate)));
        countDay.append(" days");
    }

    private void updateAbsoluteEndDateText(Calendar endDate) {
        absToMonth.setText(CalenderUtils.getMonthLabel(endDate));
        absToWeek.setText(CalenderUtils.getWeekLabel(endDate) + " " + endDate.get(Calendar.YEAR));
        absToDay.setText(String.valueOf(endDate.get(Calendar.DAY_OF_MONTH)));
        absCountDay.setText(String.valueOf(CalenderUtils.countDays(Calendar.getInstance(), endDate)));
        absCountDay.append(" days");
    }

    /**
     * Get watermark value
     *
     * @return watermark value
     */
    public String getWatermarkValue() {
        return watermarkValue;
    }

    /**
     * Init watermark change
     */
    private void initWatermarkChange(View parentView, FileFrom mFileFrom) {

        watermarkChangeLayout = (RelativeLayout) parentView.findViewById(R.id.change_watermark_layout);
        watermark = (TextView) parentView.findViewById(R.id.watermark_value);
        // init watermark value from preference config.

        if (mFileFrom == FileFrom.FILE_FROM_PROJECT_PAGE) {
            watermarkValue = mProjectWatermarkValue;
        } else {
            watermarkValue = SkyDRMApp.getInstance().getSession().getUserPreference().getWatermarkValue();
        }
        mToggleWatermark.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    watermarkChangeLayout.setVisibility(View.VISIBLE);
                    // display watermark
                    watermark.setText(""); // reset
                    EditWatermarkHelper.string2imageSpanForDisplay(mContext, watermarkValue, watermark);
                    // set underline
                    change.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
                } else {
                    watermarkChangeLayout.setVisibility(View.GONE);
                }
            }
        });

        // change
        change = (TextView) parentView.findViewById(R.id.change_label);
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                RelativeLayout layout = (RelativeLayout) LayoutInflater.from(mContext).inflate(R.layout.change_watermark_value_layout, null);
                builder.setView(layout);
                final AlertDialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();

                editWatermarkWidget = (EditWatermarkWidget) layout.findViewById(R.id.edit_watermark_widget);

                // cancel button
                btnEditWatermarkCancel = (Button) layout.findViewById(R.id.btn_cancel);
                btnEditWatermarkCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                // ok button
                btnEditWatermarkOk = (Button) layout.findViewById(R.id.btn_ok);
                btnEditWatermarkOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get the edited watermark value
                        EditText editText = editWatermarkWidget.getEditText();

                        // prevent user input empty watermark.
                        if (editText.getEditableText().toString().trim().equals("")) {
                            ToastUtil.showToast(mContext, mContext.getResources().getString(R.string.watermark_can_not_is_empty));
                            return;
                        }

                        watermarkValue = EditWatermarkHelper.imageSpan2StringEx(editText);
                        // display watermark
                        watermark.setText(""); // reset
                        EditWatermarkHelper.string2imageSpanForDisplay(mContext, watermarkValue, watermark);

                        dialog.dismiss();
                    }
                });
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        // Stop mp3 play when this activity is in background or destroy
        if (mWorkingFile != null
                && RenderHelper.judgeFileType(mWorkingFile) == FileRenderProxy.FileType.FILE_TYPE_AUDIO
                && mIFileRender instanceof Audio) {
            ((Audio) mIFileRender).onStopAudioPreview();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().removeAllStickyEvents();
        EventBus.getDefault().unregister(this);
    }

    enum ProtectType {
        CENTRAL_POLICY,
        ADHOC_POLICY
    }
}
